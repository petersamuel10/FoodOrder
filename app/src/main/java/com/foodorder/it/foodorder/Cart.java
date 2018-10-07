package com.foodorder.it.foodorder;

import android.content.DialogInterface;
import android.provider.ContactsContract;
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
import com.foodorder.it.foodorder.Model.Order;
import com.foodorder.it.foodorder.Model.Request;
import com.foodorder.it.foodorder.Model.User;
import com.foodorder.it.foodorder.ViewHolder.CartAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.zip.Inflater;

import butterknife.BindView;
import butterknife.ButterKnife;

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

    MaterialEditText edAddress;
    String address;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        ButterKnife.bind(this);

        //setup recycler view
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);

        //init firebase
        database = FirebaseDatabase.getInstance();
        requests = database.getReference("Requests");

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

        dialog.setView(custom_dialog)
                .setTitle("one more step!")
                .setMessage("Enter your Address")
                .setIcon(R.drawable.ic_shopping_cart_black_24dp);
        dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                address = edAddress.getText().toString();
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
                cart
        );
        try {

            // upload to firebase
            requests.child(String.valueOf(System.currentTimeMillis())).setValue(request);
        }catch (Exception e)
        {
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        //Delete Cart Orders
        new Database(this).cleanCart();
        Toast.makeText(this, "Thank you , Order Place", Toast.LENGTH_SHORT).show();
        finish();
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
