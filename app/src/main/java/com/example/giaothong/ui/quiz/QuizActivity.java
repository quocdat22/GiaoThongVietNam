package com.example.giaothong.ui.quiz;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;

import com.bumptech.glide.Glide;
import com.example.giaothong.R;
import com.example.giaothong.data.QuizHistoryManager;
import com.example.giaothong.model.Quiz;
import com.example.giaothong.model.QuizQuestion;
import com.example.giaothong.utils.SoundHelper;

import java.util.Locale;

public class QuizActivity extends AppCompatActivity {

    public static final String EXTRA_QUIZ = "extra_quiz";

    private Quiz quiz;
    private QuizHistoryManager historyManager;

    private TextView textQuestionNumber;
    private ProgressBar progressBar;
    private TextView textQuestion;
    private ImageView imageQuestion;
    private RadioGroup radioGroupAnswers;
    private RadioButton[] radioAnswers;
    private TextView textFeedback;
    private Button btnPrevious;
    private Button btnNext;

    private boolean answerSelected = false;
    private boolean isLastQuestion = false;

    private RadioGroup.OnCheckedChangeListener radioGroupListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        // Nhận quiz từ intent
        if (getIntent().hasExtra(EXTRA_QUIZ)) {
            quiz = (Quiz) getIntent().getSerializableExtra(EXTRA_QUIZ);
            // Đảm bảo userAnswers được khởi tạo
            quiz.initUserAnswers();
        } else {
            finish();
            return;
        }

        // Khởi tạo history manager
        historyManager = new QuizHistoryManager(PreferenceManager.getDefaultSharedPreferences(this));

        // Khởi tạo views
        initViews();

        // Thiết lập sự kiện
        setupListeners();

        // Hiển thị câu hỏi đầu tiên
        updateQuestionDisplay();
    }

    private void initViews() {
        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(quiz.getTitle());

        // Ánh xạ các view
        textQuestionNumber = findViewById(R.id.text_question_number);
        progressBar = findViewById(R.id.progress_bar);
        textQuestion = findViewById(R.id.text_question);
        imageQuestion = findViewById(R.id.image_question);
        radioGroupAnswers = findViewById(R.id.radio_group_answers);
        textFeedback = findViewById(R.id.text_feedback);
        btnPrevious = findViewById(R.id.btn_previous);
        btnNext = findViewById(R.id.btn_next);

        // Thiết lập ProgressBar
        progressBar.setMax(quiz.getQuestionCount());
        progressBar.setProgress(1);

        // Ánh xạ các RadioButton
        radioAnswers = new RadioButton[4];
        radioAnswers[0] = findViewById(R.id.radio_answer_1);
        radioAnswers[1] = findViewById(R.id.radio_answer_2);
        radioAnswers[2] = findViewById(R.id.radio_answer_3);
        radioAnswers[3] = findViewById(R.id.radio_answer_4);
    }

    private void setupListeners() {
        // Sự kiện khi chọn đáp án
        radioGroupListener = (group, checkedId) -> {
            if (!answerSelected) {
                answerSelected = true;
                
                // Xác định RadioButton được chọn
                int selectedIndex = -1;
                for (int i = 0; i < radioAnswers.length; i++) {
                    if (radioAnswers[i].getId() == checkedId) {
                        selectedIndex = i;
                        break;
                    }
                }
                
                if (selectedIndex != -1) {
                    // Kiểm tra đáp án
                    boolean isCorrect = quiz.checkAnswer(selectedIndex);
                    showFeedback(isCorrect);
                }
            }
        };
        
        // Thiết lập listener
        radioGroupAnswers.setOnCheckedChangeListener(radioGroupListener);

        // Sự kiện nút Previous
        btnPrevious.setOnClickListener(v -> {
            if (quiz.moveToPreviousQuestion()) {
                updateQuestionDisplay();
            }
        });

        // Sự kiện nút Next/Complete
        btnNext.setOnClickListener(v -> {
            if (isLastQuestion) {
                completeQuiz();
            } else if (quiz.moveToNextQuestion()) {
                updateQuestionDisplay();
            }
        });
    }

    private void updateQuestionDisplay() {
        // Tạm thời loại bỏ listener để tránh tự kích hoạt khi cập nhật UI
        radioGroupAnswers.setOnCheckedChangeListener(null);
        
        // Reset trạng thái
        answerSelected = false;
        radioGroupAnswers.clearCheck();
        textFeedback.setVisibility(View.GONE);
        
        // Cập nhật số thứ tự câu hỏi
        int currentIndex = quiz.getCurrentQuestionIndex() + 1;
        int totalQuestions = quiz.getQuestionCount();
        textQuestionNumber.setText(getString(R.string.quiz_question_format, currentIndex, totalQuestions));
        progressBar.setProgress(currentIndex);
        
        // Kiểm tra nếu là câu hỏi cuối
        isLastQuestion = (currentIndex == totalQuestions);
        btnNext.setText(isLastQuestion ? R.string.quiz_complete : R.string.quiz_next);
        
        // Cập nhật nút Previous
        btnPrevious.setEnabled(currentIndex > 1);
        
        // Lấy câu hỏi hiện tại
        QuizQuestion question = quiz.getCurrentQuestion();
        if (question != null) {
            // Hiển thị nội dung câu hỏi
            textQuestion.setText(question.getQuestion());
            
            // Hiển thị hình ảnh
            Glide.with(this)
                    .load(question.getImageUrl())
                    .placeholder(R.drawable.ic_image_placeholder)
                    .error(R.drawable.ic_image_error)
                    .fitCenter()
                    .override(800, 800)
                    .into(imageQuestion);
            
            // Hiển thị các đáp án
            for (int i = 0; i < Math.min(question.getOptions().size(), radioAnswers.length); i++) {
                radioAnswers[i].setText(question.getOptions().get(i));
                radioAnswers[i].setEnabled(true);
            }
            
            // Chọn đáp án nếu đã trả lời trước đó
            int[] userAnswers = quiz.getUserAnswers();
            int currentQuestionIndex = quiz.getCurrentQuestionIndex();
            if (userAnswers != null && currentQuestionIndex < userAnswers.length) {
                int selectedAnswer = userAnswers[currentQuestionIndex];
                if (selectedAnswer >= 0 && selectedAnswer < radioAnswers.length) {
                    radioAnswers[selectedAnswer].setChecked(true);
                    answerSelected = true;
                    
                    // Hiển thị lại feedback
                    boolean isCorrect = (selectedAnswer == question.getCorrectAnswerIndex());
                    showFeedback(isCorrect);
                }
            }
        }
        
        // Khởi tạo lại listener sau khi cập nhật UI
        radioGroupAnswers.setOnCheckedChangeListener(radioGroupListener);
    }

    private void showFeedback(boolean isCorrect) {
        QuizQuestion currentQuestion = quiz.getCurrentQuestion();
        if (currentQuestion == null) return;
        
        // Thiết lập hiển thị phản hồi
        textFeedback.setVisibility(View.VISIBLE);
        if (isCorrect) {
            textFeedback.setBackgroundResource(R.drawable.bg_feedback_correct);
            textFeedback.setText(getString(R.string.quiz_answer_correct));
            // Phát âm thanh đúng
            SoundHelper.playCorrectSound(this);
        } else {
            textFeedback.setBackgroundResource(R.drawable.bg_feedback_incorrect);
            String correctAnswer = currentQuestion.getOptions().get(currentQuestion.getCorrectAnswerIndex());
            textFeedback.setText(getString(R.string.quiz_answer_incorrect, correctAnswer));
            // Phát âm thanh sai
            SoundHelper.playWrongSound(this);
        }
    }

    private void completeQuiz() {
        // Đánh dấu bài trắc nghiệm đã hoàn thành
        quiz.complete();
        
        // Lưu bài trắc nghiệm vào lịch sử
        historyManager.saveQuiz(quiz);
        
        // Mở màn hình kết quả
        Intent intent = new Intent(this, QuizResultActivity.class);
        intent.putExtra(QuizResultActivity.EXTRA_QUIZ, quiz);
        startActivity(intent);
        
        // Đóng màn hình hiện tại
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
} 