package com.MagicDevelopers.buzzup.LOGIN.Registro;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.MagicDevelopers.buzzup.Modelos.Usuario;
import com.MagicDevelopers.buzzup.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class Registro1Activity extends AppCompatActivity {

    private TextInputEditText etNombre, etApellido, etDescripcion;
    private MaterialButton btnSiguiente;
    private ImageView logoImage;
    private TextView titleText, subTitleText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro1);

        // Inicialización de vistas
        logoImage = findViewById(R.id.logoImage);
        titleText = findViewById(R.id.titleText);
        subTitleText = findViewById(R.id.subTitleText);
        etNombre = findViewById(R.id.etNombre);
        etApellido = findViewById(R.id.etApellido);
        etDescripcion = findViewById(R.id.etDescripcion);
        btnSiguiente = findViewById(R.id.btnSiguiente);

        // Ajustar interfaz para modo oscuro/claro
        setUIColors();

        // Acción al presionar "Continuar"
        btnSiguiente.setOnClickListener(v -> {
            String nombre = etNombre.getText().toString().trim();
            String apellido = etApellido.getText().toString().trim();
            String descripcion = etDescripcion.getText().toString().trim();

            // Validación básica
            if (nombre.isEmpty() || apellido.isEmpty()) {
                Toast.makeText(this, "Nombre y Apellido son obligatorios", Toast.LENGTH_SHORT).show();
                return;
            }

            // Crear objeto Usuario con solo los datos de esta pantalla
            Usuario usuario = new Usuario(nombre, apellido, descripcion);

            // Pasar al Registro2Activity
            Intent intent = new Intent(this, Registro2Activity.class);
            intent.putExtra("usuario", usuario);
            startActivity(intent);
        });
    }

    private void setUIColors() {
        boolean isDarkMode = (getResources().getConfiguration().uiMode &
                Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;

        if (isDarkMode) {
            logoImage.setImageResource(R.drawable.logo_dark);
            titleText.setTextColor(ContextCompat.getColor(this, android.R.color.white));
            subTitleText.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        } else {
            logoImage.setImageResource(R.drawable.logo_light);
            titleText.setTextColor(ContextCompat.getColor(this, android.R.color.black));
            subTitleText.setTextColor(ContextCompat.getColor(this, android.R.color.black));
        }
    }
}
