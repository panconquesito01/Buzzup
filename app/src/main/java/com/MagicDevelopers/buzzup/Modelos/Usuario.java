package com.MagicDevelopers.buzzup.Modelos;

import java.io.Serializable;

public class Usuario implements Serializable {
    // Identificadores únicos
    private String userid;
    private String receptorid;
    private String emisorid;

    // Registro1
    private String nombre;
    private String apellido;
    private String descripcion;

    // Registro2
    private String correo;
    private String contrasena;

    // Registro3
    private String fechaNacimiento;

    // Registro4
    private String fotoPerfilUrl;           // Imagen recortada (miniatura)
    private String fotoPerfilCompletaUrl;     // Imagen completa original

    // Registro6 (ejemplo: foto para publicaciones)
    private String fotoPublicacionUrl;

    // Registro7
    private boolean guardarInformacion;

    // Constructor vacío (requerido para Firebase)
    public Usuario() {
    }

    // Constructor completo (con todos los campos)
    public Usuario(String nombre, String apellido, String descripcion, String correo, String contrasena,
                   String fechaNacimiento, String fotoPerfilUrl, String fotoPerfilCompletaUrl,
                   String fotoPublicacionUrl, boolean guardarInformacion) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.descripcion = descripcion;
        this.correo = correo;
        this.contrasena = contrasena;
        this.fechaNacimiento = fechaNacimiento;
        this.fotoPerfilUrl = fotoPerfilUrl;
        this.fotoPerfilCompletaUrl = fotoPerfilCompletaUrl;
        this.fotoPublicacionUrl = fotoPublicacionUrl;
        this.guardarInformacion = guardarInformacion;
    }

    // Constructor para Registro1 (solo 3 parámetros)
    public Usuario(String nombre, String apellido, String descripcion) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.descripcion = descripcion;
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
    public void setReceptorid(String receptorid) {
        this.receptorid = receptorid;
    }

    public String getEmisorid() {
        return emisorid;
    }
    public void setEmisorid(String emisorid) {
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
}
