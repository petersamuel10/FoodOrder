package com.foodorder.it.foodorder;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.foodorder.it.foodorder.Interface.ItemClickListener;
import com.foodorder.it.foodorder.Model.Food;
import com.foodorder.it.foodorder.ViewHolder.FoodViewHolder;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class FoodList extends AppCompatActivity {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    MaterialSearchBar materialSearchBar;

    FirebaseDatabase database;
    DatabaseReference foodList;

    String categoryId="";
    FirebaseRecyclerAdapter<Food,FoodViewHolder> adapter;

    //search functionality
    FirebaseRecyclerAdapter<Food,FoodViewHolder> searchAdapter;
    List<String> suggestList = new ArrayList<>();

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

        materialSearchBar = findViewById(R.id.searchBar);


        // get Intent
        if(getIntent()!=null)
            categoryId = getIntent().getStringExtra("categoryId");
        if(!categoryId.isEmpty()&& categoryId!=null)
        {
            LoadFoodList(categoryId);
        }

        LoadSuggest();  // search suggest function
        materialSearchBar.setLastSuggestions(suggestList);
        materialSearchBar.setCardViewElevation(10);
        materialSearchBar.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                // when user type their text ,we will change suggest list 
                List<String> suggest = new ArrayList<>();
                for(String search:suggestList)
                {
                    if(search.toLowerCase().contains(materialSearchBar.getText().toLowerCase()))
                        suggest.add(search);
                }
                materialSearchBar.setLastSuggestions(suggest);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        materialSearchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {
                // when search closed
                // restore original adapter
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onSearchConfirmed(CharSequence text) {

                //when search is finished
                startSearch(text);
            }

            @Override
            public void onButtonClicked(int buttonCode) {

            }
        });

    }

    private void startSearch(CharSequence text) {
        searchAdapter =new FirebaseRecyclerAdapter<Food, FoodViewHolder>(
                       Food.class,
                       R.layout.food_item,
                       FoodViewHolder.class,
                       foodList.orderByChild("Name").equalTo(text.toString())) {
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
                        foodDetails.putExtra("foodId",searchAdapter.getRef(position).getKey());   // send food id to the new activity
                        startActivity(foodDetails);
                    }
                });
            }
        };
        recyclerView.setAdapter(searchAdapter);
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

    private void LoadSuggest() {

        foodList.orderByChild("MenuId").equalTo(categoryId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot postSnapshot:dataSnapshot.getChildren()) {
                    Food item = postSnapshot.getValue(Food.class);
                    suggestList.add(item.getName()); // all name of food to suggest list
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
