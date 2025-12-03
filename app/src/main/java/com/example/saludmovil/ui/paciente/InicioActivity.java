package com.example.saludmovil.ui.paciente;

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
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.example.saludmovil.R;
import com.example.saludmovil.ui.global.RolesActivity;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class InicioActivity extends AppCompatActivity {

    TextView saludoUsuario;
    ImageButton btnPerfil;
    ImageButton btnRitmoCardiaco;
    MaterialCardView cardAgendarCita, cardUbicanos, cardEspecialidades, cardRecetasMedicas, cardMisCitas, cardSalir;

    private MaterialCardView cardProximaCita;
    private TextView tvCitaPendienteInfo;
    private TextView tvCitaPendienteDoctor;
    private String idUsuario;
    private String rolUsuario;
    private FirebaseFirestore db;

    private static final String CHANNEL_ID = "citas_channel";
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                revisarYEnviarNotificacion();
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio);

        db = FirebaseFirestore.getInstance();

        saludoUsuario = findViewById(R.id.saludoUsuario);
        btnPerfil = findViewById(R.id.buttonMiPerfil);
        btnRitmoCardiaco = findViewById(R.id.buttonRitmoCardiaco);
        cardAgendarCita = findViewById(R.id.cardAgendarCita);
        cardUbicanos = findViewById(R.id.cardUbicanos);
        cardEspecialidades = findViewById(R.id.cardEspecialidades);
        cardRecetasMedicas = findViewById(R.id.cardRecetasMedicas);
        cardMisCitas = findViewById(R.id.cardMisCitas);
        cardSalir = findViewById(R.id.cardSalir);

        cardProximaCita = findViewById(R.id.cardProximaCita);
        tvCitaPendienteInfo = findViewById(R.id.tvCitaPendienteInfo);
        tvCitaPendienteDoctor = findViewById(R.id.tvCitaPendienteDoctor);

        SharedPreferences sp = getSharedPreferences("datos_usuario", MODE_PRIVATE);
        idUsuario = sp.getString("id_usuario", "");
        rolUsuario = sp.getString("rol_usuario", "");

        if (idUsuario.isEmpty() || rolUsuario.isEmpty()) {
            Toast.makeText(this, "Error de sesión, por favor inicie de nuevo", Toast.LENGTH_SHORT).show();
            irALogin();
            return;
        }

        mostrarSaludoPersonalizado();

        if (rolUsuario.equals("paciente")) {
            verificarPerfilCompleto();
        }

        configurarListeners();
        crearCanalDeNotificacion();
        solicitarPermisoYEnviarNotificacion();
    }

    private void verificarPerfilCompleto() {
        db.collection("pacientes")
                .document(idUsuario)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String alergias = documentSnapshot.getString("alergias");
                        String estatura = documentSnapshot.getString("estatura");

                        if (alergias == null || alergias.isEmpty() || estatura == null || estatura.isEmpty()) {
                            mostrarDialogoCompletarPerfil();
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error al verificar perfil", Toast.LENGTH_SHORT).show());
    }

    private void solicitarPermisoYEnviarNotificacion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED) {
                revisarYEnviarNotificacion();
            } else {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        } else {
            revisarYEnviarNotificacion();
        }
    }

    private void crearCanalDeNotificacion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Recordatorio de Citas";
            String description = "Canal para notificar sobre próximas citas médicas";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void revisarYEnviarNotificacion() {
        db.collection("citas")
                .whereEqualTo("id_paciente", idUsuario)
                .whereEqualTo("estado", "agendada")
                .orderBy("fecha", Query.Direction.ASCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        QueryDocumentSnapshot document = (QueryDocumentSnapshot) queryDocumentSnapshots.getDocuments().get(0);

                        String fecha = document.getString("fecha");
                        String hora = document.getString("hora");
                        String nombreDoctor = document.getString("nombre_doctor_temp");

                        String fechaFormateada = formatearFecha(fecha);
                        tvCitaPendienteInfo.setText(fechaFormateada + " - " + hora);
                        tvCitaPendienteDoctor.setText("Con: " + nombreDoctor);

                        cardProximaCita.post(() -> cardProximaCita.setVisibility(View.VISIBLE));
                        enviarNotificacionPush(nombreDoctor, fechaFormateada, hora);
                    } else {
                        cardProximaCita.post(() -> cardProximaCita.setVisibility(View.GONE));
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar citas: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    cardProximaCita.post(() -> cardProximaCita.setVisibility(View.GONE));
                });
    }

    private void enviarNotificacionPush(String doctor, String fecha, String hora) {
        String tituloNotif = "Recordatorio de Cita";
        String textoNotif = "Tu próxima cita es con " + doctor + " el " + fecha + " a las " + hora;

        Intent intent = new Intent(this, InicioActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.health_cross_24px)
                .setContentTitle(tituloNotif)
                .setContentText(textoNotif)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(textoNotif))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        try {
            notificationManager.notify(1, builder.build());
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

    private void configurarListeners() {
        btnPerfil.setOnClickListener(v -> {
            Intent intent = new Intent(InicioActivity.this, PerfilPacienteActivity.class);
            intent.putExtra("id_usuario", idUsuario);
            startActivity(intent);
        });
        btnRitmoCardiaco.setOnClickListener(v -> {
            Intent intent = new Intent(InicioActivity.this, RitmoCardiacoActivity.class);
            startActivity(intent);
        });
        cardAgendarCita.setOnClickListener(v -> {
            Intent intent = new Intent(InicioActivity.this, AgendarCitaActivity.class);
            startActivity(intent);
        });
        cardUbicanos.setOnClickListener(v -> {
            Intent intent = new Intent(InicioActivity.this, UbicanosMapActivity.class);
            startActivity(intent);
        });
        cardEspecialidades.setOnClickListener(v -> {
            Intent intent = new Intent(InicioActivity.this, EspecialidadesActivity.class);
            startActivity(intent);
        });

        cardRecetasMedicas.setOnClickListener(v -> {
            Intent intent = new Intent(InicioActivity.this, RecetasActivity.class);
            intent.putExtra("id_usuario", idUsuario);
            startActivity(intent);
        });

        cardMisCitas.setOnClickListener(v -> {
            Intent intent = new Intent(InicioActivity.this, MisCitasPacienteActivity.class);
            intent.putExtra("id_usuario", idUsuario);
            startActivity(intent);
        });

        cardSalir.setOnClickListener(v -> irALogin());
    }

    private void mostrarSaludoPersonalizado() {
        db.collection("pacientes")
                .document(idUsuario)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String nombre = documentSnapshot.getString("nombre");
                        saludoUsuario.setText("¡Hola, " + (nombre != null ? nombre : "Usuario") + "!");
                    } else {
                        saludoUsuario.setText("¡Hola, Usuario!");
                    }
                })
                .addOnFailureListener(e -> {
                    saludoUsuario.setText("¡Hola, Usuario!");
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

    private void mostrarDialogoCompletarPerfil() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Perfil Médico Incompleto")
                .setMessage("Para brindarte un mejor servicio, por favor completa tu perfil médico.")
                .setPositiveButton("Completar Perfil", (dialog, which) -> {
                    Intent intent = new Intent(InicioActivity.this, PerfilPacienteActivity.class);
                    intent.putExtra("id_usuario", idUsuario);
                    startActivity(intent);
                })
                .setNegativeButton("Más Tarde", (dialog, which) -> dialog.dismiss())
                .setCancelable(false)
                .show();
    }
}
