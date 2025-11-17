package com.example.saludmovil.data;

public class Cita {
    private int idCita;
    private int idPaciente;
    private String fecha;
    private String hora;
    private String estado;
    private String motivo;
    private String nombrePaciente;

    public Cita(int idCita, int idPaciente, String fecha, String hora, String estado, String motivo, String nombrePaciente) {
        this.idCita = idCita;
        this.idPaciente = idPaciente;
        this.fecha = fecha;
        this.hora = hora;
        this.estado = estado;
        this.motivo = motivo;
        this.nombrePaciente = nombrePaciente;
    }

    public int getIdCita() { return idCita; }
    public int getIdPaciente() { return idPaciente; }
    public String getFecha() { return fecha; }
    public String getHora() { return hora; }
    public String getEstado() { return estado; }
    public String getMotivo() { return motivo; }
    public String getNombrePaciente() { return nombrePaciente; }
}