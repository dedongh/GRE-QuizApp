package com.example.gre.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.gre.Adapter.CategoryAdapter;
import com.example.gre.Model.Category;
import com.example.gre.Model.User;
import com.example.gre.R;
import com.example.gre.Utility.GridSpacingItemDecoration;
import com.example.gre.Utility.Tools;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.internal.NavigationMenu;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import io.github.yavski.fabspeeddial.FabSpeedDial;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    private ActionBar actionBar;
    FirebaseAuth auth;
    FirebaseDatabase db;
    DatabaseReference users;
    DatabaseReference scores;
    private FirebaseUser currentUser;
    private TextView txt_user_name, txt_user_score;
    private FabSpeedDial study_now;

    private ImageView highest_score, fastest_time, most_correct, nice_move, all_correct, wel_done;

    private RecyclerView recyclerView;

    FirebaseRecyclerOptions<Category> options;
    FirebaseRecyclerAdapter<Category, CategoryAdapter> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        users = db.getReference("users");
        scores = db.getReference("scores");
        currentUser = auth.getCurrentUser();

        initToolbar();

        initializeView();
    }

    private void initializeView() {
        txt_user_name = (TextView) findViewById(R.id.user_name);
        txt_user_score = (TextView) findViewById(R.id.user_score);

        highest_score = (ImageView)findViewById(R.id.highest_score);
        fastest_time = (ImageView)findViewById(R.id.fastest_time);
        most_correct = (ImageView)findViewById(R.id.most_correct);
        nice_move = (ImageView)findViewById(R.id.nice_move);
        all_correct = (ImageView)findViewById(R.id.all_correct);
        wel_done = (ImageView)findViewById(R.id.wel_done);

        SharedPreferences userPref = getSharedPreferences("USER", MODE_PRIVATE);

        //txt_user_name.setText(userPref.getString("user_name", null));

       loadAchievements();


        recyclerView = (RecyclerView) findViewById(R.id.cat_recycleriew);

        study_now = (FabSpeedDial) findViewById(R.id.study_activity);
        study_now.setMenuListener(new FabSpeedDial.MenuListener() {
            @Override
            public boolean onPrepareMenu(NavigationMenu navigationMenu) {
                return true;
            }

            @Override
            public boolean onMenuItemSelected(MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.action_start_quantitative) {
                    Intent intent = new Intent(MainActivity.this, StudyActivity.class);
                    intent.putExtra("SESSION_TYPE", "quantitative");
                    startActivity(intent);
                }
                if (menuItem.getItemId() == R.id.action_start_verbal) {
                    Intent intent = new Intent(MainActivity.this, StudyActivity.class);
                    intent.putExtra("SESSION_TYPE", "verbal");
                    startActivity(intent);
                }
                return true;
            }

            @Override
            public void onMenuClosed() {

            }
        });

        recyclerView.setLayoutManager(new GridLayoutManager(this, 4, LinearLayoutManager.VERTICAL, false));

        displayUserScores();
    }

    private void displayUserScores() {
        Query query = scores.child(currentUser.getUid());

        options = new FirebaseRecyclerOptions.Builder<Category>()
                .setQuery(query, Category.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<Category, CategoryAdapter>(options) {
            @Override
            protected void onBindViewHolder(@NonNull CategoryAdapter holder, int position, @NonNull Category model) {
                holder.category_name.setText(model.getName());
                holder.user_score.setText(model.getScore() + "%");
                holder.current_progress.setProgress(Integer.parseInt(String.valueOf(model.getScore())));
            }

            @NonNull
            @Override
            public CategoryAdapter onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(getBaseContext()).inflate(R.layout.scores_layout, parent, false);

                return new CategoryAdapter(view);

            }
        };
        adapter.startListening();
        recyclerView.setAdapter(adapter);
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        /*toolbar.setNavigationIcon(R.drawable.ic_menu);
        toolbar.getNavigationIcon().setColorFilter(getResources()
                .getColor(R.color.grey_60), PorterDuff.Mode.SRC_ATOP);*/

        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setTitle("GRE");
        Tools.setSystemBarColor(this, R.color.grey_5);
        Tools.setSystemBarLight(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            auth.signOut();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
        if (item.getItemId() == R.id.action_cat) {
            startActivity(new Intent(MainActivity.this, AddCategoryActivity.class));
        }
        if (item.getItemId() == R.id.action_add_question) {
            startActivity(new Intent(MainActivity.this, AddQuestionActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        adapter.stopListening();
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAchievements();
    }

    private void loadAchievements() {
        // display user scores

        users.child(currentUser.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                        txt_user_name.setText(user.getName());
                        txt_user_score.setText(user.getScore());

                        int score = Integer.parseInt(user.getScore());

                        if (score >= 90) {
                            highest_score.setVisibility(View.VISIBLE);
                            all_correct.setVisibility(View.VISIBLE);
                            most_correct.setVisibility(View.VISIBLE);
                            fastest_time.setVisibility(View.VISIBLE);
                            wel_done.setVisibility(View.VISIBLE);
                        }

                        if (score >= 86) {
                            most_correct.setVisibility(View.VISIBLE);
                            fastest_time.setVisibility(View.VISIBLE);
                        }
                        if (score >= 82) {
                            nice_move.setVisibility(View.VISIBLE);
                            fastest_time.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}