package com.example.ekszerboltprojekt;
import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

// 🔹 Interfész a kosárba adáshoz
interface OnItemAddToCartListener {
    void onAddToCart();
}

public class ShoppingItemAdapter extends RecyclerView.Adapter<ShoppingItemAdapter.ViewHolder> implements Filterable {

    private ArrayList<ShoppingItem> mItemlist;
    private List<ShoppingItem> itemListFull;
    private Context mContext;
    private OnItemAddToCartListener addToCartListener;

    public void setOnItemAddToCartListener(OnItemAddToCartListener listener) {
        this.addToCartListener = listener;
    }

    public ShoppingItemAdapter(Context context, ArrayList<ShoppingItem> itemList) {
        this.mItemlist = itemList;
        this.mContext = context;
        this.itemListFull = new ArrayList<>(itemList); // eredeti lista mentése
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View mItemView = LayoutInflater.from(mContext).inflate(R.layout.list_item, parent, false);
        return new ViewHolder(mItemView, this);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ShoppingItem mCurrent = mItemlist.get(position);
        holder.bindTo(mCurrent);

        // 🔄 animáció
        Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.slide_in_row);
        holder.itemView.startAnimation(animation);
    }

    @Override
    public int getItemCount() {
        return mItemlist.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView mTitleText;
        private TextView mInfoText;
        private TextView mPriceText;
        private ImageView mItemImage;
        private RatingBar mRatingBar;
        private Button mButton;
        private OnItemAddToCartListener listener;

        public ViewHolder(@NonNull View itemView, ShoppingItemAdapter adapter) {
            super(itemView);

            mTitleText = itemView.findViewById(R.id.itemTitle);
            mInfoText = itemView.findViewById(R.id.subTitle);
            mPriceText = itemView.findViewById(R.id.price);
            mItemImage = itemView.findViewById(R.id.itemImage);
            mRatingBar = itemView.findViewById(R.id.ratingBar);
            mButton = itemView.findViewById(R.id.add_to_cart);

            this.listener = adapter.addToCartListener; // 🔹 ide mentjük el a hivatkozást
        }

        public void bindTo(ShoppingItem currentItem) {
            mTitleText.setText(currentItem.getName());
            mInfoText.setText(currentItem.getInfo());
            mPriceText.setText(currentItem.getPrice());
            mItemImage.setImageResource(currentItem.getImageResource());
            mRatingBar.setRating(currentItem.getRated());

            mButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAddToCart();

                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    FirebaseFirestore db = FirebaseFirestore.getInstance();

                    if (user != null) {
                        String uid = user.getUid();
                        String termekNev = currentItem.getName();

                        db.collection("kosarak")
                                .document(uid)
                                .collection("termekek")
                                .whereEqualTo("name", termekNev)
                                .get()
                                .addOnSuccessListener(query -> {
                                    if (!query.isEmpty()) {
                                        String docId = query.getDocuments().get(0).getId();
                                        Long qty = query.getDocuments().get(0).getLong("quantity");
                                        long ujMennyiseg = (qty != null ? qty : 1) + 1;

                                        db.collection("kosarak")
                                                .document(uid)
                                                .collection("termekek")
                                                .document(docId)
                                                .update("quantity", ujMennyiseg)
                                                .addOnSuccessListener(aVoid -> {
                                                    // ⬅️ 🔴 Kosár ikon frissítése broadcasttal
                                                    Intent intent = new Intent("KOSAR_VALTOZOTT");
                                                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
                                                });
                                    } else {
                                        Map<String, Object> ujTermek = new HashMap<>();
                                        ujTermek.put("name", currentItem.getName());
                                        ujTermek.put("price", currentItem.getPrice());
                                        ujTermek.put("quantity", 1);
                                        ujTermek.put("imageResource", currentItem.getImageResource());

                                        db.collection("kosarak")
                                                .document(uid)
                                                .collection("termekek")
                                                .add(ujTermek)
                                                .addOnSuccessListener(ref -> {
                                                    // ⬅️ 🔴 Kosár ikon frissítése broadcasttal
                                                    Intent intent = new Intent("KOSAR_VALTOZOTT");
                                                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
                                                });
                                    }
                                });
                    }

                    // 🔔 ÉRTESÍTÉS
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.POST_NOTIFICATIONS)
                                != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions((Activity) mContext,
                                    new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
                        }
                    }

                    NotificationManager notificationManager = (NotificationManager)
                            mContext.getSystemService(Context.NOTIFICATION_SERVICE);

                    String channelId = "kosar_channel";
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        NotificationChannel channel = new NotificationChannel(
                                channelId,
                                "Kosár értesítések",
                                NotificationManager.IMPORTANCE_DEFAULT);
                        notificationManager.createNotificationChannel(channel);
                    }

                    NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext, channelId)
                            .setSmallIcon(R.drawable.ic_shopping_cart)
                            .setContentTitle("Kosárba adva")
                            .setContentText(currentItem.getName() + " a kosárhoz lett adva.")
                            .setAutoCancel(true);

                    notificationManager.notify(new Random().nextInt(), builder.build());

                    // ⏰ ALARM 30 mp MÚLVA
                    Intent intent = new Intent(mContext, KosarEmlekeztetoReceiver.class);
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(
                            mContext, 0, intent, PendingIntent.FLAG_IMMUTABLE);

                    AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
                    long triggerTime = System.currentTimeMillis() + 30 *  1000; // 30 mp
                    alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
                }
            });
        }

    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<ShoppingItem> filteredList = new ArrayList<>();

                if (constraint == null || constraint.length() == 0) {
                    filteredList.addAll(itemListFull);
                } else {
                    String filterPattern = constraint.toString().toLowerCase().trim();

                    for (ShoppingItem item : itemListFull) {
                        if (item.getName().toLowerCase().contains(filterPattern)) {
                            filteredList.add(item);
                        }
                    }
                }

                FilterResults results = new FilterResults();
                results.values = filteredList;
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                mItemlist.clear();
                mItemlist.addAll((List<ShoppingItem>) results.values);
                notifyDataSetChanged();
            }
        };
    }

    public void updateFullList() {
        itemListFull = new ArrayList<>(mItemlist);
    }
}
