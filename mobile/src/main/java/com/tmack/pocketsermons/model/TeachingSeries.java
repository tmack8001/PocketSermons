package com.tmack.pocketsermons.model;

/**
 * @author Trevor (drummer8001@gmail.com)
 * @since x.x.x
 */
public class TeachingSeries {
    private String id;
    private String title;
    private String description;
    private String imageUri;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }
}
