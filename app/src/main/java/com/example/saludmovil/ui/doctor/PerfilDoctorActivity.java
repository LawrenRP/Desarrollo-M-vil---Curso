package com.example.saludmovil.ui.doctor;

import com.example.saludmovil.BuildConfig;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class PerfilDoctorActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView tvNombre, tvDNI, tvCMP, tvEspecialidad, tvNombreArchivo;
    private TextInputEditText etTelefono, etCorreo;
    private Button btnGuardar, btnVerTitulo, btnActualizarTitulo;

    private FirebaseFirestore db;
    private String idUsuarioDoctor;
    private String nombreArchivoActual = "";

    private ActivityResultLauncher<String> filePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil_doctor);

        db = FirebaseFirestore.getInstance();

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

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        SharedPreferences sp = getSharedPreferences("datos_usuario", MODE_PRIVATE);
        idUsuarioDoctor = sp.getString("id_usuario", "");

        if (idUsuarioDoctor.isEmpty()) {
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
                            nombreArchivoActual = "titulo_" + System.currentTimeMillis() + ".pdf";
                        }
                        boolean exito = copiarArchivoPrivado(uri, nombreArchivoActual);
                        if (exito) {
                            tvNombreArchivo.setText(nombreArchivoActual);
                            actualizarRutaTituloEnFirestore(nombreArchivoActual);
                        } else {
                            Toast.makeText(this, "Error al guardar el archivo", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }

    private void cargarDatosDoctor() {
        db.collection("doctores").document(idUsuarioDoctor)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String nombreCompleto = document.getString("nombre_completo");
                        String dni = document.getString("dni");
                        String cmp = document.getString("cmp");
                        String especialidad = document.getString("especialidad");
                        String celular = document.getString("celular");
                        String correo = document.getString("correo");
                        String rutaTitulo = document.getString("ruta_titulo_universitario");

                        if (nombreCompleto != null) tvNombre.setText("Dr. " + nombreCompleto);
                        if (dni != null) tvDNI.setText("DNI: " + dni);
                        if (cmp != null) tvCMP.setText("CMP: " + cmp);
                        if (especialidad != null) {
                            tvEspecialidad.setText("Especialidad: " + especialidad);
                        } else {
                            tvEspecialidad.setText("Especialidad: General");
                        }

                        if (celular != null) etTelefono.setText(celular);
                        if (correo != null) etCorreo.setText(correo);

                        if (rutaTitulo != null && !rutaTitulo.isEmpty()) {
                            nombreArchivoActual = rutaTitulo;
                            tvNombreArchivo.setText(rutaTitulo);
                        } else {
                            nombreArchivoActual = "";
                            tvNombreArchivo.setText("Sin título adjunto");
                        }
                    } else {
                        Toast.makeText(this, "No se encontró el perfil del doctor", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar perfil: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void guardarCambios() {
        String nuevoTelefono = etTelefono.getText().toString().trim();
        String nuevoCorreo = etCorreo.getText().toString().trim();

        if (nuevoTelefono.isEmpty() || nuevoCorreo.isEmpty()) {
            Toast.makeText(this, "Los campos no pueden estar vacíos", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("celular", nuevoTelefono);
        updates.put("correo", nuevoCorreo);

        db.collection("doctores").document(idUsuarioDoctor)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Perfil actualizado correctamente", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al guardar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void actualizarTitulo() {
        filePickerLauncher.launch("application/pdf");
    }

    private void actualizarRutaTituloEnFirestore(String nuevaRuta) {
        db.collection("doctores").document(idUsuarioDoctor)
                .update("ruta_titulo_universitario", nuevaRuta)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Título actualizado con éxito", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al actualizar ruta en la nube", Toast.LENGTH_SHORT).show();
                });
    }

    private void verTitulo() {
        if (nombreArchivoActual.isEmpty()) {
            Toast.makeText(this, "No hay un título guardado para ver", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            File file = new File(getFilesDir(), nombreArchivoActual);

            if (!file.exists()) {
                Toast.makeText(this, "El archivo no está en este dispositivo. Fue subido desde otro lugar.", Toast.LENGTH_LONG).show();
                return;
            }

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
}
