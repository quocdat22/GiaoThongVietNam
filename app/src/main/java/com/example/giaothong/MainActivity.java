package com.example.giaothong;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
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
import com.example.giaothong.ui.TrafficSignDetailBottomSheet;
import com.example.giaothong.viewmodel.TrafficSignViewModel;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import android.content.Context;
import android.graphics.Rect;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private TrafficSignViewModel viewModel;
    private TrafficSignAdapter adapter;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ChipGroup chipGroupCategories;
    private Chip chipAll, chipCam, chipNguyHiem, chipHieuLenh, chipChiDan, chipPhu;
    private TextView textEmptyState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        
        // Set up edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        
        // Initialize views
        setupViews();
        
        // Initialize ViewModel - use the proper constructor for Java
        viewModel = new ViewModelProvider(this).get(TrafficSignViewModel.class);
        
        // Set up adapter
        adapter = new TrafficSignAdapter(this, new ArrayList<>());
        adapter.setOnItemClickListener(this::showTrafficSignDetail);
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
    
    private void setupViews() {
        recyclerView = findViewById(R.id.recyclerViewSigns);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        chipGroupCategories = findViewById(R.id.chipGroupCategories);
        textEmptyState = findViewById(R.id.textEmptyState);
        SearchView searchView = findViewById(R.id.searchView);
        
        // Hiển thị thông báo trống phù hợp
        textEmptyState.setText(R.string.no_signs_found);
        
        // Khởi tạo các chip lọc
        chipAll = findViewById(R.id.chipAll);
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
        
        // Thiết lập SearchView
        searchView.setQueryHint(getString(R.string.search_hint));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                viewModel.setSearchQuery(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                viewModel.setSearchQuery(newText);
                return true;
            }
        });
        
        // Thiết lập nút xóa tìm kiếm
        ImageView clearButton = searchView.findViewById(androidx.appcompat.R.id.search_close_btn);
        clearButton.setOnClickListener(v -> {
            searchView.setQuery("", false);
            searchView.clearFocus();
            viewModel.setSearchQuery("");
        });
    }
    
    /**
     * Thiết lập màu sắc cho các chip
     */
    private void setupChips() {
        chipAll.setCheckedIconVisible(true);
        
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
                return;
            }
            
            // Lấy danh mục từ chip được chọn
            String category = "";
            if (checkedId == R.id.chipAll) {
                category = "";
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
}