package com.MagicDevelopers.buzzup.LOGIN.Registro;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.MagicDevelopers.buzzup.Modelos.Usuario;
import com.MagicDevelopers.buzzup.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class Registro2Activity extends AppCompatActivity {

    private ImageView ivBuzzUp;
    private TextInputEditText etCorreo, etPassword, etConfirmPassword;
    private MaterialButton btnContinuar;

    private Usuario usuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro2);

        // Inicializar vistas
        ivBuzzUp = findViewById(R.id.ivBuzzUp);
        etCorreo = findViewById(R.id.etCorreo);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnContinuar = findViewById(R.id.btnContinuar);

        // Obtener el objeto Usuario que viene desde Registro1Activity
        usuario = (Usuario) getIntent().getSerializableExtra("usuario");

        // Configurar logo según modo
        setLogoImage();

        btnContinuar.setOnClickListener(v -> {
            String correo = etCorreo.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            // Validaciones
            if (correo.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.length() < 6) {
                Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show();
                return;
            }

            // Guardar datos en el objeto Usuario (NO creamos usuario en FirebaseAuth aún)
            usuario.setCorreo(correo);
            usuario.setContrasena(password);

            // Pasar a Registro3Activity
            Intent intent = new Intent(this, Registro3Activity.class);
            intent.putExtra("usuario", usuario);
            startActivity(intent);
        });
    }

    private void setLogoImage() {
        boolean isDarkMode = (getResources().getConfiguration().uiMode &
                android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES;
        ivBuzzUp.setImageResource(isDarkMode ? R.drawable.logo_dark : R.drawable.logo_light);
    }
}
