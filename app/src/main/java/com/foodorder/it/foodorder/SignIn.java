package com.foodorder.it.foodorder;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.foodorder.it.foodorder.Common.Common;
import com.foodorder.it.foodorder.Model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.rey.material.widget.CheckBox;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.paperdb.Paper;

public class SignIn extends AppCompatActivity {

    @BindView(R.id.edphone)
    MaterialEditText phone;
    @BindView(R.id.edpassword)
    MaterialEditText password;
    @BindView(R.id.btnsignIn)
    Button signIn;
    @BindView(R.id.ckbRemember)
    CheckBox ckbRemember;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        ButterKnife.bind(this);

        // Init firebase database
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference table_user = database.getReference("User");

        //init paper
        Paper.init(this);

        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Common.isConnectToTheInternet(getBaseContext()))
                {
                    //save user and password
                    if(ckbRemember.isChecked())
                    {
                        Paper.book().write(Common.USER_KEY,phone.getText().toString());
                        Paper.book().write(Common.PWD_KEY,password.getText().toString());
                    }

                    final ProgressDialog mDialog = new ProgressDialog(SignIn.this);
                mDialog.setMessage("Please waiting !....");
                mDialog.show();

                table_user.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        // check if the user not exist
                        if (dataSnapshot.child(phone.getText().toString()).exists()) {
                            mDialog.dismiss();
                            //get user information
                            User user = dataSnapshot.child(phone.getText().toString()).getValue(User.class);
                            user.setPhone(phone.getText().toString());
                            if (user.getPassword().equals(password.getText().toString())) {
                                Intent homeIntent = new Intent(SignIn.this, Home.class);
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
                    Toast.makeText(SignIn.this, "Please check your Connection !!", Toast.LENGTH_SHORT).show();
                    return;
                }
                }
        });
    }
}
