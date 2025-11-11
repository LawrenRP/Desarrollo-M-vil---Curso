package com.example.saludmovil.data;

public class Cita {
    private int idCita;
    private String fecha;
    private String hora;
    private String estado;
    private String motivo;
    private String nombrePaciente;

    public Cita(int idCita, String fecha, String hora, String estado, String motivo, String nombrePaciente) {
        this.idCita = idCita;
        this.fecha = fecha;
        this.hora = hora;
        this.estado = estado;
        this.motivo = motivo;
        this.nombrePaciente = nombrePaciente;
    }

    public int getIdCita() {
        return idCita;
    }

    public String getFecha() {
        return fecha;
    }

    public String getHora() {
        return hora;
    }

    public String getEstado() {
        return estado;
    }

    public String getMotivo() {
        return motivo;
    }

    public String getNombrePaciente() {
        return nombrePaciente;
    }
}
