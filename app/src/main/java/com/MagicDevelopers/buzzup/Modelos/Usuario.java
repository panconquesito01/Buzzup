package com.MagicDevelopers.buzzup.Modelos;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class Usuario implements Serializable {
    private String userid;
    private String receptorid;
    private String emisorid;

    private String nombre;
    private String apellido;
    private String descripcion;
    private String correo;
    private String contrasena;
    private String fechaNacimiento;
    private String telefono; // NUEVO CAMPO
    private String fotoPerfilUrl;
    private String fotoPerfilCompletaUrl;
    private String fotoPublicacionUrl;
    private boolean guardarInformacion;

    // Constructor vacío (requerido para Firebase)
    public Usuario() {
    }

    // Constructor completo
    public Usuario(String nombre, String apellido, String descripcion, String correo, String contrasena,
                   String fechaNacimiento, String telefono, String fotoPerfilUrl, String fotoPerfilCompletaUrl,
                   String fotoPublicacionUrl, boolean guardarInformacion) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.descripcion = descripcion;
        this.correo = correo;
        this.contrasena = contrasena;
        this.fechaNacimiento = fechaNacimiento;
        this.telefono = telefono;
        this.fotoPerfilUrl = fotoPerfilUrl;
        this.fotoPerfilCompletaUrl = fotoPerfilCompletaUrl;
        this.fotoPublicacionUrl = fotoPublicacionUrl;
        this.guardarInformacion = guardarInformacion;
        this.emisorid = generateUniqueId();
        this.receptorid = generateUniqueId();
    }

    // Constructor para Registro1 (solo 3 parámetros)
    public Usuario(String nombre, String apellido, String descripcion) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.descripcion = descripcion;
        this.emisorid = generateUniqueId();
        this.receptorid = generateUniqueId();
    }

    // Método para generar un ID único
    private String generateUniqueId() {
        return UUID.randomUUID().toString();
    }

    // Getters y Setters

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getReceptorid() {
        return receptorid;
    }

    public void setReceptorId(String receptorid) {
        this.receptorid = receptorid;
    }

    public String getEmisorid() {
        return emisorid;
    }

    public void setEmisorId(String emisorid) {
        this.emisorid = emisorid;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public String getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(String fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getFotoPerfilUrl() {
        return fotoPerfilUrl;
    }

    public void setFotoPerfilUrl(String fotoPerfilUrl) {
        this.fotoPerfilUrl = fotoPerfilUrl;
    }

    public String getFotoPerfilCompletaUrl() {
        return fotoPerfilCompletaUrl;
    }

    public void setFotoPerfilCompletaUrl(String fotoPerfilCompletaUrl) {
        this.fotoPerfilCompletaUrl = fotoPerfilCompletaUrl;
    }

    public String getFotoPublicacionUrl() {
        return fotoPublicacionUrl;
    }

    public void setFotoPublicacionUrl(String fotoPublicacionUrl) {
        this.fotoPublicacionUrl = fotoPublicacionUrl;
    }

    public boolean isGuardarInformacion() {
        return guardarInformacion;
    }

    public void setGuardarInformacion(boolean guardarInformacion) {
        this.guardarInformacion = guardarInformacion;
    }

    //Metodo para calcular la edad
    public int calcularEdad() {
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
}
