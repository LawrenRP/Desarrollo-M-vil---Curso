package com.example.saludmovil.ui.doctor;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
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

import com.example.saludmovil.R;
import com.example.saludmovil.ui.global.RolesActivity;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class InicioDoctorActivity extends AppCompatActivity {

    private TextView saludoDoctor;
    private MaterialCardView cardDoctorCitas, cardDoctorBuscarPaciente, cardDoctorPerfil, cardDoctorSalir;
    private MaterialCardView cardProximaCitaDoctor;
    private TextView tvProximaCitaInfo;
    private TextView tvProximaCitaPaciente;

    private String idUsuarioDoctor;
    private FirebaseFirestore db;

    private static final String CHANNEL_ID = "citas_doctores_channel";
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                cargarDatosDeInicio();
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio_doctor);

        saludoDoctor = findViewById(R.id.saludoDoctor);
        cardDoctorCitas = findViewById(R.id.cardDoctorCitas);
        cardDoctorBuscarPaciente = findViewById(R.id.cardDoctorBuscarPaciente);
        cardDoctorPerfil = findViewById(R.id.cardDoctorPerfil);
        cardDoctorSalir = findViewById(R.id.cardDoctorSalir);
        cardProximaCitaDoctor = findViewById(R.id.cardProximaCitaDoctor);
        tvProximaCitaInfo = findViewById(R.id.tvProximaCitaInfo);
        tvProximaCitaPaciente = findViewById(R.id.tvProximaCitaPaciente);

        db = FirebaseFirestore.getInstance();

        SharedPreferences sp = getSharedPreferences("datos_usuario", MODE_PRIVATE);
        idUsuarioDoctor = sp.getString("id_usuario", "");
        String rolUsuario = sp.getString("rol_usuario", "");

        if (idUsuarioDoctor.isEmpty() || !rolUsuario.equals("doctor")) {
            Toast.makeText(this, "Error de sesión. Por favor, inicie de nuevo.", Toast.LENGTH_LONG).show();
            irALogin();
            return;
        }

        cardDoctorSalir.setOnClickListener(v -> irALogin());
        cardDoctorCitas.setOnClickListener(v -> {
            Intent intent = new Intent(InicioDoctorActivity.this, MisCitasDoctorActivity.class);
            startActivity(intent);
        });
        cardDoctorBuscarPaciente.setOnClickListener(v -> {
            Intent intent = new Intent(InicioDoctorActivity.this, BuscarPacienteActivity.class);
            startActivity(intent);
        });
        cardDoctorPerfil.setOnClickListener(v -> {
            Intent intent = new Intent(InicioDoctorActivity.this, PerfilDoctorActivity.class);
            startActivity(intent);
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mostrarSaludoPersonalizado();
        crearCanalDeNotificacion();
        solicitarPermisoYCargarDatos();
    }

    private void solicitarPermisoYCargarDatos() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED) {
                cargarDatosDeInicio();
            } else {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        } else {
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

    private void cargarDatosDeInicio() {
        db.collection("citas")
                .whereEqualTo("id_doctor", idUsuarioDoctor)
                .whereEqualTo("estado", "agendada")
                .orderBy("fecha", Query.Direction.ASCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        QueryDocumentSnapshot document = (QueryDocumentSnapshot) queryDocumentSnapshots.getDocuments().get(0);

                        String fecha = document.getString("fecha");
                        String hora = document.getString("hora");
                        String nombrePaciente = document.getString("nombre_paciente_temp");
                        if (nombrePaciente == null) nombrePaciente = "Paciente (Sin nombre)";

                        String fechaFormateada = formatearFecha(fecha);
                        tvProximaCitaInfo.setText(fechaFormateada + " - " + hora);
                        tvProximaCitaPaciente.setText("Paciente: " + nombrePaciente);

                        cardProximaCitaDoctor.post(() -> cardProximaCitaDoctor.setVisibility(View.VISIBLE));
                        enviarNotificacionPush(nombrePaciente, fechaFormateada, hora);
                    } else {
                        cardProximaCitaDoctor.post(() -> cardProximaCitaDoctor.setVisibility(View.GONE));
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar citas: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    cardProximaCitaDoctor.post(() -> cardProximaCitaDoctor.setVisibility(View.GONE));
                });
    }

    private void enviarNotificacionPush(String paciente, String fecha, String hora) {
        String titulo = "Próxima Cita";
        String texto = "Tu próxima cita es con el paciente " + paciente + " el " + fecha + " a las " + hora;

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
            notificationManager.notify(2, builder.build());
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

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
        db.collection("doctores")
                .document(idUsuarioDoctor)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String nombreCompleto = documentSnapshot.getString("nombre_completo");
                        saludoDoctor.setText("¡Bienvenido, Dr. " + (nombreCompleto != null ? nombreCompleto : "Doctor") + "!");
                    } else {
                        saludoDoctor.setText("¡Bienvenido, Doctor!");
                    }
                })
                .addOnFailureListener(e -> {
                    saludoDoctor.setText("¡Bienvenido, Doctor!");
                });
    }

    private void irALogin() {
        SharedPreferences sp = getSharedPreferences("datos_usuario", MODE_PRIVATE);
        sp.edit().clear().apply();
        Intent intent = new Intent(this, RolesActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
