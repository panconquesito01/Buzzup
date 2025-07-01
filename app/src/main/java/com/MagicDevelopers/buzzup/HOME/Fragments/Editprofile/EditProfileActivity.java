package com.MagicDevelopers.buzzup.HOME.Fragments.Editprofile;

import android.app.ProgressDialog;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.MagicDevelopers.buzzup.R;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.firestore.*;

import java.util.HashMap;

public class EditProfileActivity extends AppCompatActivity {

    private ImageView backgroundImage, profileImage;
    private TextView textChangeBackground, btnChangeProfilePhoto;
    private TextInputEditText editName, editBio, editCountry, editBirthDate;
    private MaterialButton btnSaveChanges;

    private Uri selectedProfileUri = null;
    private Uri selectedBackgroundUri = null;

    private DatabaseReference userRef;
    private StorageReference storageRef;
    private DocumentReference firestoreRef;
    private ProgressDialog progressDialog;

    private final ActivityResultLauncher<String> profileImagePicker =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedProfileUri = uri;
                    profileImage.setImageURI(uri);
                }
            });

    private final ActivityResultLauncher<String> backgroundImagePicker =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedBackgroundUri = uri;
                    backgroundImage.setImageURI(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        backgroundImage = findViewById(R.id.backgroundImage);
        profileImage = findViewById(R.id.profileImage);
        textChangeBackground = findViewById(R.id.textChangeBackground);
        btnChangeProfilePhoto = findViewById(R.id.btnChangeProfilePhoto);
        editName = findViewById(R.id.editName);
        editBio = findViewById(R.id.editBio);
        editCountry = findViewById(R.id.editCountry);
        editBirthDate = findViewById(R.id.editBirthDate);
        btnSaveChanges = findViewById(R.id.btnSaveChanges);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference("usuarios").child(userId);
        storageRef = FirebaseStorage.getInstance().getReference("profile_images").child(userId);
        firestoreRef = FirebaseFirestore.getInstance().collection("usuarios").document(userId);

        loadUserProfile();

        btnChangeProfilePhoto.setOnClickListener(v -> profileImagePicker.launch("image/*"));
        findViewById(R.id.backgroundContainer).setOnClickListener(v -> backgroundImagePicker.launch("image/*"));
        btnSaveChanges.setOnClickListener(v -> saveProfileChanges());
    }

    private void loadUserProfile() {
        progressDialog.setMessage("Cargando datos...");
        progressDialog.show();
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                progressDialog.dismiss();
                if (snapshot.exists()) {
                    String nombre = snapshot.child("nombre").getValue(String.class);
                    String descripcion = snapshot.child("descripcion").getValue(String.class);
                    String pais = snapshot.child("pais").getValue(String.class);
                    String fechaNacimiento = snapshot.child("fechaNacimiento").getValue(String.class);
                    String fotoPerfilUrl = snapshot.child("fotoPerfilUrl").getValue(String.class);
                    String fondoUrl = snapshot.child("fotoFondoUrl").getValue(String.class);

                    editName.setText(nombre);
                    editBio.setText(descripcion);
                    editCountry.setText(pais);
                    editBirthDate.setText(fechaNacimiento);

                    Glide.with(EditProfileActivity.this)
                            .load(fotoPerfilUrl)
                            .placeholder(R.drawable.ic_person_placeholder)
                            .circleCrop()
                            .into(profileImage);

                    Glide.with(EditProfileActivity.this)
                            .load(fondoUrl)
                            .placeholder(android.R.color.darker_gray)
                            .centerCrop()
                            .into(backgroundImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressDialog.dismiss();
                Toast.makeText(EditProfileActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveProfileChanges() {
        String nombre = editName.getText().toString().trim();
        String bio = editBio.getText().toString().trim();
        String pais = editCountry.getText().toString().trim();
        String fechaNacimiento = editBirthDate.getText().toString().trim();

        if (TextUtils.isEmpty(nombre)) {
            editName.setError("El nombre es requerido");
            return;
        }

        progressDialog.setMessage("Guardando cambios...");
        progressDialog.show();

        HashMap<String, Object> updates = new HashMap<>();
        updates.put("nombre", nombre);
        updates.put("descripcion", bio);
        updates.put("pais", pais);
        updates.put("fechaNacimiento", fechaNacimiento);

        uploadImagesAndSave(updates);
    }

    private void uploadImagesAndSave(HashMap<String, Object> updates) {
        if (selectedProfileUri != null) {
            storageRef.child("profile.jpg").putFile(selectedProfileUri).addOnSuccessListener(taskSnapshot ->
                    storageRef.child("profile.jpg").getDownloadUrl().addOnSuccessListener(uri -> {
                        updates.put("fotoPerfilUrl", uri.toString());
                        uploadBackgroundImage(updates);
                    })).addOnFailureListener(e -> {
                progressDialog.dismiss();
                Toast.makeText(this, "Error subiendo foto de perfil", Toast.LENGTH_SHORT).show();
            });
        } else {
            uploadBackgroundImage(updates);
        }
    }

    private void uploadBackgroundImage(HashMap<String, Object> updates) {
        if (selectedBackgroundUri != null) {
            storageRef.child("background.jpg").putFile(selectedBackgroundUri).addOnSuccessListener(taskSnapshot ->
                    storageRef.child("background.jpg").getDownloadUrl().addOnSuccessListener(uri -> {
                        updates.put("fotoFondoUrl", uri.toString());
                        updateBothDatabases(updates);
                    })).addOnFailureListener(e -> {
                progressDialog.dismiss();
                Toast.makeText(this, "Error subiendo imagen de fondo", Toast.LENGTH_SHORT).show();
            });
        } else {
            updateBothDatabases(updates);
        }
    }

    private void updateBothDatabases(HashMap<String, Object> updates) {
        userRef.updateChildren(updates).addOnSuccessListener(unused -> {
            firestoreRef.set(updates, SetOptions.merge())
                    .addOnSuccessListener(unused2 -> {
                        progressDialog.dismiss();
                        Toast.makeText(this, "Perfil actualizado correctamente", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(this, "Error guardando en Firestore", Toast.LENGTH_SHORT).show();
                    });
        }).addOnFailureListener(e -> {
            progressDialog.dismiss();
            Toast.makeText(this, "Error guardando en Realtime Database", Toast.LENGTH_SHORT).show();
        });
    }
}
