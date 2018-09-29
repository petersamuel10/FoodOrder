package com.foodorder.it.foodorder;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.foodorder.it.foodorder.Model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SignUp extends AppCompatActivity {

    @BindView(R.id.edphone)
    MaterialEditText phone;
    @BindView(R.id.edpassword)
    MaterialEditText password;
    @BindView(R.id.edName)
    MaterialEditText name;
    @BindView(R.id.btnSignUp)
    Button signUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        ButterKnife.bind(this);
        // Init firebase database
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference table_user = database.getReference("User");
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final ProgressDialog mDial = new ProgressDialog(SignUp.this);
                mDial.setMessage("Please waiting ....");
                mDial.show();
                table_user.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        mDial.dismiss();
                        //chif if the phone is already exist
                        if(dataSnapshot.child(phone.getText().toString()).exists()) {
                            Toast.makeText(getApplicationContext(),"Tis phone number is already exist in the database !!",Toast.LENGTH_LONG).show();
                        }
                        else
                        {
                            User user = new User(name.getText().toString(),password.getText().toString());
                            table_user.child(phone.getText().toString()).setValue(user);
                            Toast.makeText(getApplicationContext(),"Sign Up Successfully !!",Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });
    }

}
