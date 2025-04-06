package com.example.giaothong.ui.quiz;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.giaothong.R;
import com.example.giaothong.model.Quiz;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adapter cho RecyclerView hiển thị lịch sử bài trắc nghiệm
 */
public class QuizHistoryAdapter extends RecyclerView.Adapter<QuizHistoryAdapter.QuizViewHolder> {

    private final List<Quiz> quizList;
    private final OnQuizItemClickListener listener;
    private final SimpleDateFormat dateFormat;

    public QuizHistoryAdapter(List<Quiz> quizList, OnQuizItemClickListener listener) {
        this.quizList = quizList;
        this.listener = listener;
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    }

    @NonNull
    @Override
    public QuizViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_quiz_history, parent, false);
        return new QuizViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuizViewHolder holder, int position) {
        Quiz quiz = quizList.get(position);
        holder.bind(quiz);
    }

    @Override
    public int getItemCount() {
        return quizList.size();
    }

    /**
     * ViewHolder cho item quiz
     */
    class QuizViewHolder extends RecyclerView.ViewHolder {
        private final TextView textTitle;
        private final TextView textDescription;
        private final TextView textDate;
        private final TextView textScore;

        public QuizViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitle = itemView.findViewById(R.id.text_quiz_title);
            textDescription = itemView.findViewById(R.id.text_quiz_description);
            textDate = itemView.findViewById(R.id.text_quiz_date);
            textScore = itemView.findViewById(R.id.text_quiz_score);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onQuizItemClick(quizList.get(position));
                }
            });
        }

        public void bind(Quiz quiz) {
            textTitle.setText(quiz.getTitle());
            textDescription.setText(quiz.getDescription());
            
            // Lấy ID và trích xuất timestamp
            try {
                String id = quiz.getId();
                if (id != null && id.startsWith("quiz_")) {
                    String timestampStr = id.substring("quiz_".length());
                    long timestamp = Long.parseLong(timestampStr);
                    Date date = new Date(timestamp);
                    textDate.setText(dateFormat.format(date));
                } else {
                    textDate.setText("");
                }
            } catch (Exception e) {
                textDate.setText("");
            }
            
            // Hiển thị điểm số
            float score = quiz.calculateScore();
            textScore.setText(String.format(Locale.getDefault(), "%.1f", score));
        }
    }

    /**
     * Interface cho sự kiện click vào item
     */
    public interface OnQuizItemClickListener {
        void onQuizItemClick(Quiz quiz);
    }
} 