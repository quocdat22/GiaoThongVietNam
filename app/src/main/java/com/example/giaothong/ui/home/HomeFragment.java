package com.example.giaothong.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.giaothong.R;
import com.example.giaothong.adapter.HomeCategoryAdapter;
import com.example.giaothong.data.DailyProgressManager;
import com.example.giaothong.model.Quiz;
import com.example.giaothong.model.TrafficSign;
import com.example.giaothong.repository.TrafficSignRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * Fragment for the Home screen
 */
public class HomeFragment extends Fragment {

    private static final String PREF_DAILY_QUIZ_ID = "daily_quiz_id";
    private static final String PREF_DAILY_QUIZ_DATE = "daily_quiz_date";
    private static final int DAILY_SIGN_COUNT = 10;

    private RecyclerView recyclerViewCategories;
    private MaterialCardView cardViewFlashcards;
    private MaterialCardView cardViewQuiz;
    
    // Thêm các view hiển thị tiến độ
    private CircularProgressIndicator progressIndicator;
    private TextView textViewProgressPercent;
    private TextView textViewProgressDate;
    private TextView textViewGoalCount;
    private TextView textViewLearnedCount;
    private TextView textViewAccuracyPercent;
    private MaterialButton buttonDailyPractice;
    
    private DailyProgressManager progressManager;
    private TrafficSignRepository repository;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize progress manager
        progressManager = new DailyProgressManager(
                PreferenceManager.getDefaultSharedPreferences(requireContext()));
        
        // Initialize repository
        repository = TrafficSignRepository.getInstance(requireContext());

        // Initialize views
        recyclerViewCategories = view.findViewById(R.id.recyclerViewCategories);
        cardViewFlashcards = view.findViewById(R.id.cardViewFlashcards);
        cardViewQuiz = view.findViewById(R.id.cardViewQuiz);
        
        // Initialize progress views
        progressIndicator = view.findViewById(R.id.progressIndicator);
        textViewProgressPercent = view.findViewById(R.id.textViewProgressPercent);
        textViewProgressDate = view.findViewById(R.id.textViewProgressDate);
        textViewGoalCount = view.findViewById(R.id.textViewGoalCount);
        textViewLearnedCount = view.findViewById(R.id.textViewLearnedCount);
        textViewAccuracyPercent = view.findViewById(R.id.textViewAccuracyPercent);
        buttonDailyPractice = view.findViewById(R.id.buttonDailyPractice);
        
        setupCategoriesRecyclerView();
        setupClickListeners();
        updateProgressDisplay();
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Cập nhật dữ liệu tiến độ mỗi khi Fragment được hiển thị lại
        updateProgressDisplay();
    }
    
    /**
     * Cập nhật hiển thị tiến độ học tập
     */
    private void updateProgressDisplay() {
        // Cập nhật ngày
        textViewProgressDate.setText(progressManager.getFormattedCurrentDate());
        
        // Cập nhật tiến độ
        int progressPercent = progressManager.getProgressPercentToday();
        progressIndicator.setProgress(progressPercent);
        textViewProgressPercent.setText(progressPercent + "%");
        
        // Cập nhật mục tiêu
        int goal = progressManager.getDailyGoal();
        textViewGoalCount.setText(String.valueOf(goal));
        
        // Cập nhật số lượng đã học
        int learned = progressManager.getLearnedSignsToday();
        textViewLearnedCount.setText(String.valueOf(learned));
        
        // Cập nhật độ chính xác
        int accuracy = progressManager.getAccuracyToday();
        textViewAccuracyPercent.setText(accuracy + "%");
    }
    
    private void setupCategoriesRecyclerView() {
        // Set up layout manager for categories (grid with 4 columns)
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 4);
        recyclerViewCategories.setLayoutManager(layoutManager);
        
        // Load traffic signs to get categories and their first signs
        repository.getTrafficSigns(trafficSigns -> {
            if (trafficSigns == null || trafficSigns.isEmpty()) {
                Toast.makeText(requireContext(), R.string.traffic_signs_not_available, Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Get available categories
            List<String> categoryCodeList = repository.getAvailableCategories();
            
            // Create category items for the adapter
            List<HomeCategoryAdapter.CategoryItem> categoryItems = new ArrayList<>();
            
            // Add "All" category with a custom icon
            if (!trafficSigns.isEmpty()) {
                categoryItems.add(new HomeCategoryAdapter.CategoryItem(
                        "all",
                        "All Signs",
                        "",  // No specific code for all
                        trafficSigns.size(),
                        null  // We'll use a custom drawable instead of first sign
                ));
            }
            
            // Add specific categories
            for (String categoryCode : categoryCodeList) {
                // Get traffic signs for this category
                List<TrafficSign> categorySigns;
                String categoryPrefix;
                
                // Map the short category codes to the full prefixes used in getTrafficSignsByCategory
                switch (categoryCode) {
                    case "P.": // Biển báo cấm
                        categoryPrefix = "bien_bao_cam";
                        break;
                    case "W.": // Biển báo nguy hiểm
                        categoryPrefix = "bien_nguy_hiem_va_canh_bao";
                        break;
                    case "I.": // Biển báo chỉ dẫn
                        categoryPrefix = "bien_chi_dan";
                        break;
                    case "R.": // Biển báo hiệu lệnh
                        categoryPrefix = "bien_hieu_lenh";
                        break;
                    case "S.": // Biển báo phụ
                        categoryPrefix = "bien_phu";
                        break;
                    default:
                        categoryPrefix = categoryCode;
                }
                
                // Filter manually by category prefix
                categorySigns = new ArrayList<>();
                for (TrafficSign sign : trafficSigns) {
                    if (sign.getId().startsWith(categoryPrefix)) {
                        categorySigns.add(sign);
                    }
                }
                
                if (!categorySigns.isEmpty()) {
                    // Get display name for this category
                    String displayName = repository.getCategoryDisplayName(categoryCode);
                    
                    // Use the first sign in this category as the icon
                    TrafficSign firstSign = categorySigns.get(0);
                    
                    // Create category item
                    categoryItems.add(new HomeCategoryAdapter.CategoryItem(
                            categoryCode,
                            displayName,
                            categoryCode,
                            categorySigns.size(),
                            firstSign
                    ));
                }
            }
            
            // Create adapter and set to recycler view
            HomeCategoryAdapter adapter = new HomeCategoryAdapter(requireContext(), categoryItems);
            adapter.setOnCategoryClickListener(category -> {
                // When category is clicked, navigate to Search fragment with category filter
                // Map the category code to the format expected by the search fragment
                String categoryFilter = "";
                
                if (category.getCategoryId().equals("all")) {
                    // For "All" category, use empty string to show all signs
                    categoryFilter = "";
                } else {
                    // For specific categories, map to the internal identifier
                    switch (category.getCode()) {
                        case "P.":
                            categoryFilter = "bien_bao_cam";
                            break;
                        case "W.":
                            categoryFilter = "bien_nguy_hiem_va_canh_bao";
                            break;
                        case "I.":
                            categoryFilter = "bien_chi_dan";
                            break;
                        case "R.":
                            categoryFilter = "bien_hieu_lenh";
                            break;
                        case "S.":
                            categoryFilter = "bien_phu";
                            break;
                    }
                }
                
                // Lưu giá trị cuối cùng của categoryFilter để đảm bảo nó là "effectively final"
                final String finalCategoryFilter = categoryFilter;
                
                // Use the activity to change the selected navigation item to Search
                BottomNavigationView bottomNav = requireActivity().findViewById(R.id.bottomNavigation);
                bottomNav.setSelectedItemId(R.id.navigation_search);
                
                // Set a delay to pass the arguments after navigation
                new Handler().postDelayed(() -> {
                    if (getActivity() != null && !isDetached()) {
                        // Pass the category filter to the SearchFragment via shared ViewModel
                        Bundle args = new Bundle();
                        args.putString("category", finalCategoryFilter);
                        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
                        navController.navigate(R.id.navigation_search, args);
                    }
                }, 100);
            });
            
            recyclerViewCategories.setAdapter(adapter);
        });
    }
    
    private void setupClickListeners() {
        // Set click listeners for the cards
        
        // Flashcards study mode
        cardViewFlashcards.setOnClickListener(v -> {
            // Navigate to Flashcards screen
            Navigation.findNavController(v).navigate(R.id.navigation_study);
        });
        
        // Quiz study mode
        cardViewQuiz.setOnClickListener(v -> {
            // Navigate to Study fragment with quiz tab selected
            Bundle args = new Bundle();
            args.putInt("study_mode", 2); // 2 = MODE_QUIZ as defined in StudyFragment
            Navigation.findNavController(v).navigate(R.id.navigation_study, args);
        });
        
        // Daily Practice button
        buttonDailyPractice.setOnClickListener(v -> {
            createOrOpenDailyPracticeSet();
        });
    }
    
    /**
     * Creates a new daily practice set or opens the existing one for today
     */
    private void createOrOpenDailyPracticeSet() {
        // Check if we already have a quiz for today
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String savedDate = PreferenceManager.getDefaultSharedPreferences(requireContext())
                .getString(PREF_DAILY_QUIZ_DATE, "");
        
        // If we already have a quiz for today, open it
        if (currentDate.equals(savedDate)) {
            String quizId = PreferenceManager.getDefaultSharedPreferences(requireContext())
                    .getString(PREF_DAILY_QUIZ_ID, "");
            if (!quizId.isEmpty()) {
                openDailyQuiz(quizId);
                return;
            }
        }
        
        // Otherwise, create a new quiz for today
        createDailyQuiz();
    }
    
    /**
     * Creates a new daily practice quiz with a set of random traffic signs
     */
    private void createDailyQuiz() {
        // Show loading progress
        Toast.makeText(requireContext(), "Creating today's practice set...", Toast.LENGTH_SHORT).show();
        
        // Get all traffic signs from repository
        repository.getTrafficSigns(trafficSigns -> {
            if (trafficSigns == null || trafficSigns.isEmpty()) {
                Toast.makeText(requireContext(), "No traffic signs available", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Create a random subset of traffic signs for today
            List<TrafficSign> dailySigns = getRandomSigns(trafficSigns, DAILY_SIGN_COUNT);
            
            // Create a quiz with these signs
            Quiz dailyQuiz = Quiz.createFromTrafficSigns(
                    "Daily Practice - " + progressManager.getFormattedCurrentDate(),
                    "Practice with these " + DAILY_SIGN_COUNT + " traffic signs today",
                    dailySigns,
                    DAILY_SIGN_COUNT
            );
            
            // Save the quiz ID and date
            String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            PreferenceManager.getDefaultSharedPreferences(requireContext()).edit()
                    .putString(PREF_DAILY_QUIZ_ID, dailyQuiz.getId())
                    .putString(PREF_DAILY_QUIZ_DATE, currentDate)
                    .apply();
            
            // Open the quiz
            openQuizActivity(dailyQuiz);
        });
    }
    
    /**
     * Opens an existing daily quiz by ID
     */
    private void openDailyQuiz(String quizId) {
        // TODO: Implement logic to load the quiz by ID from storage
        // For now, we'll just create a new one
        Toast.makeText(requireContext(), "Opening today's practice set...", Toast.LENGTH_SHORT).show();
        createDailyQuiz();
    }
    
    /**
     * Opens the Quiz activity with the given quiz
     */
    private void openQuizActivity(Quiz quiz) {
        // For better navigation and to avoid the crash on back press, 
        // we use a startActivity with proper flags instead of Navigation component
        try {
            // Import the QuizActivity class
            Class<?> quizActivityClass = Class.forName("com.example.giaothong.ui.quiz.QuizActivity");
            Intent intent = new Intent(requireActivity(), quizActivityClass);
            intent.putExtra("extra_quiz", quiz); // Use the same extra name as in QuizActivity
            startActivity(intent);
            
            Toast.makeText(requireContext(), "Daily practice set ready!", Toast.LENGTH_SHORT).show();
        } catch (ClassNotFoundException e) {
            // Fallback to navigation component if the class isn't found
            Navigation.findNavController(requireView()).navigate(R.id.navigation_quiz);
            Toast.makeText(requireContext(), "Daily practice set ready!", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Returns a random subset of traffic signs
     */
    private List<TrafficSign> getRandomSigns(List<TrafficSign> allSigns, int count) {
        List<TrafficSign> randomSigns = new ArrayList<>();
        List<TrafficSign> tempList = new ArrayList<>(allSigns);
        Random random = new Random();
        
        int size = Math.min(count, tempList.size());
        for (int i = 0; i < size; i++) {
            int randomIndex = random.nextInt(tempList.size());
            randomSigns.add(tempList.get(randomIndex));
            tempList.remove(randomIndex);
        }
        
        return randomSigns;
    }
} 