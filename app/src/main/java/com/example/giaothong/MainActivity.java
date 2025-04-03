package com.example.giaothong;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import com.example.giaothong.ui.FlashcardsFragment;
import com.example.giaothong.ui.SearchFragment;
import com.example.giaothong.utils.SearchHistoryManager;
import com.example.giaothong.utils.SharedPreferencesManager;
import com.example.giaothong.utils.ThemeUtils;
import com.example.giaothong.viewmodel.TrafficSignViewModel;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private SharedPreferencesManager prefsManager;
    private MenuItem darkModeItem;
    private Fragment currentFragment;

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
        
        // Mặc định hiển thị màn hình tìm kiếm khi khởi động
        if (savedInstanceState == null) {
            showSearchScreen();
            navigationView.setCheckedItem(R.id.nav_search);
        }
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
    public boolean onOptionsItemSelected(MenuItem item) {
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
        } else if (id == R.id.nav_settings) {
            Toast.makeText(this, "Tính năng đang phát triển", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_about) {
            Toast.makeText(this, "Tính năng đang phát triển", Toast.LENGTH_SHORT).show();
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