package com.example.giaothong;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.navigation.fragment.NavHostFragment;

import com.example.giaothong.notification.ReminderManager;
import com.example.giaothong.repository.TrafficSignRepository;
import com.example.giaothong.utils.OfflineImageManager;
import com.example.giaothong.utils.SharedPreferencesManager;
import com.example.giaothong.utils.ThemeUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import android.Manifest;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private NavController navController;
    private Toolbar toolbar;
    private SharedPreferencesManager prefsManager;
    private TrafficSignRepository trafficSignRepository;
    private OfflineImageManager offlineImageManager;
    private ReminderManager reminderManager;
    private AppBarConfiguration appBarConfiguration;
    
    // Đăng ký launcher để xin cấp quyền thông báo
    private final androidx.activity.result.ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new androidx.activity.result.contract.ActivityResultContracts.RequestPermission(), isGranted -> {
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
        setContentView(R.layout.activity_main);
        
        // Khởi tạo Toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        // Thiết lập Bottom Navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        
        // Thiết lập Navigation Controller với nav_graph
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        navController = navHostFragment.getNavController();
        
        // Thiết lập các màn hình cấp cao nhất (không hiển thị nút Up)
        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, 
                R.id.navigation_study, 
                R.id.navigation_search, 
                R.id.navigation_profile
        ).build();
        
        // Kết nối NavigationUI với AppBar và BottomNavigationView
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(bottomNavigationView, navController);
        
        // Xử lý navigation với bottom nav đặc biệt để sửa lỗi quay về Home
        bottomNavigationView.setOnItemSelectedListener(item -> {
            // Sử dụng navigate thay vì setupWithNavController
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_home) {
                navController.navigate(R.id.navigation_home);
                return true;
            } else if (itemId == R.id.navigation_study) {
                navController.navigate(R.id.navigation_study);
                return true;
            } else if (itemId == R.id.navigation_search) {
                Bundle emptyArgs = new Bundle(); // Truyền bundle rỗng để tránh dùng lại arguments cũ
                navController.navigate(R.id.navigation_search, emptyArgs);
                return true;
            } else if (itemId == R.id.navigation_profile) {
                navController.navigate(R.id.navigation_profile);
                return true;
            }
            return false;
        });
        
        // Khởi tạo TrafficSignRepository và bắt đầu tải dữ liệu
        initializeDataManagers();
        
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
            reminderManager = new ReminderManager(this);
            
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
            
            // Tải trước tất cả hình ảnh biển báo
            trafficSignRepository.preloadAllImages();
            
            Log.d(TAG, "Hoàn tất tiến trình tải trước dữ liệu trong nền");
        }).start();
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp();
    }
}