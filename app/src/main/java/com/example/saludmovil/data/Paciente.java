package com.example.saludmovil.data;

public class Paciente {

    private int idUsuario;
    private String dni;
    private String nombre;
    private String apellido;

    public Paciente(int idUsuario, String dni, String nombre, String apellido) {
        this.idUsuario = idUsuario;
        this.dni = dni;
        this.nombre = nombre;
        this.apellido = apellido;
    }
    public int getIdUsuario() {
        return idUsuario;
    }

    public String getDni() {
        return dni;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellido() {
        return apellido;
    }
}