package com.MagicDevelopers.buzzup.HOME.Fragments;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.MagicDevelopers.buzzup.Adaptadores.SuggestionsAdapter;
import com.MagicDevelopers.buzzup.Modelos.UserSuggestion;
import com.MagicDevelopers.buzzup.R;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class PerfilUsuarioActivity extends AppCompatActivity {

    private ImageView profileImage, backgroundImage;
    private TextView fullName, username, bio, ageCountry;
    private TextView followersCountTextView, followingCountTextView, postsCountTextView;
    private MaterialButton btnFollow;
    private ImageButton btnBack, btnSettings;
    private RecyclerView recyclerSuggestions;
    private TextView tvShowSuggestions;
    private View layoutSuggestionsHeader;

    private String viewedUserId;
    private String currentUserId;

    private DatabaseReference userRef;
    private DatabaseReference followRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil_usuario);

        // Recibir userId del Intent o si es null, mostrar perfil propio
        viewedUserId = getIntent().getStringExtra("userId");
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if (viewedUserId == null) {
            // Mostrar perfil propio
            viewedUserId = currentUserId;
        }

        // Referencias UI
        backgroundImage = findViewById(R.id.backgroundImage);
        profileImage = findViewById(R.id.profileImage);
        fullName = findViewById(R.id.fullName);
        username = findViewById(R.id.username);
        bio = findViewById(R.id.bio);
        ageCountry = findViewById(R.id.ageCountry);
        btnFollow = findViewById(R.id.btnFollow);
        btnBack = findViewById(R.id.btnBack);
        btnSettings = findViewById(R.id.btnSettings);
        followersCountTextView = findViewById(R.id.followersCount);
        followingCountTextView = findViewById(R.id.followingCount);
        postsCountTextView = findViewById(R.id.postsCount);
        recyclerSuggestions = findViewById(R.id.recyclerSuggestions);
        tvShowSuggestions = findViewById(R.id.tvShowSuggestions);
        layoutSuggestionsHeader = findViewById(R.id.layoutSuggestionsHeader);

        // Botón atrás
        btnBack.setOnClickListener(v -> finish());

        // Botón opciones (bloquear, reportar, restringir)
        btnSettings.setOnClickListener(v -> showOptionsMenu(v));

        // Gradiente superior para fondo
        boolean isDarkMode = (getResources().getConfiguration().uiMode &
                Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        int endColor = ContextCompat.getColor(this,
                isDarkMode ? R.color.background_dark : R.color.background_light);
        GradientDrawable gradient = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{Color.TRANSPARENT, endColor}
        );
        backgroundImage.setForeground(gradient);

        userRef = FirebaseDatabase.getInstance().getReference("usuarios").child(viewedUserId);
        followRef = FirebaseDatabase.getInstance().getReference("Follow");

        loadUserData();
        setupFollowButton();
        loadFollowStats();
        loadPostsCount();
        loadUserSuggestions();

        // Botón cerrar sugerencias (X)
        ImageButton btnCloseSuggestions = findViewById(R.id.btnCloseSuggestions);
        btnCloseSuggestions.setOnClickListener(v -> {
            layoutSuggestionsHeader.setVisibility(View.GONE);
            recyclerSuggestions.setVisibility(View.GONE);
            tvShowSuggestions.setVisibility(View.VISIBLE);
        });

        // Texto mostrar nuevamente
        tvShowSuggestions.setOnClickListener(v -> {
            layoutSuggestionsHeader.setVisibility(View.VISIBLE);
            recyclerSuggestions.setVisibility(View.VISIBLE);
            tvShowSuggestions.setVisibility(View.GONE);
        });
    }

    private void loadUserData() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) return;

                String nombre = snapshot.child("nombre").getValue(String.class);
                String apellido = snapshot.child("apellido").getValue(String.class);
                String descripcion = snapshot.child("descripcion").getValue(String.class);
                String fechaNacimiento = snapshot.child("fechaNacimiento").getValue(String.class);
                String fotoPerfilUrl = snapshot.child("fotoPerfilUrl").getValue(String.class);
                String fondoUrl = snapshot.child("fotoFondoUrl").getValue(String.class);
                String nombreUsuario = snapshot.child("nombreUsuario").getValue(String.class);
                String pais = snapshot.child("pais").getValue(String.class);

                String nombreCompleto = "";
                if (nombre != null) nombreCompleto += nombre;
                if (apellido != null && !apellido.isEmpty()) {
                    nombreCompleto += " " + apellido;
                }
                fullName.setText(nombreCompleto.trim().isEmpty() ? "Usuario" : nombreCompleto.trim());

                username.setText(nombreUsuario != null ? "@" + nombreUsuario : "@usuario");

                bio.setText(descripcion != null ? descripcion : "");

                String paisFinal = pais != null ? pais : "País desconocido";
                if (fechaNacimiento != null && !fechaNacimiento.isEmpty()) {
                    int edad = calcularEdad(fechaNacimiento);
                    ageCountry.setText(edad + " años · " + paisFinal);
                } else {
                    ageCountry.setText(paisFinal);
                }

                Glide.with(PerfilUsuarioActivity.this)
                        .load(fotoPerfilUrl)
                        .placeholder(R.drawable.ic_person_placeholder)
                        .circleCrop()
                        .into(profileImage);

                if (fondoUrl != null && !fondoUrl.isEmpty()) {
                    Glide.with(PerfilUsuarioActivity.this)
                            .load(fondoUrl)
                            .placeholder(android.R.color.darker_gray)
                            .centerCrop()
                            .into(backgroundImage);
                } else {
                    backgroundImage.setImageResource(android.R.color.darker_gray);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void setupFollowButton() {
        if (viewedUserId.equals(currentUserId)) {
            btnFollow.setVisibility(View.GONE); // No te sigues a ti mismo
            return;
        }

        // Escuchar en tiempo real si currentUser sigue viewedUser
        followRef.child(currentUserId).child("following").child(viewedUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        boolean isFollowing = snapshot.exists();
                        updateFollowButton(isFollowing);

                        btnFollow.setOnClickListener(v -> {
                            if (isFollowing) {
                                unfollowUser();
                            } else {
                                followUser();
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void followUser() {
        followRef.child(viewedUserId).child("followers").child(currentUserId).setValue(true);
        followRef.child(currentUserId).child("following").child(viewedUserId).setValue(true);
        Toast.makeText(this, "Ahora sigues a este usuario", Toast.LENGTH_SHORT).show();
    }

    private void unfollowUser() {
        followRef.child(viewedUserId).child("followers").child(currentUserId).removeValue();
        followRef.child(currentUserId).child("following").child(viewedUserId).removeValue();
        Toast.makeText(this, "Has dejado de seguir a este usuario", Toast.LENGTH_SHORT).show();
    }

    private void loadFollowStats() {
        // Seguidores
        followRef.child(viewedUserId).child("followers").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long followersCount = snapshot.getChildrenCount();
                followersCountTextView.setText(String.valueOf(followersCount));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // Siguiendo
        followRef.child(viewedUserId).child("following").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long followingCount = snapshot.getChildrenCount();
                followingCountTextView.setText(String.valueOf(followingCount));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void loadPostsCount() {
        DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference("posts").child(viewedUserId);
        postsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long postsCount = snapshot.getChildrenCount();
                postsCountTextView.setText(String.valueOf(postsCount));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void updateFollowButton(boolean isFollowing) {
        boolean isDarkMode = (getResources().getConfiguration().uiMode &
                Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;

        btnFollow.setText(isFollowing ? "Siguiendo" : "Seguir");

        if (isDarkMode) {
            btnFollow.setTextColor(ContextCompat.getColor(this, android.R.color.black));
            btnFollow.setBackgroundTintList(ContextCompat.getColorStateList(this, android.R.color.transparent));
        } else {
            btnFollow.setTextColor(ContextCompat.getColor(this, android.R.color.white));
            btnFollow.setBackgroundTintList(ContextCompat.getColorStateList(this, android.R.color.transparent));
        }
    }

    private int calcularEdad(String fechaNacimiento) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date fechaNac = sdf.parse(fechaNacimiento);
            long diffMillis = System.currentTimeMillis() - fechaNac.getTime();
            return (int) (diffMillis / (1000L * 60 * 60 * 24 * 365));
        } catch (Exception e) {
            return 0;
        }
    }

    private void showOptionsMenu(View anchor) {
        PopupMenu popupMenu = new PopupMenu(this, anchor);
        MenuInflater inflater = popupMenu.getMenuInflater();
        inflater.inflate(R.menu.menu_profile_options, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_block) {
                Toast.makeText(this, "Usuario bloqueado", Toast.LENGTH_SHORT).show();
                return true;
            } else if (id == R.id.action_report) {
                Toast.makeText(this, "Usuario reportado", Toast.LENGTH_SHORT).show();
                return true;
            } else if (id == R.id.action_restrict) {
                Toast.makeText(this, "Usuario restringido", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });

        popupMenu.show();
    }

    private void loadUserSuggestions() {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("usuarios");
        DatabaseReference followRef = FirebaseDatabase.getInstance().getReference("Follow");

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<UserSuggestion> suggestions = new ArrayList<>();

                for (DataSnapshot child : snapshot.getChildren()) {
                    String userId = child.getKey();
                    if (userId.equals(currentUserId)) continue;

                    String nombre = child.child("nombre").getValue(String.class);
                    String apellido = child.child("apellido").getValue(String.class);
                    String nombreUsuario = child.child("nombreUsuario").getValue(String.class);
                    String fotoPerfilUrl = child.child("fotoPerfilUrl").getValue(String.class);

                    String fullName = (nombre != null ? nombre : "") + (apellido != null ? " " + apellido : "");
                    String usernameFinal = (nombreUsuario != null && !nombreUsuario.isEmpty()) ? nombreUsuario : "usuario";

                    suggestions.add(new UserSuggestion(
                            userId,
                            fullName.trim(),
                            usernameFinal,
                            fotoPerfilUrl != null ? fotoPerfilUrl : "",
                            false  // inicialmente no sabemos si sigue, lo actualizaremos luego
                    ));
                }

                SuggestionsAdapter adapter = new SuggestionsAdapter(PerfilUsuarioActivity.this, suggestions, new SuggestionsAdapter.OnSuggestionClickListener() {
                    @Override
                    public void onProfileClick(UserSuggestion suggestion) {
                        // Abrir el perfil de usuario sugerido
                        if (!suggestion.getUserId().equals(viewedUserId)) {
                            Intent intent = new Intent(PerfilUsuarioActivity.this, PerfilUsuarioActivity.class);
                            intent.putExtra("userId", suggestion.getUserId());
                            startActivity(intent);
                        }
                    }

                    @Override
                    public void onFollowClick(UserSuggestion suggestion, boolean isFollowing) {
                        String targetUserId = suggestion.getUserId();

                        if (isFollowing) {
                            // Dejar de seguir
                            followRef.child(currentUserId).child("following").child(targetUserId).removeValue();
                            followRef.child(targetUserId).child("followers").child(currentUserId).removeValue();
                        } else {
                            // Seguir
                            followRef.child(currentUserId).child("following").child(targetUserId).setValue(true);
                            followRef.child(targetUserId).child("followers").child(currentUserId).setValue(true);
                        }
                    }
                });

                recyclerSuggestions.setLayoutManager(new LinearLayoutManager(PerfilUsuarioActivity.this, LinearLayoutManager.HORIZONTAL, false));
                recyclerSuggestions.setAdapter(adapter);

                // Después de cargar la lista, actualizar el estado "isFollowing" de cada sugerencia
                followRef.child(currentUserId).child("following").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (UserSuggestion suggestion : suggestions) {
                            boolean sigue = snapshot.hasChild(suggestion.getUserId());
                            suggestion.setFollowing(sigue);
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PerfilUsuarioActivity.this, "Error al cargar sugerencias", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
