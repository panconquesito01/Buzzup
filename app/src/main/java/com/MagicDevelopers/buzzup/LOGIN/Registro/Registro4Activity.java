package com.MagicDevelopers.buzzup.LOGIN.Registro;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import com.yalantis.ucrop.UCrop;
import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class Registro4Activity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int PERMISSION_REQUEST_CODE = 100;

    private CircleImageView ivProfile;
    private MaterialButton btnSeleccionarFoto, btnContinuarRegistro4;
    private UsuarioController usuarioController;
    private String userId; // Puede ser nulo en un flujo acumulativo
    private Usuario usuario;
    private Uri originalImageUri = null;   // Imagen original completa
    private Uri croppedImageUri = null;      // Imagen recortada (miniatura)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro4);

        // Inicializar vistas
        ivProfile = findViewById(R.id.ivProfile);
        btnSeleccionarFoto = findViewById(R.id.btnSeleccionarFoto);
        btnContinuarRegistro4 = findViewById(R.id.btnContinuarRegistro4);

        // Inicializar el controlador
        usuarioController = new UsuarioController();

        // Recibir el objeto Usuario y userId (puede ser null) desde la actividad anterior
        userId = getIntent().getStringExtra("userId");
        usuario = (Usuario) getIntent().getSerializableExtra("usuario");

        // Al pulsar el botón para seleccionar imagen, se solicita el permiso (según versión)
        btnSeleccionarFoto.setOnClickListener(v -> {
            String permissionToRequest;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissionToRequest = Manifest.permission.READ_MEDIA_IMAGES;
            } else {
                permissionToRequest = Manifest.permission.READ_EXTERNAL_STORAGE;
            }
            if (ContextCompat.checkSelfPermission(Registro4Activity.this, permissionToRequest)
                    != PackageManager.PERMISSION_GRANTED) {
                // Mostrar el diálogo de permisos
                ActivityCompat.requestPermissions(Registro4Activity.this,
                        new String[]{permissionToRequest},
                        PERMISSION_REQUEST_CODE);
            } else {
                openImagePicker();
            }
        });

        // Al continuar, comprobamos que se hayan seleccionado ambas URIs (original y recortada)
        btnContinuarRegistro4.setOnClickListener(v -> {
            if (originalImageUri == null || croppedImageUri == null) {
                Toast.makeText(Registro4Activity.this, "Debes seleccionar y recortar una imagen", Toast.LENGTH_SHORT).show();
                return;
            }

            // Actualizar el objeto Usuario con ambas URIs
            usuario.setFotoPerfilUrl(croppedImageUri.toString());
            usuario.setFotoPerfilCompletaUrl(originalImageUri.toString());

            // Crear mapa con los datos a actualizar
            Map<String, Object> datosRegistro4 = new HashMap<>();
            datosRegistro4.put("fotoPerfilUrl", croppedImageUri.toString());
            datosRegistro4.put("fotoPerfilCompletaUrl", originalImageUri.toString());

            // Si userId no es nulo, actualizamos en Firebase; de lo contrario, simplemente avanzamos
            if (userId != null) {
                usuarioController.actualizarUsuario(userId, datosRegistro4, exito -> {
                    if (exito) {
                        Intent intent = new Intent(Registro4Activity.this, Registro5Activity.class);
                        intent.putExtra("userId", userId);
                        intent.putExtra("usuario", usuario);
                        startActivity(intent);
                    } else {
                        Toast.makeText(Registro4Activity.this, "Error al actualizar la foto de perfil", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                // Si userId es nulo, simplemente pasamos el objeto Usuario actualizado al siguiente paso
                Intent intent = new Intent(Registro4Activity.this, Registro5Activity.class);
                intent.putExtra("usuario", usuario);
                startActivity(intent);
            }
        });
    }

    // Método para abrir el selector de imágenes
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Selecciona una imagen"), PICK_IMAGE_REQUEST);
    }

    // Manejo de la respuesta de la solicitud de permisos
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openImagePicker();
            } else {
                Toast.makeText(this, "El permiso es necesario para seleccionar imágenes", Toast.LENGTH_SHORT).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    // Manejo de onActivityResult para la selección y recorte de la imagen
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            // Guardar la URI original de la imagen seleccionada
            originalImageUri = data.getData();
            // Crear una URI destino para la imagen recortada en el directorio de caché
            Uri destinationUri = Uri.fromFile(new File(getCacheDir(), "cropped_" + System.currentTimeMillis() + ".jpg"));
            // Iniciar UCrop para recortar la imagen (aspecto 1:1, tamaño máximo 500x500)
            UCrop.of(originalImageUri, destinationUri)
                    .withAspectRatio(1, 1)
                    .withMaxResultSize(500, 500)
                    .start(Registro4Activity.this);
        } else if (requestCode == UCrop.REQUEST_CROP && resultCode == RESULT_OK) {
            // Obtener la imagen recortada de UCrop
            Uri resultUri = UCrop.getOutput(data);
            if (resultUri != null) {
                croppedImageUri = resultUri;
                // Mostrar la imagen recortada en el CircleImageView
                ivProfile.setImageURI(croppedImageUri);
            }
        } else if (requestCode == UCrop.REQUEST_CROP && resultCode == UCrop.RESULT_ERROR) {
            final Throwable cropError = UCrop.getError(data);
            Toast.makeText(this, "Error en el recorte: " + cropError.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
