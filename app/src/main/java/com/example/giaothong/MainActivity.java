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
        recyclerView = findViewById(R.id.recyclerViewSigns);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        chipGroupCategories = findViewById(R.id.chipGroupCategories);
        textEmptyState = findViewById(R.id.textEmptyState);
        
        // Khởi tạo các chip lọc
        chipAll = findViewById(R.id.chipAll);
        chipCam = findViewById(R.id.chipCam);
        chipNguyHiem = findViewById(R.id.chipNguyHiem);
        chipHieuLenh = findViewById(R.id.chipHieuLenh);
        chipChiDan = findViewById(R.id.chipChiDan);
        chipPhu = findViewById(R.id.chipPhu);
        
        // Set up chips với màu sắc tương ứng
        setupChips();
        
        // Set up RecyclerView
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2)); // 2 columns
        
        // Set up SwipeRefreshLayout
        swipeRefreshLayout.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light
        );
        
        swipeRefreshLayout.setOnRefreshListener(() -> {
            // Khi người dùng vuốt để làm mới
            if (viewModel != null) {
                viewModel.refreshTrafficSigns();
                Toast.makeText(MainActivity.this, "Đang làm mới dữ liệu...", Toast.LENGTH_SHORT).show();
            }
        });
        
        // Initialize ViewModel - use the proper constructor for Java
        viewModel = new ViewModelProvider(this).get(TrafficSignViewModel.class);
        
        // Observe traffic sign data
        viewModel.getTrafficSigns().observe(this, trafficSigns -> {
            // Create and set adapter when data is available
            adapter = new TrafficSignAdapter(this, trafficSigns);
            
            // Set item click listener
            adapter.setOnItemClickListener((trafficSign, position) -> {
                // Hiển thị chi tiết biển báo trong bottom sheet
                showTrafficSignDetails(trafficSign);
            });
            
            recyclerView.setAdapter(adapter);
            
            // Cập nhật trạng thái UI
            updateStatusUI(trafficSigns.size());
            
            // Dừng hiệu ứng làm mới nếu đang hoạt động
            if (swipeRefreshLayout.isRefreshing()) {
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }
    
    /**
     * Thiết lập các chips lọc
     */
    private void setupChips() {
        // Thiết lập màu sắc cho text khi được chọn
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
                category = "Biển báo cấm";
            } else if (checkedId == R.id.chipNguyHiem) {
                category = "Biển báo nguy hiểm và cảnh báo";
            } else if (checkedId == R.id.chipHieuLenh) {
                category = "Biển hiệu lệnh";
            } else if (checkedId == R.id.chipChiDan) {
                category = "Biển chỉ dẫn";
            } else if (checkedId == R.id.chipPhu) {
                category = "Biển phụ";
            }
            
            // Cập nhật bộ lọc
            viewModel.setCategory(category);
        });
    }
    
    /**
     * Cập nhật trạng thái UI dựa trên số biển báo hiển thị
     * @param signCount Số biển báo hiện đang hiển thị
     */
    private void updateStatusUI(int signCount) {
        if (textEmptyState != null) {
            if (signCount == 0) {
                // Hiển thị trạng thái trống
                textEmptyState.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                // Hiển thị danh sách
                textEmptyState.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        }
    }
    
    /**
     * Hiển thị chi tiết biển báo trong bottom sheet
     */
    private void showTrafficSignDetails(TrafficSign trafficSign) {
        TrafficSignDetailBottomSheet bottomSheet = TrafficSignDetailBottomSheet.newInstance(trafficSign);
        bottomSheet.show(getSupportFragmentManager(), "traffic_sign_detail");
    }
}