package com.MagicDevelopers.buzzup;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.MagicDevelopers.buzzup.LOGIN.LoginActivity;
import com.bumptech.glide.Glide;

public class SplashActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Establecer el layout
        setContentView(R.layout.activity_splash);

        // Ocultar la barra de estado y hacerla transparente
        Window window = getWindow();
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        window.setStatusBarColor(Color.TRANSPARENT);

        // Detectar el modo oscuro o claro correctamente
        ImageView logoImage = findViewById(R.id.logoImage);
        int nightModeFlags = getApplicationContext().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;

        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
            logoImage.setImageResource(R.drawable.logo_dark);
        } else {
            logoImage.setImageResource(R.drawable.logo_light);
        }

        // Cargar GIF en el ImageView usando Glide
        ImageView loadingGif = findViewById(R.id.loadingGif);
        Glide.with(this).asGif().load(R.drawable.loading).into(loadingGif);

        // Pantalla de carga por 4 segundos y redirigir a LoginActivity
        new Handler().postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            finish();
        }, 4000);
    }
}
