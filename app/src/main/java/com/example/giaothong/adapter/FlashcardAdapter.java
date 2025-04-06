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
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.giaothong.R;
import com.example.giaothong.model.Flashcard;

import java.util.List;

public class FlashcardAdapter extends RecyclerView.Adapter<FlashcardAdapter.FlashcardViewHolder> {

    private final Context context;
    private final List<Flashcard> flashcards;
    private OnCardActionListener listener;
    
    // Theo dõi trạng thái lật của mỗi thẻ
    private final boolean[] flipped;

    public interface OnCardActionListener {
        void onCardFlip(int position, boolean isFlipped);
    }

    public FlashcardAdapter(Context context, List<Flashcard> flashcards) {
        this.context = context;
        this.flashcards = flashcards;
        this.flipped = new boolean[flashcards.size()];
    }

    public void setOnCardActionListener(OnCardActionListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public FlashcardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_flashcard, parent, false);
        return new FlashcardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FlashcardViewHolder holder, int position) {
        Flashcard flashcard = flashcards.get(position);
        
        // Cập nhật UI dựa trên trạng thái lật
        holder.bind(flashcard, flipped[position]);
    }

    @Override
    public int getItemCount() {
        return flashcards.size();
    }
    
    /**
     * Lật thẻ ở vị trí cụ thể
     */
    public void flipCard(int position) {
        if (position >= 0 && position < flipped.length) {
            flipped[position] = !flipped[position];
            notifyItemChanged(position);
            
            if (listener != null) {
                listener.onCardFlip(position, flipped[position]);
            }
        }
    }
    
    /**
     * Đặt lại trạng thái thẻ (sử dụng khi chuyển sang thẻ mới)
     */
    public void resetCardState(int position) {
        if (position >= 0 && position < flipped.length) {
            flipped[position] = false;
            notifyItemChanged(position);
        }
    }
    
    /**
     * Đặt lại trạng thái tất cả các thẻ
     */
    public void resetAllCards() {
        for (int i = 0; i < flipped.length; i++) {
            flipped[i] = false;
        }
        notifyDataSetChanged();
    }

    class FlashcardViewHolder extends RecyclerView.ViewHolder {
        private final CardView cardViewFlashcard;
        private final ImageView imageFlashcard;
        private final TextView textFlashcardContent;
        private final TextView textCardSide;
        private final TextView textTapToFlip;

        public FlashcardViewHolder(@NonNull View itemView) {
            super(itemView);
            cardViewFlashcard = itemView.findViewById(R.id.cardViewFlashcard);
            imageFlashcard = itemView.findViewById(R.id.imageFlashcard);
            textFlashcardContent = itemView.findViewById(R.id.textFlashcardContent);
            textCardSide = itemView.findViewById(R.id.textCardSide);
            textTapToFlip = itemView.findViewById(R.id.textTapToFlip);
            
            // Thiết lập sự kiện click để lật thẻ
            cardViewFlashcard.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    flipCard(position);
                }
            });
        }

        public void bind(Flashcard flashcard, boolean isFlipped) {
            // Hiển thị hình ảnh nếu có
            if (flashcard.getImageUrl() != null && !flashcard.getImageUrl().isEmpty()) {
                imageFlashcard.setVisibility(View.VISIBLE);
                
                try {
                    String imageUrl = flashcard.getImageUrl();
                    
                    // Xử lý các kiểu đường dẫn hình ảnh khác nhau
                    if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
                        // URL đầy đủ từ API
                        Glide.with(context)
                            .load(imageUrl)
                            .placeholder(R.drawable.ic_image_placeholder)
                            .error(R.drawable.ic_image_error)
                            .fitCenter()
                            .into(imageFlashcard);
                    } else if (imageUrl.startsWith("bien_bao/")) {
                        // Hình ảnh từ assets "bien_bao/pxxx.png"
                        String fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1, imageUrl.lastIndexOf("."));
                        
                        // Thử tải từ drawable trước
                        int resourceId = context.getResources().getIdentifier(
                                fileName, "drawable", context.getPackageName());
                        
                        if (resourceId != 0) {
                            Glide.with(context)
                                .load(resourceId)
                                .placeholder(R.drawable.ic_image_placeholder)
                                .error(R.drawable.ic_image_error)
                                .fitCenter()
                                .into(imageFlashcard);
                        } else {
                            // Thử tải từ assets
                            Glide.with(context)
                                .load("file:///android_asset/" + imageUrl)
                                .placeholder(R.drawable.ic_image_placeholder)
                                .error(R.drawable.ic_image_error)
                                .fitCenter()
                                .into(imageFlashcard);
                        }
                    } else {
                        // Thử tải ảnh trực tiếp từ đường dẫn
                        Glide.with(context)
                            .load(imageUrl)
                            .placeholder(R.drawable.ic_image_placeholder)
                            .error(R.drawable.ic_image_error)
                            .fitCenter()
                            .into(imageFlashcard);
                    }
                } catch (Exception e) {
                    // Log lỗi và hiển thị ảnh mặc định
                    Log.e("FlashcardAdapter", "Lỗi tải ảnh: " + e.getMessage());
                    imageFlashcard.setImageResource(R.drawable.ic_image_error);
                }
            } else {
                imageFlashcard.setVisibility(View.GONE);
            }
            
            // Cập nhật nội dung thẻ dựa trên trạng thái lật
            if (isFlipped) {
                // Mặt sau - câu trả lời
                textFlashcardContent.setText(flashcard.getAnswer());
                textCardSide.setText(R.string.answer);
            } else {
                // Mặt trước - câu hỏi
                textFlashcardContent.setText(flashcard.getQuestion());
                textCardSide.setText(R.string.question);
            }
            
            // Hiển thị "Chạm để lật thẻ" khi chưa lật sang mặt sau
            textTapToFlip.setVisibility(isFlipped ? View.GONE : View.VISIBLE);
        }
    }
} 