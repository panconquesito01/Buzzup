package com.MagicDevelopers.buzzup.LOGIN.Registro;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.MagicDevelopers.buzzup.HOME.HomeActivity;
import com.MagicDevelopers.buzzup.Modelos.Usuario;
import com.MagicDevelopers.buzzup.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class Registro5Activity extends AppCompatActivity {

    private Button btnGuardarAhora, btnAhoraNo;
    private ImageView logoImage;
    private TextView titleText, descText;

    private Usuario usuario;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestoreDb;
    private DatabaseReference realtimeDbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro5);

        logoImage       = findViewById(R.id.logoImage);
        titleText       = findViewById(R.id.titleText);
        descText        = findViewById(R.id.descText);
        btnGuardarAhora = findViewById(R.id.btnGuardarAhora);
        btnAhoraNo      = findViewById(R.id.btnAhoraNo);

        setLogoAndTextColors();

        mAuth = FirebaseAuth.getInstance();
        firestoreDb = FirebaseFirestore.getInstance();
        realtimeDbRef = FirebaseDatabase.getInstance().getReference("usuarios");

        usuario = (Usuario) getIntent().getSerializableExtra("usuario");

        if (usuario == null) {
            Toast.makeText(this, "Error: datos de usuario no disponibles.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        btnGuardarAhora.setOnClickListener(v -> crearUsuarioFirebase(true));
        btnAhoraNo.setOnClickListener(v -> crearUsuarioFirebase(false));
    }

    private void crearUsuarioFirebase(boolean mantenerSesion) {
        mAuth.createUserWithEmailAndPassword(usuario.getCorreo(), usuario.getContrasena())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            String uid = firebaseUser.getUid();
                            usuario.setUserid(uid);

                            // Si hay imagen seleccionada
                            if (usuario.getFotoPerfilUrl() != null && !usuario.getFotoPerfilUrl().isEmpty()) {
                                Uri localUri = Uri.parse(usuario.getFotoPerfilUrl());
                                StorageReference refCropped = FirebaseStorage.getInstance().getReference()
                                        .child("profile_images").child(uid).child("profile.jpg");

                                refCropped.putFile(localUri)
                                        .continueWithTask(t -> {
                                            if (!t.isSuccessful()) {
                                                throw t.getException();
                                            }
                                            return refCropped.getDownloadUrl();
                                        })
                                        .addOnSuccessListener(downloadUri -> {
                                            usuario.setFotoPerfilUrl(downloadUri.toString());
                                            usuario.setFotoPerfilCompletaUrl(downloadUri.toString());
                                            actualizarPerfilFirebase(firebaseUser, downloadUri, uid, mantenerSesion);
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(this, "Error al subir la imagen: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                            actualizarPerfilFirebase(firebaseUser, null, uid, mantenerSesion);
                                        });
                            } else {
                                actualizarPerfilFirebase(firebaseUser, null, uid, mantenerSesion);
                            }
                        } else {
                            Toast.makeText(this, "Error al obtener usuario Firebase.", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(this, "Error al crear usuario: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void actualizarPerfilFirebase(FirebaseUser firebaseUser, Uri photoUri, String uid, boolean mantenerSesion) {
        UserProfileChangeRequest.Builder builder = new UserProfileChangeRequest.Builder()
                .setDisplayName(usuario.getNombre() + " " + usuario.getApellido());

        if (photoUri != null) {
            builder.setPhotoUri(photoUri);
        }

        firebaseUser.updateProfile(builder.build())
                .addOnCompleteListener(profileTask -> {
                    guardarUsuarioEnFirestore(uid, mantenerSesion);
                    guardarUsuarioEnRealtimeDatabase(uid);
                });
    }

    private void guardarUsuarioEnFirestore(String uid, boolean mantenerSesion) {
        Map<String, Object> usuarioMap = new HashMap<>();
        usuarioMap.put("userid", usuario.getUserid());
        usuarioMap.put("nombre", usuario.getNombre());
        usuarioMap.put("apellido", usuario.getApellido());
        usuarioMap.put("descripcion", usuario.getDescripcion());
        usuarioMap.put("correo", usuario.getCorreo());
        usuarioMap.put("fechaNacimiento", usuario.getFechaNacimiento());
        usuarioMap.put("fotoPerfilUrl", usuario.getFotoPerfilUrl() != null ? usuario.getFotoPerfilUrl() : "");
        usuarioMap.put("fotoPerfilCompletaUrl", usuario.getFotoPerfilCompletaUrl() != null ? usuario.getFotoPerfilCompletaUrl() : "");
        usuarioMap.put("fotoPublicacionUrl", usuario.getFotoPublicacionUrl() != null ? usuario.getFotoPublicacionUrl() : "");
        usuarioMap.put("guardarInformacion", mantenerSesion);
        usuarioMap.put("emisorid", usuario.getEmisorid());
        usuarioMap.put("receptorid", usuario.getReceptorid());
        usuarioMap.put("telefono", ""); // Campo vacío por defecto

        firestoreDb.collection("usuarios").document(uid)
                .set(usuarioMap)
                .addOnSuccessListener(aVoid -> {
                    guardarPreferenciaLocal(mantenerSesion);
                    if (!mantenerSesion) {
                        mAuth.signOut();
                    }
                    Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show();
                    irHome();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al guardar usuario: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void guardarUsuarioEnRealtimeDatabase(String uid) {
        Map<String, Object> usuarioMap = new HashMap<>();
        usuarioMap.put("userid", usuario.getUserid());
        usuarioMap.put("nombre", usuario.getNombre());
        usuarioMap.put("apellido", usuario.getApellido());
        usuarioMap.put("descripcion", usuario.getDescripcion());
        usuarioMap.put("correo", usuario.getCorreo());
        usuarioMap.put("fechaNacimiento", usuario.getFechaNacimiento());
        usuarioMap.put("fotoPerfilUrl", usuario.getFotoPerfilUrl() != null ? usuario.getFotoPerfilUrl() : "");
        usuarioMap.put("fotoPerfilCompletaUrl", usuario.getFotoPerfilCompletaUrl() != null ? usuario.getFotoPerfilCompletaUrl() : "");
        usuarioMap.put("fotoPublicacionUrl", usuario.getFotoPublicacionUrl() != null ? usuario.getFotoPublicacionUrl() : "");
        usuarioMap.put("guardarInformacion", usuario.isGuardarInformacion());
        usuarioMap.put("emisorid", usuario.getEmisorid());
        usuarioMap.put("receptorid", usuario.getReceptorid());
        usuarioMap.put("telefono", ""); // Campo vacío por defecto

        realtimeDbRef.child(uid).setValue(usuarioMap)
                .addOnSuccessListener(aVoid -> {
                    // Guardado exitoso
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error guardando en Realtime DB: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void guardarPreferenciaLocal(boolean mantenerSesion) {
        SharedPreferences pref = getSharedPreferences("USER_PREFS", MODE_PRIVATE);
        pref.edit().putBoolean("mantenerSesion", mantenerSesion).apply();
    }

    private void irHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        finishAffinity();
    }

    private void setLogoAndTextColors() {
        boolean dark = (getResources().getConfiguration().uiMode &
                Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;

        if (dark) {
            logoImage.setImageResource(R.drawable.logo_dark);
            titleText.setTextColor(Color.WHITE);
            descText.setTextColor(Color.WHITE);
            btnGuardarAhora.setTextColor(Color.BLACK);
            btnAhoraNo.setTextColor(Color.BLACK);
            btnGuardarAhora.setBackgroundColor(Color.WHITE);
            btnAhoraNo.setBackgroundColor(Color.WHITE);
        } else {
            logoImage.setImageResource(R.drawable.logo_light);
            titleText.setTextColor(Color.BLACK);
            descText.setTextColor(Color.BLACK);
            btnGuardarAhora.setTextColor(Color.WHITE);
            btnAhoraNo.setTextColor(Color.WHITE);
            btnGuardarAhora.setBackgroundColor(Color.BLACK);
            btnAhoraNo.setBackgroundColor(Color.BLACK);
        }
    }
}
