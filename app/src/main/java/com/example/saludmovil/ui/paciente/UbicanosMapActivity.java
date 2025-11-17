package com.example.saludmovil.ui.paciente;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.saludmovil.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.maps.android.PolyUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class UbicanosMapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap miMapa;
    private FusedLocationProviderClient clienteUbicacion;
    private RequestQueue colaDePeticiones;
    private MaterialToolbar toolbar;
    private MaterialButton btnObtenerDirecciones;

    private final LatLng UBICACION_CLINICA = new LatLng(-12.001943, -76.999517);
    private final String NOMBRE_CLINICA = "Posta Médica Salud Móvil";
    private static final int CODIGO_PERMISO_UBICACION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ubicanos_map);

        toolbar = findViewById(R.id.toolbarMap);
        btnObtenerDirecciones = findViewById(R.id.btnObtenerDirecciones);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        clienteUbicacion = LocationServices.getFusedLocationProviderClient(this);
        colaDePeticiones = Volley.newRequestQueue(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        btnObtenerDirecciones.setOnClickListener(v -> {
            Uri gmmIntentUri = Uri.parse("google.navigation:q=" + UBICACION_CLINICA.latitude + "," + UBICACION_CLINICA.longitude);
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");

            if (mapIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(mapIntent);
            } else {
                Toast.makeText(this, "Google Maps no está instalado.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        miMapa = googleMap;
        miMapa.addMarker(new MarkerOptions().position(UBICACION_CLINICA).title(NOMBRE_CLINICA));


        miMapa.moveCamera(CameraUpdateFactory.newLatLngZoom(UBICACION_CLINICA, 15f));

        activarMiUbicacion();
    }

    private void activarMiUbicacion() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (miMapa != null) {
                miMapa.setMyLocationEnabled(true);
                obtenerUbicacionYTrazarRuta();
            }
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, CODIGO_PERMISO_UBICACION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CODIGO_PERMISO_UBICACION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                activarMiUbicacion();
            } else {
                Toast.makeText(this, "Permiso de ubicación denegado.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void obtenerUbicacionYTrazarRuta() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        clienteUbicacion.getLastLocation().addOnSuccessListener(this, ubicacion -> {
            if (ubicacion != null) {
                LatLng origen = new LatLng(ubicacion.getLatitude(), ubicacion.getLongitude());
                trazarRuta(origen, UBICACION_CLINICA);
            } else {
                Toast.makeText(this, "No se pudo obtener la ubicación actual.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void trazarRuta(LatLng origen, LatLng destino) {
        String url = "https://maps.googleapis.com/maps/api/directions/json?origin="
                + origen.latitude + "," + origen.longitude + "&destination="
                + destino.latitude + "," + destino.longitude + "&key=" + obtenerClaveApi();

        JsonObjectRequest peticionJson = new JsonObjectRequest(Request.Method.GET, url, null,
                respuesta -> {
                    try {
                        if (respuesta.getJSONArray("routes").length() > 0) {
                            JSONObject ruta = respuesta.getJSONArray("routes").getJSONObject(0);
                            JSONObject polilineaResumen = ruta.getJSONObject("overview_polyline");
                            String polilineaCodificada = polilineaResumen.getString("points");
                            List<LatLng> listaDePuntos = PolyUtil.decode(polilineaCodificada);
                            miMapa.addPolyline(new PolylineOptions().addAll(listaDePuntos).width(12).color(Color.BLUE).geodesic(true));
                        } else {
                            Toast.makeText(this, "No se encontraron rutas.", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error al procesar la ruta.", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(this, "Error al obtener la ruta.", Toast.LENGTH_SHORT).show();
                });

        colaDePeticiones.add(peticionJson);
    }

    private String obtenerClaveApi() {
        try {
            ApplicationInfo infoApp = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = infoApp.metaData;
            return bundle.getString("com.google.android.geo.API_KEY");
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}