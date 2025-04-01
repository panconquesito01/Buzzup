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

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;

public class Registro4Activity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int PERMISSION_REQUEST_CODE = 100;

    private CircleImageView ivProfile;
    private MaterialButton btnSeleccionarFoto, btnContinuarRegistro4, btnAhoraNo;
    private UsuarioController usuarioController;
    private String userId; // Puede ser nulo en un flujo acumulativo
    private Usuario usuario;
    private Uri originalImageUri = null;   // Imagen original completa
    private Uri croppedImageUri = null;      // Imagen recortada (miniatura)

    // Firebase
    private FirebaseStorage firebaseStorage;
    private FirebaseFirestore firebaseFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro4);

        // Inicializar vistas
        ivProfile = findViewById(R.id.ivProfile);
        btnSeleccionarFoto = findViewById(R.id.btnSeleccionarFoto);
        btnContinuarRegistro4 = findViewById(R.id.btnContinuarRegistro4);
        btnAhoraNo = findViewById(R.id.btnAhoraNo);  // Nuevo botón para "Ahora no"

        // Inicializar el controlador y Firebase
        usuarioController = new UsuarioController();
        firebaseStorage = FirebaseStorage.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

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

        // Al pulsar el botón "Ahora no", continuar sin seleccionar la imagen
        btnAhoraNo.setOnClickListener(v -> {
            // Continuar sin foto de perfil
            usuario.setFotoPerfilUrl("");  // Foto de perfil vacía
            usuario.setFotoPerfilCompletaUrl("");  // Foto de perfil completa vacía

            // Crear mapa con los datos a actualizar
            Map<String, Object> datosRegistro4 = new HashMap<>();
            datosRegistro4.put("fotoPerfilUrl", "");
            datosRegistro4.put("fotoPerfilCompletaUrl", "");

            // Si userId no es nulo, actualizamos en Firebase; de lo contrario, simplemente avanzamos
            if (userId != null) {
                actualizarUsuarioEnFirebase(datosRegistro4);
            } else {
                // Si userId es nulo, simplemente pasamos el objeto Usuario actualizado al siguiente paso
                Intent intent = new Intent(Registro4Activity.this, Registro5Activity.class);
                intent.putExtra("usuario", usuario);
                startActivity(intent);
            }
        });

        // Al continuar, comprobamos que se hayan seleccionado ambas URIs (original y recortada)
        btnContinuarRegistro4.setOnClickListener(v -> {
            if (originalImageUri == null || croppedImageUri == null) {
                Toast.makeText(Registro4Activity.this, "Debes seleccionar y recortar una imagen", Toast.LENGTH_SHORT).show();
                return;
            }

            // Subir la imagen a Firebase y obtener la URL
            uploadImageToFirebase(originalImageUri, croppedImageUri);
        });
    }

    private void uploadImageToFirebase(Uri originalImageUri, Uri croppedImageUri) {
        // Subir la imagen original a Firebase Storage
        StorageReference originalImageRef = firebaseStorage.getReference().child("profile_images").child(userId + "_original.jpg");
        originalImageRef.putFile(originalImageUri)
                .addOnSuccessListener(taskSnapshot -> originalImageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String originalImageUrl = uri.toString();

                    // Subir la imagen recortada a Firebase Storage
                    StorageReference croppedImageRef = firebaseStorage.getReference().child("profile_images").child(userId + "_cropped.jpg");
                    croppedImageRef.putFile(croppedImageUri)
                            .addOnSuccessListener(taskSnapshot2 -> croppedImageRef.getDownloadUrl().addOnSuccessListener(uri2 -> {
                                String croppedImageUrl = uri2.toString();

                                // Agregar emisorId y receptorId al mapa de datos
                                String emisorId = userId;  // Asignamos emisorId al usuario actual
                                String receptorId = "otro_usuario_id";  // Aquí puedes obtener el receptorId según tu lógica

                                // Actualizar el objeto Usuario con las URLs y los IDs
                                usuario.setFotoPerfilUrl(croppedImageUrl);
                                usuario.setFotoPerfilCompletaUrl(originalImageUrl);
                                usuario.setEmisorId(emisorId);
                                usuario.setReceptorId(receptorId);

                                // Crear mapa con los datos a actualizar
                                Map<String, Object> datosRegistro4 = new HashMap<>();
                                datosRegistro4.put("fotoPerfilUrl", croppedImageUrl);
                                datosRegistro4.put("fotoPerfilCompletaUrl", originalImageUrl);
                                datosRegistro4.put("emisorId", emisorId);
                                datosRegistro4.put("receptorId", receptorId);

                                // Actualizar los datos del usuario en Firestore
                                if (userId != null) {
                                    actualizarUsuarioEnFirebase(datosRegistro4);
                                }
                            }))
                            .addOnFailureListener(e -> Toast.makeText(Registro4Activity.this, "Error al subir la imagen recortada", Toast.LENGTH_SHORT).show());
                }))
                .addOnFailureListener(e -> Toast.makeText(Registro4Activity.this, "Error al subir la imagen original", Toast.LENGTH_SHORT).show());
    }

    private void actualizarUsuarioEnFirebase(Map<String, Object> datosRegistro4) {
        // Actualizamos los datos en Firestore
        DocumentReference usuarioRef = firebaseFirestore.collection("usuarios").document(userId);
        usuarioRef.update(datosRegistro4)
                .addOnSuccessListener(aVoid -> {
                    Intent intent = new Intent(Registro4Activity.this, Registro5Activity.class);
                    intent.putExtra("userId", userId);
                    intent.putExtra("usuario", usuario);
                    startActivity(intent);
                })
                .addOnFailureListener(e -> Toast.makeText(Registro4Activity.this, "Error al actualizar los datos en Firebase", Toast.LENGTH_SHORT).show());
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
            // Obtener URI de la imagen seleccionada
            Uri imageUri = data.getData();
            if (imageUri != null) {
                // Recortar la imagen
                UCrop.of(imageUri, Uri.fromFile(new File(getCacheDir(), "cropped_image.jpg")))
                        .withAspectRatio(1, 1)
                        .start(Registro4Activity.this);
            }
        }

        if (requestCode == UCrop.REQUEST_CROP && resultCode == RESULT_OK && data != null) {
            // Obtener URI de la imagen recortada
            Uri resultUri = UCrop.getOutput(data);
            if (resultUri != null) {
                croppedImageUri = resultUri;
                ivProfile.setImageURI(croppedImageUri);
            }
        }
    }
}
