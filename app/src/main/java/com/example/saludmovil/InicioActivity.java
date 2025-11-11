package com.example.saludmovil;

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
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

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
    private int idUsuario;
    private String rolUsuario;


    private static final String CHANNEL_ID = "citas_channel";
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    revisarYEnviarNotificacion();
                } else {
                    Toast.makeText(this, "No se mostrarán notificaciones de citas.", Toast.LENGTH_SHORT).show();
                }
            });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio);

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
        idUsuario = sp.getInt("id_usuario", -1);
        rolUsuario = sp.getString("rol_usuario", "");

        if (idUsuario == -1 || rolUsuario.isEmpty()) {
            Toast.makeText(this, "Error de sesión, por favor inicie de nuevo", Toast.LENGTH_SHORT).show();
            irALogin();
            return;
        }

        mostrarSaludoPersonalizado();

        if (rolUsuario.equals("paciente")) {
            BaseDeDatos bd = new BaseDeDatos(getApplicationContext());
            if (!bd.isPerfilCompletoPorId(idUsuario)) {
                mostrarDialogoCompletarPerfil();
            }
        }

        configurarListeners();
        crearCanalDeNotificacion();
        solicitarPermisoYEnviarNotificacion(); // Este método ahora hará todo
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

    // --- ✨ MÉTODO ACTUALIZADO CON LA CORRECCIÓN 2 (EL .post) ✨ ---
    private void revisarYEnviarNotificacion() {
        BaseDeDatos bd = new BaseDeDatos(this);
        Cursor cursor = bd.getProximaCita(idUsuario);

        if (cursor != null && cursor.moveToFirst()) {
            // --- 1. EXTRAEMOS LOS DATOS REALES DE LA BD ---
            int fechaIndex = cursor.getColumnIndex("fecha");
            int horaIndex = cursor.getColumnIndex("hora");
            int doctorIndex = cursor.getColumnIndex("nombre_completo");

            String fechaCitaDB = cursor.getString(fechaIndex); // ej: "2025-11-09"
            String horaCita = cursor.getString(horaIndex);      // ej: "11:30 AM"
            String doctorCita = cursor.getString(doctorIndex);  // ej: "Jeraldine Murillo Sequeiros"

            cursor.close();
            bd.close();

            // --- 2. FORMATEAMOS LA FECHA PARA LA UI ---
            String fechaFormateada = formatearFecha(fechaCitaDB); // ej: "09 de noviembre"

            // --- 3. MOSTRAMOS LA TARJETA (CON LA CORRECCIÓN DEL .post) ---
            tvCitaPendienteInfo.setText(fechaFormateada + " - " + horaCita);
            tvCitaPendienteDoctor.setText("Con: " + doctorCita);
            cardProximaCita.post(new Runnable() {
                @Override
                public void run() {
                    cardProximaCita.setVisibility(View.VISIBLE);
                }
            });

            // --- 4. ENVIAMOS LA NOTIFICACIÓN PUSH ---
            String tituloNotif = "Recordatorio de Cita";
            String textoNotif = "Tu próxima cita es con " + doctorCita + " el " + fechaCitaDB + " a las " + horaCita;

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
        } else {
            // Si no hay cita, nos aseguramos de que la tarjeta esté oculta
            cardProximaCita.post(new Runnable() {
                @Override
                public void run() {
                    cardProximaCita.setVisibility(View.GONE);
                }
            });
            if(cursor != null) cursor.close();
            bd.close();
        }
    }

    // --- ✨ MÉTODO AUXILIAR PARA FORMATEAR LA FECHA ✨ ---
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


    // --- El resto de tus métodos (configurarListeners, etc. se mantienen igual) ---
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
            Toast.makeText(this, "Abriendo recetas médicas...", Toast.LENGTH_SHORT).show();
        });
        cardMisCitas.setOnClickListener(v -> {
            Intent intent = new Intent(InicioActivity.this, MisCitasPacienteActivity.class);
            startActivity(intent);
        });
        cardSalir.setOnClickListener(v -> irALogin());
    }

    private void mostrarSaludoPersonalizado() {
        BaseDeDatos bd = new BaseDeDatos(getApplicationContext());
        String nombreUsuario = "Usuario";
        if (rolUsuario.equals("paciente")) {
            nombreUsuario = bd.getNombrePaciente(idUsuario);
        } else if (rolUsuario.equals("doctor")) {
            nombreUsuario = bd.getNombreDoctor(idUsuario);
        }
        saludoUsuario.setText("¡Hola, " + nombreUsuario + "!");
    }

    private void irALogin() {
        SharedPreferences sp = getSharedPreferences("datos_usuario", MODE_PRIVATE);
        sp.edit().clear().apply();
        Intent intent = new Intent(InicioActivity.this, RolesActivity.class);
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