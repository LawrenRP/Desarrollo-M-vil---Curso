package com.example.saludmovil.data;

import java.io.Serializable;

public class Cita implements Serializable {
    private int idCita;          // ID local (ya no se usa mucho, pero lo dejamos por compatibilidad)

    // ✨ CAMBIO CLAVE: Estos ahora son String para guardar "P1vVM..."
    private String idPaciente;
    private String idDoctor;
    private String idFirestore;

    private String fecha;
    private String hora;
    private String estado;
    private String motivo;
    private String nombrePaciente;
    private String nombreDoctor;

    // Constructor actualizado con String
    public Cita(int idCita, String idPaciente, String idDoctor, String idFirestore,
                String fecha, String hora, String estado, String motivo,
                String nombrePaciente, String nombreDoctor) {
        this.idCita = idCita;
        this.idPaciente = idPaciente;
        this.idDoctor = idDoctor;
        this.idFirestore = idFirestore;
        this.fecha = fecha;
        this.hora = hora;
        this.estado = estado;
        this.motivo = motivo;
        this.nombrePaciente = nombrePaciente;
        this.nombreDoctor = nombreDoctor;
    }

    public int getIdCita() { return idCita; }

    // ✨ Getters actualizados a String
    public String getIdPaciente() { return idPaciente; }
    public String getIdDoctor() { return idDoctor; }

    public String getIdFirestore() { return idFirestore; }
    public String getFecha() { return fecha; }
    public String getHora() { return hora; }
    public String getEstado() { return estado; }
    public String getMotivo() { return motivo; }
    public String getNombrePaciente() { return nombrePaciente != null ? nombrePaciente : "Paciente"; }
    public String getNombreDoctor() { return nombreDoctor != null ? nombreDoctor : "Doctor"; }
}