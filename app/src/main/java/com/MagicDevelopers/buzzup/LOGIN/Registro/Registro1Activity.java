package com.MagicDevelopers.buzzup.LOGIN.Registro;

import android.content.Intent;
import android.content.SharedPreferences;
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

        // Ajustar la interfaz según el modo oscuro/claro
        setUIColors();

        // Acción del botón "Continuar"
        btnSiguiente.setOnClickListener(v -> {
            String nombre = etNombre.getText().toString().trim();
            String apellido = etApellido.getText().toString().trim();
            String descripcion = etDescripcion.getText().toString().trim();

            // Validar campos obligatorios
            if (nombre.isEmpty() || apellido.isEmpty()) {
                Toast.makeText(Registro1Activity.this, "Nombre y Apellido son obligatorios", Toast.LENGTH_SHORT).show();
                return;
            }

            // Crear el objeto Usuario con los datos de Registro1 usando el constructor de 3 parámetros
            Usuario usuario = new Usuario(nombre, apellido, descripcion);

            // Guardar los datos localmente en SharedPreferences
            SharedPreferences sharedPreferences = getSharedPreferences("UsuarioPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("nombre", usuario.getNombre());
            editor.putString("apellido", usuario.getApellido());
            editor.putString("descripcion", usuario.getDescripcion());
            editor.apply();

            // Pasar el objeto Usuario a la siguiente Activity (Registro2Activity)
            Intent intent = new Intent(Registro1Activity.this, Registro2Activity.class);
            intent.putExtra("usuario", usuario);
            startActivity(intent);
        });
    }

    /**
     * Ajusta el logo y los textos según el modo oscuro o claro.
     */
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
