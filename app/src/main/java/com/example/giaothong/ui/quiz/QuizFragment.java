package com.example.giaothong.ui.quiz;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.giaothong.R;
import com.example.giaothong.data.QuizHistoryManager;
import com.example.giaothong.repository.TrafficSignRepository;
import com.example.giaothong.model.Quiz;
import com.example.giaothong.model.TrafficSign;
import com.google.android.material.snackbar.Snackbar;

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
        
        // Setup swipe to delete
        setupSwipeToDelete();
    }
    
    /**
     * Cài đặt tính năng kéo qua để xóa
     */
    private void setupSwipeToDelete() {
        // Tạo callback cho ItemTouchHelper
        ItemTouchHelper.SimpleCallback swipeCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            private final ColorDrawable background = new ColorDrawable(Color.RED);
            
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false; // Không hỗ trợ kéo thả
            }
            
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                // Lấy vị trí của item bị kéo
                int position = viewHolder.getAdapterPosition();
                
                // Lấy quiz để xóa
                Quiz deletedQuiz = quizHistory.get(position);
                
                // Xóa khỏi danh sách hiển thị
                quizHistory.remove(position);
                adapter.notifyItemRemoved(position);
                
                // Xóa từ lưu trữ
                historyManager.removeQuiz(deletedQuiz.getId());
                
                // Hiển thị Snackbar để hoàn tác
                Snackbar.make(recyclerView, R.string.quiz_deleted, Snackbar.LENGTH_LONG)
                        .setAction(R.string.undo, v -> {
                            // Hoàn tác xóa
                            historyManager.saveQuiz(deletedQuiz);
                            
                            // Tải lại danh sách
                            loadQuizHistory();
                        })
                        .show();
                
                // Cập nhật trạng thái rỗng
                updateEmptyState();
            }
            
            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                View itemView = viewHolder.itemView;
                
                // Chỉ vẽ nền đỏ khi kéo sang trái
                if (dX < 0) {
                    // Vẽ nền màu đỏ
                    background.setBounds(itemView.getRight() + (int) dX, itemView.getTop(), itemView.getRight(), itemView.getBottom());
                    background.draw(c);
                    
                    // Vẽ nút xóa
                    View deleteButtonView = LayoutInflater.from(requireContext()).inflate(R.layout.delete_button_background, recyclerView, false);
                    deleteButtonView.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), 
                                           View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                    deleteButtonView.layout(0, 0, deleteButtonView.getMeasuredWidth(), deleteButtonView.getMeasuredHeight());
                    
                    // Tính toán vị trí và vẽ view
                    int deleteButtonWidth = deleteButtonView.getMeasuredWidth();
                    int itemHeight = itemView.getBottom() - itemView.getTop();
                    int deleteButtonLeft = itemView.getRight() - deleteButtonWidth - 16; // Padding
                    int deleteButtonTop = itemView.getTop() + (itemHeight - deleteButtonView.getMeasuredHeight()) / 2;
                    
                    c.save();
                    c.translate(deleteButtonLeft, deleteButtonTop);
                    deleteButtonView.draw(c);
                    c.restore();
                }
                
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };
        
        // Áp dụng ItemTouchHelper vào RecyclerView
        new ItemTouchHelper(swipeCallback).attachToRecyclerView(recyclerView);
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
        // Lấy danh sách biển báo từ repository
        repository.getTrafficSigns(trafficSigns -> {
            if (trafficSigns == null || trafficSigns.isEmpty()) {
                Toast.makeText(requireContext(), R.string.traffic_signs_not_available, Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Tạo view cho dialog
            View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_select_category, null);
            
            // Tìm các view trong layout
            RecyclerView recyclerView = dialogView.findViewById(R.id.recycler_categories);
            Button btnCancel = dialogView.findViewById(R.id.btn_cancel);
            Button btnStart = dialogView.findViewById(R.id.btn_start);
            
            // Tạo AlertDialog
            AlertDialog dialog = new AlertDialog.Builder(requireContext())
                    .setView(dialogView)
                    .create();
            
            // Lấy danh sách các danh mục
            List<String> categoryCodeList = repository.getAvailableCategories();
            
            // Tạo danh sách các mục danh mục để hiển thị
            List<CategoryAdapter.CategoryItem> categoryItems = new ArrayList<>();
            for (String categoryCode : categoryCodeList) {
                // Lấy biển báo thuộc danh mục này
                List<TrafficSign> categoryTrafficSigns = repository.getTrafficSignsByCategory(categoryCode);
                
                // Thêm thông tin danh mục vào danh sách
                String categoryName = repository.getCategoryDisplayName(categoryCode);
                int signCount = categoryTrafficSigns.size();
                
                categoryItems.add(new CategoryAdapter.CategoryItem(categoryCode, categoryName, signCount));
            }
            
            // Tạo adapter và thiết lập cho RecyclerView
            CategoryAdapter adapter = new CategoryAdapter(requireContext(), categoryItems);
            recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
            recyclerView.setAdapter(adapter);
            
            // Khởi tạo biến để lưu danh mục được chọn
            final CategoryAdapter.CategoryItem[] selectedCategory = {null};
            
            // Lắng nghe sự kiện chọn danh mục
            adapter.setOnCategorySelectedListener((category, position) -> {
                selectedCategory[0] = category;
                btnStart.setEnabled(true);
            });
            
            // Thiết lập sự kiện cho các nút
            btnCancel.setOnClickListener(v -> dialog.dismiss());
            
            btnStart.setOnClickListener(v -> {
                if (selectedCategory[0] == null) {
                    Toast.makeText(requireContext(), R.string.select_category, Toast.LENGTH_SHORT).show();
                    return;
                }
                
                // Lấy thông tin danh mục được chọn
                String categoryCode = selectedCategory[0].getCode();
                String categoryName = selectedCategory[0].getName();
                
                // Tạo bài trắc nghiệm từ danh mục
                Quiz quiz = Quiz.createFromCategory(
                        getString(R.string.category_quiz_title),
                        getString(R.string.quiz_category_description, categoryName),
                        trafficSigns,
                        categoryCode,
                        categoryName
                );
                
                // Kiểm tra xem bài trắc nghiệm đã được tạo thành công chưa
                if (quiz == null || quiz.getQuestionCount() < 4) {
                    Toast.makeText(requireContext(), R.string.not_enough_signs, Toast.LENGTH_SHORT).show();
                    return;
                }
                
                // Đóng dialog
                dialog.dismiss();
                
                // Mở màn hình làm bài trắc nghiệm
                startQuizActivity(quiz);
            });
            
            // Hiển thị dialog
            dialog.show();
        });
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