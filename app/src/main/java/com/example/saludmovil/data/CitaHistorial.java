package com.example.saludmovil.data;

public class CitaHistorial {

    private String fecha;
    private String hora;
    private String estado;
    private String motivo;

    public CitaHistorial(String fecha, String hora, String estado, String motivo) {
        this.fecha = fecha;
        this.hora = hora;
        this.estado = estado;
        this.motivo = motivo;
    }


    public String getFecha() { return fecha; }
    public String getHora() { return hora; }
    public String getEstado() { return estado; }
    public String getMotivo() { return motivo; }
}