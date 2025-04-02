package com.example.giaothong.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.giaothong.R;
import com.example.giaothong.model.TrafficSign;

import java.util.List;

/**
 * Adapter for displaying traffic signs in a RecyclerView
 */
public class TrafficSignAdapter extends RecyclerView.Adapter<TrafficSignAdapter.TrafficSignViewHolder> {

    private final List<TrafficSign> trafficSigns;
    private final Context context;
    private OnItemClickListener listener;

    /**
     * Interface for click events on traffic sign items
     */
    public interface OnItemClickListener {
        void onItemClick(TrafficSign trafficSign, int position);
    }

    public TrafficSignAdapter(Context context, List<TrafficSign> trafficSigns) {
        this.context = context;
        this.trafficSigns = trafficSigns;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
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

    public class TrafficSignViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageSign;
        private final TextView textName;

        public TrafficSignViewHolder(@NonNull View itemView) {
            super(itemView);
            imageSign = itemView.findViewById(R.id.image_traffic_sign);
            textName = itemView.findViewById(R.id.text_sign_name);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onItemClick(trafficSigns.get(position), position);
                }
            });
        }

        public void bind(TrafficSign trafficSign) {
            textName.setText(trafficSign.getName());

            // Load image using Glide
            try {
                String imageUrl = trafficSign.getImagePath();
                
                // Kiểm tra xem có phải URL không
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    if (imageUrl.startsWith("http")) {
                        // Tải hình ảnh từ URL
                        Glide.with(context)
                            .load(imageUrl)
                            .apply(new RequestOptions()
                                    .centerInside()
                                    .override(300, 300))
                            .placeholder(R.drawable.ic_launcher_foreground)
                            .error(R.drawable.ic_launcher_foreground)
                            .into(imageSign);
                    } else {
                        // Tải từ assets
                        String assetPath = "file:///android_asset/" + imageUrl;
                        Glide.with(context)
                            .load(assetPath)
                            .apply(new RequestOptions()
                                    .centerInside()
                                    .override(300, 300))
                            .placeholder(R.drawable.ic_launcher_foreground)
                            .error(R.drawable.ic_launcher_foreground)
                            .into(imageSign);
                    }
                } else {
                    // Nếu không có hình ảnh, hiển thị placeholder
                    imageSign.setImageResource(R.drawable.ic_launcher_foreground);
                }
            } catch (Exception e) {
                // Nếu tải hình ảnh thất bại, hiển thị placeholder
                imageSign.setImageResource(R.drawable.ic_launcher_foreground);
            }
        }
    }
} 