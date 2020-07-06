package com.example.gre.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.gre.Model.Category;
import com.example.gre.Model.User;
import com.example.gre.R;
import com.example.gre.Utility.Tools;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import dmax.dialog.SpotsDialog;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener{
    private TextInputEditText txt_email, txt_password, txt_name, txt_phone;
    private Button btn_sign_up;

    FirebaseAuth auth;
    FirebaseDatabase db;
    DatabaseReference users, scores, categories;
    FirebaseUser currentUser;

    String name, phone, email, password, user_id;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        Tools.setSystemBarColor(this, android.R.color.white);
        Tools.setSystemBarLight(this);

        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        users = db.getReference("users");
        scores = db.getReference("scores");
        categories = db.getReference("categories");

        currentUser = auth.getCurrentUser();

        initializeView();
    }

    private void initializeView() {
        txt_email = (TextInputEditText) findViewById(R.id.sign_up_email);
        txt_name = (TextInputEditText) findViewById(R.id.sign_up_name);
        txt_phone = (TextInputEditText) findViewById(R.id.sign_up_phone);
        txt_password = (TextInputEditText) findViewById(R.id.sign_up_password);

        btn_sign_up = (Button) findViewById(R.id.btn_sign_up);
        btn_sign_up.setOnClickListener(this);
    }

    public void showSignInPage(View view) {
        startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
    }

    @Override
    public void onClick(View v) {
        if (v == btn_sign_up) {
            sign_up_user();
        }
    }

    private void sign_up_user() {
        email = txt_email.getText().toString();
        password = txt_password.getText().toString();
        name = txt_name.getText().toString();
        phone = txt_phone.getText().toString();

        if (TextUtils.isEmpty(email)) {
            txt_email.setError("Please enter email");
            txt_email.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            txt_password.setError("Please enter password");
            txt_password.requestFocus();
            return;
        }
        if (password.length() < 6) {
            txt_password.setError("Password should be 6 or more characters");
            txt_password.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(name)) {
            txt_name.setError("Please enter your name");
            txt_name.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(phone)) {
            txt_phone.setError("Please enter phone number");
            txt_phone.requestFocus();
            return;
        }

        final AlertDialog progressDialog = new SpotsDialog(this);
        progressDialog.setTitle("Please Wait");
        progressDialog.show();

        auth.createUserWithEmailAndPassword(email, password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                User user = new User();
                user.setEmail(email);
                user.setName(name);
                user.setPhone(phone);
                user.setPassword(password);
                user.setScore("0");

                users.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .setValue(user)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                                SharedPreferences userPref = getSharedPreferences("USER", MODE_PRIVATE);

                                SharedPreferences.Editor editor  = userPref.edit();
                                editor.putString("user_name", name);
                                editor.commit();

                                categories
                                        .addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if (dataSnapshot.exists()) {
                                                    for (DataSnapshot cat_items : dataSnapshot.getChildren()){
                                                        String key = cat_items.child("cat_id").getValue(String.class);
                                                        Category category = new Category();
                                                        category.setName(cat_items.child("name").getValue(String.class));
                                                        category.setScore("0");

                                                        scores.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                                .child(key)
                                                                .setValue(category);

                                                    }
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });

                                progressDialog.dismiss();

                                Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(SignUpActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(SignUpActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}