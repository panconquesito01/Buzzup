package com.MagicDevelopers.buzzup.LOGIN;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Toast;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.MagicDevelopers.buzzup.HOME.HomeActivity;
import com.MagicDevelopers.buzzup.LOGIN.Registro.Registro1Activity;
import com.MagicDevelopers.buzzup.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.SignInButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

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
        TextView forgotPasswordText = findViewById(R.id.forgotPasswordText);

        // Configuración de Google Sign-In
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);

        // Ajustar colores según tema
        setLogoAndTextColors();

        // Login con correo
        loginButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Por favor, ingrese el correo y la contraseña", Toast.LENGTH_SHORT).show();
                return;
            }

            loginWithEmailPassword(email, password);
        });

        // Registro
        registerButton.setOnClickListener(v -> {
            Intent registerIntent = new Intent(LoginActivity.this, Registro1Activity.class);
            startActivity(registerIntent);
        });

        // Recuperar contraseña
        forgotPasswordText.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RecuperarContrasenaActivity.class);
            startActivity(intent);
        });

        // Login con Google
        googleSignInButton.setOnClickListener(v -> signInWithGoogle());
    }

    private void setLogoAndTextColors() {
        ImageView logoImage = findViewById(R.id.logoImage);
        TextView titleText = findViewById(R.id.titleText);
        TextView infoText = findViewById(R.id.infoText);

        boolean isDarkMode = (getResources().getConfiguration().uiMode &
                Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;

        if (isDarkMode) {
            logoImage.setImageResource(R.drawable.logo_dark);
            titleText.setTextColor(ContextCompat.getColor(this, android.R.color.white));
            infoText.setTextColor(ContextCompat.getColor(this, android.R.color.white));
            loginButton.setTextColor(Color.BLACK);
            registerButton.setTextColor(Color.BLACK);
        } else {
            logoImage.setImageResource(R.drawable.logo_light);
            titleText.setTextColor(ContextCompat.getColor(this, android.R.color.black));
            infoText.setTextColor(ContextCompat.getColor(this, android.R.color.black));
            loginButton.setTextColor(Color.WHITE);
            registerButton.setTextColor(Color.WHITE);
        }
    }

    private void loginWithEmailPassword(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        String uid = mAuth.getCurrentUser().getUid();

                        FirebaseFirestore.getInstance().collection("usuarios")
                                .document(uid)
                                .get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    if (documentSnapshot.exists()) {
                                        String telefono = documentSnapshot.getString("telefono");

                                        if (telefono == null || telefono.isEmpty()) {
                                            guardarMantenerSesion();
                                            irAHome();
                                        } else {
                                            guardarMantenerSesion();
                                            irADosFA();
                                        }
                                    } else {
                                        guardarMantenerSesion();
                                        irAHome();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    guardarMantenerSesion();
                                    irAHome();
                                });
                    } else {
                        Toast.makeText(LoginActivity.this, "Error de autenticación", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void signInWithGoogle() {
        googleSignInClient.revokeAccess().addOnCompleteListener(task -> {
            Intent signInIntent = googleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });
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
                        guardarMantenerSesion();
                        irAHome();
                    } else {
                        Toast.makeText(LoginActivity.this, "Error al autenticar con Google", Toast.LENGTH_SHORT).show();
                    }
                });
    }


     //Guarda en SharedPreferences que el usuario quiere mantener la sesión iniciada.

    private void guardarMantenerSesion() {
        SharedPreferences prefs = getSharedPreferences("USER_PREFS", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("mantenerSesion", true);
        editor.apply();
    }


     // Lanza la actividad principal.

    private void irAHome() {
        Intent mainIntent = new Intent(LoginActivity.this, HomeActivity.class);
        startActivity(mainIntent);
        finish();
    }

     //Lanza la actividad de autenticación de dos factores.

    private void irADosFA() {
        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }
}
