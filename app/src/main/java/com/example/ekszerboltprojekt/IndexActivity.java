package com.example.ekszerboltprojekt;

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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public class IndexActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private ArrayList<ShoppingItem> mItemlist;
    private ShoppingItemAdapter mAdapter;

    private FrameLayout redCircle;
    private TextView contentTextView;
    private int gridNumber = 1;
    private int cartItems = 0;

    private static final String LOG_TAG = IndexActivity.class.getName();
    private FirebaseUser user;

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


    }

    private void intializeData() {
        String[] itemsList = getResources().getStringArray(R.array.shopping_item_names);
        String[] itemsInfo = getResources().getStringArray(R.array.shopping_item_desc);
        String[] itemsPrice = getResources().getStringArray(R.array.shopping_item_price);
        TypedArray itemsImageResource = getResources().obtainTypedArray(R.array.shopping_item_images);
        TypedArray itemsRate = getResources().obtainTypedArray(R.array.shopping_item_rates);

        mItemlist.clear();

        for (int i = 0; i < itemsList.length; i++){
            mItemlist.add(new ShoppingItem(itemsImageResource.getResourceId(i,0), itemsList[i],itemsInfo[i], itemsPrice[i], itemsRate.getFloat(i,0)));
        }

        itemsImageResource.recycle();
        mAdapter.notifyDataSetChanged();

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
                return false;
            }
        });
        return true;
    }


    /*
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case SETTINGS_ID:
                Log.d(LOG_TAG, "LogOut clicked");
                FirebaseAuth.getInstance().signOut();
                finish();
                return true;
            case R.id.setting_button:
                Log.d(LOG_TAG, "Settings clicked");
                return true;
            case R.id.cart:
                Log.d(LOG_TAG, "Cart clicked");
                return true;
            default: return super.onOptionsItemSelected(item);
        }
    }

*/


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


        return super.onPrepareOptionsMenu(menu);
    }

    public void updateAlertIcon(){
        cartItems = (cartItems + 1);
        if(0 < cartItems){
            contentTextView.setText(String.valueOf(cartItems));
        }else{
            contentTextView.setText(" ");
        }

    }
}