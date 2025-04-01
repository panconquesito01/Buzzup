package com.MagicDevelopers.buzzup.LOGIN;

import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.MagicDevelopers.buzzup.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

public class RecuperarContrasenaActivity extends AppCompatActivity {

    private TextInputEditText emailInputRecuperar;
    private MaterialButton recuperarButton;
    private FirebaseAuth mAuth;
    private ImageView logoImage;
    private TextView titleRecuperarText, infoRecuperarText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recuperar_contrasena);

        // Inicializar Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Referenciar vistas
        logoImage = findViewById(R.id.logoImage);
        titleRecuperarText = findViewById(R.id.titleRecuperarText);
        infoRecuperarText = findViewById(R.id.infoRecuperarText);
        emailInputRecuperar = findViewById(R.id.emailInputRecuperar);
        recuperarButton = findViewById(R.id.recuperarButton);

        // Ajustar colores según modo oscuro o claro
        setLogoAndTextColors();

        // Acción al presionar el botón de recuperar contraseña
        recuperarButton.setOnClickListener(view -> {
            String email = emailInputRecuperar.getText().toString().trim();
            if (email.isEmpty()) {
                Toast.makeText(RecuperarContrasenaActivity.this, "Por favor, ingrese su correo electrónico", Toast.LENGTH_SHORT).show();
                return;
            }
            // Enviar correo de restablecimiento de contraseña usando Firebase
            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(RecuperarContrasenaActivity.this, "Se ha enviado un correo para recuperar la contraseña", Toast.LENGTH_SHORT).show();
                            finish(); // Vuelve a la actividad anterior (LoginActivity)
                        } else {
                            Toast.makeText(RecuperarContrasenaActivity.this, "Error al enviar el correo de recuperación", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    /**
     * Ajusta los colores del logo, textos y botón de acuerdo al modo oscuro o claro.
     */
    private void setLogoAndTextColors() {
        boolean isDarkMode = (getResources().getConfiguration().uiMode &
                Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;

        if (isDarkMode) {
            logoImage.setImageResource(R.drawable.logo_dark);
            titleRecuperarText.setTextColor(ContextCompat.getColor(this, android.R.color.white));
            infoRecuperarText.setTextColor(ContextCompat.getColor(this, android.R.color.white));
            // En modo oscuro, el texto del botón será negro
            recuperarButton.setTextColor(Color.BLACK);
        } else {
            logoImage.setImageResource(R.drawable.logo_light);
            titleRecuperarText.setTextColor(ContextCompat.getColor(this, android.R.color.black));
            infoRecuperarText.setTextColor(ContextCompat.getColor(this, android.R.color.black));
            // En modo claro, el texto del botón será blanco
            recuperarButton.setTextColor(Color.WHITE);
        }
    }
}
