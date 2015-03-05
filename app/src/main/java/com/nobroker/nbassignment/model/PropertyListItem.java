package com.nobroker.nbassignment.model;

import java.util.ArrayList;

public class PropertyListItem {
    private String title, thumbnailUrl;
    private String rent;
    private String size;

    public PropertyListItem() {
    }

    public PropertyListItem(String name, String thumbnailUrl, double rent, double size,
                 ArrayList<String> genre) {
        this.title = name;
        this.thumbnailUrl = thumbnailUrl;
        this.rent = Double.toString(rent);
        this.size = Double.toString(size);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String name) {
        this.title = name;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getRent() {
        return rent;
    }

    public void setRent(double rent) {
        this.rent = Double.toString(rent);
    }

    public String getSize() {
        return size;
    }

    public void setSize(double size) {
        this.size = Double.toString(size);
    }
}
