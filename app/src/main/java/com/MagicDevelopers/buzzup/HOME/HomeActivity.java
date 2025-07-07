package com.MagicDevelopers.buzzup.HOME;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.MagicDevelopers.buzzup.HOME.Fragments.HomeFragment;
import com.MagicDevelopers.buzzup.HOME.Fragments.NotificationsFragment;
import com.MagicDevelopers.buzzup.HOME.Fragments.ProfileFragment;
import com.MagicDevelopers.buzzup.HOME.Fragments.Upload.UploadActivity;
import com.MagicDevelopers.buzzup.HOME.Fragments.VideoFragment;
import com.MagicDevelopers.buzzup.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Inicializa el BottomNavigationView
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Cargar HomeFragment por defecto al iniciar la actividad
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.nav_host_fragment, new HomeFragment())
                    .commit();
        }

        // Configurar el listener del BottomNavigationView
        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment fragment = null;

                if (item.getItemId() == R.id.nav_home) {
                    fragment = new HomeFragment();
                } else if (item.getItemId() == R.id.nav_videos) {
                    fragment = new VideoFragment();
                } else if (item.getItemId() == R.id.nav_upload) {
                    // Lanzar Activity de Upload
                    startActivity(new Intent(HomeActivity.this, UploadActivity.class));
                    return false;
                } else if (item.getItemId() == R.id.nav_notifications) {
                    fragment = new NotificationsFragment();
                } else if (item.getItemId() == R.id.nav_profile) {
                    fragment = new ProfileFragment();
                }

                if (fragment != null) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.nav_host_fragment, fragment)
                            .commit();
                }
                return true;
            }
        });
    }
}
