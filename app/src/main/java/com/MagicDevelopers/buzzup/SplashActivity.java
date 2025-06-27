package com.MagicDevelopers.buzzup;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.MagicDevelopers.buzzup.LOGIN.LoginActivity;
import com.MagicDevelopers.buzzup.HOME.HomeActivity;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;

public class SplashActivity extends Activity {

    private static final int SPLASH_DELAY = 4000; // 4 segundos

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Establecer el layout
        setContentView(R.layout.activity_splash);

        // Ocultar la barra de estado y hacerla transparente
        Window window = getWindow();
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        window.setStatusBarColor(Color.TRANSPARENT);

        // Detectar modo oscuro o claro
        ImageView logoImage = findViewById(R.id.logoImage);
        int nightModeFlags = getApplicationContext().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
            logoImage.setImageResource(R.drawable.logo_dark);
        } else {
            logoImage.setImageResource(R.drawable.logo_light);
        }

        // Cargar GIF con Glide
        ImageView loadingGif = findViewById(R.id.loadingGif);
        Glide.with(this).asGif().load(R.drawable.loading).into(loadingGif);

        // Esperar SPLASH_DELAY ms y luego decidir a qué pantalla ir
        new Handler().postDelayed(() -> {
            SharedPreferences prefs = getSharedPreferences("USER_PREFS", MODE_PRIVATE);
            boolean mantenerSesion = prefs.getBoolean("mantenerSesion", false);
            FirebaseAuth mAuth = FirebaseAuth.getInstance();

            if (mantenerSesion && mAuth.getCurrentUser() != null) {
                // Usuario autenticado y quiere mantener sesión: ir a HomeActivity
                startActivity(new Intent(SplashActivity.this, HomeActivity.class));
            } else {
                // No hay sesión o no quiere mantener: ir a LoginActivity
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            }
            finish();
        }, SPLASH_DELAY);
    }
}
