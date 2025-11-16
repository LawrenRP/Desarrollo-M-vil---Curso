package com.example.saludmovil.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BaseDeDatos extends SQLiteOpenHelper {

    private static final String NOMBRE_BD = "SaludMovil.db";
    private static final int VERSION_BD = 4;

    // --- Definición de las Tablas ---
    private static final String CREAR_TABLA_USUARIOS = "CREATE TABLE usuarios (id INTEGER PRIMARY KEY AUTOINCREMENT, correo TEXT UNIQUE NOT NULL, contrasena TEXT NOT NULL, rol TEXT NOT NULL CHECK(rol IN ('paciente', 'doctor')))";
    private static final String CREAR_TABLA_PACIENTES = "CREATE TABLE pacientes (id_usuario INTEGER PRIMARY KEY, dni TEXT UNIQUE NOT NULL, nombre TEXT NOT NULL, apellido TEXT NOT NULL, fecha_nacimiento TEXT NOT NULL, estatura REAL, peso REAL, tipo_sangre TEXT, sexo TEXT, alergias TEXT, enfermedades_cronicas TEXT, medicamentos_actuales TEXT, nombre_contacto_emergencia TEXT, celular_contacto_emergencia TEXT, FOREIGN KEY(id_usuario) REFERENCES usuarios(id))";
    private static final String CREAR_TABLA_ESPECIALIDADES = "CREATE TABLE especialidades (id INTEGER PRIMARY KEY AUTOINCREMENT, nombre TEXT UNIQUE NOT NULL, descripcion TEXT)";
    private static final String CREAR_TABLA_DOCTORES = "CREATE TABLE doctores (id_usuario INTEGER PRIMARY KEY, nombre_completo TEXT NOT NULL, dni TEXT UNIQUE NOT NULL, fecha_nacimiento TEXT NOT NULL, celular TEXT NOT NULL, numero_colegiatura TEXT, id_especialidad INTEGER, ruta_titulo_universitario TEXT, FOREIGN KEY(id_usuario) REFERENCES usuarios(id), FOREIGN KEY(id_especialidad) REFERENCES especialidades(id))";
    private static final String CREAR_TABLA_CITAS = "CREATE TABLE citas (id INTEGER PRIMARY KEY AUTOINCREMENT, id_paciente INTEGER NOT NULL, id_doctor INTEGER NOT NULL, fecha TEXT NOT NULL, hora TEXT NOT NULL, estado TEXT NOT NULL, motivo TEXT, FOREIGN KEY(id_paciente) REFERENCES pacientes(id_usuario), FOREIGN KEY(id_doctor) REFERENCES doctores(id_usuario))";
    private static final String CREAR_TABLA_HORARIOS = "CREATE TABLE horarios (id INTEGER PRIMARY KEY AUTOINCREMENT, id_doctor INTEGER NOT NULL, turno TEXT NOT NULL, dia_semana TEXT NOT NULL, hora_inicio TEXT NOT NULL, hora_fin TEXT NOT NULL, pacientes_por_turno INTEGER NOT NULL, FOREIGN KEY(id_doctor) REFERENCES doctores(id_usuario))";
    private static final String CREAR_TABLA_HISTORIAL_CLINICO = "CREATE TABLE historial_clinico (id INTEGER PRIMARY KEY AUTOINCREMENT, id_cita INTEGER UNIQUE NOT NULL, notas_doctor TEXT NOT NULL, diagnostico TEXT, fecha_creacion TEXT NOT NULL, FOREIGN KEY(id_cita) REFERENCES citas(id))";
    private static final String CREAR_TABLA_RECETAS_MEDICAS = "CREATE TABLE recetas_medicas (id INTEGER PRIMARY KEY AUTOINCREMENT, id_cita INTEGER UNIQUE NOT NULL, medicamentos TEXT NOT NULL, indicaciones TEXT, fecha_emision TEXT NOT NULL, FOREIGN KEY(id_cita) REFERENCES citas(id))";
    private static final String CREAR_TABLA_DOCUMENTOS = "CREATE TABLE documentos (id INTEGER PRIMARY KEY AUTOINCREMENT, id_cita INTEGER, id_usuario INTEGER NOT NULL, nombre_documento TEXT NOT NULL, ruta_documento TEXT NOT NULL, tipo_documento TEXT, FOREIGN KEY(id_cita) REFERENCES citas(id), FOREIGN KEY(id_usuario) REFERENCES usuarios(id))";

    public BaseDeDatos(@Nullable Context context) {
        super(context, NOMBRE_BD, null, VERSION_BD);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREAR_TABLA_USUARIOS);
        db.execSQL(CREAR_TABLA_ESPECIALIDADES);
        String[] especialidades = {"Medicina General", "Pediatría", "Cardiología", "Dermatología", "Ginecología", "Neurología", "Psicología"};
        ContentValues cv = new ContentValues();
        for (String esp : especialidades) {
            cv.put("nombre", esp);
            cv.put("descripcion", "Especialidad de " + esp);
            db.insert("especialidades", null, cv);
            cv.clear();
        }
        db.execSQL(CREAR_TABLA_PACIENTES);
        db.execSQL(CREAR_TABLA_DOCTORES);
        db.execSQL(CREAR_TABLA_CITAS);
        db.execSQL(CREAR_TABLA_HORARIOS);
        db.execSQL(CREAR_TABLA_HISTORIAL_CLINICO);
        db.execSQL(CREAR_TABLA_RECETAS_MEDICAS);
        db.execSQL(CREAR_TABLA_DOCUMENTOS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS documentos");
        db.execSQL("DROP TABLE IF EXISTS recetas_medicas");
        db.execSQL("DROP TABLE IF EXISTS historial_clinico");
        db.execSQL("DROP TABLE IF EXISTS horarios");
        db.execSQL("DROP TABLE IF EXISTS citas");
        db.execSQL("DROP TABLE IF EXISTS doctores");
        db.execSQL("DROP TABLE IF EXISTS pacientes");
        db.execSQL("DROP TABLE IF EXISTS especialidades");
        db.execSQL("DROP TABLE IF EXISTS usuarios");
        onCreate(db);
    }

    // --- MÉTODOS DE GESTIÓN DE USUARIOS (REGISTRO Y LOGIN) ---

    public long registrarUsuario(String correo, String contrasena, String rol) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("correo", correo);
        cv.put("contrasena", contrasena);
        cv.put("rol", rol);
        long id = db.insert("usuarios", null, cv);
        db.close();
        return id;
    }

    public void registrarPaciente(long idUsuario, String dni, String nombre, String apellido, String fechaNacimiento) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("id_usuario", idUsuario);
        cv.put("dni", dni);
        cv.put("nombre", nombre);
        cv.put("apellido", apellido);
        cv.put("fecha_nacimiento", fechaNacimiento);
        db.insert("pacientes", null, cv);
        db.close();
    }

    public void registrarDoctorPaso1(long idUsuario, String nombreCompleto, String dni, String fechaNacimiento, String celular) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("id_usuario", idUsuario);
        cv.put("nombre_completo", nombreCompleto);
        cv.put("dni", dni);
        cv.put("fecha_nacimiento", fechaNacimiento);
        cv.put("celular", celular);
        db.insert("doctores", null, cv);
        db.close();
    }

    public void registrarDoctorPaso2(long idUsuario, String numColegiatura, int idEspecialidad, String rutaTitulo) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("numero_colegiatura", numColegiatura);
        cv.put("id_especialidad", idEspecialidad);
        cv.put("ruta_titulo_universitario", rutaTitulo);
        db.update("doctores", cv, "id_usuario = ?", new String[]{String.valueOf(idUsuario)});
        db.close();
    }

    public Cursor login(String correo, String contrasena) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT id, rol FROM usuarios WHERE correo = ? AND contrasena = ?", new String[]{correo, contrasena});
    }

    public Cursor loginPacientePorDNI(String dni, String contrasena) {
        SQLiteDatabase db = this.getReadableDatabase();
        // Esta consulta une las dos tablas para encontrar al usuario a través del DNI del paciente
        String query = "SELECT u.id, u.rol FROM usuarios u " +
                "JOIN pacientes p ON u.id = p.id_usuario " +
                "WHERE p.dni = ? AND u.contrasena = ?";
        return db.rawQuery(query, new String[]{dni, contrasena});
    }

    public Cursor loginDoctorPorCMP(String cmp, String contrasena) {
        SQLiteDatabase db = this.getReadableDatabase();
        // Esta consulta une las dos tablas para encontrar al usuario a través del CMP del doctor
        String query = "SELECT u.id, u.rol FROM usuarios u " +
                "JOIN doctores d ON u.id = d.id_usuario " +
                "WHERE d.numero_colegiatura = ? AND u.contrasena = ?";
        return db.rawQuery(query, new String[]{cmp, contrasena});
    }

    // --- MÉTODOS PARA OBTENER Y ACTUALIZAR PERFILES ---

    public String getNombrePaciente(int idUsuario) {
        SQLiteDatabase db = this.getReadableDatabase();
        String nombre = "Usuario";
        Cursor cursor = db.rawQuery("SELECT nombre FROM pacientes WHERE id_usuario = ?", new String[]{String.valueOf(idUsuario)});
        if (cursor.moveToFirst()) {
            int nombreIndex = cursor.getColumnIndex("nombre");
            if (nombreIndex != -1) nombre = cursor.getString(nombreIndex);
        }
        cursor.close();
        db.close();
        return nombre;
    }

    public String getNombreDoctor(int idUsuario) {
        SQLiteDatabase db = this.getReadableDatabase();
        String nombre = "Doctor";
        Cursor cursor = db.rawQuery("SELECT nombre_completo FROM doctores WHERE id_usuario = ?", new String[]{String.valueOf(idUsuario)});
        if (cursor.moveToFirst()) {
            int nombreIndex = cursor.getColumnIndex("nombre_completo");
            if (nombreIndex != -1) nombre = cursor.getString(nombreIndex);
        }
        cursor.close();
        db.close();
        return nombre;
    }

    public boolean isPerfilCompletoPorId(int idUsuario) {
        SQLiteDatabase db = this.getReadableDatabase();
        boolean completo = false;
        Cursor cursor = db.rawQuery("SELECT estatura, peso FROM pacientes WHERE id_usuario = ? AND estatura IS NOT NULL AND peso IS NOT NULL", new String[]{String.valueOf(idUsuario)});
        if (cursor.moveToFirst()) {
            completo = true;
        }
        cursor.close();
        db.close();
        return completo;
    }

    public Cursor getPerfilPacientePorId(int idUsuario) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM pacientes WHERE id_usuario = ?", new String[]{String.valueOf(idUsuario)});
    }

    public void actualizarPerfilPacientePorId(int idUsuario, String estatura, String peso, String tipoSangre, String sexo, String alergias, String enfermedades, String medicamentos, String contactoNombre, String contactoTelefono) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("estatura", estatura);
        cv.put("peso", peso);
        cv.put("tipo_sangre", tipoSangre);
        cv.put("sexo", sexo);
        cv.put("alergias", alergias);
        cv.put("enfermedades_cronicas", enfermedades);
        cv.put("medicamentos_actuales", medicamentos);
        cv.put("nombre_contacto_emergencia", contactoNombre);
        cv.put("celular_contacto_emergencia", contactoTelefono);
        db.update("pacientes", cv, "id_usuario = ?", new String[]{String.valueOf(idUsuario)});
        db.close();
    }

    // --- MÉTODOS DE GESTIÓN DE CITAS ---

    public boolean agendarCita(int idPaciente, int idDoctor, String fecha, String hora, String motivo) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("id_paciente", idPaciente);
        cv.put("id_doctor", idDoctor);
        cv.put("fecha", fecha);
        cv.put("hora", hora);
        cv.put("estado", "agendada");
        cv.put("motivo", motivo);
        long resultado = db.insert("citas", null, cv);
        db.close();
        return resultado != -1;
    }

    public Cursor getCitasPaciente(int idPaciente) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT c.*, d.nombre_completo, e.nombre as nombre_especialidad FROM citas c " + "JOIN doctores d ON c.id_doctor = d.id_usuario " + "JOIN especialidades e ON d.id_especialidad = e.id " + "WHERE c.id_paciente = ? ORDER BY c.fecha DESC";
        return db.rawQuery(query, new String[]{String.valueOf(idPaciente)});
    }

    public Cursor getCitasDoctor(int idDoctor) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT c.*, p.nombre, p.apellido FROM citas c " + "JOIN pacientes p ON c.id_paciente = p.id_usuario " + "WHERE c.id_doctor = ? ORDER BY c.fecha DESC";
        return db.rawQuery(query, new String[]{String.valueOf(idDoctor)});
    }

    // --- MÉTODOS DE GESTIÓN DE ESPECIALIDADES ---

    public int getIdEspecialidad(String nombreEspecialidad) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id FROM especialidades WHERE nombre = ?", new String[]{nombreEspecialidad});
        if (cursor.moveToFirst()) {
            int idIndex = cursor.getColumnIndex("id");
            if (idIndex != -1) {
                int id = cursor.getInt(idIndex);
                cursor.close();
                return id;
            }
        }
        cursor.close();
        // Si no existe, la creamos
        SQLiteDatabase dbWrite = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("nombre", nombreEspecialidad);
        cv.put("descripcion", "Descripción para " + nombreEspecialidad);
        long newId = dbWrite.insert("especialidades", null, cv);
        dbWrite.close();
        return (int) newId;
    }

    public Cursor getEspecialidades() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM especialidades ORDER BY nombre ASC", null);
    }

    // --- MÉTODOS DE GESTIÓN DE HISTORIAL CLÍNICO, RECETAS Y DOCUMENTOS ---

    public boolean addHistorialClinico(int idCita, String notas, String diagnostico) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("id_cita", idCita);
        cv.put("notas_doctor", notas);
        cv.put("diagnostico", diagnostico);
        cv.put("fecha_creacion", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
        long resultado = db.insert("historial_clinico", null, cv);
        db.close();
        return resultado != -1;
    }

    public boolean addRecetaMedica(int idCita, String medicamentos, String indicaciones) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("id_cita", idCita);
        cv.put("medicamentos", medicamentos);
        cv.put("indicaciones", indicaciones);
        cv.put("fecha_emision", new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));
        long resultado = db.insert("recetas_medicas", null, cv);
        db.close();
        return resultado != -1;
    }

    public Cursor getRecetasDeCita(int idCita) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM recetas_medicas WHERE id_cita = ?", new String[]{String.valueOf(idCita)});
    }

    public boolean addDocumento(int idUsuario, int idCita, String nombreDoc, String rutaDoc, String tipoDoc) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("id_usuario", idUsuario);
        cv.put("id_cita", idCita);
        cv.put("nombre_documento", nombreDoc);
        cv.put("ruta_documento", rutaDoc);
        cv.put("tipo_documento", tipoDoc);
        long resultado = db.insert("documentos", null, cv);
        db.close();
        return resultado != -1;
    }

    public Cursor getDocumentosDeCita(int idCita) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM documentos WHERE id_cita = ?", new String[]{String.valueOf(idCita)});
    }

    public Cursor getDoctoresPorEspecialidad(int idEspecialidad){
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "select id_usuario, nombre_completo FROM doctores where id_especialidad = ?";
        return db.rawQuery(query, new String[]{String.valueOf(idEspecialidad)});
    }

    public Cursor getProximaCita(int idPaciente){
        SQLiteDatabase db = this.getReadableDatabase();
        String hoy = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String query = "SELECT c.fecha, c.hora, d.nombre_completo " +
                "FROM citas c " +
                "JOIN doctores d ON c.id_doctor = d.id_usuario " +
                "WHERE c.id_paciente = ? AND c.estado = 'agendada' AND c.fecha >= ? " +
                "ORDER BY c.fecha ASC, c.hora ASC LIMIT 1";
        return db.rawQuery(query, new String[]{String.valueOf(idPaciente), hoy});
    }
    public Cursor getProximaCitaDoctor(int idDoctor){
        SQLiteDatabase db = this.getReadableDatabase();
        String hoy = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String query = "SELECT c.fecha, c.hora, p.nombre, p.apellido " +
                "FROM citas c " +
                "JOIN pacientes p ON c.id_paciente = p.id_usuario " +
                "WHERE c.id_doctor = ? AND c.estado = 'agendada' AND c.fecha >= ? " +
                "ORDER BY c.fecha ASC, c.hora ASC LIMIT 1";
        return db.rawQuery(query, new String[]{String.valueOf(idDoctor), hoy});
    }

    public Cursor getTodasCitasDoctor(int idDoctor){
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT c.id, c.fecha, c.hora, c.estado, c.motivo, p.nombre, p.apellido " +
                "FROM citas c " +
                "JOIN pacientes p ON c.id_paciente = p.id_usuario " +
                "WHERE c.id_doctor = ? " +
                "ORDER BY c.fecha DESC, c.hora DESC";
        return db.rawQuery(query, new String[]{String.valueOf(idDoctor)});
    }

    public boolean actualizarEstadoCita(int idCita, String nuevoEstado){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("estado", nuevoEstado);
        int filasAfectadas = db.update("citas", cv, "id = ?", new String[]{String.valueOf(idCita)});
        db.close();
        return filasAfectadas > 0;
    }
    public Cursor getTodasCitasPaciente(int idPaciente) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT c.id, c.fecha, c.hora, c.estado, c.motivo, d.nombre_completo " +
                "FROM citas c " +
                "JOIN doctores d ON c.id_doctor = d.id_usuario " +
                "WHERE c.id_paciente = ? " +
                "ORDER BY c.fecha DESC, c.hora DESC";
        return db.rawQuery(query, new String[]{String.valueOf(idPaciente)});
    }

    public Cursor getPerfilDoctor(int idUsuarioDoctor){
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT d.*, u.correo " +
                "FROM doctores d " +
                "JOIN usuarios u ON d.id_usuario = u.id " +
                "WHERE d.id_usuario = ?";
        return db.rawQuery(query, new String[]{String.valueOf(idUsuarioDoctor)});
    }

    public void actualizarPerfilDoctor(int idUsuarioDoctor, String nuevoTelefono, String nuevoCorreo){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cvDoctor = new ContentValues();
        cvDoctor.put("celular", nuevoTelefono);
        db.update("doctores", cvDoctor, "id_usuario = ?", new String[]{String.valueOf(idUsuarioDoctor)});
        ContentValues cvUsuario = new ContentValues();
        cvUsuario.put("correo", nuevoCorreo);
        db.update("usuarios", cvUsuario, "id = ?", new String[]{String.valueOf(idUsuarioDoctor)});
        db.close();
    }

    public void actualizarTituloDoctor(int idUsuarioDoctor, String nuevaRutaTitulo) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("ruta_titulo_universitario", nuevaRutaTitulo);
        db.update("doctores", cv, "id_usuario = ?", new String[]{String.valueOf(idUsuarioDoctor)});
        db.close();
    }

}