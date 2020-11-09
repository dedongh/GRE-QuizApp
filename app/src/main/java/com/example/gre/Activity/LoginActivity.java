package com.example.gre.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gre.R;
import com.example.gre.Utility.Tools;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import dmax.dialog.SpotsDialog;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    private TextInputEditText txt_email, txt_password;
    private Button btn_sign_in;

    FirebaseAuth auth;
    FirebaseDatabase db;
    DatabaseReference users;

    String name, phone, email, password, user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Tools.setSystemBarColor(this, android.R.color.white);
        Tools.setSystemBarLight(this);

        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        users = db.getReference("users");

        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser != null) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }

        initializeView();
    }

    private void initializeView() {
        txt_email = (TextInputEditText) findViewById(R.id.login_email);
        txt_password = (TextInputEditText) findViewById(R.id.login_password);
        btn_sign_in = (Button) findViewById(R.id.btn_sign_in);

        btn_sign_in.setOnClickListener(this);

    }

    public void showSignUpPage(View view) {
        startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
    }


    @Override
    public void onClick(View v) {
        if (v == btn_sign_in) {
            showCustomDialog();
        }
    }

    private void sign_in_user() {
        email = txt_email.getText().toString();
        password = txt_password.getText().toString();

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

        final AlertDialog progressDialog = new SpotsDialog(this);
        progressDialog.setTitle("Please Wait");
        progressDialog.show();

        auth.signInWithEmailAndPassword(email,password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                progressDialog.dismiss();
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(LoginActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showCustomDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
        dialog.setContentView(R.layout.disclaimer_layout);
        dialog.setCancelable(false);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;


        TextView txt_disclaimer = ((TextView) dialog.findViewById(R.id.tv_content));
        txt_disclaimer.setMovementMethod(LinkMovementMethod.getInstance());
        txt_disclaimer.setText(getString(R.string.login_disc));

        ((Button) dialog.findViewById(R.id.bt_accept)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                sign_in_user();
            }
        });

        dialog.show();
        dialog.getWindow().setAttributes(lp);
    }
}