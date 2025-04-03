package com.example.giaothong.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Model dữ liệu cho một bộ thẻ ghi nhớ
 */
public class FlashcardDeck implements Serializable {
    private long id;
    private String name;
    private String description;
    private String category;
    private long createdAt;
    private long lastModified;
    private int cardCount;
    private List<Flashcard> cards;

    public FlashcardDeck() {
        this.createdAt = System.currentTimeMillis();
        this.lastModified = System.currentTimeMillis();
        this.cards = new ArrayList<>();
    }

    public FlashcardDeck(String name) {
        this();
        this.name = name;
    }

    public FlashcardDeck(String name, String description) {
        this(name);
        this.description = description;
    }

    public FlashcardDeck(String name, String description, String category) {
        this(name, description);
        this.category = category;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getLastModified() {
        return lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    public void updateLastModified() {
        this.lastModified = System.currentTimeMillis();
    }

    public int getCardCount() {
        return cardCount;
    }

    public void setCardCount(int cardCount) {
        this.cardCount = cardCount;
    }

    public List<Flashcard> getCards() {
        return cards;
    }

    public void setCards(List<Flashcard> cards) {
        this.cards = cards;
        this.cardCount = cards.size();
    }

    public void addCard(Flashcard card) {
        if (this.cards == null) {
            this.cards = new ArrayList<>();
        }
        card.setDeckId(this.id);
        this.cards.add(card);
        this.cardCount = this.cards.size();
        updateLastModified();
    }

    public void removeCard(Flashcard card) {
        if (this.cards != null) {
            this.cards.remove(card);
            this.cardCount = this.cards.size();
            updateLastModified();
        }
    }

    public void removeCard(int position) {
        if (this.cards != null && position >= 0 && position < this.cards.size()) {
            this.cards.remove(position);
            this.cardCount = this.cards.size();
            updateLastModified();
        }
    }
} 