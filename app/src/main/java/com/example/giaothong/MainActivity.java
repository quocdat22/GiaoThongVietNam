package com.example.giaothong;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.giaothong.adapter.TrafficSignAdapter;
import com.example.giaothong.model.TrafficSign;
import com.example.giaothong.ui.SearchHistoryPopup;
import com.example.giaothong.ui.TrafficSignDetailBottomSheet;
import com.example.giaothong.utils.SearchHistoryManager;
import com.example.giaothong.utils.SharedPreferencesManager;
import com.example.giaothong.utils.ThemeUtils;
import com.example.giaothong.utils.Utils;
import com.example.giaothong.viewmodel.TrafficSignViewModel;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements TrafficSignDetailBottomSheet.OnPinStatusChangeListener {

    private TrafficSignViewModel viewModel;
    private TrafficSignAdapter adapter;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ChipGroup chipGroupCategories;
    private Chip chipAll, chipCam, chipNguyHiem, chipHieuLenh, chipChiDan, chipPhu, chipPinned;
    private TextView textEmptyState;
    private SharedPreferencesManager prefsManager;
    private SearchHistoryManager searchHistoryManager;
    private SearchView searchView;
    private SearchHistoryPopup searchHistoryPopup;
    private View cardSearch;
    private MenuItem darkModeItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Áp dụng theme trước khi setContentView
        prefsManager = new SharedPreferencesManager(this);
        ThemeUtils.applyThemeFromPreferences(prefsManager);
        
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        
        // Set up edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            
            // Kiểm tra nếu bàn phím xuất hiện hoặc biến mất
            Insets imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime());
            boolean isKeyboardVisible = imeInsets.bottom > 0;
            
            // Nếu bàn phím biến mất nhưng SearchView vẫn có focus
            if (!isKeyboardVisible && searchView != null && searchView.hasFocus()) {
                // Ẩn lịch sử nếu bàn phím biến mất
                hideSearchHistory();
            }
            
            return insets;
        });
        
        // Initialize views
        setupViews();
        
        // Initialize ViewModel - use the proper constructor for Java
        viewModel = new ViewModelProvider(this).get(TrafficSignViewModel.class);
        viewModel.setPreferencesManager(prefsManager);
        
        // Set up adapter
        adapter = new TrafficSignAdapter(this, new ArrayList<>());
        adapter.setOnItemClickListener(this::showTrafficSignDetail);
        adapter.setOnItemLongClickListener(this::togglePinStatus);
        recyclerView.setAdapter(adapter);
        
        // Observe traffic signs
        viewModel.getTrafficSigns().observe(this, trafficSigns -> {
            adapter.setTrafficSigns(trafficSigns);
            // Stop refreshing animation if it was started
            if (swipeRefreshLayout.isRefreshing()) {
                swipeRefreshLayout.setRefreshing(false);
            }
            
            // Show/hide empty state if needed
            if (trafficSigns.isEmpty()) {
                textEmptyState.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                textEmptyState.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        });
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
            // Xóa tất cả ghim
            viewModel.clearAllPins();
            
            // Nếu đang ở tab Đã ghim, chuyển về tab Tất cả
            if (chipPinned.isChecked()) {
                chipAll.setChecked(true);
                viewModel.setCategory("");
                viewModel.setShowOnlyPinned(false);
            }
            
            Toast.makeText(this, R.string.all_pins_cleared, Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.action_clear_history) {
            // Xóa tất cả lịch sử tìm kiếm
            searchHistoryManager.clearSearchHistory();
            
            // Cập nhật popup nếu đang hiển thị
            if (searchHistoryPopup.isShowing()) {
                searchHistoryPopup.updateHistoryData();
            }
            
            Toast.makeText(this, R.string.history_cleared, Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.action_toggle_dark_mode) {
            // Chuyển đổi chế độ tối/sáng
            boolean isDarkMode = ThemeUtils.toggleDarkMode(this, prefsManager);
            
            // Cập nhật trạng thái menu item
            item.setChecked(isDarkMode);
            
            // Hiển thị thông báo
            Toast.makeText(
                    this, 
                    isDarkMode ? R.string.dark_mode_on : R.string.dark_mode_off,
                    Toast.LENGTH_SHORT
            ).show();
            
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    /**
     * Đảo trạng thái ghim khi người dùng nhấn giữ một biển báo
     */
    private boolean togglePinStatus(TrafficSign sign, int position) {
        viewModel.togglePinStatus(sign);
        
        // Hiển thị thông báo
        Toast.makeText(
                this, 
                sign.isPinned() ? R.string.sign_pinned : R.string.sign_unpinned, 
                Toast.LENGTH_SHORT
        ).show();
        
        return true;
    }
    
    private void setupViews() {
        recyclerView = findViewById(R.id.recyclerViewSigns);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        chipGroupCategories = findViewById(R.id.chipGroupCategories);
        textEmptyState = findViewById(R.id.textEmptyState);
        cardSearch = findViewById(R.id.cardSearch);
        searchView = findViewById(R.id.searchView);
        
        // Set hint text color for SearchView
        EditText searchEditText = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        if (searchEditText != null) {
            searchEditText.setHintTextColor(ContextCompat.getColor(this, R.color.colorTextSecondary));
            searchEditText.setTextColor(ContextCompat.getColor(this, R.color.colorTextPrimary));
        }
        
        // Khởi tạo SearchHistoryManager
        searchHistoryManager = new SearchHistoryManager(this);
        
        // Hiển thị thông báo trống phù hợp
        textEmptyState.setText(R.string.no_signs_found);
        
        // Khởi tạo các chip lọc
        chipAll = findViewById(R.id.chipAll);
        chipPinned = findViewById(R.id.chipPinned);
        chipCam = findViewById(R.id.chipCam);
        chipNguyHiem = findViewById(R.id.chipNguyHiem);
        chipHieuLenh = findViewById(R.id.chipHieuLenh);
        chipChiDan = findViewById(R.id.chipChiDan);
        chipPhu = findViewById(R.id.chipPhu);
        
        // Thiết lập màu sắc cho các chip
        setupChips();
        
        // Thiết lập bộ lọc danh mục
        setupCategoryFilter();
        
        // Thiết lập RecyclerView
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(2, 8, true));
        
        // Thiết lập SwipeRefreshLayout
        swipeRefreshLayout.setColorSchemeResources(
                R.color.colorPrimary,
                R.color.colorCam,
                R.color.colorNguyHiem,
                R.color.colorHieuLenh
        );
        
        swipeRefreshLayout.setOnRefreshListener(() -> {
            viewModel.refreshTrafficSigns();
            Toast.makeText(this, R.string.refreshing_data, Toast.LENGTH_SHORT).show();
        });
        
        // Thiết lập SearchView và lịch sử tìm kiếm
        setupSearchView();
    }
    
    /**
     * Thiết lập màu sắc cho các chip
     */
    private void setupChips() {
        chipAll.setCheckedIconVisible(true);
        
        chipPinned.setCheckedIconVisible(true);
        chipPinned.setChipStrokeColorResource(R.color.colorPinIcon);
        chipPinned.setChipIconResource(R.drawable.ic_pin_small);
        chipPinned.setChipIconTintResource(R.color.colorPinIcon);
        
        chipCam.setCheckedIconVisible(true);
        chipCam.setChipStrokeColorResource(R.color.colorCam);
        
        chipNguyHiem.setCheckedIconVisible(true);
        chipNguyHiem.setChipStrokeColorResource(R.color.colorNguyHiem);
        
        chipHieuLenh.setCheckedIconVisible(true);
        chipHieuLenh.setChipStrokeColorResource(R.color.colorHieuLenh);
        
        chipChiDan.setCheckedIconVisible(true);
        chipChiDan.setChipStrokeColorResource(R.color.colorChiDan);
        
        chipPhu.setCheckedIconVisible(true);
        chipPhu.setChipStrokeColorResource(R.color.colorPhu);
    }
    
    /**
     * Thiết lập bộ lọc danh mục cho các chip
     */
    private void setupCategoryFilter() {
        // Xử lý sự kiện chọn chip
        chipGroupCategories.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == View.NO_ID) {
                // Không có chip nào được chọn, hiển thị tất cả
                viewModel.setCategory("");
                viewModel.setShowOnlyPinned(false);
                return;
            }
            
            // Bỏ filter theo biển báo đã ghim trước khi xử lý lọc khác
            viewModel.setShowOnlyPinned(false);
            
            // Lấy danh mục từ chip được chọn
            String category = "";
            if (checkedId == R.id.chipAll) {
                category = "";
            } else if (checkedId == R.id.chipPinned) {
                // Xử lý đặc biệt cho chip Đã ghim
                viewModel.setShowOnlyPinned(true);
                return;
            } else if (checkedId == R.id.chipCam) {
                category = "bien_bao_cam";
            } else if (checkedId == R.id.chipNguyHiem) {
                category = "bien_nguy_hiem_va_canh_bao";
            } else if (checkedId == R.id.chipHieuLenh) {
                category = "bien_hieu_lenh";
            } else if (checkedId == R.id.chipChiDan) {
                category = "bien_chi_dan";
            } else if (checkedId == R.id.chipPhu) {
                category = "bien_phu";
            }
            
            // Cập nhật bộ lọc
            viewModel.setCategory(category);
        });
    }
    
    /**
     * Thiết lập SearchView và lịch sử tìm kiếm
     */
    private void setupSearchView() {
        // Khởi tạo SearchHistoryPopup
        searchHistoryPopup = new SearchHistoryPopup(this, cardSearch, searchHistoryManager);
        
        // Thiết lập sự kiện khi chọn một mục trong lịch sử
        searchHistoryPopup.setOnHistoryItemClickListener(query -> {
            searchView.setQuery(query, true);
        });
        
        // Thiết lập SearchView
        searchView.setQueryHint(getString(R.string.search_hint));
        
        // Đảm bảo SearchView có thể nhận focus và xử lý đúng
        searchView.setFocusable(true);
        searchView.setIconified(false);
        searchView.clearFocus(); // Xóa focus ban đầu để người dùng có thể chọn vào một cách chủ động
        searchView.setOnClickListener(v -> showKeyboardAndHistory()); // Khi click vào bất kỳ phần nào của SearchView
        
        // Thiết lập nhấn vào CardView cũng sẽ focus vào SearchView và hiển thị bàn phím
        cardSearch.setOnClickListener(v -> {
            // Yêu cầu focus cho SearchView
            searchView.requestFocus();
            
            // Hiển thị bàn phím và lịch sử tìm kiếm
            showKeyboardAndHistory();
        });
        
        // Hiển thị lịch sử khi focus vào SearchView
        searchView.setOnQueryTextFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                // Khi nhận focus, hiển thị bàn phím và lịch sử
                showKeyboardAndHistory();
            } else if (!hasFocus) {
                hideSearchHistory();
            }
        });
        
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!query.trim().isEmpty()) {
                    // Lưu từ khóa vào lịch sử
                    searchHistoryManager.addSearchQuery(query);
                    
                    // Cập nhật bộ lọc
                    viewModel.setSearchQuery(query);
                    
                    // Ẩn bàn phím
                    searchView.clearFocus();
                    
                    // Ẩn lịch sử
                    hideSearchHistory();
                }
                
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Cập nhật bộ lọc
                viewModel.setSearchQuery(newText);
                
                // Xử lý hiển thị lịch sử
                if (searchView.hasFocus()) {
                    if (newText.isEmpty()) {
                        // Nếu xóa hết thì hiển thị lịch sử
                        showSearchHistory();
                    } else {
                        // Nếu đang nhập thì ẩn lịch sử
                        hideSearchHistory();
                    }
                }
                
                return true;
            }
        });
        
        // Thiết lập nút xóa tìm kiếm
        ImageView clearButton = searchView.findViewById(androidx.appcompat.R.id.search_close_btn);
        clearButton.setOnClickListener(v -> {
            searchView.setQuery("", false);
            viewModel.setSearchQuery("");
            
            // Xóa focus khi người dùng bấm nút xóa để thoát khỏi tìm kiếm
            searchView.clearFocus();
            // Đảm bảo ẩn popup lịch sử
            hideSearchHistory();
        });
    }
    
    /**
     * Hiển thị lịch sử tìm kiếm
     */
    private void showSearchHistory() {
        // Luôn hiển thị popup kể cả khi chưa có lịch sử
        searchHistoryPopup.show(cardSearch);
        
        // Hiển thị thông báo khi người dùng lần đầu tìm kiếm
//        if (searchHistoryManager.getSearchHistory().isEmpty()) {
//            Toast.makeText(this, R.string.search_first_time_tip, Toast.LENGTH_SHORT).show();
//        }
    }
    
    /**
     * Ẩn lịch sử tìm kiếm và bàn phím khi cần
     */
    private void hideSearchHistory() {
        if (searchHistoryPopup.isShowing()) {
            searchHistoryPopup.dismiss();
        }
    }
    
    /**
     * Hiển thị bàn phím và lịch sử tìm kiếm đồng thời
     */
    private void showKeyboardAndHistory() {
        // Hiển thị bàn phím trước
        Utils.showKeyboard(this, searchView);
        
        // Đặt độ trễ nhỏ để đảm bảo bàn phím đã được hiển thị
        searchView.post(() -> {
            // Sử dụng postDelayed để đợi bàn phím hiển thị hoàn tất
            searchView.postDelayed(() -> {
                // Hiển thị popup lịch sử tìm kiếm sau khi bàn phím hiển thị
                showSearchHistory();
            }, 50); // Độ trễ 100ms để đợi bàn phím hiển thị
        });
    }
    
    /**
     * Hiển thị chi tiết biển báo trong bottom sheet
     */
    private void showTrafficSignDetail(TrafficSign trafficSign, int position) {
        TrafficSignDetailBottomSheet bottomSheet = TrafficSignDetailBottomSheet.newInstance(trafficSign);
        bottomSheet.show(getSupportFragmentManager(), "traffic_sign_detail");
    }
    
    /**
     * Lớp tạo khoảng cách giữa các item trong RecyclerView dạng Grid
     */
    private class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {
        private final int spanCount;
        private final int spacing;
        private final boolean includeEdge;

        public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }

        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, 
                                   @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view);
            int column = position % spanCount;

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount;
                outRect.right = (column + 1) * spacing / spanCount;

                if (position < spanCount) {
                    outRect.top = spacing;
                }
                outRect.bottom = spacing;
            } else {
                outRect.left = column * spacing / spanCount;
                outRect.right = spacing - (column + 1) * spacing / spanCount;
                if (position >= spanCount) {
                    outRect.top = spacing;
                }
            }
        }
    }

    @Override
    public void onPinStatusChanged(TrafficSign trafficSign, boolean isPinned) {
        // Cập nhật trạng thái ghim trong ViewModel
        viewModel.updatePinStatus(trafficSign, isPinned);
        
        // Nếu đang ở tab Đã ghim và bỏ ghim một biển báo, cần refresh danh sách
        if (chipPinned.isChecked() && !isPinned) {
            // Chỉ cần refresh lại adapter thay vì gọi API mới
            adapter.notifyDataSetChanged();
        }
    }
}