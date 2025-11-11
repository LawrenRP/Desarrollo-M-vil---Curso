package com.example.saludmovil.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

// Esta clase será nuestro "cajón de herramientas" para funciones de validación.
public class Validaciones {


    private Validaciones() {}

    public static boolean esValido(String miClave){
        // Banderas para verificar si la contraseña contiene al menos una letra, un número y un caracter especial.
        int f1=0, f2=0, f3=0;
        // Verifica si la longitud de la contraseña es menor a 8 caracteres.
        if(miClave.length() < 8){
            return false;
        } else {
            // Recorre la contraseña para buscar una letra.
            for (int i = 0; i < miClave.length(); i++){
                if (Character.isLetter(miClave.charAt(i))){
                    f1 = 1; // Si encuentra una letra, cambia la bandera a 1.
                }
            }
            // Recorre la contraseña para buscar un número.
            for (int r = 0; r < miClave.length(); r++){
                if (Character.isDigit(miClave.charAt(r))){
                    f2 = 1; // Si encuentra un número, cambia la bandera a 1.
                }
            }
            // Recorre la contraseña para buscar un caracter especial.
            for (int p = 0; p < miClave.length(); p++){
                char c = miClave.charAt(p);
                // Comprueba si el caracter es un caracter especial usando su código ASCII.
                if (c >= 33 && c <= 46 || c == 64){
                    f3 = 1; // Si encuentra un caracter especial, cambia la bandera a 1.
                }
            }
            // Si las tres banderas son 1, significa que la contraseña es válida.
            if (f1 == 1 && f2 == 1 && f3 == 1)
                return true;
            return false; // Si alguna bandera no es 1, la contraseña no es válida.
        }
    }


    public static boolean esFechaNacimientoValida(String fechaStr) {
        if (fechaStr == null || fechaStr.isEmpty()) {
            return false;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        sdf.setLenient(false); // No permite fechas inválidas como 32/01/2023

        try {
            Date fechaNacimiento = sdf.parse(fechaStr);
            Date fechaActual = new Date();

            // 1. No puede ser una fecha futura
            if (fechaNacimiento.after(fechaActual)) {
                return false;
            }

            // 2. No puede tener más de 120 años
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.YEAR, -120);
            Date fechaMinima = cal.getTime();
            if (fechaNacimiento.before(fechaMinima)) {
                return false;
            }

            // 3. Debe ser mayor de 18 años
            cal.setTime(new Date());
            cal.add(Calendar.YEAR, -18);
            Date fechaMayorDeEdad = cal.getTime();
            if (fechaNacimiento.after(fechaMayorDeEdad)) {
                return false;
            }

        } catch (ParseException e) {
            // Si el formato de la fecha es incorrecto (ej. "hola mundo")
            return false;
        }

        // Si pasa todas las validaciones
        return true;
    }

    public static String capitalizarPalabras(String texto) {
        if (texto == null || texto.isEmpty()) {
            return "";
        }
        String[] palabras = texto.toLowerCase().split(" ");
        StringBuilder resultado = new StringBuilder();
        for (String palabra : palabras) {
            if (!palabra.isEmpty()) {
                resultado.append(Character.toUpperCase(palabra.charAt(0)))
                        .append(palabra.substring(1))
                        .append(" ");
            }
        }
        return resultado.toString().trim();
    }

}