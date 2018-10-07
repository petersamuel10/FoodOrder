package com.foodorder.it.foodorder;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.foodorder.it.foodorder.Common.Common;
import com.foodorder.it.foodorder.Model.User;
import com.google.android.gms.signin.SignIn;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.paperdb.Paper;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.signUp)
    Button signUp;
    @BindView(R.id.signIn)
    Button signIn;
    @BindView(R.id.sLog)
    TextView txtSlogan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);
        Typeface face = Typeface.createFromAsset(getAssets(),"fonts/NABILA.TTF");
        txtSlogan.setTypeface(face);

        //init paper
        Paper.init(this);

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent_signUp = new Intent (MainActivity.this, SignUp.class);
                startActivity(intent_signUp);
            }
        });

        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent_signIn = new Intent (MainActivity.this, com.foodorder.it.foodorder.SignIn.class);
                startActivity(intent_signIn);
            }
        });

        //check Remember the user
        String user  = Paper.book().read(Common.USER_KEY);
        String psw = Paper.book().read(Common.PWD_KEY);

        if(user!=null && psw !=null)
        {
            if(!user.isEmpty()&&!psw.isEmpty())
            {
                Login(user,psw);
            }
        }

    }

    // login if is remember
    private void Login(final String phone, final String psw) {

        // Init firebase database
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference table_user = database.getReference("User");
        if (Common.isConnectToTheInternet(getBaseContext()))
        {
            final ProgressDialog mDialog = new ProgressDialog(MainActivity.this);
            mDialog.setMessage("Please waiting !....");
            mDialog.show();

            table_user.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    // check if the user not exist
                    if (dataSnapshot.child(phone).exists()) {
                        mDialog.dismiss();
                        //get user information
                        User user = dataSnapshot.child(phone).getValue(User.class);
                        user.setPhone(phone);
                        if (user.getPassword().equals(psw)){
                            Intent homeIntent = new Intent(MainActivity.this, Home.class);
                            Common.CurrentUser = user;
                            startActivity(homeIntent);
                            finish();
                        } else {
                            Toast.makeText(getApplicationContext(), "Wrong Password!!", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        mDialog.dismiss();
                        Toast.makeText(getApplicationContext(), "User not exist in the database !!", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
        else {
            Toast.makeText(MainActivity.this, "Please check your Connection !!", Toast.LENGTH_SHORT).show();
            return;
        }

    }
}
