package com.MagicDevelopers.buzzup.HOME.Fragments.Upload;

import android.Manifest;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.MagicDevelopers.buzzup.Adapters.GalleryAdapter;
import com.MagicDevelopers.buzzup.Modelos.Upload.MediaModel;
import com.MagicDevelopers.buzzup.R;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class UploadActivity extends AppCompatActivity
        implements AlbumBottomSheetDialogFragment.OnAlbumSelectedListener {

    private static final int REQUEST_PERMISSION = 123;

    private ImageView imagePreview;
    private TextView textCounter;
    private TabLayout tabs;
    private FrameLayout folderSelector;
    private TextView textFolderLabel;
    private RecyclerView recyclerGallery;

    private final List<MediaModel> mediaList = new ArrayList<>();
    private final List<MediaModel> selectedList = new ArrayList<>();
    private GalleryAdapter adapter;

    // bucketName ("" == Recientes) → album info
    private final Map<String, AlbumBottomSheetDialogFragment.AlbumInfo> albumMap = new LinkedHashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        // --- Toolbar personalizado ---
        View customToolbar = findViewById(R.id.custom_toolbar);
        ImageButton btnClose     = customToolbar.findViewById(R.id.btnClose);
        MaterialButton btnNext   = customToolbar.findViewById(R.id.btnNext);

        btnClose.setOnClickListener(v -> finish());
        btnNext .setOnClickListener(v -> {
            if (selectedList.isEmpty()) {
                Toast.makeText(this, "Selecciona al menos uno", Toast.LENGTH_SHORT).show();
            } else {
                ArrayList<String> uris = new ArrayList<>();
                for (MediaModel m : selectedList) uris.add(m.getUri());
                startActivity(new Intent(this, EditPostActivity.class)
                        .putStringArrayListExtra("mediaUris", uris));
            }
        });

        // --- Resto de vistas ---
        imagePreview    = findViewById(R.id.imagePreview);
        textCounter     = findViewById(R.id.textCounter);
        tabs            = findViewById(R.id.tabLayoutModes);
        folderSelector  = findViewById(R.id.folderSelector);
        textFolderLabel = findViewById(R.id.textFolderLabel);
        recyclerGallery = findViewById(R.id.recyclerGallery);

        // Configurar pestañas
        tabs.addTab(tabs.newTab().setText("Publicación"));
        tabs.addTab(tabs.newTab().setText("Historia"));
        tabs.addTab(tabs.newTab().setText("Reel"));

        // Configurar RecyclerView
        recyclerGallery.setLayoutManager(new GridLayoutManager(this, 3));
        adapter = new GalleryAdapter(mediaList, item -> {
            if (selectedList.contains(item)) selectedList.remove(item);
            else selectedList.add(item);
            updateCounter();
            if (selectedList.size() == 1) {
                Glide.with(this)
                        .load(selectedList.get(0).getUri())
                        .into(imagePreview);
            } else {
                imagePreview.setImageDrawable(null);
            }
        });
        recyclerGallery.setAdapter(adapter);

        // Al hacer clic en el selector de carpeta
        folderSelector.setOnClickListener(v -> {
            new AlbumBottomSheetDialogFragment(albumMap)
                    .show(getSupportFragmentManager(), "albums");
        });

        // Permisos y carga inicial
        checkPermissions();
    }

    private void updateCounter() {
        textCounter.setText("Seleccionados: " + selectedList.size());
    }

    private void checkPermissions() {
        String[] perms = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                ? new String[]{Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO}
                : new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};

        boolean granted = true;
        for (String p : perms) {
            if (checkSelfPermission(p) != PackageManager.PERMISSION_GRANTED) {
                granted = false;
                break;
            }
        }
        if (!granted) {
            requestPermissions(perms, REQUEST_PERMISSION);
        } else {
            buildAlbumsAndLoad("");
        }
    }

    private void buildAlbumsAndLoad(String filterBucket) {
        mediaList.clear();
        albumMap.clear();

        // 1) Agregar todos los álbumes
        String[] proj = {
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.MEDIA_TYPE,
                MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME
        };
        String sel = MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE + " OR "
                + MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;

        Cursor cursor = getContentResolver().query(
                MediaStore.Files.getContentUri("external"),
                proj, sel, null,
                MediaStore.Files.FileColumns.DATE_ADDED + " DESC"
        );

        Map<String, AlbumBottomSheetDialogFragment.AlbumInfo> temp = new LinkedHashMap<>();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                long id       = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID));
                int type      = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MEDIA_TYPE));
                String bucket = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME));
                Uri uri = ContentUris.withAppendedId(
                        type == MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
                                ? MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                                : MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        id
                );
                AlbumBottomSheetDialogFragment.AlbumInfo ai = temp.get(bucket);
                if (ai == null) {
                    ai = new AlbumBottomSheetDialogFragment.AlbumInfo(bucket, uri.toString(), 1);
                    temp.put(bucket, ai);
                } else {
                    ai.count++;
                }
            }
            cursor.close();
        }

        // 2) “Recientes” al inicio
        int total = temp.values().stream().mapToInt(a -> a.count).sum();
        String cover = temp.values().iterator().next().coverUri;
        albumMap.put("", new AlbumBottomSheetDialogFragment.AlbumInfo("", cover, total));
        albumMap.putAll(temp);

        // 3) Cargar solo el bucket filtrado
        for (Map.Entry<String, AlbumBottomSheetDialogFragment.AlbumInfo> e : albumMap.entrySet()) {
            if (filterBucket.equals(e.getKey())) {
                String[] p2 = { MediaStore.Files.FileColumns._ID, MediaStore.Files.FileColumns.MEDIA_TYPE };
                String sel2 = "(" + sel + ")";
                String[] args = null;
                if (!e.getKey().isEmpty()) {
                    sel2 += " AND " + MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME + "=?";
                    args = new String[]{ e.getKey() };
                }
                Cursor c2 = getContentResolver().query(
                        MediaStore.Files.getContentUri("external"),
                        p2, sel2, args,
                        MediaStore.Files.FileColumns.DATE_ADDED + " DESC"
                );
                if (c2 != null) {
                    while (c2.moveToNext()) {
                        long id2  = c2.getLong(c2.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID));
                        int t2    = c2.getInt(c2.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MEDIA_TYPE));
                        Uri u2 = ContentUris.withAppendedId(
                                t2 == MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
                                        ? MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                                        : MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                                id2
                        );
                        mediaList.add(new MediaModel(u2.toString(),
                                t2 == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO));
                    }
                    c2.close();
                }
                break;
            }
        }

        // 4) Actualizar UI
        textFolderLabel.setText(
                filterBucket.isEmpty() ? "Imágenes recientes" : filterBucket
        );
        updateCounter();
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onAlbumSelected(String bucketName) {
        buildAlbumsAndLoad(bucketName == null ? "" : bucketName);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION) {
            checkPermissions();
        }
    }
}
