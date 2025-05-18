package com.example.ekszerboltprojekt;

public class ShoppingItem {

    private String name;
    private String info;
    private String price;
    private float rated;
    private int imageResource; // ❗ final eltávolítva

    public ShoppingItem() {
        // Szükséges Firestore-hoz
    }

    public ShoppingItem(int imageResource, String name, String info, String price, float rated) {
        this.imageResource = imageResource;
        this.name = name;
        this.info = info;
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
