package com.example.giaothong.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.giaothong.R;
import com.example.giaothong.model.TrafficSign;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

/**
 * Adapter for displaying traffic signs in a RecyclerView
 */
public class TrafficSignAdapter extends RecyclerView.Adapter<TrafficSignAdapter.TrafficSignViewHolder> {

    private final List<TrafficSign> trafficSigns;
    private final Context context;
    private OnItemClickListener clickListener;
    private OnItemLongClickListener longClickListener;

    /**
     * Interface for click events on traffic sign items
     */
    public interface OnItemClickListener {
        void onItemClick(TrafficSign trafficSign, int position);
    }
    
    /**
     * Interface for long click events on traffic sign items
     */
    public interface OnItemLongClickListener {
        boolean onItemLongClick(TrafficSign trafficSign, int position);
    }

    public TrafficSignAdapter(Context context, List<TrafficSign> trafficSigns) {
        this.context = context;
        this.trafficSigns = trafficSigns;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.clickListener = listener;
    }
    
    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.longClickListener = listener;
    }
    
    /**
     * Cập nhật danh sách biển báo
     * @param newTrafficSigns Danh sách biển báo mới
     */
    public void setTrafficSigns(List<TrafficSign> newTrafficSigns) {
        this.trafficSigns.clear();
        this.trafficSigns.addAll(newTrafficSigns);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TrafficSignViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_traffic_sign, parent, false);
        return new TrafficSignViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TrafficSignViewHolder holder, int position) {
        TrafficSign trafficSign = trafficSigns.get(position);
        holder.bind(trafficSign);
    }

    @Override
    public int getItemCount() {
        return trafficSigns.size();
    }

    class TrafficSignViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageView;
        private final TextView textName;
        private final TextView textDescription;
        private final CardView cardView;
        private final ImageView pinIndicator;

        TrafficSignViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageViewSign);
            textName = itemView.findViewById(R.id.textViewSignName);
            textDescription = itemView.findViewById(R.id.textViewDescription);
            cardView = itemView.findViewById(R.id.cardViewSignImage);
            pinIndicator = itemView.findViewById(R.id.imagePinIndicator);
            
            // Set up click listeners
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && clickListener != null) {
                    clickListener.onItemClick(trafficSigns.get(position), position);
                }
            });
            
            itemView.setOnLongClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && longClickListener != null) {
                    return longClickListener.onItemLongClick(trafficSigns.get(position), position);
                }
                return false;
            });
        }

        void bind(TrafficSign trafficSign) {
            textName.setText(trafficSign.getName());
            
            // Set the description text
            if (trafficSign.getDescription() != null && !trafficSign.getDescription().isEmpty()) {
                textDescription.setText(trafficSign.getDescription());
                textDescription.setVisibility(View.VISIBLE);
                Log.d("TrafficSignAdapter", "Sign " + trafficSign.getId() + " has description: " + trafficSign.getDescription());
            } else {
                textDescription.setVisibility(View.GONE);
                Log.d("TrafficSignAdapter", "Sign " + trafficSign.getId() + " has NO description");
            }
            
            // Hiển thị trạng thái ghim với visual distinction
            if (trafficSign.isPinned()) {
                // Show pin indicator icon
                pinIndicator.setVisibility(View.VISIBLE);
                
                // Change background color of image card
                cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.colorPinnedBackground));
                
                // Make the title text a different color - use accent color for pinned items
                textName.setTextColor(ContextCompat.getColor(context, R.color.colorAccent));
                
                // Make the text bold for pinned items
                textName.setTypeface(textName.getTypeface(), android.graphics.Typeface.BOLD);
            } else {
                // Hide pin indicator for unpinned items
                pinIndicator.setVisibility(View.GONE);
                
                // Reset to default background
                cardView.setCardBackgroundColor(ContextCompat.getColor(context, android.R.color.white));
                
                // Reset text color
                textName.setTextColor(ContextCompat.getColor(context, R.color.gray_800));
                
                // Reset text style
                textName.setTypeface(textName.getTypeface(), android.graphics.Typeface.NORMAL);
            }
            
            // Tải hình ảnh
            String imagePath = trafficSign.getImagePath();
            if (imagePath != null && !imagePath.isEmpty()) {
                // Set scaleType to FIT_CENTER to prevent cropping
                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                
                // Prepare RequestOptions for flexible image loading
                RequestOptions requestOptions = new RequestOptions()
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .error(R.drawable.ic_launcher_foreground)
                        .fitCenter() // Use fitCenter instead of centerCrop to maintain aspect ratio
                        .diskCacheStrategy(DiskCacheStrategy.ALL); // Cache images for better performance
                
                if (imagePath.startsWith("http")) {
                    // Load từ URL
                    Glide.with(context)
                            .load(imagePath)
                            .apply(requestOptions)
                            .into(imageView);
                } else {
                    // Load từ assets
                    String assetPath = "file:///android_asset/" + imagePath;
                    Glide.with(context)
                            .load(assetPath)
                            .apply(requestOptions)
                            .into(imageView);
                }
            } else {
                // Hiển thị ảnh mặc định nếu không có ảnh
                imageView.setScaleType(ImageView.ScaleType.CENTER);
                imageView.setImageResource(R.drawable.ic_launcher_foreground);
            }
        }
    }
} 