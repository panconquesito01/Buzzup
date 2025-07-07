package com.MagicDevelopers.buzzup.HOME.Fragments;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.MagicDevelopers.buzzup.Adaptadores.SuggestionsAdapter;
import com.MagicDevelopers.buzzup.Modelos.UserSuggestion;
import com.MagicDevelopers.buzzup.R;
import com.bumptech.glide.Glide;
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
import java.util.Random;

public class ProfileFragment extends Fragment {

    private ImageView profileImage;
    private TextView fullName, username, bio, ageCountry;
    private TextView postsCount, followersCount, followingCount;
    private RecyclerView recyclerSuggestions;
    private DatabaseReference userRef;

    private String currentUserId;

    // Nuevos elementos para mostrar/ocultar sugerencias
    private LinearLayout layoutSuggestionsHeader;
    private TextView tvShowSuggestions;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        profileImage = view.findViewById(R.id.profileImage);
        fullName = view.findViewById(R.id.fullName);
        username = view.findViewById(R.id.username);
        bio = view.findViewById(R.id.bio);
        ageCountry = view.findViewById(R.id.ageCountry);
        postsCount = view.findViewById(R.id.postsCount);
        followersCount = view.findViewById(R.id.followersCount);
        followingCount = view.findViewById(R.id.followingCount);
        ImageView backgroundImage = view.findViewById(R.id.backgroundImage);
        recyclerSuggestions = view.findViewById(R.id.recyclerSuggestions);

        layoutSuggestionsHeader = view.findViewById(R.id.layoutSuggestionsHeader);
        tvShowSuggestions = view.findViewById(R.id.tvShowSuggestions);

        view.findViewById(R.id.btnEditProfile).setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), com.MagicDevelopers.buzzup.HOME.Fragments.Editprofile.EditProfileActivity.class));
        });

        view.findViewById(R.id.btnSettings).setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), com.MagicDevelopers.buzzup.HOME.Fragments.Settings.SettingsActivity.class));
        });

        boolean isDarkMode = (getResources().getConfiguration().uiMode &
                Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;

        int endColor = ContextCompat.getColor(requireContext(),
                isDarkMode ? R.color.background_dark : R.color.background_light);

        GradientDrawable gradient = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{Color.TRANSPARENT, endColor}
        );
        backgroundImage.setForeground(gradient);

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        loadUserDataRealtime(backgroundImage);
        loadUserSuggestions();
        loadUserStatsRealtime();

        // Lógica para botón cerrar sugerencias
        view.findViewById(R.id.btnCloseSuggestions).setOnClickListener(v -> {
            layoutSuggestionsHeader.setVisibility(View.GONE);
            recyclerSuggestions.setVisibility(View.GONE);
            tvShowSuggestions.setVisibility(View.VISIBLE);
        });

        // Lógica para mostrar nuevamente
        tvShowSuggestions.setOnClickListener(v -> {
            layoutSuggestionsHeader.setVisibility(View.VISIBLE);
            recyclerSuggestions.setVisibility(View.VISIBLE);
            tvShowSuggestions.setVisibility(View.GONE);
        });

        return view;
    }

    private void loadUserDataRealtime(ImageView backgroundImage) {
        userRef = FirebaseDatabase.getInstance()
                .getReference("usuarios")
                .child(currentUserId);

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) return;

                String nombre = snapshot.child("nombre").getValue(String.class);
                String apellido = snapshot.child("apellido").getValue(String.class);
                String descripcion = snapshot.child("descripcion").getValue(String.class);
                String fechaNacimiento = snapshot.child("fechaNacimiento").getValue(String.class);
                String fotoUrl = snapshot.child("fotoPerfilUrl").getValue(String.class);
                String fondoUrl = snapshot.child("fotoFondoUrl").getValue(String.class);
                String nombreUsuario = snapshot.child("nombreUsuario").getValue(String.class);
                String pais = snapshot.child("pais").getValue(String.class);

                if (fondoUrl != null && !fondoUrl.isEmpty()) {
                    Glide.with(requireActivity())
                            .load(fondoUrl)
                            .placeholder(android.R.color.darker_gray)
                            .centerCrop()
                            .into(backgroundImage);
                } else {
                    backgroundImage.setImageResource(android.R.color.darker_gray);
                }

                StringBuilder nombreCompleto = new StringBuilder();
                if (nombre != null) nombreCompleto.append(nombre);
                if (apellido != null && !apellido.isEmpty()) {
                    if (nombreCompleto.length() > 0) nombreCompleto.append(" ");
                    nombreCompleto.append(apellido);
                }
                String nombreFinal = nombreCompleto.toString().trim().isEmpty() ? "Usuario" : nombreCompleto.toString().trim();
                fullName.setText(nombreFinal);

                if (nombreUsuario != null && !nombreUsuario.isEmpty()) {
                    username.setText("@" + nombreUsuario);
                } else {
                    String provisionalUsername = generarUsernameProvisional(nombre);
                    username.setText("@" + provisionalUsername);
                    userRef.child("nombreUsuario").setValue(provisionalUsername);
                }

                bio.setText(descripcion != null ? descripcion : "");

                String paisFinal = pais != null && !pais.isEmpty() ? pais : "País desconocido";
                if (fechaNacimiento != null && !fechaNacimiento.isEmpty()) {
                    int edad = calcularEdad(fechaNacimiento);
                    ageCountry.setText(edad + " años · " + paisFinal);
                } else {
                    ageCountry.setText(paisFinal);
                }

                Glide.with(requireActivity())
                        .load(fotoUrl)
                        .placeholder(R.drawable.ic_person_placeholder)
                        .circleCrop()
                        .into(profileImage);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(requireContext(), "Error al cargar datos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUserStatsRealtime() {
        DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference("posts").child(currentUserId);

        DatabaseReference followersRef = FirebaseDatabase.getInstance().getReference("Follow")
                .child(currentUserId).child("followers");

        DatabaseReference followingRef = FirebaseDatabase.getInstance().getReference("Follow")
                .child(currentUserId).child("following");

        // Publicaciones propias
        postsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long count = snapshot.getChildrenCount();
                postsCount.setText(String.valueOf(count));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // Seguidores = personas que me siguen a mí
        followersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long count = snapshot.getChildrenCount();
                followersCount.setText(String.valueOf(count));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // Siguiendo = personas a las que yo sigo
        followingRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long count = snapshot.getChildrenCount();
                followingCount.setText(String.valueOf(count));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
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

                SuggestionsAdapter adapter = new SuggestionsAdapter(requireContext(), suggestions, new SuggestionsAdapter.OnSuggestionClickListener() {
                    @Override
                    public void onProfileClick(UserSuggestion suggestion) {
                        Intent intent = new Intent(requireContext(), com.MagicDevelopers.buzzup.HOME.Fragments.PerfilUsuarioActivity.class);
                        intent.putExtra("userId", suggestion.getUserId());
                        startActivity(intent);
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

                recyclerSuggestions.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
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
                Toast.makeText(requireContext(), "Error al cargar sugerencias", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String generarUsernameProvisional(String nombre) {
        String base = (nombre != null && !nombre.isEmpty()) ? nombre.toLowerCase().replaceAll("\\s+", "") : "usuario";
        int randomNum = new Random().nextInt(900) + 100;
        return base + randomNum;
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
}
