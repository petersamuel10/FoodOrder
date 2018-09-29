package com.foodorder.it.foodorder;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.foodorder.it.foodorder.Interface.ItemClickListener;
import com.foodorder.it.foodorder.Model.Food;
import com.foodorder.it.foodorder.ViewHolder.FoodViewHolder;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

public class FoodList extends AppCompatActivity {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference foodList;

    String categoryId="";
    FirebaseRecyclerAdapter<Food,FoodViewHolder> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_list);

        //init firebase
        database = FirebaseDatabase.getInstance();
        foodList = database.getReference("Foods");

        recyclerView = findViewById(R.id.recycler_food);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // get Intent
        if(getIntent()!=null)
            categoryId = getIntent().getStringExtra("categoryId");
        if(!categoryId.isEmpty()&& categoryId!=null)
        {
            LoadFoodList(categoryId);

        }

    }

    private void LoadFoodList(String categoryId) {

        adapter = new FirebaseRecyclerAdapter<Food, FoodViewHolder>
                (Food.class,R.layout.food_item,FoodViewHolder.class,
                 foodList.orderByChild("MenuId").equalTo(categoryId)) { //like :select * from foods where MenuId = categoryId;
            @Override
            protected void populateViewHolder(FoodViewHolder viewHolder, Food model, int position) {

                viewHolder.food_name.setText(model.getName());
                Picasso.with(FoodList.this).load(model.getImage()).into(viewHolder.food_image);

                final Food local = model;
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void Onclick(View view, int position, boolean isLongClick) {
                        //start food details activity
                        Intent foodDetails = new Intent(FoodList.this,FoodDetails.class);
                        foodDetails.putExtra("foodId",adapter.getRef(position).getKey());   // send food id to the new activity
                        startActivity(foodDetails);
                    }
                });
            }
        };

        //set adapter
        recyclerView.setAdapter(adapter);


    }
}
