package com.example.gre.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.gre.Model.Category;
import com.example.gre.R;
import com.example.gre.Utility.Tools;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Iterator;

import dmax.dialog.SpotsDialog;

public class AddCategoryActivity extends AppCompatActivity {

    private ActionBar actionBar;
    FirebaseAuth auth;
    FirebaseDatabase db;
    DatabaseReference categoriesRef, scores;

    private EditText txt_category_name;
    private Button btn_add_category, btn_save_scores;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_category);

        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        categoriesRef = db.getReference("categories");
        scores = db.getReference("scores");

        initToolbar();

        txt_category_name = (EditText) findViewById(R.id.txt_add_cat);
        btn_add_category = (Button) findViewById(R.id.btn_add_cat);
        btn_save_scores = (Button) findViewById(R.id.btn_save_scores);

        btn_add_category.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = txt_category_name.getText().toString();

                if (TextUtils.isEmpty(name)) {
                    txt_category_name.setError("Enter category name");
                    txt_category_name.requestFocus();
                    return;
                }


                String push_key =  categoriesRef.push().getKey();
                Category category = new Category();
                category.setName(name);
                category.setCat_id(push_key);


                final AlertDialog progressDialog = new SpotsDialog(AddCategoryActivity.this);
                progressDialog.setTitle("Please Wait");
                progressDialog.show();

                categoriesRef.child(push_key).setValue(category)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                progressDialog.dismiss();
                                Toast.makeText(AddCategoryActivity.this, "Saved", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(AddCategoryActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                });

            }
        });

        btn_save_scores.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                categoriesRef
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
            }
        });

    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        actionBar = getSupportActionBar();
        actionBar.setTitle("Add Category");
        Tools.setSystemBarColor(this, R.color.grey_5);
        Tools.setSystemBarLight(this);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}