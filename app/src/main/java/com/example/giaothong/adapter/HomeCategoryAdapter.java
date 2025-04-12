package com.example.giaothong.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.giaothong.R;
import com.example.giaothong.model.TrafficSign;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

/**
 * Adapter to display categories on the Home screen
 */
public class HomeCategoryAdapter extends RecyclerView.Adapter<HomeCategoryAdapter.CategoryViewHolder> {

    private final Context context;
    private final List<CategoryItem> categories;
    private OnCategoryClickListener listener;

    /**
     * Data class for category information
     */
    public static class CategoryItem {
        private final String categoryId;
        private final String name;
        private final String code; // Code used in the repository
        private final int count;
        private final TrafficSign firstSign; // First sign in the category to use as icon

        public CategoryItem(String categoryId, String name, String code, int count, TrafficSign firstSign) {
            this.categoryId = categoryId;
            this.name = name;
            this.code = code;
            this.count = count;
            this.firstSign = firstSign;
        }

        public String getCategoryId() {
            return categoryId;
        }

        public String getName() {
            return name;
        }

        public String getCode() {
            return code;
        }

        public int getCount() {
            return count;
        }

        public TrafficSign getFirstSign() {
            return firstSign;
        }
    }

    /**
     * Interface for category click events
     */
    public interface OnCategoryClickListener {
        void onCategoryClick(CategoryItem category);
    }

    public HomeCategoryAdapter(Context context, List<CategoryItem> categories) {
        this.context = context;
        this.categories = categories;
    }

    public void setOnCategoryClickListener(OnCategoryClickListener listener) {
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
        holder.bind(category);
    }

    @Override
    public int getItemCount() {
        return categories != null ? categories.size() : 0;
    }

    class CategoryViewHolder extends RecyclerView.ViewHolder {
        private final TextView textCategoryName;
        private final ImageView imageViewCategory;
        private final MaterialCardView cardViewCategory;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            textCategoryName = itemView.findViewById(R.id.textViewCategoryName);
            imageViewCategory = itemView.findViewById(R.id.imageViewCategory);
            cardViewCategory = itemView.findViewById(R.id.cardViewCategory);

            itemView.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onCategoryClick(categories.get(position));
                }
            });
        }

        void bind(CategoryItem category) {
            textCategoryName.setText(category.getName());
            
            // Special case for "All Signs" category which uses a custom icon
            if (category.getCategoryId().equals("all")) {
                imageViewCategory.setImageResource(R.drawable.ic_all_category);
                return;
            }
            
            // For other categories, load the first traffic sign image as the category icon
            TrafficSign firstSign = category.getFirstSign();
            if (firstSign != null && firstSign.getImagePath() != null && !firstSign.getImagePath().isEmpty()) {
                // Create RequestOptions for consistent image loading
                RequestOptions requestOptions = new RequestOptions()
                    .placeholder(R.drawable.ic_image_placeholder)
                    .error(R.drawable.ic_image_error)
                    .fitCenter() // Use fitCenter to maintain aspect ratio
                    .diskCacheStrategy(DiskCacheStrategy.ALL);
                
                // Load image from the correct source
                if (firstSign.getImagePath().startsWith("http")) {
                    // Load from URL
                    Glide.with(context)
                        .load(firstSign.getImagePath())
                        .apply(requestOptions)
                        .into(imageViewCategory);
                } else {
                    // Load from assets
                    String assetPath = "file:///android_asset/" + firstSign.getImagePath();
                    Glide.with(context)
                        .load(assetPath)
                        .apply(requestOptions)
                        .into(imageViewCategory);
                }
            } else {
                // Default icon if no image is available
                imageViewCategory.setImageResource(R.drawable.ic_image_placeholder);
            }
        }
    }
} 