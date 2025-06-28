package com.MagicDevelopers.buzzup.LOGIN;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.MagicDevelopers.buzzup.HOME.HomeActivity;
import com.MagicDevelopers.buzzup.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class TwoFactorAuthActivity extends AppCompatActivity {

    private ImageView logoImage;
    private EditText otpDigit1, otpDigit2, otpDigit3, otpDigit4, otpDigit5, otpDigit6;
    private MaterialButton verifyButton;
    private TextView textSendSms;

    private FirebaseAuth mAuth;
    private String verificationId;
    private PhoneAuthProvider.ForceResendingToken resendToken;

    // Reemplaza con tu número real en formato internacional
    private final String telefono = "+573001112233";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_two_factor_auth);

        // Referencias de vistas
        logoImage = findViewById(R.id.logoImage);
        otpDigit1 = findViewById(R.id.otpDigit1);
        otpDigit2 = findViewById(R.id.otpDigit2);
        otpDigit3 = findViewById(R.id.otpDigit3);
        otpDigit4 = findViewById(R.id.otpDigit4);
        otpDigit5 = findViewById(R.id.otpDigit5);
        otpDigit6 = findViewById(R.id.otpDigit6);
        verifyButton = findViewById(R.id.verifyButton);
        textSendSms = findViewById(R.id.textSendSms);

        mAuth = FirebaseAuth.getInstance();

        // Colores
        setLogoAndButtonTextColor();

        // Enviar SMS al abrir
        enviarCodigo();

        // Reenviar código
        textSendSms.setOnClickListener(v -> reenviarCodigo());

        // Verificar OTP
        verifyButton.setOnClickListener(v -> verificarCodigo());
    }

    private void enviarCodigo() {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(telefono)
                        .setTimeout(2L, TimeUnit.MINUTES)
                        .setActivity(this)
                        .setCallbacks(callbacks)
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void reenviarCodigo() {
        if (resendToken == null) {
            Toast.makeText(this, "Espera antes de reenviar.", Toast.LENGTH_SHORT).show();
            return;
        }
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(telefono)
                        .setTimeout(2L, TimeUnit.MINUTES)
                        .setActivity(this)
                        .setCallbacks(callbacks)
                        .setForceResendingToken(resendToken)
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks =
            new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                @Override
                public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                    String code = credential.getSmsCode();
                    if (code != null) {
                        setOTPFields(code);
                        verificarCredential(credential);
                    }
                }

                @Override
                public void onVerificationFailed(@NonNull FirebaseException e) {
                    Toast.makeText(TwoFactorAuthActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e("TwoFactor", "Verification failed", e);
                }

                @Override
                public void onCodeSent(@NonNull String verifId,
                                       @NonNull PhoneAuthProvider.ForceResendingToken token) {
                    verificationId = verifId;
                    resendToken = token;
                    Toast.makeText(TwoFactorAuthActivity.this, "Código enviado por SMS", Toast.LENGTH_SHORT).show();
                }
            };

    private void verificarCodigo() {
        String otp = getOTP();
        if (TextUtils.isEmpty(otp) || otp.length() < 6) {
            Toast.makeText(this, "Por favor ingresa los 6 dígitos.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (verificationId == null) {
            Toast.makeText(this, "Primero solicita el código.", Toast.LENGTH_SHORT).show();
            return;
        }
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otp);
        verificarCredential(credential);
    }

    private void verificarCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Verificación exitosa", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, HomeActivity.class));
                        finish();
                    } else {
                        Toast.makeText(this, "Código incorrecto", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private String getOTP() {
        return otpDigit1.getText().toString().trim()
                + otpDigit2.getText().toString().trim()
                + otpDigit3.getText().toString().trim()
                + otpDigit4.getText().toString().trim()
                + otpDigit5.getText().toString().trim()
                + otpDigit6.getText().toString().trim();
    }

    private void setOTPFields(String code) {
        if (code.length() == 6) {
            otpDigit1.setText(String.valueOf(code.charAt(0)));
            otpDigit2.setText(String.valueOf(code.charAt(1)));
            otpDigit3.setText(String.valueOf(code.charAt(2)));
            otpDigit4.setText(String.valueOf(code.charAt(3)));
            otpDigit5.setText(String.valueOf(code.charAt(4)));
            otpDigit6.setText(String.valueOf(code.charAt(5)));
        }
    }

    private void setLogoAndButtonTextColor() {
        boolean isDarkMode = (getResources().getConfiguration().uiMode &
                Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        if (isDarkMode) {
            logoImage.setImageResource(R.drawable.logo_dark);
            verifyButton.setTextColor(Color.BLACK);
        } else {
            logoImage.setImageResource(R.drawable.logo_light);
            verifyButton.setTextColor(Color.WHITE);
        }
    }
}
