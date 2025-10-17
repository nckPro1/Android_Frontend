package com.example.frontend.model;

public class ProfileMenuItem {
    private int iconResId;
    private String label;
    private String id; // Dùng để xác định hành động

    public ProfileMenuItem(String id, String label, int iconResId) {
        this.id = id;
        this.label = label;
        this.iconResId = iconResId;
    }

    public int getIconResId() { return iconResId; }
    public String getLabel() { return label; }
    public String getId() { return id; }
}