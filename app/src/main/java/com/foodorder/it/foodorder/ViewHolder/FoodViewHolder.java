package com.foodorder.it.foodorder.ViewHolder;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.foodorder.it.foodorder.Interface.ItemClickListener;
import com.foodorder.it.foodorder.R;



public class FoodViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView food_name,food_price;
    public ImageView food_image ,fav_image,share_image,quick_cart;
    private ItemClickListener itemClickListener;

    public FoodViewHolder(@NonNull View itemView) {
        super(itemView);

        food_name = itemView.findViewById(R.id.food_name);
        food_price = itemView.findViewById(R.id.food_price);
        food_image = itemView.findViewById(R.id.food_Image);
        fav_image = itemView.findViewById(R.id.fav);
        share_image = itemView.findViewById(R.id.btnShare);
        quick_cart = itemView.findViewById(R.id.btn_quick_cart);

        itemView.setOnClickListener(this);

    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void onClick(View view) {
        itemClickListener.Onclick(view,getAdapterPosition(),false);
    }
}
