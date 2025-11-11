package com.example.saludmovil.data;
public class CitaParaPaciente {

    private int idCita;
    private String fecha;
    private String hora;
    private String estado;
    private String motivo;
    private String nombreDoctor;


    public CitaParaPaciente(int idCita, String fecha, String hora, String estado, String motivo, String nombreDoctor) {
        this.idCita = idCita;
        this.fecha = fecha;
        this.hora = hora;
        this.estado = estado;
        this.motivo = motivo;
        this.nombreDoctor = nombreDoctor;
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

    public String getNombreDoctor() {
        return nombreDoctor;
    }
}