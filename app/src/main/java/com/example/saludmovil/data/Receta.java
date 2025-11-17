package com.example.saludmovil.data;

import java.util.ArrayList;

public class Receta {
    private int id;
    private int idCita;
    private String fechaEmision;
    private String nombreDoctor;
    private String especialidad;
    private ArrayList<MedicamentoRecetado> listaMedicamentos;

    public Receta(int id, int idCita, String fechaEmision, String nombreDoctor, String especialidad) {
        this.id = id;
        this.idCita = idCita;
        this.fechaEmision = fechaEmision;
        this.nombreDoctor = nombreDoctor;
        this.especialidad = especialidad;
        this.listaMedicamentos = new ArrayList<>();
    }

    public int getId() { return id; }
    public int getIdCita() { return idCita; }
    public String getFechaEmision() { return fechaEmision; }
    public String getNombreDoctor() { return nombreDoctor; }
    public String getEspecialidad() { return especialidad; }

    public ArrayList<MedicamentoRecetado> getListaMedicamentos() { return listaMedicamentos; }
    public void setListaMedicamentos(ArrayList<MedicamentoRecetado> lista) { this.listaMedicamentos = lista; }
}