package com.example.giaothong.ui.quiz;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.giaothong.R;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

/**
 * Adapter để hiển thị danh sách các danh mục biển báo trong dialog chọn danh mục
 */
public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private List<CategoryItem> categories;
    private Context context;
    private OnCategorySelectedListener listener;
    private int selectedPosition = -1;
    
    /**
     * Class lưu trữ thông tin danh mục để hiển thị
     */
    public static class CategoryItem {
        private String code;
        private String name;
        private int count;
        
        public CategoryItem(String code, String name, int count) {
            this.code = code;
            this.name = name;
            this.count = count;
        }
        
        public String getCode() {
            return code;
        }
        
        public String getName() {
            return name;
        }
        
        public int getCount() {
            return count;
        }
    }
    
    /**
     * Interface lắng nghe sự kiện chọn danh mục
     */
    public interface OnCategorySelectedListener {
        void onCategorySelected(CategoryItem category, int position);
    }
    
    public CategoryAdapter(Context context, List<CategoryItem> categories) {
        this.context = context;
        this.categories = categories;
    }
    
    public void setOnCategorySelectedListener(OnCategorySelectedListener listener) {
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        CategoryItem category = categories.get(position);
        
        holder.textCategoryName.setText(category.getName());
        holder.textCategoryCount.setText(context.getString(R.string.signs_count, category.getCount()));
        
        // Disable card if there are not enough signs
        boolean hasEnoughSigns = category.getCount() >= 4;
        holder.itemView.setEnabled(hasEnoughSigns);
        holder.cardView.setAlpha(hasEnoughSigns ? 1.0f : 0.5f);
        
        // Set selected state
        holder.cardView.setChecked(position == selectedPosition);
        
        // Handle click
        holder.cardView.setOnClickListener(v -> {
            if (!hasEnoughSigns) {
                return;
            }
            
            int prevSelected = selectedPosition;
            selectedPosition = position;
            
            // Update UI
            if (prevSelected != -1) {
                notifyItemChanged(prevSelected);
            }
            notifyItemChanged(selectedPosition);
            
            // Notify listener
            if (listener != null) {
                listener.onCategorySelected(category, position);
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return categories != null ? categories.size() : 0;
    }
    
    /**
     * ViewHolder for category items
     */
    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView textCategoryName;
        TextView textCategoryCount;
        MaterialCardView cardView;
        
        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            textCategoryName = itemView.findViewById(R.id.textViewCategoryName);
            
            // Create a dummy TextView for the count
            textCategoryCount = new TextView(itemView.getContext());
            textCategoryCount.setVisibility(View.GONE);
            
            // Check if the itemView is already a MaterialCardView
            if (itemView instanceof MaterialCardView) {
                cardView = (MaterialCardView) itemView;
            } else {
                // Try to find a card view in the layout
                View cardViewParent = itemView.findViewById(R.id.cardViewCategory);
                if (cardViewParent instanceof MaterialCardView) {
                    cardView = (MaterialCardView) cardViewParent;
                } else {
                    // Create a dummy card view
                    cardView = new MaterialCardView(itemView.getContext());
                }
            }
        }
    }
} 