package com.example.giaothong.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.giaothong.R;
import com.example.giaothong.adapter.SearchHistoryAdapter;
import com.example.giaothong.utils.SearchHistoryManager;

import java.util.List;

/**
 * Lớp hiển thị popup lịch sử tìm kiếm
 */
public class SearchHistoryPopup {
    
    private final Context context;
    private final PopupWindow popupWindow;
    private final View contentView;
    private final SearchHistoryManager historyManager;
    private final SearchHistoryAdapter adapter;
    private final RecyclerView recyclerViewHistory;
    private final TextView textNoHistory;
    
    // Callback interfaces
    private OnHistoryItemClickListener itemClickListener;
    
    public SearchHistoryPopup(Context context, View anchorView, SearchHistoryManager historyManager) {
        this.context = context;
        this.historyManager = historyManager;
        
        // Khởi tạo view từ layout
        contentView = LayoutInflater.from(context).inflate(R.layout.search_history_dropdown, null);
        
        // Tìm các view con
        recyclerViewHistory = contentView.findViewById(R.id.recyclerViewHistory);
        textNoHistory = contentView.findViewById(R.id.textNoHistory);
        
        // Thiết lập RecyclerView
        recyclerViewHistory.setLayoutManager(new LinearLayoutManager(context));
        List<String> historyItems = historyManager.getSearchHistory();
        adapter = new SearchHistoryAdapter(historyItems);
        recyclerViewHistory.setAdapter(adapter);
        
        // Hiển thị trạng thái trống nếu cần
        updateEmptyState();
        
        // Thiết lập sự kiện
        setupListeners();
        
        // Khởi tạo popup window
        popupWindow = new PopupWindow(
                contentView,
                anchorView.getWidth(),
                CardView.LayoutParams.WRAP_CONTENT,
                true
        );
        
        // Thiết lập animation và touch outside
        popupWindow.setAnimationStyle(android.R.style.Animation_Dialog);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(false);
        popupWindow.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
        popupWindow.setTouchable(true);
    }
    
    /**
     * Hiển thị popup tại vị trí anchor view
     * @param anchorView View làm điểm neo
     */
    public void show(View anchorView) {
        if (!isShowing()) {
            // Cập nhật dữ liệu trước khi hiển thị
            updateHistoryData();
            
            // Đảm bảo popup có đúng chiều rộng của anchorView
            contentView.measure(View.MeasureSpec.makeMeasureSpec(anchorView.getWidth(), View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            
            // Hiển thị popup đúng vị trí dưới SearchView với anchor mode at bottom
            popupWindow.setWidth(anchorView.getWidth());
            
            // Đảm bảo popup nằm ở đúng vị trí dưới SearchView
            int[] location = new int[2];
            anchorView.getLocationOnScreen(location);
            int x = location[0];
            int y = location[1] + anchorView.getHeight();
            
            // Sử dụng showAtLocation thay vì showAsDropDown để kiểm soát tốt hơn vị trí
            popupWindow.showAtLocation(anchorView, android.view.Gravity.NO_GRAVITY, x, y);
        }
    }
    
    /**
     * Ẩn popup
     */
    public void dismiss() {
        if (isShowing()) {
            popupWindow.dismiss();
        }
    }
    
    /**
     * Kiểm tra popup có đang hiển thị không
     * @return true nếu đang hiển thị
     */
    public boolean isShowing() {
        return popupWindow.isShowing();
    }
    
    /**
     * Cập nhật dữ liệu lịch sử tìm kiếm
     */
    public void updateHistoryData() {
        List<String> historyItems = historyManager.getSearchHistory();
        adapter.updateHistory(historyItems);
        updateEmptyState();
    }
    
    /**
     * Thiết lập các sự kiện cho các view
     */
    private void setupListeners() {
        // Sự kiện click vào item lịch sử
        adapter.setOnHistoryItemClickListener(query -> {
            if (itemClickListener != null) {
                itemClickListener.onHistoryItemClick(query);
            }
            dismiss();
        });
        
        // Sự kiện xóa một item lịch sử
        adapter.setOnHistoryItemRemoveListener((query, position) -> {
            historyManager.removeSearchQuery(query);
            adapter.removeItem(position);
            updateEmptyState();
        });
    }
    
    /**
     * Cập nhật trạng thái trống của lịch sử
     */
    private void updateEmptyState() {
        if (adapter.getItemCount() == 0) {
            textNoHistory.setVisibility(View.VISIBLE);
            recyclerViewHistory.setVisibility(View.GONE);
        } else {
            textNoHistory.setVisibility(View.GONE);
            recyclerViewHistory.setVisibility(View.VISIBLE);
        }
    }
    
    /**
     * Đặt listener xử lý sự kiện click vào item lịch sử
     * @param listener Listener cần đặt
     */
    public void setOnHistoryItemClickListener(OnHistoryItemClickListener listener) {
        this.itemClickListener = listener;
    }
    
    /**
     * Interface xử lý sự kiện khi click vào item lịch sử
     */
    public interface OnHistoryItemClickListener {
        void onHistoryItemClick(String query);
    }
} 