package com.example.giaothong.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.giaothong.R;
import com.example.giaothong.model.Flashcard;

import java.util.List;

public class FlashcardEditAdapter extends RecyclerView.Adapter<FlashcardEditAdapter.ViewHolder> {

    private final Context context;
    private List<Flashcard> flashcards;
    private OnFlashcardActionListener listener;

    public interface OnFlashcardActionListener {
        void onEditClick(Flashcard flashcard, int position);
        void onDeleteClick(Flashcard flashcard, int position);
    }

    public FlashcardEditAdapter(Context context, List<Flashcard> flashcards) {
        this.context = context;
        this.flashcards = flashcards;
    }

    public void setOnFlashcardActionListener(OnFlashcardActionListener listener) {
        this.listener = listener;
    }

    public void updateData(List<Flashcard> newFlashcards) {
        this.flashcards = newFlashcards;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_flashcard_edit, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Flashcard flashcard = flashcards.get(position);
        holder.bind(flashcard, position);
    }

    @Override
    public int getItemCount() {
        return flashcards != null ? flashcards.size() : 0;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textCardNumber;
        private final TextView textQuestion;
        private final TextView textAnswer;
        private final ImageView imageFlashcard;
        private final ImageButton buttonEdit;
        private final ImageButton buttonDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textCardNumber = itemView.findViewById(R.id.textCardNumber);
            textQuestion = itemView.findViewById(R.id.textQuestion);
            textAnswer = itemView.findViewById(R.id.textAnswer);
            imageFlashcard = itemView.findViewById(R.id.imageFlashcard);
            buttonEdit = itemView.findViewById(R.id.buttonEdit);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);

            buttonEdit.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onEditClick(flashcards.get(position), position);
                }
            });

            buttonDelete.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onDeleteClick(flashcards.get(position), position);
                }
            });
        }

        public void bind(Flashcard flashcard, int position) {
            // Hiển thị số thứ tự
            textCardNumber.setText(String.valueOf(position + 1));
            
            // Hiển thị câu hỏi và câu trả lời
            textQuestion.setText(flashcard.getQuestion());
            textAnswer.setText(flashcard.getAnswer());
            
            // Hiển thị hình ảnh nếu có
            if (flashcard.getImageUrl() != null && !flashcard.getImageUrl().isEmpty()) {
                imageFlashcard.setVisibility(View.VISIBLE);
                Glide.with(context)
                        .load(flashcard.getImageUrl())
                        .placeholder(R.drawable.ic_image_placeholder)
                        .error(R.drawable.ic_image_error)
                        .into(imageFlashcard);
            } else {
                imageFlashcard.setVisibility(View.GONE);
            }
        }
    }
} 