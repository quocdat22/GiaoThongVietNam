package com.example.giaothong.ui.quiz;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.giaothong.R;
import com.example.giaothong.model.QuizQuestion;

import java.util.List;

/**
 * Adapter hiển thị danh sách câu hỏi để xem lại bài làm
 */
public class QuizReviewAdapter extends RecyclerView.Adapter<QuizReviewAdapter.QuestionViewHolder> {

    private final List<QuizQuestion> questions;
    private final int[] userAnswers;

    public QuizReviewAdapter(List<QuizQuestion> questions, int[] userAnswers) {
        this.questions = questions;
        this.userAnswers = userAnswers;
    }

    @NonNull
    @Override
    public QuestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_quiz_review, parent, false);
        return new QuestionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuestionViewHolder holder, int position) {
        holder.bind(questions.get(position), position);
    }

    @Override
    public int getItemCount() {
        return questions != null ? questions.size() : 0;
    }

    /**
     * ViewHolder for each question item
     */
    class QuestionViewHolder extends RecyclerView.ViewHolder {
        private final TextView textQuestionNumber;
        private final ImageView imageResultIcon;
        private final TextView textQuestion;
        private final ImageView imageQuestion;
        private final TextView[] optionViews;
        private final TextView textExplanation;

        public QuestionViewHolder(@NonNull View itemView) {
            super(itemView);
            textQuestionNumber = itemView.findViewById(R.id.text_question_number);
            imageResultIcon = itemView.findViewById(R.id.image_result_icon);
            textQuestion = itemView.findViewById(R.id.text_question);
            imageQuestion = itemView.findViewById(R.id.image_question);
            
            // Initialize option views
            optionViews = new TextView[4];
            optionViews[0] = itemView.findViewById(R.id.text_option_1);
            optionViews[1] = itemView.findViewById(R.id.text_option_2);
            optionViews[2] = itemView.findViewById(R.id.text_option_3);
            optionViews[3] = itemView.findViewById(R.id.text_option_4);
            
            textExplanation = itemView.findViewById(R.id.text_explanation);
        }

        public void bind(QuizQuestion question, int position) {
            // Set question number
            textQuestionNumber.setText(itemView.getContext().getString(
                    R.string.quiz_question_format, position + 1, questions.size()));
            
            // Set question text
            textQuestion.setText(question.getQuestion());
            
            // Load question image
            Glide.with(itemView.getContext())
                    .load(question.getImageUrl())
                    .placeholder(R.drawable.ic_image_placeholder)
                    .error(R.drawable.ic_image_error)
                    .fitCenter()
                    .override(600, 600)
                    .into(imageQuestion);
            
            // Lấy thông tin đáp án
            int correctAnswerIndex = question.getCorrectAnswerIndex();
            int userSelectedIndex = userAnswers[position];
            
            // Hiển thị icon kết quả (đúng/sai)
            boolean isCorrect = (userSelectedIndex == correctAnswerIndex);
            imageResultIcon.setImageResource(isCorrect ? 
                    R.drawable.ic_check_circle : R.drawable.ic_cancel_circle);
            
            // Hiển thị các phương án
            List<String> options = question.getOptions();
            for (int i = 0; i < optionViews.length && i < options.size(); i++) {
                optionViews[i].setText(options.get(i));
                
                // Thiết lập màu nền dựa trên đáp án đúng/sai
                if (i == correctAnswerIndex) {
                    // Đáp án đúng luôn màu xanh
                    optionViews[i].setBackgroundResource(R.drawable.bg_option_correct);
                } else if (i == userSelectedIndex && !isCorrect) {
                    // Đáp án người dùng chọn sai - màu đỏ
                    optionViews[i].setBackgroundResource(R.drawable.bg_option_incorrect);
                } else {
                    // Các đáp án khác
                    optionViews[i].setBackgroundResource(R.drawable.bg_option_normal);
                }
            }
            
            // Set explanation
            String explanation = question.getExplanation();
            if (explanation != null && !explanation.isEmpty()) {
                textExplanation.setVisibility(View.VISIBLE);
                textExplanation.setText(itemView.getContext().getString(
                        R.string.quiz_explanation) + ": " + explanation);
            } else {
                textExplanation.setVisibility(View.GONE);
            }
        }
    }
} 