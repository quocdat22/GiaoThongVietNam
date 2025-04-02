package com.example.giaothong.model;

import java.io.Serializable;

/**
 * Model class representing a traffic sign
 */
public class TrafficSign implements Serializable {
    private String id;
    private String name;
    private String description;
    private String imagePath;
    private String category;

    public TrafficSign(String id, String name, String description, String imagePath, String category) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.imagePath = imagePath;
        this.category = category;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
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

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
} 