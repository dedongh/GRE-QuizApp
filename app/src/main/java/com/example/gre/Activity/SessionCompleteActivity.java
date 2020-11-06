package com.example.gre.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gre.Common.Common;
import com.example.gre.R;
import com.example.gre.Utility.Tools;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Map;

public class SessionCompleteActivity extends AppCompatActivity implements View.OnClickListener {

    FirebaseAuth auth;
    FirebaseDatabase db;
    DatabaseReference users, scores;
    private FirebaseUser currentUser;
    private TextView txt_score, txt_questions_answered, txt_congrats,
            txt_nicely, txt_arithmetic, txt_algebra, txt_functions,
            txt_completion, txt_sentence, txt_linear;
    private RelativeLayout parent_layout, congrats_layout, remarksLayout;
    private Button btn_view_report, btn_close_quiz;
    private String session_type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_complete);

        txt_questions_answered = (TextView) findViewById(R.id.txt_total_question);

        txt_score = (TextView) findViewById(R.id.txt_final_score);

        parent_layout = (RelativeLayout) findViewById(R.id.parentLayout);
        congrats_layout = (RelativeLayout) findViewById(R.id.congrats_layout);
        remarksLayout = (RelativeLayout) findViewById(R.id.remarksLayout);

        btn_view_report = (Button) findViewById(R.id.view_reports);
        btn_close_quiz = (Button) findViewById(R.id.close_quiz);
        btn_view_report.setOnClickListener(this);
        btn_close_quiz.setOnClickListener(this);

        txt_congrats = (TextView) findViewById(R.id.txtReady);
        txt_nicely = (TextView) findViewById(R.id.txt_nicely);
        txt_arithmetic = (TextView) findViewById(R.id.txt_arithmetic);
        txt_algebra = (TextView) findViewById(R.id.txt_algebra);
        txt_functions = (TextView) findViewById(R.id.txt_functions);
        txt_completion = (TextView) findViewById(R.id.txt_completion);
        txt_sentence = (TextView) findViewById(R.id.txt_sentence);
        txt_linear = (TextView) findViewById(R.id.txt_linear);

        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        users = db.getReference("users");
        scores = db.getReference("scores");
        currentUser = auth.getCurrentUser();

        Bundle extra = getIntent().getExtras();

        if (extra != null) {

            int score = extra.getInt("SCORE");
            int correct_questions = extra.getInt("CORRECT");
            int total_questions = extra.getInt("TOTAL");
            session_type = extra.getString("SESSION_TYPE");

            String user_score = Integer.toString(score);

            txt_score.setText(String.format("SCORE : %d", score));

            txt_questions_answered.setText(String.format("PASSED : %d / %d", correct_questions, total_questions));

            // set background colour based on scores
            if (score <= 88) {
                Tools.setSystemBarColor(this, R.color.deep_orange_400);
                Tools.setSystemBarLight(this);
                parent_layout.setBackgroundColor(getResources().getColor(R.color.deep_orange_400));
                txt_nicely.setVisibility(View.VISIBLE);
            }

            if (score > 88 && score <= 94) {
                Tools.setSystemBarColor(this, R.color.blue_400);
                Tools.setSystemBarLight(this);
                parent_layout.setBackgroundColor(getResources().getColor(R.color.blue_400));
            }

            if (score > 94) {
                Tools.setSystemBarColor(this, R.color.green_400);
                Tools.setSystemBarLight(this);
                parent_layout.setBackgroundColor(getResources().getColor(R.color.green_400));
                txt_congrats.setVisibility(View.VISIBLE);
            }

            users.child(currentUser.getUid())
                    .child("score")
                    .setValue(user_score).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(SessionCompleteActivity.this, "Scores Updated", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(SessionCompleteActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

            for (Map.Entry m : Common.scores_by_category.entrySet()) {
                int values = 80 + (int) m.getValue();
                String key = (String) m.getKey();
                Log.e("TAG", "Key : " + m.getKey() + " Value : " + m.getValue());

                scores.child(currentUser.getUid()).child((String) m.getKey())
                        .child("score")
                        .setValue(Integer.toString(values))
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("TAG", "onFailure: " + e.getMessage());
                    }
                });

                if (session_type.equals("quantitative")) {
                    if (((String) m.getKey()).contains("-M9tT4Wru41q2OH2Wh7F") && (int) m.getValue() < 2) {
                        txt_arithmetic.setVisibility(View.VISIBLE);
                    }
                    if (((String) m.getKey()).contains("-M9tTAWCE5-1YWmG1MyD") && (int) m.getValue() < 2) {
                        txt_algebra.setVisibility(View.VISIBLE);
                    }
                    if (((String) m.getKey()).contains("-M9tTHwEQ1hzdfiThaDe") && (int) m.getValue() < 2) {
                        txt_functions.setVisibility(View.VISIBLE);
                    }
                    if (((String) m.getKey()).contains("-M9tSxAmlSI-aLhLhF6W") && (int) m.getValue() < 2) {
                        txt_linear.setVisibility(View.VISIBLE);
                    }
                }else {
                    if (((String) m.getKey()).contains("-M9tSex9FINqJugUjfUC") && (int) m.getValue() < 2) {
                        txt_completion.setVisibility(View.VISIBLE);
                    }
                    if (((String) m.getKey()).contains("-M9tSq1IQKMPYSEHZAl0") && (int) m.getValue() < 2) {
                        txt_sentence.setVisibility(View.VISIBLE);
                    }
                }

            }

        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Common.scores_by_category.clear();
    }

    @Override
    public void onClick(View v) {
        if (v == btn_view_report) {
            congrats_layout.setVisibility(View.GONE);
            btn_view_report.setVisibility(View.GONE);
            btn_close_quiz.setVisibility(View.VISIBLE);
            remarksLayout.setVisibility(View.VISIBLE);
        }
        if (v == btn_close_quiz) {
            finish();
        }
    }
}