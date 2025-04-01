package com.MagicDevelopers.buzzup.LOGIN.Registro;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.MagicDevelopers.buzzup.HOME.HomeActivity;
import com.MagicDevelopers.buzzup.R;

public class Registro5Activity extends AppCompatActivity {

    private Button btnGuardarAhora, btnAhoraNo;
    private ImageView logoImage;
    private TextView titleText, descText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro5);

        // Referenciar vistas
        logoImage = findViewById(R.id.logoImage);
        titleText = findViewById(R.id.titleText);
        descText = findViewById(R.id.descText);
        btnGuardarAhora = findViewById(R.id.btnGuardarAhora);
        btnAhoraNo = findViewById(R.id.btnAhoraNo);

        // Ajustar colores y logo según el modo oscuro/claro
        setLogoAndTextColors();

        // Botón "Guardar Ahora"
        btnGuardarAhora.setOnClickListener(v -> {
            guardarPreferencia(true);
            irAMainActivity();
        });

        // Botón "Ahora No"
        btnAhoraNo.setOnClickListener(v -> {
            guardarPreferencia(false);
            irAMainActivity();
        });
    }

    private void guardarPreferencia(boolean mantenerSesion) {
        SharedPreferences preferences = getSharedPreferences("USER_PREFS", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("mantenerSesion", mantenerSesion);
        editor.apply();
    }

    private void irAMainActivity() {
        Intent intent = new Intent(Registro5Activity.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * Ajusta el logo y los colores del texto según el modo oscuro o claro.
     */
    private void setLogoAndTextColors() {
        boolean isDarkMode = (getResources().getConfiguration().uiMode &
                Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;

        if (isDarkMode) {
            // Modo oscuro: color blanco para el texto y logo oscuro
            logoImage.setImageResource(R.drawable.logo_dark);  // Cambiar al logo oscuro
            titleText.setTextColor(Color.WHITE);  // Texto en blanco en modo oscuro
            descText.setTextColor(Color.WHITE);  // Descripción en blanco en modo oscuro
            btnGuardarAhora.setTextColor(Color.BLACK);  // Texto del botón en negro
            btnAhoraNo.setTextColor(Color.BLACK);  // Texto del botón en negro
            btnGuardarAhora.setBackgroundColor(Color.WHITE);  // Fondo blanco para el botón
            btnAhoraNo.setBackgroundColor(Color.WHITE);  // Fondo blanco para el botón
        } else {
            // Modo claro: color negro para el texto y logo claro
            logoImage.setImageResource(R.drawable.logo_light);  // Cambiar al logo claro
            titleText.setTextColor(Color.BLACK);  // Texto en negro en modo claro
            descText.setTextColor(Color.BLACK);  // Descripción en negro en modo claro
            btnGuardarAhora.setTextColor(Color.WHITE);  // Texto del botón en blanco
            btnAhoraNo.setTextColor(Color.WHITE);  // Texto del botón en blanco
            btnGuardarAhora.setBackgroundColor(Color.BLACK);  // Fondo negro para el botón
            btnAhoraNo.setBackgroundColor(Color.BLACK);  // Fondo negro para el botón
        }
    }
}
