package com.MagicDevelopers.buzzup.LOGIN.Registro;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.MagicDevelopers.buzzup.Controlladores.UsuarioController;
import com.MagicDevelopers.buzzup.Modelos.Usuario;
import com.MagicDevelopers.buzzup.R;
import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;

import ja.burhanrashid52.photoeditor.PhotoEditor;
import ja.burhanrashid52.photoeditor.PhotoEditorView;
import ja.burhanrashid52.photoeditor.SaveSettings;

public class Registro5Activity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 10;
    private static final int PERMISSION_REQUEST_CODE = 101;

    private android.widget.ImageView ivOption1, ivOption2, ivOption3;
    private PhotoEditorView photoEditorView;
    private MaterialButton btnTerminarEdicion, btnContinuarRegistro5;

    private PhotoEditor photoEditor;
    private UsuarioController usuarioController;
    private Usuario usuario;
    private String userId;

    private int selectedOption = -1;
    private Uri selectedPublicationImageUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro5);

        ivOption1 = findViewById(R.id.ivOption1);
        ivOption2 = findViewById(R.id.ivOption2);
        ivOption3 = findViewById(R.id.ivOption3);
        photoEditorView = findViewById(R.id.photoEditorView);
        btnTerminarEdicion = findViewById(R.id.btnTerminarEdicion);
        btnContinuarRegistro5 = findViewById(R.id.btnContinuarRegistro5);

        photoEditor = new PhotoEditor.Builder(this, photoEditorView).build();

        photoEditorView.setVisibility(android.view.View.GONE);
        btnTerminarEdicion.setVisibility(android.view.View.GONE);

        usuarioController = new UsuarioController();
        userId = getIntent().getStringExtra("userId");
        usuario = (Usuario) getIntent().getSerializableExtra("usuario");

        ivOption1.setOnClickListener(v -> {
            selectedOption = 1;
            requestImagePermissionAndOpenPicker();
        });
        ivOption2.setOnClickListener(v -> {
            selectedOption = 2;
            requestImagePermissionAndOpenPicker();
        });
        ivOption3.setOnClickListener(v -> {
            selectedOption = 3;
            requestImagePermissionAndOpenPicker();
        });

        btnTerminarEdicion.setOnClickListener(v -> {
            try {
                File file = new File(getCacheDir(), String.format(Locale.getDefault(), "edited_%d.jpg", System.currentTimeMillis()));
                SaveSettings saveSettings = new SaveSettings.Builder().setClearViewsEnabled(true).setTransparencyEnabled(true).build();

                photoEditor.saveAsFile(file.getAbsolutePath(), saveSettings, new PhotoEditor.OnSaveListener() {
                    @Override
                    public void onSuccess(@NonNull String imagePath) {
                        selectedPublicationImageUri = Uri.fromFile(new File(imagePath));

                        if (selectedOption == 1) {
                            ivOption1.setImageURI(selectedPublicationImageUri);
                        } else if (selectedOption == 2) {
                            ivOption2.setImageURI(selectedPublicationImageUri);
                        } else if (selectedOption == 3) {
                            ivOption3.setImageURI(selectedPublicationImageUri);
                        }

                        photoEditorView.setVisibility(android.view.View.GONE);
                        btnTerminarEdicion.setVisibility(android.view.View.GONE);

                        Toast.makeText(Registro5Activity.this, "Edición finalizada", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Registro5Activity.this, "Error al guardar la imagen editada", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(Registro5Activity.this, "Error inesperado al procesar la imagen", Toast.LENGTH_SHORT).show();
            }
        });

        btnContinuarRegistro5.setOnClickListener(v -> {
            if (selectedPublicationImageUri == null) {
                Toast.makeText(Registro5Activity.this, "Debes seleccionar y editar una imagen", Toast.LENGTH_SHORT).show();
                return;
            }

            usuario.setFotoPublicacionUrl(selectedPublicationImageUri.toString());
            if (userId != null) {
                usuarioController.guardarPublicacion(userId, selectedPublicationImageUri.toString(), exito -> {
                    if (exito) {
                        Intent intent = new Intent(Registro5Activity.this, Registro6Activity.class);
                        intent.putExtra("userId", userId);
                        intent.putExtra("usuario", usuario);
                        startActivity(intent);
                    } else {
                        Toast.makeText(Registro5Activity.this, "Error al guardar la publicación", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Intent intent = new Intent(Registro5Activity.this, Registro6Activity.class);
                intent.putExtra("usuario", usuario);
                startActivity(intent);
            }
        });
    }

    private void requestImagePermissionAndOpenPicker() {
        String permission;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permission = Manifest.permission.READ_MEDIA_IMAGES;
        } else {
            permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        }
        if (ContextCompat.checkSelfPermission(Registro5Activity.this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(Registro5Activity.this, new String[]{permission}, PERMISSION_REQUEST_CODE);
        } else {
            openImagePicker();
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Selecciona una imagen"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openImagePicker();
            } else {
                Toast.makeText(this, "El permiso es necesario para seleccionar imágenes", Toast.LENGTH_SHORT).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            photoEditorView.setVisibility(android.view.View.VISIBLE);
            btnTerminarEdicion.setVisibility(android.view.View.VISIBLE);
            photoEditorView.getSource().setImageURI(imageUri);
        }
    }
}
