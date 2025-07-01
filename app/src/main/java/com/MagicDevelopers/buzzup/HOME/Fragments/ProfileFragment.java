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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.MagicDevelopers.buzzup.R;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ProfileFragment extends Fragment {

    private ImageView profileImage;
    private TextView fullName, username, bio, ageCountry;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Referencias UI
        profileImage = view.findViewById(R.id.profileImage);
        fullName = view.findViewById(R.id.fullName);
        username = view.findViewById(R.id.username);
        bio = view.findViewById(R.id.bio);
        ageCountry = view.findViewById(R.id.ageCountry);
        ImageView backgroundImage = view.findViewById(R.id.backgroundImage);

        view.findViewById(R.id.btnEditProfile).setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), com.MagicDevelopers.buzzup.HOME.Fragments.Editprofile.EditProfileActivity.class));
        });

        // Detectar si el usuario tiene modo oscuro activado
        boolean isDarkMode = (getResources().getConfiguration().uiMode &
                Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;

        // Escoger el color de fondo adecuado
        int endColor = ContextCompat.getColor(requireContext(),
                isDarkMode ? R.color.background_dark : R.color.background_light);

        // Crear un degradado dinámico
        GradientDrawable gradient = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{
                        Color.TRANSPARENT,
                        endColor
                });

        // Asignar el degradado como foreground (encima de la imagen)
        backgroundImage.setForeground(gradient);

        // Obtener el UID del usuario actual
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Referencia al nodo del usuario en Firebase
        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("usuarios")
                .child(currentUserId);

        // Cargar los datos del usuario
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String nombre = snapshot.child("nombre").getValue(String.class);
                    String apellido = snapshot.child("apellido").getValue(String.class);
                    String descripcion = snapshot.child("descripcion").getValue(String.class);
                    String fechaNacimiento = snapshot.child("fechaNacimiento").getValue(String.class);
                    String fotoUrl = snapshot.child("fotoPerfilUrl").getValue(String.class);
                    String fondoUrl = snapshot.child("fotoFondoUrl").getValue(String.class);

                    Glide.with(requireActivity())
                            .load(fondoUrl)
                            .placeholder(android.R.color.darker_gray)
                            .centerCrop()
                            .into(backgroundImage);

                    // Nombre completo
                    String nombreCompleto = "";
                    if (nombre != null) nombreCompleto += nombre;
                    if (apellido != null) nombreCompleto += " " + apellido;
                    fullName.setText(nombreCompleto.trim());

                    // Username
                    if (nombre != null) {
                        username.setText("@" + nombre.toLowerCase());
                    } else {
                        username.setText("@usuario");
                    }

                    // Bio
                    bio.setText(descripcion != null ? descripcion : "");

                    // Edad y país
                    if (fechaNacimiento != null && !fechaNacimiento.isEmpty()) {
                        int edad = calcularEdad(fechaNacimiento);
                        ageCountry.setText(edad + " años · Colombia");
                    } else {
                        ageCountry.setText("Edad no disponible");
                    }

                    // Imagen de perfil
                    Glide.with(requireActivity())
                            .load(fotoUrl)
                            .placeholder(R.drawable.ic_person_placeholder)
                            .circleCrop()
                            .into(profileImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Manejo de error
            }
        });

        return view;
    }

    // Calcular edad a partir de la fecha
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
