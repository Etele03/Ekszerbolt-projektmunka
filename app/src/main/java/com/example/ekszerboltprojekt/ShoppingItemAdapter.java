package com.example.ekszerboltprojekt;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.Filter;
import android.widget.Filterable;

import java.util.ArrayList;
import java.util.List;

public class ShoppingItemAdapter extends RecyclerView.Adapter<ShoppingItemAdapter.ViewHolder> implements Filterable {

    private ArrayList<ShoppingItem> mItemlist;
    private List<ShoppingItem> itemListFull;
    private Context mContext;

    public ShoppingItemAdapter(Context context, ArrayList<ShoppingItem> itemList) {
        this.mItemlist = itemList;
        this.mContext = context;
        this.itemListFull = new ArrayList<>(itemList); // másolat az eredeti adathoz
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

        // Animáció betöltése és alkalmazása
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

        public ViewHolder(@NonNull View itemView, ShoppingItemAdapter adapter) {
            super(itemView);

            mTitleText = itemView.findViewById(R.id.itemTitle);
            mInfoText = itemView.findViewById(R.id.subTitle);
            mPriceText = itemView.findViewById(R.id.price);
            mItemImage = itemView.findViewById(R.id.itemImage);
            mRatingBar = itemView.findViewById(R.id.ratingBar);
            mButton = itemView.findViewById(R.id.add_to_cart);
        }

        public void bindTo(ShoppingItem currentItem) {
            mTitleText.setText(currentItem.getName());
            mInfoText.setText(currentItem.getInfo());
            mPriceText.setText(currentItem.getPrice());
            mItemImage.setImageResource(currentItem.getImageResource());
            mRatingBar.setRating(currentItem.getRated());

            // Kosár gomb működését ide írhatod
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
