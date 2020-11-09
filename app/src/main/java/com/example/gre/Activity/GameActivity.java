package com.example.gre.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
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
import java.util.List;
import java.util.stream.Collectors;

public class GameActivity extends AppCompatActivity implements View.OnClickListener{

    private String game_type;
    private TextView txt_total_question, txt_question, txt_category_name;
    private RadioGroup radioGroup;
    private RadioButton option_a, option_b, radioButton;
    private CardView next_question;

    int index = -1, score = 80, thisQuestion = 0, totalQuestion, correctAnswer;

    FirebaseAuth auth;
    FirebaseDatabase db;
    DatabaseReference users, scores, questions;
    private FirebaseUser currentUser;

    List<Question> tempQuestions = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        Tools.setSystemBarColor(this,  android.R.color.black);
        Tools.setSystemBarLight(this);

        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        users = db.getReference("users");
        scores = db.getReference("scores");
        questions = db.getReference("questions");
        currentUser = auth.getCurrentUser();

        txt_question = (TextView) findViewById(R.id.txt_question);
        txt_total_question = (TextView) findViewById(R.id.txt_total_question);
        txt_category_name = (TextView) findViewById(R.id.txt_category_name);

        if (getIntent().getStringExtra("GAME_TYPE") != null) {
            game_type = getIntent().getStringExtra("GAME_TYPE");
        }

        if (game_type.equals("pmg")) {
            txt_category_name.setText("Play Math Game");
        } else {
            txt_category_name.setText("Play Vocabulary Game");
        }

        radioGroup = (RadioGroup) findViewById(R.id.game_radio_group);

        option_a = (RadioButton) findViewById(R.id.rd_option_a);
        option_b = (RadioButton) findViewById(R.id.rd_option_b);

        next_question = (CardView) findViewById(R.id.next);
        next_question.setOnClickListener(this);

        loadQuestion(game_type);

    }

    private void loadQuestion(String game_type) {
        // clear list if previous questions exist
        if (Common.questionArrayList.size() > 0) {
            Common.questionArrayList.clear();
        }

        questions.child(game_type)
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
                        // limit to only 3 records
                        Common.questionArrayList = tempQuestions.stream().limit(3).collect(Collectors.<Question>toList());

                        totalQuestion = Common.questionArrayList.size();


                        showQuestion(++index);
                        Log.e("HMMM", "loadQuestion: index " + index);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void showQuestion(int index) {
        Log.e("HMMM", "showQuestion: index " + index);
        if (index < totalQuestion) {
            thisQuestion++;

            txt_total_question.setText(String.format("%d / %d", thisQuestion, totalQuestion));

            txt_question.setText(Common.questionArrayList.get(index).getQuestion());

            option_a.setText(Common.questionArrayList.get(index).getOption_a());
            option_b.setText(Common.questionArrayList.get(index).getOption_b());
        } else {
            showDialogCongrat();
        }
    }

    @Override
    public void onClick(View v) {
        if (v == next_question) {
            int selectedID = radioGroup.getCheckedRadioButtonId();

            radioButton = findViewById(selectedID);

            if (index < totalQuestion) {
                if (selectedID == -1) {
                    Toast.makeText(this, "Please select an answer", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (radioButton.getText().toString().equals(Common.questionArrayList.get(index).getAnswer())) {
                    correctAnswer++;
                    showQuestion(++index); // next question
                } else {
                    Toast.makeText(this, "Wrong", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        }
    }

    private void showDialogCongrat() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
        dialog.setContentView(R.layout.congrats_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setCancelable(false);
        TextView textView = (TextView) dialog.findViewById(R.id.tv_version);
        if (game_type.equals("pmg")) {
            textView.setText(getString(R.string.math));
        } else {
            textView.setText(getString(R.string.vocab));
        }
        dialog.findViewById(R.id.bt_action).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
              finish();
            }
        });
        dialog.show();
    }
}