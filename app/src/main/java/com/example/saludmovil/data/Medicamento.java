package com.example.saludmovil.data;

public class Medicamento {
    private int id;
    private String nombre;
    private String presentacion;
    private int stock;
    private boolean seleccionado = false;
    private String cantidadUsuario = "";
    private String indicacionesUsuario = "";

    public Medicamento(int id, String nombre, String presentacion, int stock) {
        this.id = id;
        this.nombre = nombre;
        this.presentacion = presentacion;
        this.stock = stock;
    }

    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getPresentacion() { return presentacion; }
    public int getStock() { return stock; }

    public boolean isSeleccionado() { return seleccionado; }
    public void setSeleccionado(boolean seleccionado) { this.seleccionado = seleccionado; }

    public String getCantidadUsuario() { return cantidadUsuario; }
    public void setCantidadUsuario(String cantidadUsuario) { this.cantidadUsuario = cantidadUsuario; }

    public String getIndicacionesUsuario() { return indicacionesUsuario; }
    public void setIndicacionesUsuario(String indicacionesUsuario) { this.indicacionesUsuario = indicacionesUsuario; }
}