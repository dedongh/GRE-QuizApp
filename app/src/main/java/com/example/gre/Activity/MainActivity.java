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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.gre.Adapter.CategoryAdapter;
import com.example.gre.Model.Category;
import com.example.gre.R;
import com.example.gre.Utility.GridSpacingItemDecoration;
import com.example.gre.Utility.Tools;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

public class MainActivity extends AppCompatActivity {

    private ActionBar actionBar;
    FirebaseAuth auth;
    FirebaseDatabase db;
    DatabaseReference users;
    DatabaseReference scores;
    private FirebaseUser currentUser;
    private TextView txt_user_name;

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

        SharedPreferences userPref = getSharedPreferences("USER", MODE_PRIVATE);

        txt_user_name.setText(userPref.getString("user_name",null));
        recyclerView = (RecyclerView) findViewById(R.id.cat_recycleriew);

        recyclerView.setLayoutManager(new GridLayoutManager(this, 4, LinearLayoutManager.VERTICAL, false));

      /*  int spanCount = 4; // 3 columns
        int spacing = 15; // 50px
        boolean includeEdge = true;
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(spanCount, spacing, includeEdge));
*/
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
                holder.user_score.setText(model.getScore()+"%");
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

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        adapter.stopListening();
    }
}