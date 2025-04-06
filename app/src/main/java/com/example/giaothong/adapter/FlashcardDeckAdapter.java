package com.example.giaothong.adapter;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.giaothong.R;
import com.example.giaothong.model.FlashcardDeck;

import java.util.List;

public class FlashcardDeckAdapter extends RecyclerView.Adapter<FlashcardDeckAdapter.DeckViewHolder> {

    private final Context context;
    private List<FlashcardDeck> decks;
    private OnItemClickListener onItemClickListener;
    private OnItemLongClickListener onItemLongClickListener;

    public interface OnItemClickListener {
        void onItemClick(FlashcardDeck deck, int position);
    }
    
    public interface OnItemLongClickListener {
        boolean onItemLongClick(FlashcardDeck deck, int position);
    }

    public FlashcardDeckAdapter(Context context, List<FlashcardDeck> decks) {
        this.context = context;
        this.decks = decks;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }
    
    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.onItemLongClickListener = listener;
    }

    public void updateData(List<FlashcardDeck> newDecks) {
        this.decks = newDecks;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DeckViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_flashcard_deck, parent, false);
        return new DeckViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeckViewHolder holder, int position) {
        FlashcardDeck deck = decks.get(position);
        holder.bind(deck);
    }

    @Override
    public int getItemCount() {
        return decks.size();
    }

    class DeckViewHolder extends RecyclerView.ViewHolder {
        private final TextView textDeckName;
        private final TextView textDeckDescription;
        private final TextView textDeckCategory;
        private final TextView textCardCount;
        private final TextView textLastModified;

        public DeckViewHolder(@NonNull View itemView) {
            super(itemView);
            textDeckName = itemView.findViewById(R.id.textDeckName);
            textDeckDescription = itemView.findViewById(R.id.textDeckDescription);
            textDeckCategory = itemView.findViewById(R.id.textDeckCategory);
            textCardCount = itemView.findViewById(R.id.textCardCount);
            textLastModified = itemView.findViewById(R.id.textLastModified);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && onItemClickListener != null) {
                    onItemClickListener.onItemClick(decks.get(position), position);
                }
            });
            
            itemView.setOnLongClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && onItemLongClickListener != null) {
                    return onItemLongClickListener.onItemLongClick(decks.get(position), position);
                }
                return false;
            });
        }

        public void bind(FlashcardDeck deck) {
            textDeckName.setText(deck.getName());
            
            textDeckDescription.setVisibility(View.GONE);
            
            if (deck.getCategory() != null && !deck.getCategory().isEmpty()) {
                textDeckCategory.setText(deck.getCategory());
                textDeckCategory.setVisibility(View.VISIBLE);
            } else {
                textDeckCategory.setVisibility(View.GONE);
            }
            
            textCardCount.setText(context.getString(R.string.cards_count, deck.getCardCount()));
            
            textLastModified.setVisibility(View.GONE);
        }
    }
} 