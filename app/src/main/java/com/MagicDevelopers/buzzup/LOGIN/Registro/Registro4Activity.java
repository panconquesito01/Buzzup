package com.MagicDevelopers.buzzup.LOGIN.Registro;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.MagicDevelopers.buzzup.Modelos.Usuario;
import com.MagicDevelopers.buzzup.R;
import com.google.android.material.button.MaterialButton;
import com.yalantis.ucrop.UCrop;

import java.io.File;

import de.hdodenhof.circleimageview.CircleImageView;

public class Registro4Activity extends AppCompatActivity {

    private CircleImageView ivProfile;
    private MaterialButton btnSeleccionarFoto, btnContinuar, btnAhoraNo;

    private Usuario usuario;
    private Uri uriCortada;

    private final ActivityResultLauncher<String> permisoLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) abrirGaleria();
                else Toast.makeText(this, "Permiso necesario para escoger imágenes", Toast.LENGTH_SHORT).show();
            });

    private final ActivityResultLauncher<Intent> galeriaLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uriOriginal = result.getData().getData();
                    if (uriOriginal != null) {
                        File destino = new File(getCacheDir(), "cropped_" + System.currentTimeMillis() + ".jpg");
                        UCrop.of(uriOriginal, Uri.fromFile(destino))
                                .withAspectRatio(1, 1)
                                .start(this);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro4);

        ivProfile          = findViewById(R.id.ivProfile);
        btnSeleccionarFoto = findViewById(R.id.btnSeleccionarFoto);
        btnContinuar       = findViewById(R.id.btnContinuarRegistro4);
        btnAhoraNo         = findViewById(R.id.btnAhoraNo);

        usuario = (Usuario) getIntent().getSerializableExtra("usuario");

        if (usuario == null) {
            Toast.makeText(this, "Error crítico: Usuario no válido.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        btnSeleccionarFoto.setOnClickListener(v -> pedirPermisoYAbrirGaleria());

        btnContinuar.setOnClickListener(v -> {
            if (uriCortada == null) {
                Toast.makeText(this, "Selecciona y recorta una imagen", Toast.LENGTH_SHORT).show();
                return;
            }

            File file = new File(uriCortada.getPath());
            if (!file.exists()) {
                Toast.makeText(this, "La imagen recortada no existe. Vuelve a seleccionarla.", Toast.LENGTH_LONG).show();
                return;
            }

            // Aquí simplemente guardamos la URI local para que en Registro5 se suba
            usuario.setFotoPerfilUrl(uriCortada.toString());

            Intent i = new Intent(this, Registro5Activity.class);
            i.putExtra("usuario", usuario);
            startActivity(i);
            finish();
        });

        btnAhoraNo.setOnClickListener(v -> {
            // Si no quiere subir imagen, simplemente pasamos a Registro5
            usuario.setFotoPerfilUrl("");
            usuario.setFotoPerfilCompletaUrl("");

            Intent i = new Intent(this, Registro5Activity.class);
            i.putExtra("usuario", usuario);
            startActivity(i);
            finish();
        });
    }

    private void pedirPermisoYAbrirGaleria() {
        String permiso = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                ? Manifest.permission.READ_MEDIA_IMAGES
                : Manifest.permission.READ_EXTERNAL_STORAGE;

        if (ContextCompat.checkSelfPermission(this, permiso) == PackageManager.PERMISSION_GRANTED) {
            abrirGaleria();
        } else {
            permisoLauncher.launch(permiso);
        }
    }

    private void abrirGaleria() {
        Intent pick = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pick.setType("image/*");
        galeriaLauncher.launch(Intent.createChooser(pick, "Selecciona una imagen"));
    }

    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);

        if (req == UCrop.REQUEST_CROP && res == RESULT_OK && data != null) {
            uriCortada = UCrop.getOutput(data);
            if (uriCortada != null) {
                File f = new File(uriCortada.getPath());
                Log.d("Registro4Activity", "Archivo recortado: " + f.getAbsolutePath() + " existe? " + f.exists());
                ivProfile.setImageURI(uriCortada);
            }
        }
    }
}
