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
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.giaothong.R;
import com.example.giaothong.model.Flashcard;

import java.lang.reflect.Field;
import java.util.List;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;

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
            // Lấy ViewHolder trực tiếp từ RecyclerView trong ViewPager2
            RecyclerView.ViewHolder viewHolder = null;
            try {
                // Tìm ViewPager2 trong Activity
                if (context instanceof androidx.appcompat.app.AppCompatActivity) {
                    ViewPager2 viewPager = ((androidx.appcompat.app.AppCompatActivity) context)
                            .findViewById(R.id.viewPagerCards);
                    if (viewPager != null) {
                        Field recyclerViewField = ViewPager2.class.getDeclaredField("mRecyclerView");
                        recyclerViewField.setAccessible(true);
                        RecyclerView recyclerView = (RecyclerView) recyclerViewField.get(viewPager);
                        viewHolder = recyclerView.findViewHolderForAdapterPosition(position);
                    }
                }
            } catch (Exception e) {
                Log.e("FlashcardAdapter", "Không thể lấy ViewHolder", e);
            }
            
            if (viewHolder instanceof FlashcardViewHolder) {
                FlashcardViewHolder holder = (FlashcardViewHolder) viewHolder;
                Flashcard flashcard = flashcards.get(position);
                boolean isCurrentlyFlipped = flipped[position];
                
                // Thực hiện animation lật thẻ
                if (isCurrentlyFlipped) {
                    // Đang ở mặt sau, lật về mặt trước
                    animateCardFlip(holder, flashcard, false, position);
                } else {
                    // Đang ở mặt trước, lật sang mặt sau
                    animateCardFlip(holder, flashcard, true, position);
                }
            } else {
                // Phương án dự phòng nếu không tìm thấy ViewHolder
                flipped[position] = !flipped[position];
                notifyItemChanged(position);
                
                if (listener != null) {
                    listener.onCardFlip(position, flipped[position]);
                }
            }
        }
    }
    
    /**
     * Thực hiện animation lật thẻ với hiệu ứng mượt mà
     */
    private void animateCardFlip(FlashcardViewHolder holder, Flashcard flashcard, boolean toFlipped, int position) {
        // Cấu hình camera distance
        float scale = context.getResources().getDisplayMetrics().density;
        holder.cardViewFlashcard.setCameraDistance(8000 * scale);
        
        // Thiết lập các animator
        final ObjectAnimator firstHalfAnimator = ObjectAnimator.ofFloat(
                holder.cardViewFlashcard, "rotationY", 
                toFlipped ? 0f : 180f, 
                toFlipped ? 90f : 90f);
        
        final ObjectAnimator secondHalfAnimator = ObjectAnimator.ofFloat(
                holder.cardViewFlashcard, "rotationY", 
                toFlipped ? 90f : 90f, 
                toFlipped ? 180f : 0f);
        
        // Thiết lập thời gian
        firstHalfAnimator.setDuration(150);
        secondHalfAnimator.setDuration(150);
        
        // Thêm listener cho nửa đầu animation
        firstHalfAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // Cập nhật UI khi thẻ ở giữa animation (quay 90 độ)
                if (toFlipped) {
                    // Cập nhật thành UI của mặt sau
                    holder.textFlashcardContent.setText(flashcard.getAnswer());
                    holder.textCardSide.setText(R.string.answer);
                    holder.textTapToFlip.setVisibility(View.GONE);
                } else {
                    // Cập nhật thành UI của mặt trước
                    holder.textFlashcardContent.setText(flashcard.getQuestion());
                    holder.textCardSide.setText(R.string.question);
                    holder.textTapToFlip.setVisibility(View.VISIBLE);
                }
                
                // Đảo ngược các thành phần con để đọc được khi lật
                holder.textFlashcardContent.setRotationY(toFlipped ? -180f : 0f);
                holder.textCardSide.setRotationY(toFlipped ? -180f : 0f);
                holder.imageFlashcard.setRotationY(toFlipped ? -180f : 0f);
                holder.textTapToFlip.setRotationY(toFlipped ? -180f : 0f);
                
                // Bắt đầu nửa sau của animation
                secondHalfAnimator.start();
            }
        });
        
        // Thêm listener cho toàn bộ animation
        secondHalfAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // Cập nhật trạng thái sau khi hoàn tất animation
                flipped[position] = toFlipped;
                
                // Thông báo sự kiện lật thẻ
                if (listener != null) {
                    listener.onCardFlip(position, toFlipped);
                }
            }
        });
        
        // Bắt đầu animation
        firstHalfAnimator.start();
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
            // Thiết lập camera distance để tạo hiệu ứng 3D tốt hơn
            float scale = context.getResources().getDisplayMetrics().density;
            cardViewFlashcard.setCameraDistance(8000 * scale);
            
            // Đặt cardView quay đúng góc dựa trên trạng thái
            cardViewFlashcard.setRotationY(isFlipped ? 180f : 0f);
            
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
            
            // Đảo ngược nội dung khi thẻ bị lật để đọc được
            // Đối với văn bản quay 180 độ, chúng ta phải quay lại -180 để có thể đọc được
            if (isFlipped) {
                // Chú ý: Chúng ta phải quay mọi phần tử con bên trong
                textFlashcardContent.setRotationY(-180f);
                textCardSide.setRotationY(-180f);
                imageFlashcard.setRotationY(-180f);
                textTapToFlip.setRotationY(-180f);
                
                // Mặt sau - câu trả lời
                textFlashcardContent.setText(flashcard.getAnswer());
                textCardSide.setText(R.string.answer);
                // Ẩn hint khi đã lật thẻ
                textTapToFlip.setVisibility(View.GONE);
            } else {
                // Quay về vị trí bình thường
                textFlashcardContent.setRotationY(0f);
                textCardSide.setRotationY(0f);
                imageFlashcard.setRotationY(0f);
                textTapToFlip.setRotationY(0f);
                
                // Mặt trước - câu hỏi
                textFlashcardContent.setText(flashcard.getQuestion());
                textCardSide.setText(R.string.question);
                // Hiển thị "Chạm để lật thẻ" khi chưa lật
                textTapToFlip.setVisibility(View.VISIBLE);
            }
        }
    }
} 