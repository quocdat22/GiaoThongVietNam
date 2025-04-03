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

    public interface OnItemClickListener {
        void onItemClick(FlashcardDeck deck, int position);
    }

    public FlashcardDeckAdapter(Context context, List<FlashcardDeck> decks) {
        this.context = context;
        this.decks = decks;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
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
        }

        public void bind(FlashcardDeck deck) {
            textDeckName.setText(deck.getName());
            
            if (deck.getDescription() != null && !deck.getDescription().isEmpty()) {
                textDeckDescription.setText(deck.getDescription());
                textDeckDescription.setVisibility(View.VISIBLE);
            } else {
                textDeckDescription.setVisibility(View.GONE);
            }
            
            if (deck.getCategory() != null && !deck.getCategory().isEmpty()) {
                textDeckCategory.setText(deck.getCategory());
                textDeckCategory.setVisibility(View.VISIBLE);
            } else {
                textDeckCategory.setVisibility(View.GONE);
            }
            
            textCardCount.setText(context.getString(R.string.cards_count, deck.getCardCount()));
            
            if (deck.getLastModified() > 0) {
                String timeAgo = DateUtils.getRelativeTimeSpanString(
                        deck.getLastModified(),
                        System.currentTimeMillis(),
                        DateUtils.MINUTE_IN_MILLIS,
                        DateUtils.FORMAT_ABBREV_RELATIVE
                ).toString();
                
                textLastModified.setText(context.getString(R.string.last_modified, timeAgo));
                textLastModified.setVisibility(View.VISIBLE);
            } else {
                textLastModified.setVisibility(View.GONE);
            }
        }
    }
} 