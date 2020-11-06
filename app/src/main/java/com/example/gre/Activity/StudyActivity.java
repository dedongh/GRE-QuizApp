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
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gre.Common.Common;
import com.example.gre.Model.Category;
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
    private RadioButton option_a, option_b, option_c, option_d, option_e, radioButton;
    private CardView next_question, previous_question;
    private TextView txt_total_question, txt_question, txt_category_name;
    private CheckBox chk_option_a, chk_option_b, chk_option_c, chk_option_d, chk_option_e, chk_option_f;

    private CardView check_box_options, radio_btn_options;

    final static long INTERVAL = 1000; // 1sec
    final static  long TIMEOUT = 30000; // 30s

    CountDownTimer countDownTimer;
    int index = 0, score = 80, thisQuestion = 0, totalQuestion, correctAnswer;

    FirebaseAuth auth;
    FirebaseDatabase db;
    DatabaseReference users, scores, questions, categories;
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
        categories = db.getReference("categories");
        currentUser = auth.getCurrentUser();

        check_box_options = (CardView) findViewById(R.id.chk_options);
        radio_btn_options = (CardView) findViewById(R.id.radio_options);


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

        //checkboxes
        chk_option_a = (CheckBox) findViewById(R.id.chk_option_a);
        chk_option_b = (CheckBox) findViewById(R.id.chk_option_b);
        chk_option_c = (CheckBox) findViewById(R.id.chk_option_c);
        chk_option_d = (CheckBox) findViewById(R.id.chk_option_d);
        chk_option_e = (CheckBox) findViewById(R.id.chk_option_e);
        chk_option_f = (CheckBox) findViewById(R.id.chk_option_f);

        txt_question = (TextView) findViewById(R.id.txt_question);
        txt_total_question = (TextView) findViewById(R.id.txt_total_question);
        txt_category_name = (TextView) findViewById(R.id.txt_category_name);

        next_question = (CardView) findViewById(R.id.next);
        previous_question = (CardView) findViewById(R.id.previous);

        next_question.setOnClickListener(this);
        previous_question.setOnClickListener(this);

    }


    @Override
    public void onClick(View v) {
        if (v == next_question) {
            int selectedID = radioGroup.getCheckedRadioButtonId();

            radioButton = findViewById(selectedID);



            // check if there's still questions in the list
           if (index < totalQuestion) {

               if (Common.questionArrayList.get(index).getAnswer_2() == null) {
                   if (selectedID == -1) {
                       Toast.makeText(this, "Please select an answer", Toast.LENGTH_SHORT).show();
                       return;
                   }

                   if (radioButton.getText().toString().equals(Common.questionArrayList.get(index).getAnswer())) {
                       score += 2;
                       correctAnswer++;

                       if (Common.scores_by_category.get(Common.questionArrayList.get(index).getCategory_id()) == null) {
                           Common.scores_by_category.put(Common.questionArrayList.get(index).getCategory_id(), 0);
                       }
                       Common.scores_by_category.put(Common.questionArrayList.get(index).getCategory_id(), Common.scores_by_category.get(Common.questionArrayList.get(index).getCategory_id()) + 2);
                       showQuestion(++index); // next question
                   } else {
                       if (Common.scores_by_category.get(Common.questionArrayList.get(index).getCategory_id()) == null) {
                           Common.scores_by_category.put(Common.questionArrayList.get(index).getCategory_id(), 0);
                       }
                       Common.scores_by_category.put(Common.questionArrayList.get(index).getCategory_id(), Common.scores_by_category.get(Common.questionArrayList.get(index).getCategory_id()) + 0);

                       showQuestion(++index);
                   }
               }

               else {

                   ArrayList<String> checkedAnswers = new ArrayList<>();
                   if (chk_option_a.isChecked()) {
                       checkedAnswers.add(chk_option_a.getText().toString());
                   }
                   if (chk_option_b.isChecked()) {
                       checkedAnswers.add(chk_option_b.getText().toString());
                   }
                   if (chk_option_c.isChecked()) {
                       checkedAnswers.add(chk_option_c.getText().toString());
                   }
                   if (chk_option_d.isChecked()) {
                       checkedAnswers.add(chk_option_d.getText().toString());
                   }
                   if (chk_option_e.isChecked()) {
                       checkedAnswers.add(chk_option_e.getText().toString());
                   }
                   if (chk_option_f.isChecked()) {
                       checkedAnswers.add(chk_option_f.getText().toString());
                   }


                   if (Common.scores_by_category.get(Common.questionArrayList.get(index).getCategory_id()) == null) {
                       Common.scores_by_category.put(Common.questionArrayList.get(index).getCategory_id(), 0);
                   }
                   if (checkAns(checkedAnswers, Common.questionArrayList.get(index).getAnswer(), Common.questionArrayList.get(index).getAnswer_2())) {
                       score += 2;
                       correctAnswer++;
                       Common.scores_by_category.put(Common.questionArrayList.get(index).getCategory_id(), Common.scores_by_category.get(Common.questionArrayList.get(index).getCategory_id()) + 2);

                       showQuestion(++index);
                   } else {
                       Common.scores_by_category.put(Common.questionArrayList.get(index).getCategory_id(), Common.scores_by_category.get(Common.questionArrayList.get(index).getCategory_id()) + 0);
                       showQuestion(++index);
                   }
               }


            }
        }

        if (v == previous_question) {
               //showQuestion(--index);

        }
    }

    private boolean checkAns(ArrayList<String> checked, String ans1, String ans2){
        ArrayList<String> correctAns = new ArrayList<>();
        for(String ans : checked){
            if(ans.equals(ans1) || ans.equals(ans2)){
                correctAns.add(ans);
            }
        }

        if(correctAns.size() == 2)
             return true;
        else
            return false;
    }

    private void showQuestion(int index) {
        if (index < totalQuestion) {
            thisQuestion++;
            txt_total_question.setText(String.format("%d / %d", thisQuestion, totalQuestion));

            txt_question.setText(Common.questionArrayList.get(index).getQuestion());

            // radio buttons
            option_a.setText(Common.questionArrayList.get(index).getOption_a());
            option_b.setText(Common.questionArrayList.get(index).getOption_b());
            option_c.setText(Common.questionArrayList.get(index).getOption_c());
            option_d.setText(Common.questionArrayList.get(index).getOption_d());
            option_e.setText(Common.questionArrayList.get(index).getOption_e());

            if (Common.questionArrayList.get(index).getAnswer_2() != null) {
                check_box_options.setVisibility(View.VISIBLE);
                radio_btn_options.setVisibility(View.GONE);
            } else {
                radio_btn_options.setVisibility(View.VISIBLE);
                check_box_options.setVisibility(View.GONE);
            }

            // checkboxes
            chk_option_a.setText(Common.questionArrayList.get(index).getOption_a());
            chk_option_b.setText(Common.questionArrayList.get(index).getOption_b());
            chk_option_c.setText(Common.questionArrayList.get(index).getOption_c());
            chk_option_d.setText(Common.questionArrayList.get(index).getOption_d());
            chk_option_e.setText(Common.questionArrayList.get(index).getOption_e());
            chk_option_f.setText(Common.questionArrayList.get(index).getOption_f());


            final String cat_id = Common.questionArrayList.get(index).getCategory_id();

            categories.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot categorySnapShot : snapshot.getChildren()){
                        Category category = categorySnapShot.getValue(Category.class);
                        if (category.getCat_id().equals(cat_id)) {
                            txt_category_name.setText(category.getName());
                        }

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(StudyActivity.this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        } else {
            // final question do something
            Intent intent = new Intent(StudyActivity.this, SessionCompleteActivity.class);
            Bundle bundle = new Bundle();
            bundle.putInt("SCORE", score);
            bundle.putInt("TOTAL", totalQuestion);
            bundle.putInt("CORRECT", correctAnswer);
            bundle.putString("SESSION_TYPE", session_type);
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