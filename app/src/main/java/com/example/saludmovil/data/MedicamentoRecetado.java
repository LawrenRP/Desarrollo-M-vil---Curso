package com.example.saludmovil.data;

import java.io.Serializable;

public class MedicamentoRecetado implements Serializable {

    private int idMedicamento;
    private String nombre;
    private String cantidad;
    private String indicaciones;

    public MedicamentoRecetado(int idMedicamento, String nombre, String cantidad, String indicaciones) {
        this.idMedicamento = idMedicamento;
        this.nombre = nombre;
        this.cantidad = cantidad;
        this.indicaciones = indicaciones;
    }

    // Getters
    public int getIdMedicamento() { return idMedicamento; }
    public String getNombre() { return nombre; }
    public String getCantidad() { return cantidad; }
    public String getIndicaciones() { return indicaciones; }
}