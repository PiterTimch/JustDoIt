package com.example.justdoit.dto.zadachi;

public class ZadachaItemDTO {
    private long id;
    private String name;
    private String image;

    private boolean selected = false;

    public ZadachaItemDTO(long id, String name, String image) {
        this.id = id;
        this.name = name;
        this.image = image;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
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

    public String getImage() { return image; }

    public void setImage(String image) { this.image = image; }
}