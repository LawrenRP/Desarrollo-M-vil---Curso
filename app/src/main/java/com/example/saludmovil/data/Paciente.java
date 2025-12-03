package com.example.saludmovil.data;

public class Paciente {

    private String idUsuario; // ✨ AHORA ES STRING (ID de Firestore)
    private String dni;
    private String nombre;
    private String apellido;

    public Paciente(String idUsuario, String dni, String nombre, String apellido) {
        this.idUsuario = idUsuario;
        this.dni = dni;
        this.nombre = nombre;
        this.apellido = apellido;
    }

    public String getIdUsuario() { return idUsuario; }
    public String getDni() { return dni; }
    public String getNombre() { return nombre; }
    public String getApellido() { return apellido; }
}