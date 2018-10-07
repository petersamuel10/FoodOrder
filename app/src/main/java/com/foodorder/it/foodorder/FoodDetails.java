package com.foodorder.it.foodorder;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.foodorder.it.foodorder.Common.Common;
import com.foodorder.it.foodorder.Database.Database;
import com.foodorder.it.foodorder.Model.Food;
import com.foodorder.it.foodorder.Model.Order;
import com.foodorder.it.foodorder.Model.Rating;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.stepstone.apprating.AppRatingDialog;
import com.stepstone.apprating.listener.RatingDialogListener;

import java.lang.reflect.Array;
import java.util.Arrays;

public class FoodDetails extends AppCompatActivity implements RatingDialogListener {

    TextView food_name, food_price, food_description;
    ImageView food_image;
    CollapsingToolbarLayout collapsingToolbarLayout;
    ElegantNumberButton numberButton;
    FloatingActionButton btnCart, btnRating;

    RatingBar ratingBar;

    String foodId = "";

    FirebaseDatabase database;
    DatabaseReference foods;
    DatabaseReference ratingTbl;

    Food currentFood;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_details);

        //init firebase
        database = FirebaseDatabase.getInstance();
        foods = database.getReference("Foods");
        ratingTbl = database.getReference("Rating");

        // init views
        food_name = findViewById(R.id.food_name);
        food_price = findViewById(R.id.food_price);
        food_description = findViewById(R.id.food_description);

        food_image = findViewById(R.id.img_food);

        numberButton = findViewById(R.id.number_button);
        btnCart = findViewById(R.id.btnCart);
        btnRating = findViewById(R.id.btnRating);
        ratingBar = findViewById(R.id.ratingBar);

        btnRating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(FoodDetails.this, "donnnnnnnnne", Toast.LENGTH_SHORT).show();
                  showRatingDialog();
            }
        });

        btnCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new Database(getBaseContext()).addToCart(new Order(
                        foodId,
                        currentFood.getName(),
                        numberButton.getNumber(),
                        currentFood.getPrice(),
                        currentFood.getDiscount()
                ));
                Toast.makeText(FoodDetails.this, "Order added to Cart", Toast.LENGTH_SHORT).show();
            }
        });


        collapsingToolbarLayout = findViewById(R.id.collapsing);
        collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.expanedAppBar);
        collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.collapsedAppBar);

        //Get Food Id from Intent
        if(getIntent()!=null)
            foodId = getIntent().getStringExtra("foodId");

        if(!foodId.isEmpty()&&foodId!=null) {
         if(Common.isConnectToTheInternet(getBaseContext())) {

             GetDetailsFood(foodId);
             getRatingFood(foodId);
         }
            else
         {
             Toast.makeText(this, "Please check your Connection !!", Toast.LENGTH_SHORT).show();
         }
        }

    }

    private void getRatingFood(String foodId) {
       // Rating rating = new Rating();
        Query query = ratingTbl.orderByChild("foodId").equalTo(foodId);

        query.addValueEventListener(new ValueEventListener() {
            int sum = 0;
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
               int count = 0;
                count = (int) dataSnapshot.getChildrenCount();
                for(DataSnapshot postSnapshot : dataSnapshot.getChildren())
                {
                    Rating rating1 = postSnapshot.getValue(Rating.class);
                    sum+= Integer.parseInt(rating1.getRateValue());
                }
                if(count!=0) {
                    float average = sum / count;
                    ratingBar.setRating(average);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void showRatingDialog() {

        new AppRatingDialog.Builder()
                .setPositiveButtonText("Submit")
                .setNegativeButtonText("Cancel")
                .setNoteDescriptions(Arrays.asList("Very Bad", "Not good", "Quite ok", "Very Good", "Excellent !!!"))
                .setDefaultRating(1)
                .setTitle("Rate this application")
                .setDescription("Please select some stars and give your feedback")
                .setCommentInputEnabled(true)
                .setStarColor(R.color.colorAccent)
                .setNoteDescriptionTextColor(R.color.colorPrimaryDark)
                .setTitleTextColor(R.color.colorPrimary)
                .setDescriptionTextColor(R.color.colorPrimary)
                .setHint("Please write your comment here ...")
                .setHintTextColor(R.color.colorAccent)
                .setCommentTextColor(R.color.white)
                .setCommentBackgroundColor(R.color.colorPrimaryDark)
                .setWindowAnimation(R.style.RatingDialogFadeAnim)
                .create(FoodDetails.this)
                .show();

    }

    private void GetDetailsFood(final String foodId) {

        foods.child(foodId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                currentFood = dataSnapshot.getValue(Food.class);


                //Set Image
                Picasso.with(FoodDetails.this).load(currentFood.getImage()).into(food_image);

                collapsingToolbarLayout.setTitle(currentFood.getName());

                food_price.setText(currentFood.getPrice());
                food_name.setText(currentFood.getName());
                food_description.setText(currentFood.getDescription());

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onNegativeButtonClicked() {

    }

    @Override
    public void onNeutralButtonClicked() {

    }

    @Override
    public void onPositiveButtonClicked(int value, String comment) {

        final Rating rating = new Rating(Common.CurrentUser.getPhone(),foodId,String.valueOf(value),comment);

        ratingTbl.child(Common.CurrentUser.getPhone()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ratingTbl.child(Common.CurrentUser.getPhone()).setValue(rating);
                getRatingFood(foodId);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

}
