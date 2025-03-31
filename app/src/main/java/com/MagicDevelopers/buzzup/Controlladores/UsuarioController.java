package com.MagicDevelopers.buzzup.Controlladores;

import com.MagicDevelopers.buzzup.Modelos.Usuario;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;
import java.util.Map;

public class UsuarioController {

    private DatabaseReference usersRef;

    // Constructor: Inicializa la referencia al nodo "usuarios" en Firebase
    public UsuarioController() {
        usersRef = FirebaseDatabase.getInstance().getReference("usuarios");
    }

    /**
     * Guarda el objeto Usuario en Firebase.
     * Genera un userId único y asigna receptorid y emisorid antes de guardarlo.
     *
     * @param usuario  Objeto Usuario con la información acumulada.
     * @param callback Callback para notificar si la operación fue exitosa.
     */
    public void guardarUsuario(Usuario usuario, final UsuarioGuardadoCallback callback) {
        // Generar un ID único usando push().getKey()
        String userId = usersRef.push().getKey();
        if (userId != null) {
            usuario.setUserid(userId);
            usuario.setReceptorid("receptor_" + userId);
            usuario.setEmisorid("emisor_" + userId);

            usersRef.child(userId).setValue(usuario)
                    .addOnCompleteListener(task -> {
                        callback.onUsuarioGuardado(task.isSuccessful());
                    });
        } else {
            callback.onUsuarioGuardado(false);
        }
    }

    /**
     * Actualiza parcialmente el usuario en Firebase.
     *
     * @param userId      El ID del usuario a actualizar.
     * @param nuevosDatos Un mapa con los campos a actualizar.
     * @param callback    Callback para notificar si la operación fue exitosa.
     */
    public void actualizarUsuario(String userId, Map<String, Object> nuevosDatos, ActualizacionCallback callback) {
        usersRef.child(userId).updateChildren(nuevosDatos)
                .addOnCompleteListener(task -> callback.onActualizacionExitosa(task.isSuccessful()));
    }

    /**
     * Guarda una publicación en el nodo "Publicaciones" y la asocia al usuario.
     *
     * @param userId    El ID del usuario que publica.
     * @param imageUrl  La URL (o URI convertida a String) de la imagen de la publicación.
     * @param callback  Callback para notificar si la publicación se guardó exitosamente.
     */
    public void guardarPublicacion(String userId, String imageUrl, PublicacionCallback callback) {
        // Referencia al nodo "Publicaciones"
        DatabaseReference publicacionesRef = FirebaseDatabase.getInstance().getReference("Publicaciones");
        // Generar un ID único para la publicación
        String pubId = publicacionesRef.push().getKey();
        if (pubId == null) {
            callback.onPublicacionGuardada(false);
            return;
        }
        // Crear un mapa con la información de la publicación
        Map<String, Object> data = new HashMap<>();
        data.put("pubId", pubId);
        data.put("userId", userId);
        data.put("imageUrl", imageUrl);
        data.put("timestamp", ServerValue.TIMESTAMP);

        // Guardar la publicación en "Publicaciones/{pubId}"
        publicacionesRef.child(pubId).setValue(data).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Ahora agregar este pubId al nodo de publicaciones del usuario
                DatabaseReference userPublicationsRef = usersRef.child(userId).child("publicaciones");
                userPublicationsRef.child(pubId).setValue(true).addOnCompleteListener(task2 -> {
                    callback.onPublicacionGuardada(task2.isSuccessful());
                });
            } else {
                callback.onPublicacionGuardada(false);
            }
        });
    }

    /**
     * Interfaz para notificar el resultado del guardado del usuario.
     */
    public interface UsuarioGuardadoCallback {
        void onUsuarioGuardado(boolean exito);
    }

    /**
     * Interfaz para notificar el resultado de la actualización del usuario.
     */
    public interface ActualizacionCallback {
        void onActualizacionExitosa(boolean exito);
    }

    /**
     * Interfaz para notificar el resultado del guardado de la publicación.
     */
    public interface PublicacionCallback {
        void onPublicacionGuardada(boolean exito);
    }
}
