package com.MagicDevelopers.buzzup.LOGIN.Registro;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.MagicDevelopers.buzzup.Controlladores.UsuarioController;
import com.MagicDevelopers.buzzup.Modelos.Usuario;
import com.MagicDevelopers.buzzup.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class Registro3Activity extends AppCompatActivity {

    private ImageView ivLogo;
    private TextInputEditText etFechaNacimiento;
    private MaterialButton btnContinuarRegistro3;
    private UsuarioController usuarioController;
    private String userId;   // Puede ser null si aún no se ha guardado en Firebase
    private Usuario usuario;
    private String selectedDate = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro3);

        // Inicializar vistas
        ivLogo = findViewById(R.id.ivLogo);
        etFechaNacimiento = findViewById(R.id.etFechaNacimiento);
        btnContinuarRegistro3 = findViewById(R.id.btnContinuarRegistro3);

        // Inicializar el controlador
        usuarioController = new UsuarioController();

        // Recibir el objeto Usuario y userId (puede ser null) desde Registro2Activity
        userId = getIntent().getStringExtra("userId");
        usuario = (Usuario) getIntent().getSerializableExtra("usuario");

        // Configurar el logo según el modo oscuro/claro
        setLogoImage();

        // Configurar DatePickerDialog al hacer clic en el campo de fecha
        etFechaNacimiento.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(Registro3Activity.this,
                    new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {
                            // Formatear la fecha: día/(mes+1)/año
                            selectedDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                            etFechaNacimiento.setText(selectedDate);
                        }
                    }, year, month, day);
            datePickerDialog.show();
        });

        btnContinuarRegistro3.setOnClickListener(v -> {
            if (etFechaNacimiento.getText().toString().trim().isEmpty()) {
                Toast.makeText(Registro3Activity.this, "Debes seleccionar tu fecha de nacimiento", Toast.LENGTH_SHORT).show();
                return;
            }

            // Actualizar el objeto Usuario con la fecha de nacimiento
            usuario.setFechaNacimiento(selectedDate);

            // Si ya se ha guardado en Firebase (userId no es null), actualizar ese campo; de lo contrario, simplemente acumula los datos
            if (userId != null) {
                Map<String, Object> datosRegistro3 = new HashMap<>();
                datosRegistro3.put("fechaNacimiento", selectedDate);
                usuarioController.actualizarUsuario(userId, datosRegistro3, exito -> {
                    if (exito) {
                        Intent intent = new Intent(Registro3Activity.this, Registro4Activity.class);
                        intent.putExtra("userId", userId);
                        intent.putExtra("usuario", usuario);
                        startActivity(intent);
                    } else {
                        Toast.makeText(Registro3Activity.this, "Error al actualizar la fecha de nacimiento", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                // Si userId es null, simplemente pasa el objeto Usuario al siguiente Activity
                Intent intent = new Intent(Registro3Activity.this, Registro4Activity.class);
                intent.putExtra("usuario", usuario);
                startActivity(intent);
            }
        });
    }

    /**
     * Configura el logo según el modo oscuro o claro.
     */
    private void setLogoImage() {
        boolean isDarkMode = (getResources().getConfiguration().uiMode &
                android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES;
        if (isDarkMode) {
            ivLogo.setImageResource(R.drawable.logo_dark);
        } else {
            ivLogo.setImageResource(R.drawable.logo_light);
        }
    }
}
