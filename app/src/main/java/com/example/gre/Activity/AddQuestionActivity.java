package com.example.gre.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.gre.Model.Category;
import com.example.gre.Model.Question;
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

import java.util.ArrayList;
import java.util.Collection;

import dmax.dialog.SpotsDialog;

public class AddQuestionActivity extends AppCompatActivity {

    FirebaseAuth auth;
    FirebaseDatabase db;
    DatabaseReference categoriesRef, questionsRef;
    ArrayList<String> categoryArray = new ArrayList<>();
    ArrayList<String> categoryNameArray = new ArrayList<>();
    private Spinner spn_category;
    private Button btn_save_question;
    private String selected_category, category_id, category_name;
    ArrayAdapter<String> arrayCategoryAdapter;

    private EditText txt_question, txt_optionA,txt_optionB,txt_optionC,
            txt_optionD,txt_optionE,txt_optionF, txt_answer, txt_question_type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_question);

        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        categoriesRef = db.getReference("categories");
        questionsRef = db.getReference("questions");

        initToolbar();

        initializeView();

        load_category();
    }

    private void load_category() {
        categoriesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot categorySnapshot : dataSnapshot.getChildren()) {
                    String cat_id = categorySnapshot.child("cat_id").getValue(String.class);
                    String cat_name = categorySnapshot.child("name").getValue(String.class);
                    categoryArray.add(cat_id);
                    categoryNameArray.add(cat_name);
                }

                arrayCategoryAdapter = new ArrayAdapter<String>(
                        getApplicationContext(), R.layout.category_spinner_layout, R.id.network_spn, categoryNameArray);

                spn_category.setAdapter(arrayCategoryAdapter);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        spn_category.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                category_name = categoryNameArray.get(position);
                category_id = categoryArray.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void initializeView() {
        spn_category = (Spinner) findViewById(R.id.spn_category_name);
        btn_save_question = (Button) findViewById(R.id.btn_save_question);

        txt_question = (EditText)findViewById(R.id.txt_question);
        txt_optionA = (EditText)findViewById(R.id.txt_optionA);
        txt_optionB = (EditText)findViewById(R.id.txt_optionB);
        txt_optionC = (EditText)findViewById(R.id.txt_optionC);
        txt_optionD = (EditText)findViewById(R.id.txt_optionD);
        txt_optionE = (EditText)findViewById(R.id.txt_optionE);
        txt_optionF = (EditText)findViewById(R.id.txt_optionF);
        txt_answer = (EditText)findViewById(R.id.txt_answer);
        txt_question_type = (EditText)findViewById(R.id.question_type);

        btn_save_question.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog progressDialog = new SpotsDialog(AddQuestionActivity.this);
                progressDialog.setTitle("Please Wait");
                progressDialog.show();

                Question question = new Question();
                question.setQuestion(txt_question.getText().toString());
                question.setOption_a(txt_optionA.getText().toString());
                question.setOption_b(txt_optionB.getText().toString());
              /*  question.setOption_c(txt_optionC.getText().toString());
                question.setOption_d(txt_optionD.getText().toString());
                question.setOption_e(txt_optionE.getText().toString());
                question.setOption_f(txt_optionF.getText().toString());*/
                question.setAnswer(txt_answer.getText().toString());
                //question.setCategory_id(category_id);

                questionsRef.child(txt_question_type.getText().toString().toLowerCase())
                        .push()
                        .setValue(question)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                progressDialog.dismiss();
                                Toast.makeText(AddQuestionActivity.this, "Saved", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(AddQuestionActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Add New Question");
        Tools.setSystemBarColor(this, R.color.colorPrimaryDark1);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}