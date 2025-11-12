package com.example.saludmovil.ui.doctor;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.saludmovil.database.BaseDeDatos;
import com.example.saludmovil.R;
import com.example.saludmovil.ui.global.RolesActivity;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class InicioDoctorActivity extends AppCompatActivity {

    private TextView saludoDoctor;
    private MaterialCardView cardDoctorCitas, cardDoctorBuscarPaciente, cardDoctorPerfil, cardDoctorSalir;
    private MaterialCardView cardProximaCitaDoctor;
    private TextView tvProximaCitaInfo;
    private TextView tvProximaCitaPaciente;

    private int idUsuarioDoctor;
    private BaseDeDatos bd; // ✨ Hacemos la BD una variable de la clase

    private static final String CHANNEL_ID = "citas_doctores_channel";
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                // Independientemente del permiso, cargamos la info
                // El método interno se encargará de si envía o no la notificación
                cargarDatosDeInicio();
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio_doctor);

        // --- Vinculamos Vistas ---
        saludoDoctor = findViewById(R.id.saludoDoctor);
        cardDoctorCitas = findViewById(R.id.cardDoctorCitas);
        cardDoctorBuscarPaciente = findViewById(R.id.cardDoctorBuscarPaciente);
        cardDoctorPerfil = findViewById(R.id.cardDoctorPerfil);
        cardDoctorSalir = findViewById(R.id.cardDoctorSalir);
        cardProximaCitaDoctor = findViewById(R.id.cardProximaCitaDoctor);
        tvProximaCitaInfo = findViewById(R.id.tvProximaCitaInfo);
        tvProximaCitaPaciente = findViewById(R.id.tvProximaCitaPaciente);

        bd = new BaseDeDatos(this); // ✨ Inicializamos la BD una sola vez

        // --- Obtenemos Sesión ---
        SharedPreferences sp = getSharedPreferences("datos_usuario", MODE_PRIVATE);
        idUsuarioDoctor = sp.getInt("id_usuario", -1);
        String rolUsuario = sp.getString("rol_usuario", "");

        if (idUsuarioDoctor == -1 || !rolUsuario.equals("doctor")) {
            Toast.makeText(this, "Error de sesión. Por favor, inicie de nuevo.", Toast.LENGTH_LONG).show();
            irALogin();
            return;
        }

        // --- Configuramos Listeners ---
        cardDoctorSalir.setOnClickListener(v -> irALogin());
        cardDoctorCitas.setOnClickListener(v -> {
            Intent intent = new Intent(InicioDoctorActivity.this, MisCitasDoctorActivity.class);
            startActivity(intent);
        });
        cardDoctorBuscarPaciente.setOnClickListener(v -> Toast.makeText(this, "Abriendo Buscador de Pacientes...", Toast.LENGTH_SHORT).show());
        cardDoctorPerfil.setOnClickListener(v -> {
            Intent intent = new Intent(InicioDoctorActivity.this, PerfilDoctorActivity.class);
            startActivity(intent);
        });

        // --- Ajuste de UI (EdgeToEdge) ---
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // --- Cargamos todos los datos ---
        mostrarSaludoPersonalizado();
        crearCanalDeNotificacion();
        solicitarPermisoYCargarDatos(); // ✨ Un solo método de arranque
    }

    private void solicitarPermisoYCargarDatos() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED) {
                // Permiso concedido, cargamos todo
                cargarDatosDeInicio();
            } else {
                // No tenemos permiso, lo pedimos.
                // El launcher se encargará de llamar a cargarDatosDeInicio()
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        } else {
            // Versiones antiguas, no se necesita permiso, cargamos todo
            cargarDatosDeInicio();
        }
    }

    private void crearCanalDeNotificacion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Próximas Citas (Doctor)";
            String description = "Canal para notificar a los doctores sobre sus próximas citas.";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    // --- ✨ MÉTODO UNIFICADO (Optimizado) ✨ ---
    // Este método ahora hace el trabajo de cargarProximaCita Y enviarNotificacion
    private void cargarDatosDeInicio() {
        // Consultamos la BD UNA SOLA VEZ
        Cursor cursor = bd.getProximaCitaDoctor(idUsuarioDoctor);

        if (cursor != null && cursor.moveToFirst()) {
            // --- 1. Extraemos los datos ---
            int fechaIndex = cursor.getColumnIndex("fecha");
            int horaIndex = cursor.getColumnIndex("hora");
            int nombreIndex = cursor.getColumnIndex("nombre");
            int apellidoIndex = cursor.getColumnIndex("apellido");
            String fechaCita = cursor.getString(fechaIndex);
            String horaCita = cursor.getString(horaIndex);
            String nombrePaciente = cursor.getString(nombreIndex);
            String apellidoPaciente = cursor.getString(apellidoIndex);

            // --- 2. Mostramos la TARJETA ---
            String fechaFormateada = formatearFecha(fechaCita);
            tvProximaCitaInfo.setText(fechaFormateada + " - " + horaCita);
            tvProximaCitaPaciente.setText("Paciente: " + nombrePaciente + " " + apellidoPaciente);
            cardProximaCitaDoctor.setVisibility(View.VISIBLE);

            // --- 3. Enviamos la NOTIFICACIÓN PUSH ---
            String titulo = "Próxima Cita";
            String texto = "Tu próxima cita es con el paciente " + nombrePaciente + " " + apellidoPaciente +
                    " el " + fechaCita + " a las " + horaCita;

            Intent intent = new Intent(this, InicioDoctorActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.drawable.health_cross_24px)
                    .setContentTitle(titulo)
                    .setContentText(texto)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(texto))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            try {
                // Verificamos el permiso OTRA VEZ por si acaso (necesario para el try/catch)
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                    notificationManager.notify(2, builder.build());
                }
            } catch (SecurityException e) {
                e.printStackTrace();
            }

        } else {
            // Si no hay citas, ocultamos la tarjeta
            cardProximaCitaDoctor.setVisibility(View.GONE);
        }

        // Cerramos el cursor
        if(cursor != null) cursor.close();
    }

    // --- ✨ YA NO NECESITAMOS el método duplicado 'enviarNotificacionProximaCitaDoctor' ---
    // --- ✨ YA NO NECESITAMOS el método duplicado 'cargarProximaCita' ---

    private String formatearFecha(String fechaDB) {
        try {
            SimpleDateFormat formatoEntrada = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat formatoSalida = new SimpleDateFormat("dd 'de' MMMM", new Locale("es", "ES"));
            Date date = formatoEntrada.parse(fechaDB);
            return formatoSalida.format(date);
        } catch (Exception e) {
            e.printStackTrace();
            return fechaDB;
        }
    }

    private void mostrarSaludoPersonalizado() {
        // Ya no abrimos/cerramos la BD aquí, ya está abierta
        String nombreDoctor = bd.getNombreDoctor(idUsuarioDoctor);
        if (nombreDoctor != null && !nombreDoctor.isEmpty()) {
            saludoDoctor.setText("¡Bienvenido, " + nombreDoctor + "!");
        }
        // No cerramos la BD aquí, se cierra en onDestroy
    }

    private void irALogin() {
        SharedPreferences sp = getSharedPreferences("datos_usuario", MODE_PRIVATE);
        sp.edit().clear().apply();
        Intent intent = new Intent(InicioDoctorActivity.this, RolesActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // ✨ NUEVO: Cerramos la BD cuando la actividad se destruye
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bd != null) {
            bd.close();
        }
    }
}