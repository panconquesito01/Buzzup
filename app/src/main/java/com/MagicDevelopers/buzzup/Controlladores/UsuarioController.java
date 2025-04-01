package com.MagicDevelopers.buzzup.Controlladores;

import android.icu.text.SimpleDateFormat;

import com.MagicDevelopers.buzzup.Modelos.Usuario;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class UsuarioController {

    private DatabaseReference usersRef;

    public UsuarioController() {
        usersRef = FirebaseDatabase.getInstance().getReference("usuarios");
    }

    public void guardarUsuario(Usuario usuario, final UsuarioGuardadoCallback callback) {
        String userId = usersRef.push().getKey();
        if (userId != null) {
            usuario.setUserid(userId);
            usuario.setReceptorId("receptor_" + userId);
            usuario.setEmisorId("emisor_" + userId);

            // Se calcula la edad antes de guardar
            usuario.setFechaNacimiento(usuario.getFechaNacimiento());

            usersRef.child(userId).setValue(usuario)
                    .addOnCompleteListener(task -> callback.onUsuarioGuardado(task.isSuccessful()));
        } else {
            callback.onUsuarioGuardado(false);
        }
    }

    public void actualizarUsuario(String userId, Map<String, Object> nuevosDatos, ActualizacionCallback callback) {
        if (nuevosDatos.containsKey("fechaNacimiento")) {
            String fechaNacimiento = (String) nuevosDatos.get("fechaNacimiento");
            int edad = calcularEdad(fechaNacimiento);
            nuevosDatos.put("edad", edad);
        }

        usersRef.child(userId).updateChildren(nuevosDatos)
                .addOnCompleteListener(task -> callback.onActualizacionExitosa(task.isSuccessful()));
    }

    private int calcularEdad(String fechaNacimiento) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date fechaNac = dateFormat.parse(fechaNacimiento);
            if (fechaNac == null) return 0;

            Calendar nacimiento = Calendar.getInstance();
            nacimiento.setTime(fechaNac);

            Calendar hoy = Calendar.getInstance();

            int edad = hoy.get(Calendar.YEAR) - nacimiento.get(Calendar.YEAR);

            if (hoy.get(Calendar.DAY_OF_YEAR) < nacimiento.get(Calendar.DAY_OF_YEAR)) {
                edad--;
            }
            return edad;
        } catch (Exception e) {
            return 0;
        }
    }


    public interface UsuarioGuardadoCallback {
        void onUsuarioGuardado(boolean exito);
    }

    public interface ActualizacionCallback {
        void onActualizacionExitosa(boolean exito);
    }
}
