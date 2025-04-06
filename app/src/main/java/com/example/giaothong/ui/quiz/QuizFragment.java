package com.example.giaothong.ui.quiz;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.giaothong.R;
import com.example.giaothong.data.QuizHistoryManager;
import com.example.giaothong.repository.TrafficSignRepository;
import com.example.giaothong.model.Quiz;

import java.util.ArrayList;
import java.util.List;

public class QuizFragment extends Fragment implements QuizHistoryAdapter.OnQuizItemClickListener {

    private RecyclerView recyclerView;
    private TextView textEmptyState;
    private QuizHistoryAdapter adapter;
    private List<Quiz> quizHistory;
    private QuizHistoryManager historyManager;
    private TrafficSignRepository repository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_quiz, container, false);
        
        // Khởi tạo các view
        recyclerView = view.findViewById(R.id.recycler_quiz_history);
        textEmptyState = view.findViewById(R.id.text_empty_state);
        
        // Khởi tạo repository và history manager
        repository = TrafficSignRepository.getInstance(requireContext());
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
        historyManager = new QuizHistoryManager(prefs);
        
        // Cài đặt RecyclerView
        setupRecyclerView();
        
        // Cài đặt các sự kiện click
        view.findViewById(R.id.btn_start_random_quiz).setOnClickListener(v -> startRandomQuiz());
        view.findViewById(R.id.btn_start_category_quiz).setOnClickListener(v -> showCategoryDialog());
        
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadQuizHistory();
    }

    private void setupRecyclerView() {
        quizHistory = new ArrayList<>();
        adapter = new QuizHistoryAdapter(quizHistory, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
    }

    private void loadQuizHistory() {
        // Lấy lịch sử bài trắc nghiệm từ SharedPreferences
        quizHistory.clear();
        quizHistory.addAll(historyManager.getQuizHistory());
        adapter.notifyDataSetChanged();
        
        // Cập nhật trạng thái rỗng
        updateEmptyState();
    }

    private void updateEmptyState() {
        if (quizHistory.isEmpty()) {
            textEmptyState.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            textEmptyState.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void startRandomQuiz() {
        // Lấy dữ liệu biển báo từ repository
        repository.getTrafficSigns(trafficSigns -> {
            if (trafficSigns != null && !trafficSigns.isEmpty()) {
                // Tạo bài trắc nghiệm ngẫu nhiên
                Quiz quiz = Quiz.createFromTrafficSigns(
                        getString(R.string.quiz_title),
                        getString(R.string.quiz_description),
                        trafficSigns
                );
                
                // Mở màn hình làm bài trắc nghiệm
                startQuizActivity(quiz);
            }
        });
    }

    private void showCategoryDialog() {
        // Hiển thị dialog chọn danh mục biển báo
        // Sẽ triển khai sau
    }

    private void startQuizActivity(Quiz quiz) {
        Intent intent = new Intent(requireContext(), QuizActivity.class);
        intent.putExtra(QuizActivity.EXTRA_QUIZ, quiz);
        startActivity(intent);
    }

    @Override
    public void onQuizItemClick(Quiz quiz) {
        // Mở màn hình xem kết quả bài trắc nghiệm đã làm
        Intent intent = new Intent(requireContext(), QuizResultActivity.class);
        intent.putExtra(QuizResultActivity.EXTRA_QUIZ, quiz);
        startActivity(intent);
    }
} 