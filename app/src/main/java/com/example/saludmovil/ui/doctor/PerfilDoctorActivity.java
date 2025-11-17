package com.example.saludmovil.ui.doctor;

import com.example.saludmovil.BuildConfig;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

import com.example.saludmovil.R;
import com.example.saludmovil.database.BaseDeDatos;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class PerfilDoctorActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView tvNombre, tvDNI, tvCMP, tvEspecialidad, tvNombreArchivo;
    private TextInputEditText etTelefono, etCorreo;
    private Button btnGuardar, btnVerTitulo, btnActualizarTitulo;

    private BaseDeDatos bd;
    private int idUsuarioDoctor;
    private String nombreArchivoActual = "";

    private ActivityResultLauncher<String> filePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil_doctor);

        bd = new BaseDeDatos(this);

        toolbar = findViewById(R.id.toolbarPerfilDoctor);
        tvNombre = findViewById(R.id.tvPerfilDoctorNombre);
        tvDNI = findViewById(R.id.tvPerfilDoctorDNI);
        tvCMP = findViewById(R.id.tvPerfilDoctorCMP);
        tvEspecialidad = findViewById(R.id.tvPerfilDoctorEspecialidad);
        tvNombreArchivo = findViewById(R.id.tvPerfilDoctorNombreArchivo);
        etTelefono = findViewById(R.id.etPerfilDoctorTelefono);
        etCorreo = findViewById(R.id.etPerfilDoctorCorreo);
        btnGuardar = findViewById(R.id.btnGuardarCambiosDoctor);
        btnVerTitulo = findViewById(R.id.btnVerTitulo);
        btnActualizarTitulo = findViewById(R.id.btnActualizarTitulo);

        // --- Configuración de la Toolbar ---
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        // --- Obtener ID de Sesión ---
        SharedPreferences sp = getSharedPreferences("datos_usuario", MODE_PRIVATE);
        idUsuarioDoctor = sp.getInt("id_usuario", -1);
        if (idUsuarioDoctor == -1) {
            Toast.makeText(this, "Error de sesión", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        cargarDatosDoctor();

        btnGuardar.setOnClickListener(v -> guardarCambios());
        btnActualizarTitulo.setOnClickListener(v -> actualizarTitulo());
        btnVerTitulo.setOnClickListener(v -> verTitulo());

        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        if (nombreArchivoActual.isEmpty()) {
                            Toast.makeText(this, "Error: No se encontró el nombre del archivo original.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        boolean exito = copiarArchivoPrivado(uri, nombreArchivoActual);
                        if (exito) {
                            Toast.makeText(this, "Título actualizado con éxito", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Error al actualizar el archivo", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }

    private void cargarDatosDoctor() {
        Cursor cursor = bd.getPerfilDoctor(idUsuarioDoctor);
        if (cursor != null && cursor.moveToFirst()) {

            int nombreIndex = cursor.getColumnIndex("nombre_completo");
            int dniIndex = cursor.getColumnIndex("dni");
            int cmpIndex = cursor.getColumnIndex("numero_colegiatura");
            int celularIndex = cursor.getColumnIndex("celular");
            int correoIndex = cursor.getColumnIndex("correo");
            int rutaIndex = cursor.getColumnIndex("ruta_titulo_universitario");
            int especialidadIndex = cursor.getColumnIndex("id_especialidad");

            if(nombreIndex != -1) tvNombre.setText("Dr. " + cursor.getString(nombreIndex));
            if(dniIndex != -1) tvDNI.setText("DNI: " + cursor.getString(dniIndex));
            if(cmpIndex != -1) tvCMP.setText("CMP: " + cursor.getString(cmpIndex));
            if(celularIndex != -1) etTelefono.setText(cursor.getString(celularIndex));
            if(correoIndex != -1) etCorreo.setText(cursor.getString(correoIndex));

            if(rutaIndex != -1) {
                nombreArchivoActual = cursor.getString(rutaIndex);
                tvNombreArchivo.setText(nombreArchivoActual);
            } else {
                nombreArchivoActual = "";
            }

            if(especialidadIndex != -1) {
                int idEspecialidad = cursor.getInt(especialidadIndex);
                tvEspecialidad.setText("ID Especialidad: " + idEspecialidad);
            }

            cursor.close();
        }
    }

    private void guardarCambios() {
        String nuevoTelefono = etTelefono.getText().toString().trim();
        String nuevoCorreo = etCorreo.getText().toString().trim();

        if (nuevoTelefono.isEmpty() || nuevoCorreo.isEmpty()) {
            Toast.makeText(this, "Los campos no pueden estar vacíos", Toast.LENGTH_SHORT).show();
            return;
        }

        bd.actualizarPerfilDoctor(idUsuarioDoctor, nuevoTelefono, nuevoCorreo);
        Toast.makeText(this, "Perfil actualizado con éxito", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void actualizarTitulo() {
        filePickerLauncher.launch("application/pdf");
    }

    private void verTitulo() {
        if (nombreArchivoActual.isEmpty()) {
            Toast.makeText(this, "No hay un título guardado.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            File file = new File(getFilesDir(), nombreArchivoActual);
            Uri fileUri = FileProvider.getUriForFile(this,
                    BuildConfig.APPLICATION_ID + ".provider",
                    file);

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(fileUri, "application/pdf");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(Intent.createChooser(intent, "Abrir título con..."));

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al abrir el archivo. ¿Tienes un lector de PDF?", Toast.LENGTH_LONG).show();
        }
    }
    private boolean copiarArchivoPrivado(Uri uri, String nombreArchivo) {
        try (InputStream inputStream = getContentResolver().openInputStream(uri);
             FileOutputStream outputStream = openFileOutput(nombreArchivo, Context.MODE_PRIVATE)) {
            if (inputStream == null) return false;
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.flush();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bd != null) {
            bd.close();
        }
    }
}