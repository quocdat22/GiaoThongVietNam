package com.example.giaothong.ui.quiz;

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

/**
 * Activity hiển thị xem lại bài trắc nghiệm đã làm
 */
public class QuizReviewActivity extends AppCompatActivity {

    public static final String EXTRA_QUIZ = "extra_quiz";

    private Quiz quiz;
    private RecyclerView recyclerView;
    private QuizReviewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_review);

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
        getSupportActionBar().setTitle(R.string.quiz_review_title);

        // Khởi tạo RecyclerView
        recyclerView = findViewById(R.id.recycler_review);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Thiết lập adapter
        adapter = new QuizReviewAdapter(quiz.getQuestions(), quiz.getUserAnswers());
        recyclerView.setAdapter(adapter);

        // Button quay lại
        Button btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
} 