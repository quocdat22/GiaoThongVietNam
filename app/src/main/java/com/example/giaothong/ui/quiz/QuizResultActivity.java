package com.example.giaothong.ui.quiz;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.giaothong.R;
import com.example.giaothong.model.Quiz;

import java.util.Locale;

/**
 * Màn hình hiển thị kết quả sau khi hoàn thành bài trắc nghiệm
 */
public class QuizResultActivity extends AppCompatActivity {

    public static final String EXTRA_QUIZ = "extra_quiz";

    private Quiz quiz;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_result);

        // Nhận quiz từ intent
        if (getIntent().hasExtra(EXTRA_QUIZ)) {
            quiz = (Quiz) getIntent().getSerializableExtra(EXTRA_QUIZ);
        } else {
            finish();
            return;
        }

        // Thiết lập toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.quiz_result_title);

        // Hiển thị kết quả
        displayResults();

        // Thiết lập sự kiện cho các nút
        setupButtons();
    }

    private void displayResults() {
        // Hiển thị tên bài trắc nghiệm
        TextView textTitle = findViewById(R.id.text_quiz_title);
        textTitle.setText(quiz.getTitle());

        // Hiển thị số câu đúng
        TextView textCorrectAnswers = findViewById(R.id.text_correct_answers);
        textCorrectAnswers.setText(getString(R.string.quiz_result_correct, 
                quiz.getCorrectAnswers(), quiz.getQuestionCount()));

        // Hiển thị điểm số
        TextView textScore = findViewById(R.id.text_score);
        textScore.setText(getString(R.string.quiz_result_score, quiz.calculateScore()));

        // Hiển thị đánh giá dựa trên điểm số
        TextView textEvaluation = findViewById(R.id.text_evaluation);
        float score = quiz.calculateScore();
        if (score >= 9) {
            textEvaluation.setText("Xuất sắc! Bạn nắm vững kiến thức về biển báo giao thông!");
        } else if (score >= 7) {
            textEvaluation.setText("Tốt! Bạn đã có kiến thức khá tốt về biển báo giao thông.");
        } else if (score >= 5) {
            textEvaluation.setText("Đạt yêu cầu. Bạn cần ôn tập thêm về biển báo giao thông.");
        } else {
            textEvaluation.setText("Chưa đạt. Bạn cần học kỹ hơn về biển báo giao thông.");
        }
    }

    private void setupButtons() {
        // Nút xem lại bài làm
        Button btnReview = findViewById(R.id.btn_review);
        btnReview.setOnClickListener(v -> {
            // Mở màn hình xem lại chi tiết từng câu hỏi
            Intent intent = new Intent(this, QuizReviewActivity.class);
            intent.putExtra(QuizReviewActivity.EXTRA_QUIZ, quiz);
            startActivity(intent);
        });

        // Nút làm bài mới
        Button btnNewQuiz = findViewById(R.id.btn_new_quiz);
        btnNewQuiz.setOnClickListener(v -> {
            // Trở về màn hình chính của tính năng trắc nghiệm
            Intent intent = new Intent(this, QuizActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            finish();
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
} 