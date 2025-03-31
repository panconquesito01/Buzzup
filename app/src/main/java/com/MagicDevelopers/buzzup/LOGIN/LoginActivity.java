package com.MagicDevelopers.buzzup.LOGIN;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Toast;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.MagicDevelopers.buzzup.LOGIN.Registro.Registro1Activity;
import com.MagicDevelopers.buzzup.MainActivity;
import com.MagicDevelopers.buzzup.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.SignInButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText emailInput, passwordInput;
    private MaterialButton loginButton, registerButton;
    private SignInButton googleSignInButton;
    private FirebaseAuth mAuth;
    private GoogleSignInClient googleSignInClient;
    private static final int RC_SIGN_IN = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Inicializar Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Inicializar vistas
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);
        registerButton = findViewById(R.id.registerButton);
        googleSignInButton = findViewById(R.id.googleSignInButton);

        // Configuración de Google Sign-In
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);

        // Ajustar colores del logo, textos y botones según el tema
        setLogoAndTextColors();

        // Acción de inicio de sesión con correo
        loginButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Por favor, ingrese el correo y la contraseña", Toast.LENGTH_SHORT).show();
                return;
            }

            loginWithEmailPassword(email, password);
        });

        // Acción de registro
        registerButton.setOnClickListener(v -> {
            Intent registerIntent = new Intent(LoginActivity.this, Registro1Activity.class);
            startActivity(registerIntent);
        });

        // Acción de inicio de sesión con Google
        googleSignInButton.setOnClickListener(v -> signInWithGoogle());
    }

    private void setLogoAndTextColors() {
        ImageView logoImage = findViewById(R.id.logoImage);
        TextView titleText = findViewById(R.id.titleText);
        TextView infoText = findViewById(R.id.infoText);
        // Se obtienen también los botones para actualizar su color
        MaterialButton loginButton = findViewById(R.id.loginButton);
        MaterialButton registerButton = findViewById(R.id.registerButton);

        boolean isDarkMode = (getResources().getConfiguration().uiMode &
                Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;

        if (isDarkMode) {
            logoImage.setImageResource(R.drawable.logo_dark);
            titleText.setTextColor(ContextCompat.getColor(this, android.R.color.white));
            infoText.setTextColor(ContextCompat.getColor(this, android.R.color.white));
            // En modo oscuro, los botones tendrán el texto en negro
            loginButton.setTextColor(Color.BLACK);
            registerButton.setTextColor(Color.BLACK);
        } else {
            logoImage.setImageResource(R.drawable.logo_light);
            titleText.setTextColor(ContextCompat.getColor(this, android.R.color.black));
            infoText.setTextColor(ContextCompat.getColor(this, android.R.color.black));
            // En modo claro, los botones tendrán el texto en blanco
            loginButton.setTextColor(Color.WHITE);
            registerButton.setTextColor(Color.WHITE);
        }
    }

    private void loginWithEmailPassword(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(mainIntent);
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, "Error de autenticación", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void signInWithGoogle() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            GoogleSignIn.getSignedInAccountFromIntent(data)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            try {
                                GoogleSignInAccount account = task.getResult(ApiException.class);
                                if (account != null) {
                                    firebaseAuthWithGoogle(account.getIdToken());
                                }
                            } catch (ApiException e) {
                                Toast.makeText(LoginActivity.this, "Error en la autenticación de Google", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(LoginActivity.this, "Error en el inicio de sesión con Google", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(mainIntent);
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, "Error al autenticar con Google", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
