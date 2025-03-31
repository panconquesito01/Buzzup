package com.MagicDevelopers.buzzup.LOGIN.Registro;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

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

        // Recibir el objeto Usuario desde Registro1Activity
        usuario = (Usuario) getIntent().getSerializableExtra("usuario");

        // Configurar la imagen del logo según el modo oscuro/claro
        setLogoImage();

        btnContinuar.setOnClickListener(v -> {
            String correo = etCorreo.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            // Validar que todos los campos estén llenos
            if (correo.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(Registro2Activity.this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validar que las contraseñas coincidan
            if (!password.equals(confirmPassword)) {
                Toast.makeText(Registro2Activity.this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validar que la contraseña tenga al menos 6 caracteres
            if (password.length() < 6) {
                Toast.makeText(Registro2Activity.this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show();
                return;
            }

            // Actualizar el objeto Usuario con los datos de Registro2
            usuario.setCorreo(correo);
            usuario.setContrasena(password);

            // Pasar al siguiente paso del registro (Registro3Activity) con el objeto actualizado
            Intent intent = new Intent(Registro2Activity.this, Registro3Activity.class);
            intent.putExtra("usuario", usuario);
            startActivity(intent);
        });
    }

    /**
     * Configura la imagen del logo según el modo oscuro o claro.
     */
    private void setLogoImage() {
        boolean isDarkMode = (getResources().getConfiguration().uiMode &
                Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        if (isDarkMode) {
            ivBuzzUp.setImageResource(R.drawable.logo_dark);
        } else {
            ivBuzzUp.setImageResource(R.drawable.logo_light);
        }
    }
}
