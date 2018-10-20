package com.foodorder.it.foodorder;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.TextView;
import android.widget.Toast;

import com.andremion.counterfab.CounterFab;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.foodorder.it.foodorder.Common.Common;
import com.foodorder.it.foodorder.Database.Database;
import com.foodorder.it.foodorder.Interface.ItemClickListener;
import com.foodorder.it.foodorder.Model.Category;
import com.foodorder.it.foodorder.Model.Token;
import com.foodorder.it.foodorder.ViewHolder.MenuViewHolder;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;
import java.util.zip.Inflater;

import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class Home extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    FirebaseDatabase database;
    DatabaseReference category;

    TextView txtFullName;
    MaterialEditText edPassword,edNewPassword ,edRepeatPassword;
    CounterFab counterFab;
    Toolbar toolbar;

    RecyclerView recycler_menu;
    RecyclerView.LayoutManager layoutManager;

    FirebaseRecyclerAdapter<Category,MenuViewHolder> adapter;
    SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //add font library
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/restaurant_font.otf")
                .setFontAttrId(R.attr.fontPath).build());

        setContentView(R.layout.activity_home);

        refrencess();

        // refresh swipe
        setupRefreshSwipe();

        // get Category and put to adapter
        FirebaseRecyclerOptions<Category> options = new FirebaseRecyclerOptions.Builder<Category>()
                .setQuery(category,Category.class).build();

        adapter = new FirebaseRecyclerAdapter<Category, MenuViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull MenuViewHolder viewHolder, int position, @NonNull Category model) {

                viewHolder.txtMenuName.setText(model.getName());
                Picasso.with(getBaseContext()).load(model.getImage()).into(viewHolder.imageView);
                final Category clickItem = model;
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void Onclick(View view, int position, boolean isLongClick) {

                        Intent foodList = new Intent(Home.this,FoodList.class);
                        foodList.putExtra("categoryId",adapter.getRef(position).getKey());
                        startActivity(foodList);
                    }
                });

            }

            @NonNull
            @Override
            public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {

                View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.menu_item,parent,false);
                return new MenuViewHolder(itemView);
            }
        };

        //paper init for destroy when logout
        Paper.init(this);

        counterFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              Intent cartIntent = new Intent(Home.this,Cart.class);
              startActivity(cartIntent);
            }
        });


        // get count on cart fbButton
        counterFab.setCount(new Database(this).getCountCart());

       // setup recyclerView
        recycler_menu.setHasFixedSize(true);
        layoutManager =new GridLayoutManager(this,2);
        recycler_menu.setLayoutManager(layoutManager);
        LayoutAnimationController controller = AnimationUtils.loadLayoutAnimation(recycler_menu.getContext(),R.anim.layout_fall_down);
        recycler_menu.setLayoutAnimation(controller);

        updateToken(FirebaseInstanceId.getInstance().getToken());

        }


    private void updateToken(String data) {

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference token = db.getReference("Tokens");

        Token token1 = new Token(data,false); // false because this token is from client app
        token.child(Common.CurrentUser.getPhone()).setValue(token1);

    }

    private void Load_menu() {

        adapter.startListening();
        adapter.notifyDataSetChanged();
        recycler_menu.setAdapter(adapter);

        //Animation
        recycler_menu.getAdapter().notifyDataSetChanged();
        recycler_menu.scheduleLayoutAnimation();
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();

        // get count on cart fbButton
        counterFab.setCount(new Database(this).getCountCart() );

        // to listen again when activity back
        if(adapter!=null)
            adapter.startListening();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.refresh)
            Load_menu();
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_menu) {
            // Handle the camera action
        } else if (id == R.id.nav_cart) {
            Intent cartIntent = new Intent(Home.this,Cart.class);
            startActivity(cartIntent);

        } else if (id == R.id.nav_orders) {
            Intent orderIntent = new Intent(Home.this,OrderStatus.class);
            startActivity(orderIntent);

        }
        else if (id == R.id.changePassword)
        {
            showChangePasswordDialog(Common.CurrentUser.getPhone());
        }
        else if (id == R.id.nav_log_out) {

            //Remove paper remember user key and password
              Paper.book().destroy();

            //Logout
            Intent signIn = new Intent(Home.this,SignIn.class);
            signIn.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(signIn);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showChangePasswordDialog(String phone) {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Home.this);
        alertDialog.setTitle("CHANGE PASSWORD");
        alertDialog.setMessage("Please fill all information");

        View layout_pws = LayoutInflater.from(this).inflate(R.layout.change_password_layout,null);
         edPassword = layout_pws.findViewById(R.id.edPassword);
         edNewPassword = layout_pws.findViewById(R.id.edNewPassword);
         edRepeatPassword = layout_pws.findViewById(R.id.edRepeatPassword);

        alertDialog.setView(layout_pws);

        alertDialog.setPositiveButton("CHANGE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                //for use spots Dialog ,use alert Dialog from android.app
                final android.app.AlertDialog waitingDialog = new SpotsDialog(Home.this);
                waitingDialog.show();

                if(edPassword.getText().toString().equals(Common.CurrentUser.getPassword())) {
                    if(edNewPassword.getText().toString().equals(edRepeatPassword.getText().toString()))
                    {
                        //change password
                        Map<String,Object> passwordUpdate = new HashMap<>();
                        passwordUpdate.put("newPassword",edNewPassword.getText().toString());

                        DatabaseReference user = database.getReference("User");
                        user.child(Common.CurrentUser.getPhone()).updateChildren(passwordUpdate).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                waitingDialog.dismiss();
                                Toast.makeText(Home.this, "Password was updated", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(Home.this, "ERROR: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });

                    }else {
                        Toast.makeText(getBaseContext(), "Repeat Password not match ", Toast.LENGTH_SHORT).show();
                        waitingDialog.dismiss();
                    }
                    }
                else {
                    waitingDialog.dismiss();
                    Toast.makeText(getBaseContext(), "Current Password is not correct", Toast.LENGTH_SHORT).show();
                }
            }
        }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });

        alertDialog.show();
    }

    private void setupRefreshSwipe() {

        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,R.color.fbutton_color_green_sea,R.color.fbutton_color_green_sea,R.color.fbutton_color_green_sea);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (Common.isConnectToTheInternet(getBaseContext()))
                    Load_menu();
                else {
                    Toast.makeText(Home.this, "Please check your Connection !!", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });

        //Default load for first time
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {

                if (Common.isConnectToTheInternet(getBaseContext()))
                    Load_menu();
                else {
                    Toast.makeText(Home.this, "Please check your Connection !!", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });
    }
    private void refrencess() {

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Food Order");
        setSupportActionBar(toolbar);

        //init database
        database = FirebaseDatabase.getInstance();
        category = database.getReference("Category");
        swipeRefreshLayout = findViewById(R.id.swipe);
        counterFab = findViewById(R.id.fab);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //set user name
        View headerView = navigationView.getHeaderView(0);
        txtFullName = headerView.findViewById(R.id.txtFullName);
        txtFullName.setText(Common.CurrentUser.getName());

        recycler_menu = findViewById(R.id.recycler_menu);

    }
}
