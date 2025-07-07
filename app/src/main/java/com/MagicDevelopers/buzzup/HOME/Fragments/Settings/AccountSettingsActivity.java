package com.MagicDevelopers.buzzup.HOME.Fragments.Settings;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.MagicDevelopers.buzzup.Adaptadores.CountryAdapter;
import com.MagicDevelopers.buzzup.R;
import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class AccountSettingsActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_PICK = 1001;
    private static final int REQUEST_IMAGE_CROP = 1002;

    private TextInputEditText editFirstName, editLastName, editUsername, editBio, editBirthDate, editPhone, editEmail;
    private TextInputEditText editCountry;
    private MaterialButton btnSaveChanges, btnChangePassword;
    private TextView changePhotoText;
    private CircleImageView profileImage;
    private Uri selectedImageUri;

    private String userId;
    private String profilePhotoUrl = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_settings);

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Cuenta");
        toolbar.setNavigationOnClickListener(v -> finish());

        // Inicializar campos
        profileImage = findViewById(R.id.profileImage);
        changePhotoText = findViewById(R.id.changePhotoText);
        editFirstName = findViewById(R.id.editFirstName);
        editLastName = findViewById(R.id.editLastName);
        editUsername = findViewById(R.id.editUsername);
        editBio = findViewById(R.id.editBio);
        editBirthDate = findViewById(R.id.editBirthDate);
        editPhone = findViewById(R.id.editPhone);
        editEmail = findViewById(R.id.editEmail);
        editCountry = findViewById(R.id.editCountry);
        btnSaveChanges = findViewById(R.id.btnSaveChanges);
        btnChangePassword = findViewById(R.id.btnChangePassword);

        editCountry.setFocusable(false);
        editCountry.setOnClickListener(v -> showCountrySelector());

        editBirthDate.setOnClickListener(v -> showDatePicker());

        changePhotoText.setOnClickListener(v -> openImagePicker());

        btnSaveChanges.setOnClickListener(v -> saveChanges());

        btnChangePassword.setOnClickListener(v ->
                startActivity(new Intent(this, ChangePasswordActivity.class))
        );

        loadUserData();
    }

    private void loadUserData() {
        FirebaseFirestore.getInstance()
                .collection("usuarios")
                .document(userId)
                .get()
                .addOnSuccessListener(this::fillFields)
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar datos: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void fillFields(DocumentSnapshot snapshot) {
        if (snapshot.exists()) {
            editFirstName.setText(snapshot.getString("nombre"));
            editLastName.setText(snapshot.getString("apellido"));
            editUsername.setText(snapshot.getString("nombreUsuario"));
            editBio.setText(snapshot.getString("descripcion"));
            editBirthDate.setText(snapshot.getString("fechaNacimiento"));
            editPhone.setText(snapshot.getString("telefono"));
            editEmail.setText(snapshot.getString("correo"));
            editCountry.setText(snapshot.getString("pais"));

            String urlCompleta = snapshot.getString("fotoPerfilCompletaUrl");
            String urlCorta = snapshot.getString("fotoPerfilUrl");

            if (urlCompleta != null && !urlCompleta.isEmpty()) {
                profilePhotoUrl = urlCompleta;
            } else if (urlCorta != null && !urlCorta.isEmpty()) {
                profilePhotoUrl = urlCorta;
            } else {
                profilePhotoUrl = "";
            }

            if (!profilePhotoUrl.isEmpty()) {
                Glide.with(this).load(profilePhotoUrl).into(profileImage);
            }
        }
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year1, month1, dayOfMonth) -> {
                    String date = String.format(Locale.getDefault(), "%04d-%02d-%02d", year1, month1 + 1, dayOfMonth);
                    editBirthDate.setText(date);
                },
                year, month, day
        );
        datePickerDialog.show();
    }

    private void showCountrySelector() {
        View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_countries, null);
        final androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(this)
                .setView(bottomSheetView)
                .create();

        EditText editSearch = bottomSheetView.findViewById(R.id.editSearchCountry);
        RecyclerView recyclerView = bottomSheetView.findViewById(R.id.recyclerCountries);

        String[] codes = Locale.getISOCountries();
        List<String> countryList = new ArrayList<>();
        for (String code : codes) {
            Locale locale = new Locale("", code);
            countryList.add(locale.getDisplayCountry());
        }
        Collections.sort(countryList);

        CountryAdapter adapter = new CountryAdapter(countryList, country -> {
            editCountry.setText(country);
            dialog.dismiss();
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        editSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                List<String> filtered = new ArrayList<>();
                for (String item : countryList) {
                    if (item.toLowerCase().contains(s.toString().toLowerCase())) {
                        filtered.add(item);
                    }
                }
                adapter.filterList(filtered);
            }
            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        dialog.show();
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK && data != null) {
            Uri sourceUri = data.getData();
            Uri destinationUri = Uri.fromFile(new File(getCacheDir(), "cropped_" + System.currentTimeMillis() + ".jpg"));
            startCrop(sourceUri, destinationUri);
        } else if (requestCode == UCrop.REQUEST_CROP && resultCode == RESULT_OK && data != null) {
            selectedImageUri = UCrop.getOutput(data);
            if (selectedImageUri != null) {
                profileImage.setImageURI(selectedImageUri);
                uploadImageToFirebase();
            }
        }
    }

    private void startCrop(Uri sourceUri, Uri destinationUri) {
        UCrop.Options options = new UCrop.Options();
        options.setCompressionQuality(85);
        options.setFreeStyleCropEnabled(true);
        options.setToolbarTitle("Recortar imagen");
        options.setStatusBarColor(ContextCompat.getColor(this, R.color.purple_700));
        options.setToolbarColor(ContextCompat.getColor(this, R.color.purple_700));
        options.setActiveControlsWidgetColor(ContextCompat.getColor(this, R.color.purple_500));

        UCrop.of(sourceUri, destinationUri)
                .withAspectRatio(1, 1)
                .withMaxResultSize(800, 800)
                .withOptions(options)
                .start(this);
    }

    private void uploadImageToFirebase() {
        if (selectedImageUri == null) return;

        StorageReference storageRef = FirebaseStorage.getInstance()
                .getReference("profile_images/" + userId + "/profile.jpg");

        storageRef.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot ->
                        storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            String downloadUrl = uri.toString();
                            profilePhotoUrl = downloadUrl;
                            updatePhotoInDatabases(downloadUrl);
                        })
                )
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error al subir imagen: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void updatePhotoInDatabases(String url) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("fotoPerfilUrl", url);
        updates.put("fotoPerfilCompletaUrl", url);

        FirebaseFirestore.getInstance()
                .collection("usuarios")
                .document(userId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    FirebaseDatabase.getInstance()
                            .getReference("usuarios")
                            .child(userId)
                            .updateChildren(updates)
                            .addOnSuccessListener(aVoid2 ->
                                    Toast.makeText(this, "Foto actualizada correctamente.", Toast.LENGTH_SHORT).show()
                            )
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Error en Realtime Database: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                            );
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error en Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void saveChanges() {
        String firstName = editFirstName.getText().toString().trim();
        String lastName = editLastName.getText().toString().trim();
        String username = editUsername.getText().toString().trim();
        String bio = editBio.getText().toString().trim();
        String birthDate = editBirthDate.getText().toString().trim();
        String phone = editPhone.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String country = editCountry.getText().toString().trim();

        if (firstName.isEmpty() || lastName.isEmpty() || username.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Por favor completa los campos obligatorios.", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("nombre", firstName);
        updates.put("apellido", lastName);
        updates.put("nombreUsuario", username);
        updates.put("descripcion", bio);
        updates.put("fechaNacimiento", birthDate);
        updates.put("pais", country);
        updates.put("telefono", phone);
        updates.put("correo", email);
        if (!profilePhotoUrl.isEmpty()) {
            updates.put("fotoPerfilCompletaUrl", profilePhotoUrl);
            updates.put("fotoPerfilUrl", profilePhotoUrl);
        }

        FirebaseFirestore.getInstance()
                .collection("usuarios")
                .document(userId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    FirebaseDatabase.getInstance()
                            .getReference("usuarios")
                            .child(userId)
                            .updateChildren(updates)
                            .addOnSuccessListener(aVoid2 -> {
                                Toast.makeText(this, "Datos actualizados correctamente.", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Firestore actualizado, pero error en Realtime: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                            );
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error al actualizar Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}
