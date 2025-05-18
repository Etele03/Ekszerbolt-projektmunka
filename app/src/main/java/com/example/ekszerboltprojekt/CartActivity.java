package com.example.ekszerboltprojekt;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class CartActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ArrayList<ShoppingItem> cartItems;
    private CartItemAdapter adapter;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        recyclerView = findViewById(R.id.cart_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        cartItems = new ArrayList<>();
        adapter = new CartItemAdapter(this, cartItems);
        recyclerView.setAdapter(adapter);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // 🔐 Bejelentkezés ellenőrzés
        if (user == null) {
            Toast.makeText(this, "Be kell jelentkezned a kosárhoz.", Toast.LENGTH_SHORT).show();
            finish(); // visszalép, ha nincs bejelentkezve
            return;
        }

        //  Kosár betöltése Firestore-ból
        String uid = user.getUid();

        db.collection("kosarak")
                .document(uid)
                .collection("termekek")
                .get()
                .addOnSuccessListener(query -> {
                    cartItems.clear();
                    for (QueryDocumentSnapshot doc : query) {
                        ShoppingItem item = doc.toObject(ShoppingItem.class);
                        cartItems.add(item);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Hiba a kosár betöltésekor", Toast.LENGTH_SHORT).show();
                    Log.e("CART", "Firestore hiba", e);
                });
    }
}
