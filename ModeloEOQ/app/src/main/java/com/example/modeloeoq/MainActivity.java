package com.example.modeloeoq;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

public class MainActivity<val> extends AppCompatActivity {

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ActionBarDrawerToggle drawerToggle;
    ImageView menu;
    NavController navController;

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(drawerToggle.onOptionsItemSelected(item)){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Defición de variables
        drawerLayout = findViewById(R.id.mainDrawerLayout);
        navigationView = findViewById(R.id.barra_navegacion);
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);
        navController = Navigation.findNavController(this, R.id.navHostFragment);
        menu = findViewById(R.id.menu_image);

        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
        navigationView.setItemIconTintList(null);

        menu.setOnClickListener(new View.OnClickListener(){
           @Override
           public void onClick(View view){
               drawerLayout.openDrawer(GravityCompat.START);
           }
        });

        NavigationUI.setupWithNavController(navigationView, navController);

    }

}