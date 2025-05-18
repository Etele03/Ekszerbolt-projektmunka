package com.example.ekszerboltprojekt;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

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

        // ➕ gomb
        holder.increaseButton.setOnClickListener(v -> {
            int newQty = item.getQuantity() + 1;
            item.setQuantity(newQty); // 🔄 Frissítés azonnal a modellben
            notifyItemChanged(holder.getAdapterPosition()); // 🔄 Nézet frissítése
            updateQuantity(item, newQty); // Firestore frissítés
        });

        holder.decreaseButton.setOnClickListener(v -> {
            int newQty = item.getQuantity() - 1;
            if (newQty <= 0) {
                deleteItem(item);
                cartItems.remove(holder.getAdapterPosition()); // 🔴 Törlés a listából
                notifyItemRemoved(holder.getAdapterPosition());
            } else {
                item.setQuantity(newQty);
                notifyItemChanged(holder.getAdapterPosition());
                updateQuantity(item, newQty);
            }
        });
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title, price, quantity;
        Button increaseButton, decreaseButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.itemImage);
            title = itemView.findViewById(R.id.itemTitle);
            price = itemView.findViewById(R.id.price);
            quantity = itemView.findViewById(R.id.itemQuantity);
            increaseButton = itemView.findViewById(R.id.increaseButton);
            decreaseButton = itemView.findViewById(R.id.decreaseButton);
        }
    }

    private void updateQuantity(ShoppingItem item, int newQty) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            String uid = user.getUid();

            db.collection("kosarak")
                    .document(uid)
                    .collection("termekek")
                    .whereEqualTo("name", item.getName())
                    .get()
                    .addOnSuccessListener(query -> {
                        if (!query.isEmpty()) {
                            String docId = query.getDocuments().get(0).getId();
                            db.collection("kosarak")
                                    .document(uid)
                                    .collection("termekek")
                                    .document(docId)
                                    .update("quantity", newQty)
                                    .addOnSuccessListener(aVoid -> {
                                        // ✅ Jelzés frissítés után
                                        Intent intent = new Intent("KOSAR_VALTOZOTT");
                                        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                                    });
                        }
                    });
        }
    }


    private void deleteItem(ShoppingItem item) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            String uid = user.getUid();

            db.collection("kosarak")
                    .document(uid)
                    .collection("termekek")
                    .whereEqualTo("name", item.getName())
                    .get()
                    .addOnSuccessListener(query -> {
                        if (!query.isEmpty()) {
                            String docId = query.getDocuments().get(0).getId();
                            db.collection("kosarak")
                                    .document(uid)
                                    .collection("termekek")
                                    .document(docId)
                                    .delete()
                                    .addOnSuccessListener(aVoid -> {
                                        // ✅ Jelzés törlés után
                                        Intent intent = new Intent("KOSAR_VALTOZOTT");
                                        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                                    });
                        }
                    });
        }
    }

}
