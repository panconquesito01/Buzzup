package com.MagicDevelopers.buzzup.LOGIN;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.MagicDevelopers.buzzup.HOME.HomeActivity;
import com.MagicDevelopers.buzzup.R;
import com.google.android.material.button.MaterialButton;

public class TwoFactorAuthActivity extends AppCompatActivity {

    private ImageView logoImage;
    private EditText otpDigit1, otpDigit2, otpDigit3, otpDigit4, otpDigit5, otpDigit6;
    private MaterialButton verifyButton;

    // Código OTP simulado para efectos demostrativos
    private final String SIMULATED_OTP = "123456";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_two_factor_auth);

        // Referenciar vistas
        logoImage = findViewById(R.id.logoImage);
        otpDigit1 = findViewById(R.id.otpDigit1);
        otpDigit2 = findViewById(R.id.otpDigit2);
        otpDigit3 = findViewById(R.id.otpDigit3);
        otpDigit4 = findViewById(R.id.otpDigit4);
        otpDigit5 = findViewById(R.id.otpDigit5);
        otpDigit6 = findViewById(R.id.otpDigit6);
        verifyButton = findViewById(R.id.verifyButton);

        // Ajustar colores y logo según el modo oscuro/claro
        setLogoAndButtonTextColor();

        // Acción al presionar el botón de verificar OTP
        verifyButton.setOnClickListener(v -> {
            String otp = getOTP();
            if (TextUtils.isEmpty(otp) || otp.length() < 6) {
                Toast.makeText(TwoFactorAuthActivity.this, "Por favor, ingrese el código OTP completo", Toast.LENGTH_SHORT).show();
                return;
            }
            if (otp.equals(SIMULATED_OTP)) {
                Toast.makeText(TwoFactorAuthActivity.this, "Verificación exitosa", Toast.LENGTH_SHORT).show();
                // Redirigir a la pantalla principal
                Intent intent = new Intent(TwoFactorAuthActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(TwoFactorAuthActivity.this, "Código OTP incorrecto", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Concatena los dígitos ingresados para formar el código OTP.
     */
    private String getOTP() {
        return otpDigit1.getText().toString().trim() +
                otpDigit2.getText().toString().trim() +
                otpDigit3.getText().toString().trim() +
                otpDigit4.getText().toString().trim() +
                otpDigit5.getText().toString().trim() +
                otpDigit6.getText().toString().trim();
    }

    /**
     * Ajusta el color del texto del botón y el logo según el modo oscuro o claro.
     */
    private void setLogoAndButtonTextColor() {
        boolean isDarkMode = (getResources().getConfiguration().uiMode &
                Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        if (isDarkMode) {
            // En modo oscuro, establecer logo oscuro y texto del botón en negro
            logoImage.setImageResource(R.drawable.logo_dark);
            verifyButton.setTextColor(Color.BLACK);
        } else {
            // En modo claro, establecer logo claro y texto del botón en blanco
            logoImage.setImageResource(R.drawable.logo_light);
            verifyButton.setTextColor(Color.WHITE);
        }
    }
}
