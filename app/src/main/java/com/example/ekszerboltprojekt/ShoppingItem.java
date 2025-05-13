package com.example.ekszerboltprojekt;

public class ShoppingItem {

    private String name;
    private String info;
    private String price;
    private float rated;
    private final int imageResource;

    public ShoppingItem(int imageResource, String info, String name, String price, float rated) {
        this.imageResource = imageResource;
        this.info = info;
        this.name = name;
        this.price = price;
        this.rated = rated;
    }

    public int getImageResource() {
        return imageResource;
    }
    public String getInfo() {
        return info;
    }
    public String getName() {
        return name;
    }
    public String getPrice() {
        return price;
    }
    public float getRated() {
        return rated;
    }
}
