package com.example.saludmovil.data;

import java.io.Serializable;
import java.util.ArrayList;

public class Receta implements Serializable {
    private String idReceta; // ✨ ID de Firestore (String)
    private String idCita;   // ✨ ID de la cita (String)
    private String fechaEmision;
    private String nombreDoctor; // Guardado en el documento
    private String especialidad; // Guardado en el documento

    // Lista de medicamentos (objetos)
    private ArrayList<MedicamentoRecetado> listaMedicamentos;

    public Receta(String idReceta, String idCita, String fechaEmision, String nombreDoctor, String especialidad, ArrayList<MedicamentoRecetado> listaMedicamentos) {
        this.idReceta = idReceta;
        this.idCita = idCita;
        this.fechaEmision = fechaEmision;
        this.nombreDoctor = nombreDoctor;
        this.especialidad = especialidad;
        this.listaMedicamentos = listaMedicamentos;
    }

    // Constructor simple para la lista (sin medicamentos detallados)
    public Receta(String idReceta, String fechaEmision, String nombreDoctor, String especialidad) {
        this.idReceta = idReceta;
        this.fechaEmision = fechaEmision;
        this.nombreDoctor = nombreDoctor;
        this.especialidad = especialidad;
        this.listaMedicamentos = new ArrayList<>();
    }

    public String getId() { return idReceta; }
    public String getIdCita() { return idCita; }
    public String getFechaEmision() { return fechaEmision; }
    public String getNombreDoctor() { return nombreDoctor; }
    public String getEspecialidad() { return especialidad; }
    public ArrayList<MedicamentoRecetado> getListaMedicamentos() { return listaMedicamentos; }
}