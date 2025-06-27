package com.MagicDevelopers.buzzup.LOGIN.Registro;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.MagicDevelopers.buzzup.Modelos.Usuario;
import com.MagicDevelopers.buzzup.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class Registro3Activity extends AppCompatActivity {

    private ImageView ivLogo;
    private TextInputEditText etFechaNacimiento;
    private MaterialButton btnContinuarRegistro3;
    private Usuario usuario;
    private String fechaFormateada = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro3);

        ivLogo = findViewById(R.id.ivLogo);
        etFechaNacimiento = findViewById(R.id.etFechaNacimiento);
        btnContinuarRegistro3 = findViewById(R.id.btnContinuarRegistro3);

        usuario = (Usuario) getIntent().getSerializableExtra("usuario");

        setLogoImage();

        etFechaNacimiento.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog picker = new DatePickerDialog(this, (view, y, m, d) -> {
                Calendar selected = Calendar.getInstance();
                selected.set(y, m, d);
                SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                fechaFormateada = formato.format(selected.getTime());

                etFechaNacimiento.setText(d + "/" + (m + 1) + "/" + y);
            }, year, month, day);

            picker.show();
        });

        btnContinuarRegistro3.setOnClickListener(v -> {
            if (fechaFormateada.isEmpty()) {
                Toast.makeText(this, "Debes seleccionar tu fecha de nacimiento", Toast.LENGTH_SHORT).show();
                return;
            }

            usuario.setFechaNacimiento(fechaFormateada);

            // NO intentar asignar userid aquí, ya que no hay usuario autenticado aún.
            // Solo pasa el usuario con fecha de nacimiento actualizada
            Intent intent = new Intent(this, Registro4Activity.class);
            intent.putExtra("usuario", usuario);
            startActivity(intent);
        });
    }

    private void setLogoImage() {
        boolean isDarkMode = (getResources().getConfiguration().uiMode &
                android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES;
        ivLogo.setImageResource(isDarkMode ? R.drawable.logo_dark : R.drawable.logo_light);
    }
}
