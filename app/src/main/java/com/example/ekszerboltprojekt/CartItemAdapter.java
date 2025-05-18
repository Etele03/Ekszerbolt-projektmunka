package com.example.ekszerboltprojekt;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CartItemAdapter extends RecyclerView.Adapter<CartItemAdapter.ViewHolder> {

    private Context context;
    private List<ShoppingItem> cartItems;

    public CartItemAdapter(Context context, List<ShoppingItem> cartItems) {
        this.context = context;
        this.cartItems = cartItems;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_cart, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ShoppingItem item = cartItems.get(position);

        holder.title.setText(item.getName());
        holder.price.setText(item.getPrice());
        holder.quantity.setText(item.getQuantity() + " db");
        holder.image.setImageResource(item.getImageResource());
        holder.quantity.setText(item.getQuantity() + " db");
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title, price, quantity;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.itemImage);
            title = itemView.findViewById(R.id.itemTitle);
            price = itemView.findViewById(R.id.price);
            quantity = itemView.findViewById(R.id.itemQuantity);
        }
    }
}
