package com.example.frontend.model;

public class TrackingStep {
    private String title;
    private String description;
    private String time;
    private boolean completed;
    private boolean current;

    // Constructors
    public TrackingStep() {}

    public TrackingStep(String title, String description, String time, boolean completed, boolean current) {
        this.title = title;
        this.description = description;
        this.time = time;
        this.completed = completed;
        this.current = current;
    }

    // Getters and Setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public boolean isCurrent() {
        return current;
    }

    public void setCurrent(boolean current) {
        this.current = current;
    }

    @Override
    public String toString() {
        return "TrackingStep{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", time='" + time + '\'' +
                ", completed=" + completed +
                ", current=" + current +
                '}';
    }
}
