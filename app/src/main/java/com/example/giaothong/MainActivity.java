package com.example.giaothong;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import com.example.giaothong.notification.ReminderManager;
import com.example.giaothong.repository.TrafficSignRepository;
import com.example.giaothong.ui.FlashcardsFragment;
import com.example.giaothong.ui.MiniGameFragment;
import com.example.giaothong.ui.SearchFragment;
import com.example.giaothong.ui.fragments.SettingsFragment;
import com.example.giaothong.ui.quiz.QuizFragment;
import com.example.giaothong.utils.OfflineImageManager;
import com.example.giaothong.utils.SearchHistoryManager;
import com.example.giaothong.utils.SharedPreferencesManager;
import com.example.giaothong.utils.ThemeUtils;
import com.example.giaothong.viewmodel.TrafficSignViewModel;
import com.google.android.material.navigation.NavigationView;

import android.Manifest;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MainActivity";
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private SharedPreferencesManager prefsManager;
    private MenuItem darkModeItem;
    private Fragment currentFragment;
    private TrafficSignRepository trafficSignRepository;
    private OfflineImageManager offlineImageManager;
    
    // Đăng ký launcher để xin cấp quyền thông báo
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // Quyền được cấp, thiết lập thông báo nhắc nhở nếu đã bật
                    setupReminderIfEnabled();
                } else {
                    // Quyền bị từ chối, thông báo cho người dùng
                    Toast.makeText(this,
                            "Cần quyền thông báo để hiển thị nhắc nhở học tập",
                            Toast.LENGTH_LONG).show();
                    
                    // Tắt tùy chọn nhắc nhở trong cài đặt
                    prefsManager.setDailyReminderEnabled(false);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Áp dụng theme trước khi setContentView
        prefsManager = new SharedPreferencesManager(this);
        ThemeUtils.applyThemeFromPreferences(prefsManager);
        
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        
        // Khởi tạo Toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        // Khởi tạo DrawerLayout và NavigationView
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        
        // Thiết lập ActionBarDrawerToggle
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        
        // Set up edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.fragment_container), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        
        // Khởi tạo TrafficSignRepository và bắt đầu tải dữ liệu
        initializeDataManagers();
        
        // Mặc định hiển thị màn hình tìm kiếm khi khởi động
        if (savedInstanceState == null) {
            showSearchScreen();
            navigationView.setCheckedItem(R.id.nav_search);
        }
        
        // Kiểm tra quyền thông báo
        checkNotificationPermission();
    }
    
    /**
     * Kiểm tra và xin cấp quyền thông báo trên Android 13+
     */
    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ yêu cầu quyền POST_NOTIFICATIONS
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) 
                    != PackageManager.PERMISSION_GRANTED) {
                
                // Luôn yêu cầu quyền thông báo khi khởi động ứng dụng
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            } else {
                // Đã có quyền, thiết lập thông báo nhắc nhở nếu đã bật
                setupReminderIfEnabled();
            }
        } else {
            // Các phiên bản Android cũ hơn không yêu cầu quyền cụ thể
            setupReminderIfEnabled();
        }
    }
    
    /**
     * Thiết lập thông báo nhắc nhở nếu đã bật trong cài đặt
     */
    private void setupReminderIfEnabled() {
        if (prefsManager.isDailyReminderEnabled()) {
            ReminderManager reminderManager = new ReminderManager(this);
            
            // Trên Android 12+, kiểm tra quyền SCHEDULE_EXACT_ALARM
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !reminderManager.canScheduleExactAlarms()) {
                // Lưu log thay vì hiện thông báo (để tránh làm phiền người dùng) 
                Log.w(TAG, "Không có quyền báo thức chính xác. Thông báo có thể không chính xác.");
            }
            
            // Vẫn lên lịch thông báo (sẽ sử dụng inexact alarm nếu không có quyền)
            boolean success = reminderManager.scheduleDailyReminder(
                    prefsManager.getReminderHour(),
                    prefsManager.getReminderMinute()
            );
            
            if (success) {
                Log.d(TAG, "Đã thiết lập thông báo nhắc nhở học tập");
            } else {
                Log.e(TAG, "Không thể thiết lập thông báo nhắc nhở học tập");
            }
        }
    }
    
    /**
     * Khởi tạo các thành phần quản lý dữ liệu và bắt đầu tải trước dữ liệu
     */
    private void initializeDataManagers() {
        Log.d(TAG, "Bắt đầu khởi tạo trình quản lý dữ liệu và tải dữ liệu");
        
        // Khởi tạo OfflineImageManager
        offlineImageManager = new OfflineImageManager(this);
        
        // Khởi tạo TrafficSignRepository
        trafficSignRepository = TrafficSignRepository.getInstance(this);
        
        // Tạo thread để tải trước các biển báo đã ghim trong nền
        new Thread(() -> {
            Log.d(TAG, "Bắt đầu tải trước dữ liệu trong nền");
            
            // Đợi chút để màn hình chính được hiển thị trước
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            // Tải trước biển báo đã ghim
            trafficSignRepository.preloadPinnedImages();
            
            // Chờ thêm một chút trước khi tải tất cả hình ảnh
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            // Tải trước tất cả hình ảnh
            trafficSignRepository.preloadAllImages();
        }).start();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        // Ẩn menu item hiển thị biển báo đã ghim vì giờ đã dùng chip
        MenuItem pinnedItem = menu.findItem(R.id.action_show_pinned);
        if (pinnedItem != null) {
            pinnedItem.setVisible(false);
        }
        
        // Thiết lập trạng thái ban đầu cho nút chuyển đổi chế độ tối
        darkModeItem = menu.findItem(R.id.action_toggle_dark_mode);
        if (darkModeItem != null) {
            darkModeItem.setChecked(prefsManager.isDarkMode());
        }
        
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.action_clear_pins) {
            clearAllPins();
            return true;
        } else if (id == R.id.action_clear_history) {
            clearSearchHistory();
            return true;
        } else if (id == R.id.action_toggle_dark_mode) {
            toggleDarkMode(item);
            return true;
        } else if (id == R.id.action_settings) {
            showSettingsScreen();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    /**
     * Xóa tất cả biển báo đã ghim
     */
    private void clearAllPins() {
        if (currentFragment instanceof SearchFragment) {
            // Chuyển yêu cầu đến SearchFragment
            TrafficSignViewModel viewModel = new ViewModelProvider(this).get(TrafficSignViewModel.class);
            viewModel.clearAllPins();
            Toast.makeText(this, R.string.all_pins_cleared, Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Xóa lịch sử tìm kiếm
     */
    private void clearSearchHistory() {
        SearchHistoryManager searchHistoryManager = new SearchHistoryManager(this);
        searchHistoryManager.clearSearchHistory();
        Toast.makeText(this, R.string.history_cleared, Toast.LENGTH_SHORT).show();
    }
    
    /**
     * Chuyển đổi chế độ tối/sáng
     */
    private void toggleDarkMode(MenuItem item) {
        boolean isDarkMode = ThemeUtils.toggleDarkMode(this, prefsManager);
        item.setChecked(isDarkMode);
        Toast.makeText(
                this, 
                isDarkMode ? R.string.dark_mode_on : R.string.dark_mode_off,
                Toast.LENGTH_SHORT
        ).show();
    }
    
    /**
     * Xử lý sự kiện khi người dùng chọn item trong navigation drawer
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.nav_search) {
            showSearchScreen();
        } else if (id == R.id.nav_flashcards) {
            showFlashcardsScreen();
        } else if (id == R.id.nav_quiz) {
            showQuizScreen();
        } else if (id == R.id.nav_mini_game) {
            showMiniGameScreen();
        } else if (id == R.id.nav_settings) {
            showSettingsScreen();
        } else if (id == R.id.nav_about) {
            showAboutDialog();
        }
        
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Hiển thị màn hình tìm kiếm
     */
    private void showSearchScreen() {
        setTitle(getString(R.string.menu_search));
        Fragment fragment = SearchFragment.newInstance();
        switchFragment(fragment);
        navigationView.setCheckedItem(R.id.nav_search);
    }
    
    /**
     * Hiển thị màn hình flashcards
     */
    private void showFlashcardsScreen() {
        setTitle(getString(R.string.menu_flashcards));
        Fragment fragment = FlashcardsFragment.newInstance();
        switchFragment(fragment);
        navigationView.setCheckedItem(R.id.nav_flashcards);
    }
    
    /**
     * Hiển thị màn hình cài đặt
     */
    private void showSettingsScreen() {
        setTitle(getString(R.string.menu_settings));
        Fragment fragment = com.example.giaothong.ui.fragments.SettingsFragment.newInstance();
        switchFragment(fragment);
        navigationView.setCheckedItem(R.id.nav_settings);
    }
    
    /**
     * Hiển thị màn hình trắc nghiệm
     */
    private void showQuizScreen() {
        setTitle(getString(R.string.menu_quiz));
        Fragment fragment = new QuizFragment();
        switchFragment(fragment);
        navigationView.setCheckedItem(R.id.nav_quiz);
    }
    
    /**
     * Hiển thị màn hình mini game
     */
    private void showMiniGameScreen() {
        setTitle(getString(R.string.menu_mini_game));
        Fragment fragment = MiniGameFragment.newInstance();
        switchFragment(fragment);
        navigationView.setCheckedItem(R.id.nav_mini_game);
    }
    
    /**
     * Hiển thị dialog thông tin ứng dụng
     */
    private void showAboutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.menu_about)
               .setMessage("Ứng dụng Biển báo Giao thông\nPhiên bản 1.0\n\nPhát triển bởi:\nNguyễn Văn A\n\n© 2023 Bản quyền")
               .setIcon(R.drawable.ic_info)
               .setPositiveButton(R.string.close, (dialog, which) -> dialog.dismiss());
        
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    
    /**
     * Chuyển đổi giữa các fragment
     */
    private void switchFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
        currentFragment = fragment;
    }

    @Override
    public void onBackPressed() {
        // Xử lý nút back, đóng drawer nếu đang mở
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } 
        // Nếu đang ở màn hình khác màn hình tìm kiếm, quay về màn hình tìm kiếm
        else if (!(currentFragment instanceof SearchFragment)) {
            showSearchScreen();
        } 
        // Nếu đang ở màn hình tìm kiếm, thoát ứng dụng
        else {
            super.onBackPressed();
        }
    }
}