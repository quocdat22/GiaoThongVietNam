package com.example.giaothong.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.giaothong.R;

import java.util.List;

/**
 * Adapter hiển thị lịch sử tìm kiếm
 */
public class SearchHistoryAdapter extends RecyclerView.Adapter<SearchHistoryAdapter.ViewHolder> {
    
    private List<String> historyItems;
    private OnHistoryItemClickListener itemClickListener;
    private OnHistoryItemRemoveListener itemRemoveListener;
    
    public SearchHistoryAdapter(List<String> historyItems) {
        this.historyItems = historyItems;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.item_search_history, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String query = historyItems.get(position);
        holder.textSearchQuery.setText(query);
        
        // Xử lý sự kiện khi click vào item
        holder.itemView.setOnClickListener(v -> {
            if (itemClickListener != null) {
                itemClickListener.onHistoryItemClick(query);
            }
        });
        
        // Xử lý sự kiện khi click vào nút xóa
        holder.buttonRemove.setOnClickListener(v -> {
            if (itemRemoveListener != null) {
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    String itemToRemove = historyItems.get(adapterPosition);
                    itemRemoveListener.onHistoryItemRemove(itemToRemove, adapterPosition);
                }
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return historyItems.size();
    }
    
    /**
     * Cập nhật danh sách lịch sử tìm kiếm
     * @param historyItems Danh sách mới
     */
    public void updateHistory(List<String> historyItems) {
        this.historyItems = historyItems;
        notifyDataSetChanged();
    }
    
    /**
     * Xóa một item khỏi danh sách
     * @param position Vị trí cần xóa
     */
    public void removeItem(int position) {
        if (position < 0 || position >= historyItems.size()) {
            return;
        }
        
        historyItems.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, historyItems.size() - position);
    }
    
    /**
     * ViewHolder cho item lịch sử tìm kiếm
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView textSearchQuery;
        final ImageButton buttonRemove;
        
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            textSearchQuery = itemView.findViewById(R.id.textSearchQuery);
            buttonRemove = itemView.findViewById(R.id.buttonRemove);
        }
    }
    
    /**
     * Interface xử lý sự kiện khi click vào item lịch sử
     */
    public interface OnHistoryItemClickListener {
        void onHistoryItemClick(String query);
    }
    
    /**
     * Interface xử lý sự kiện khi xóa một item lịch sử
     */
    public interface OnHistoryItemRemoveListener {
        void onHistoryItemRemove(String query, int position);
    }
    
    /**
     * Đặt listener xử lý sự kiện click
     * @param listener Listener cần đặt
     */
    public void setOnHistoryItemClickListener(OnHistoryItemClickListener listener) {
        this.itemClickListener = listener;
    }
    
    /**
     * Đặt listener xử lý sự kiện xóa
     * @param listener Listener cần đặt
     */
    public void setOnHistoryItemRemoveListener(OnHistoryItemRemoveListener listener) {
        this.itemRemoveListener = listener;
    }
} 