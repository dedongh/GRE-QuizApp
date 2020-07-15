package com.example.gre.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gre.Common.Common;
import com.example.gre.Model.Question;
import com.example.gre.R;
import com.example.gre.Utility.Tools;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class StudyActivity extends AppCompatActivity implements View.OnClickListener{

    private String session_type;
    private RadioGroup radioGroup;
    private RadioButton option_a, option_b, option_c, option_d, option_e;
    private RadioButton radioButton;
    private CardView next_question, previous_question;
    private TextView txt_total_question, txt_question, txt_category_name;

    final static long INTERVAL = 1000; // 1sec
    final static  long TIMEOUT = 30000; // 30s

    CountDownTimer countDownTimer;
    int index = 0, score = 80, thisQuestion = 0, totalQuestion, correctAnswer;

    FirebaseAuth auth;
    FirebaseDatabase db;
    DatabaseReference users, scores, questions;
    private FirebaseUser currentUser;

    List<Question> tempQuestions = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_study);

        Tools.setSystemBarColor(this,  R.color.green_400);
        Tools.setSystemBarLight(this);

        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        users = db.getReference("users");
        scores = db.getReference("scores");
        questions = db.getReference("questions");
        currentUser = auth.getCurrentUser();

        if (getIntent().getStringExtra("SESSION_TYPE") != null) {
            session_type = getIntent().getStringExtra("SESSION_TYPE");
        }

        initializeView();

        loadQuestion(session_type);

    }

    private void loadQuestion(String session_type) {

        // clear list if previous questions exist
        if (Common.questionArrayList.size() > 0) {
            Common.questionArrayList.clear();
        }
        questions.child(session_type)
                .addValueEventListener(new ValueEventListener() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot questionSnapShot : snapshot.getChildren()) {
                            Question question = questionSnapShot.getValue(Question.class);
                            //Common.questionArrayList.add(question);
                            tempQuestions.add(question);
                        }

                        // Random List
                        Collections.shuffle(tempQuestions);
                        Common.questionArrayList = tempQuestions.stream().limit(10).collect(Collectors.<Question>toList());

                        totalQuestion = Common.questionArrayList.size();


                        showQuestion(++index);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void initializeView() {
        radioGroup = (RadioGroup) findViewById(R.id.radio_group);

        option_a = (RadioButton) findViewById(R.id.option_a);
        option_b = (RadioButton) findViewById(R.id.option_b);
        option_c = (RadioButton) findViewById(R.id.option_c);
        option_d = (RadioButton) findViewById(R.id.option_d);
        option_e = (RadioButton) findViewById(R.id.option_e);

        txt_question = (TextView) findViewById(R.id.txt_question);
        txt_total_question = (TextView) findViewById(R.id.txt_total_question);
        txt_category_name = (TextView) findViewById(R.id.txt_category_name);

        next_question = (CardView) findViewById(R.id.next);
        previous_question = (CardView) findViewById(R.id.previous);

        next_question.setOnClickListener(this);
    }

    public void checkSelectedRadioButton(View view) {
        int selectedID = radioGroup.getCheckedRadioButtonId();

        radioButton = findViewById(selectedID);

        Toast.makeText(this, ""+ radioButton.getText().toString(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View v) {
        if (v == next_question) {
            int selectedID = radioGroup.getCheckedRadioButtonId();

            radioButton = findViewById(selectedID);

            if (selectedID == -1) {
                Toast.makeText(this, "Please select an answer", Toast.LENGTH_SHORT).show();
                return;
            }
            // check if there's still questions in the list
           if (index < totalQuestion) {
               if (radioButton.getText().toString().equals(Common.questionArrayList.get(index).getAnswer())) {
                   score += 2;
                   correctAnswer++;

                   Common.scores_by_category.put(Common.questionArrayList.get(index).getCategory_id(), score);
                   showQuestion(++index); // next question
               } else {
                   Common.scores_by_category.put(Common.questionArrayList.get(index).getCategory_id(), score);
                   showQuestion(++index);
               }
            }
        }
    }

    private void showQuestion(int index) {
        if (index < totalQuestion) {
            thisQuestion++;
            txt_total_question.setText(String.format("%d / %d", thisQuestion, totalQuestion));

            txt_question.setText(Common.questionArrayList.get(index).getQuestion());

            option_a.setText(Common.questionArrayList.get(index).getOption_a());
            option_b.setText(Common.questionArrayList.get(index).getOption_b());
            option_c.setText(Common.questionArrayList.get(index).getOption_c());
            option_d.setText(Common.questionArrayList.get(index).getOption_d());
            option_e.setText(Common.questionArrayList.get(index).getOption_e());

            //countDownTimer.start();
        } else {
            // final question do something
            Intent intent = new Intent(StudyActivity.this, SessionCompleteActivity.class);
            Bundle bundle = new Bundle();
            bundle.putInt("SCORE", score);
            bundle.putInt("TOTAL", totalQuestion);
            bundle.putInt("CORRECT", correctAnswer);
            intent.putExtras(bundle);
            startActivity(intent);
            finish();

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //totalQuestion = Common.questionArrayList.size();

        //showQuestion(++index);
    }
}