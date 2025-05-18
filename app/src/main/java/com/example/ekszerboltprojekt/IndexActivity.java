package com.example.ekszerboltprojekt;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toolbar;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.MenuItemCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.google.firebase.firestore.FieldValue;

public class IndexActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private ArrayList<ShoppingItem> mItemlist;
    private ShoppingItemAdapter mAdapter;

    private FrameLayout redCircle;
    private TextView contentTextView;
    private TextView emptyView;
    private int gridNumber = 1;
    private int cartItems = 0;

    private static final String LOG_TAG = IndexActivity.class.getName();
    private FirebaseUser user;


    private final BroadcastReceiver cartChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateAlertIcon(); // 🔄 Frissíti a piros számot
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_index);

        user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null){
            Log.d(LOG_TAG, "Authenticated user!");

        }else{
            Log.d(LOG_TAG, "Not authenticated user!");
            finish();
        }

        mRecyclerView = findViewById(R.id.recyclerview);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, gridNumber));
        mItemlist = new ArrayList<>();

        mAdapter = new ShoppingItemAdapter(this, mItemlist);
        mRecyclerView.setAdapter(mAdapter);

        intializeData();
        mAdapter.notifyDataSetChanged();      // frissítjük az adaptert
        mAdapter.updateFullList();           // most másoljuk az adatokat szűréshez
        mAdapter.setOnItemAddToCartListener(() -> updateAlertIcon());

        emptyView = findViewById(R.id.empty_view);

        LocalBroadcastManager.getInstance(this).registerReceiver(cartChangedReceiver,
                new IntentFilter("KOSAR_VALTOZOTT"));

    }

    private void intializeData() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        mItemlist.clear();

        db.collection("termekek")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        ShoppingItem item = doc.toObject(ShoppingItem.class);
                        mItemlist.add(item);
                    }

                    mAdapter.notifyDataSetChanged();
                    mAdapter.updateFullList();
                })
                .addOnFailureListener(e -> {
                    Log.e("FIRESTORE", "Hiba a Firestore lekérdezésnél", e);
                });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.shop_list_menu,menu);
        MenuItem menuItem = menu.findItem(R.id.search_bar);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menuItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                Log.d(LOG_TAG, s);
                mAdapter.getFilter().filter(s);

                // kis késleltetés, hogy megvárjuk a szűrés eredményét
                new android.os.Handler().postDelayed(() -> {
                    if (mAdapter.getItemCount() == 0) {
                        emptyView.setVisibility(View.VISIBLE);
                    } else {
                        emptyView.setVisibility(View.GONE);
                    }
                }, 100);

                return false;
            }
        });
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.log_outButton) {
            Log.d(LOG_TAG, "LogOut clicked");
            FirebaseAuth.getInstance().signOut();
            finish();
            return true;

        } else if (id == R.id.setting_button) {
            Log.d(LOG_TAG, "Settings clicked");
            return true;

        } else if (id == R.id.cart) {
            Log.d(LOG_TAG, "Cart clicked");
            Intent intent = new Intent(this, CartActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        final MenuItem alertMenuItem = menu.findItem(R.id.cart);
        FrameLayout rootView = (FrameLayout) alertMenuItem.getActionView();

        redCircle = (FrameLayout) rootView.findViewById(R.id.view_alert_red_circle);
        contentTextView = (TextView) rootView.findViewById(R.id.view_alert_count_textview);

        rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onOptionsItemSelected(alertMenuItem);
            }
        });

        updateAlertIcon(); // mindig frissítse a kosárszámot
        return super.onPrepareOptionsMenu(menu);
    }

    public void updateAlertIcon() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (user == null || redCircle == null || contentTextView == null) {
            return;
        }

        String uid = user.getUid();

        db.collection("kosarak")
                .document(uid)
                .collection("termekek")
                .get()
                .addOnSuccessListener(query -> {
                    int totalCount = 0;
                    for (QueryDocumentSnapshot doc : query) {
                        Long qty = doc.getLong("quantity");
                        totalCount += (qty != null) ? qty : 0;
                    }

                    if (totalCount > 0) {
                        redCircle.setVisibility(View.VISIBLE);
                        contentTextView.setText(String.valueOf(totalCount));
                    } else {
                        redCircle.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> Log.e("KOSAR", "Nem sikerült betölteni a kosár darabszámot", e));
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(cartChangedReceiver);
        super.onDestroy();

    }
}