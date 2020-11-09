package com.example.gre.Activity;

import androidx.appcompat.app.AppCompatActivity;
import com.example.gre.R;
import com.example.gre.Utility.Tools;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class GameOptions extends AppCompatActivity implements View.OnClickListener{

    private TextView txt_username;
    private String username, activity_type;
    private LinearLayout game_layout, quiz_layout, start_quantitative_layout, start_verbal_layout, play_math_game_layout, play_vocabulary_game_layout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Tools.setSystemBarColor(this, R.color.grey_5);
        Tools.setSystemBarLight(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_options);

        SharedPreferences userPref = getSharedPreferences("USER", MODE_PRIVATE);
        username = userPref.getString("user_name", null);
        txt_username = (TextView) findViewById(R.id.game_options_username);
        txt_username.setText(username);

        game_layout = (LinearLayout)findViewById(R.id.game_layout);
        quiz_layout = (LinearLayout)findViewById(R.id.quiz_layout);
        start_quantitative_layout = (LinearLayout)findViewById(R.id.quantitative);
        start_verbal_layout = (LinearLayout)findViewById(R.id.verbal);
        play_math_game_layout = (LinearLayout)findViewById(R.id.math_gameOption);
        play_vocabulary_game_layout = (LinearLayout)findViewById(R.id.vocabulary_gameOption);

        if (getIntent().getStringExtra("ACTIVITY") != null) {
            activity_type = getIntent().getStringExtra("ACTIVITY");
        }

        if (activity_type.equals("quiz")) {
            quiz_layout.setVisibility(View.VISIBLE);
        } else {
            game_layout.setVisibility(View.VISIBLE);
        }

        start_quantitative_layout.setOnClickListener(this);
        start_verbal_layout.setOnClickListener(this);
        play_math_game_layout.setOnClickListener(this);
        play_vocabulary_game_layout.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        if (v == start_quantitative_layout) {
            Intent intent = new Intent(GameOptions.this, StudyActivity.class);
            intent.putExtra("SESSION_TYPE", "quantitative");
            startActivity(intent);
        }
        if (v == start_verbal_layout) {
            Intent intent = new Intent(GameOptions.this, StudyActivity.class);
            intent.putExtra("SESSION_TYPE", "verbal");
            startActivity(intent);
        }
        if (v == play_math_game_layout) {
            Intent intent = new Intent(GameOptions.this, GameActivity.class);
            intent.putExtra("GAME_TYPE", "pmg");
            startActivity(intent);
        }
        if (v == play_vocabulary_game_layout) {
            Intent intent = new Intent(GameOptions.this, GameActivity.class);
            intent.putExtra("GAME_TYPE", "pvg");
            startActivity(intent);
        }
    }
}