package com.foodorder.it.foodorder;

import android.content.Context;
import android.content.DialogInterface;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.foodorder.it.foodorder.Common.Common;
import com.foodorder.it.foodorder.Database.Database;
import com.foodorder.it.foodorder.Model.MyResponse;
import com.foodorder.it.foodorder.Model.Notification;
import com.foodorder.it.foodorder.Model.Order;
import com.foodorder.it.foodorder.Model.Receivers_Token;
import com.foodorder.it.foodorder.Model.Request;
import com.foodorder.it.foodorder.Model.Token;
import com.foodorder.it.foodorder.Model.User;
import com.foodorder.it.foodorder.Remote.APIService;
import com.foodorder.it.foodorder.ViewHolder.CartAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.zip.Inflater;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class Cart extends AppCompatActivity {

    @BindView(R.id.total)
    TextView txtTotalPrice;
    @BindView(R.id.btnPlaceOrder)
    Button btnPlace;
    @BindView(R.id.listCart)
    RecyclerView recyclerView;

    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference requests;

    List<Order> cart = new ArrayList<>();
    CartAdapter adapter;

    MaterialEditText edAddress ,edComment;
    String address,comment;

    APIService mService;

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

        setContentView(R.layout.activity_cart);
        ButterKnife.bind(this);

        //setup recycler view
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);

        //init firebase
        database = FirebaseDatabase.getInstance();
        requests = database.getReference("Requests");

        // init APIService
      //  mService = Common.getFCMService();

        LoadListFood();

        btnPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(cart.size()>0)
                  showAlertDialog();
                else
                    Toast.makeText(Cart.this, "Your cart is Empty ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void LoadListFood() {

        cart = new Database(this).getCarts();
        adapter = new CartAdapter(cart,this);
        recyclerView.setAdapter(adapter);

        //Calculate total price
        int total=0;
        for(Order order:cart)
            total +=(Integer.parseInt(order.getPrice()))*(Integer.parseInt(order.getQuantity()));


        Locale locale = new Locale("en","US");
        NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);
        txtTotalPrice.setText(fmt.format(total));

    }

    private void showAlertDialog() {

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View custom_dialog = inflater.inflate(R.layout.custom_dialog,null);
        edAddress = custom_dialog.findViewById(R.id.address);
        edComment = custom_dialog.findViewById(R.id.commentEditText);

        dialog.setView(custom_dialog)
                .setTitle("one more step!")
                .setMessage("Enter your Address and comment")
                .setIcon(R.drawable.ic_shopping_cart_black_24dp);
        dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                address = edAddress.getText().toString();
                comment = edComment.getText().toString();
                addToFirebaseDatabase();
            }
        });
        dialog.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                dialogInterface.dismiss();
            }
        });
        dialog.show();
    }

    private void addToFirebaseDatabase() {
        Request request = new Request(
                Common.CurrentUser.getPhone(),
                Common.CurrentUser.getName(),
                address,
                txtTotalPrice.getText().toString(),
                cart,comment
        );

        String order_number = String.valueOf(System.currentTimeMillis());
        try {
            // upload to firebase
            requests.child(order_number).setValue(request);
        }catch (Exception e)
        {
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        //Delete Cart Orders
        new Database(this).cleanCart();
        LoadListFood();
        Toast.makeText(this, "Request send Succesffuly", Toast.LENGTH_SHORT).show();

        sendNotificationOrder(order_number);

    }

    private void sendNotificationOrder(final String order_number) {

        // get all server tokens from the database
        FirebaseDatabase db= FirebaseDatabase.getInstance();
        DatabaseReference ref = db.getReference("Tokens");

        Query query = ref.orderByChild("IsServer").equalTo(true);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot dataSnapshot1: dataSnapshot.getChildren())
                {
                    Token serverToken = dataSnapshot1.getValue(Token.class);

                    Notification notification = new Notification("peter","You have new order "+order_number);
                    Receivers_Token content = new Receivers_Token(serverToken.getToken(),notification);

                    mService.sendNotification(content)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                    if(response.body().success == 1)
                                    {
                                      Toast.makeText(getBaseContext(), "Order was updated", Toast.LENGTH_SHORT).show();
                                      finish();
                                    }
                                    else
                                        Toast.makeText(getBaseContext(), "order was updated but can't send notification", Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable t) {
                                    Log.e("ERROR:",t.getMessage());
                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if(item.getTitle().equals(Common.DELETE))
            deleteCart(item.getOrder());
        return true;
    }

    private void deleteCart(int position) {

        // delete cart from List <order> by position
        cart.remove(position);

        // delete all old data from database sql
        new Database(this).cleanCart();
        // update sql with new data
        for(Order item:cart)
        new Database(this).addToCart(item);

        LoadListFood();
    }
}
