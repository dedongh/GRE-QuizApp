package com.example.gre.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gre.Common.Common;
import com.example.gre.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Map;

public class SessionCompleteActivity extends AppCompatActivity {

    FirebaseAuth auth;
    FirebaseDatabase db;
    DatabaseReference users;
    DatabaseReference scores;
    private FirebaseUser currentUser;
    private TextView txt_score, txt_questions_answered;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_complete);

        txt_questions_answered = (TextView) findViewById(R.id.txt_total_question);

        txt_score = (TextView) findViewById(R.id.txt_final_score);

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

            String user_score = Integer.toString(score);

            txt_score.setText(String.format("SCORE : %d", score));

            txt_questions_answered.setText(String.format("PASSED : %d / %d", correct_questions, total_questions ));

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
                    Toast.makeText(SessionCompleteActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        for(Map.Entry m : Common.scores_by_category.entrySet()){
            Log.e("TAG", "Key : "+ m.getKey()+" Value : "+m.getValue());
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Common.scores_by_category.clear();
    }



    /*for(Map.Entry m : map.entrySet()){
        System.out.println(m.getKey()+" "+m.getValue());
    }*/
}