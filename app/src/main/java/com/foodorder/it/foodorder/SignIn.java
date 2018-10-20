package com.foodorder.it.foodorder;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.foodorder.it.foodorder.Common.Common;
import com.foodorder.it.foodorder.Model.User;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.rey.material.widget.CheckBox;

import java.util.zip.Inflater;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.paperdb.Paper;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class SignIn extends AppCompatActivity {

    @BindView(R.id.edphone)
    MaterialEditText phone;
    @BindView(R.id.edpassword)
    MaterialEditText password;
    @BindView(R.id.btnsignIn)
    Button signIn;
    @BindView(R.id.ckbRemember)
    CheckBox ckbRemember;
    @BindView(R.id.forgetPassword)
    TextView forgetPassword;
    String phoneSMS,passwordSMS,userSMS;

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

                table_user.addListenerForSingleValueEvent(new ValueEventListener() {
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

                                table_user.removeEventListener(this);
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
     forgetPassword.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View view) {
             android.support.v7.app.AlertDialog.Builder alert = new android.support.v7.app.AlertDialog.Builder(SignIn.this);
             View view1 =  LayoutInflater.from(SignIn.this).inflate(R.layout.forget_password_layout,null);
             final MaterialEditText phone_number ;
             phone_number = view1.findViewById(R.id.edPhneNumber);

             alert.setTitle("Forget Password")
                     .setMessage("Enter your phone number to sed SMS")
                     .setView(view1)
                     .setPositiveButton("Send", new DialogInterface.OnClickListener() {
                         @Override
                         public void onClick(DialogInterface dialogInterface, int i) {
                             DatabaseReference ref = database.getReference("User");
                             ref.orderByKey().equalTo(phone_number.getText().toString()).addValueEventListener(new ValueEventListener() {
                                 User user;
                                 @Override
                                 public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                     for (DataSnapshot dataSnapshot1:dataSnapshot.getChildren()) {
                                         user = dataSnapshot1.getValue(User.class);
                                         passwordSMS = user.getPassword();
                                         userSMS = user.getName();
                                         phoneSMS = dataSnapshot1.getKey();
                                     }
                                       sendSMS();
                                 }

                                 @Override
                                 public void onCancelled(@NonNull DatabaseError databaseError) {

                                 }
                             });
                         }
                     }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                 @Override
                 public void onClick(DialogInterface dialogInterface, int i) {
                 }
             });

             alert.show();

         }
     });
    }

    private void sendSMS() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.SEND_SMS)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.SEND_SMS}, 0);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==0)
        {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(phoneSMS,null,"Food Order Dear "+userSMS+" your password is"+passwordSMS,null,null);
                Toast.makeText(this, "Password send in SMS", Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(this, "Faild to send SMS , please try again", Toast.LENGTH_SHORT).show();
                return;
            }
        }

    }
}


/*

 */
