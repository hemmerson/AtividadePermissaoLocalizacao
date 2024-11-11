package com.example.atividadepermissaolocalizacao;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.Manifest;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

public class SecondActivity extends AppCompatActivity {
    private MaterialButton buttonMyLocation;
    private FloatingActionButton buttonPreviousPage;
    private MapView mapSecond;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final String PERMISSION = Manifest.permission.ACCESS_FINE_LOCATION;
    private FusedLocationProviderClient clientLocation;

    private final String[] PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        setupViews();
        setupMap();

        clientLocation = LocationServices.getFusedLocationProviderClient(this);
        buttonMyLocation.setOnClickListener(v -> {
            clicar();
        });

        buttonPreviousPage.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void clicar() {
        solicitarPermissao();
    }

    private void solicitarPermissao() {
        int temPermissao = ContextCompat.checkSelfPermission(this, PERMISSION);
        if (temPermissao != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{PERMISSION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getLocation();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        mapSecond.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapSecond.onPause();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        } else {
            if (grantResults.length > 0) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getLocation();
                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, PERMISSION)) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle("Atenção")
                                .setMessage("Permissão necessária para recuperar a localização!")
                                .setCancelable(false)
                                .setPositiveButton("SIM", (dialog, which) -> {
                                    ActivityCompat.requestPermissions(SecondActivity.this, new String[]{PERMISSION}, LOCATION_PERMISSION_REQUEST_CODE);
                                })
                                .setNegativeButton("NÃO", (dialog, which) -> {
                                    Toast.makeText(SecondActivity.this, "Precisa da permissão para funcionar... ADEUS...", Toast.LENGTH_SHORT).show();
                                    finish();
                                });
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                }
            } else {
                finish();
            }
        }
    }

    private void setupViews() {
        buttonMyLocation = findViewById(R.id.buttonMyLocation);
        buttonPreviousPage = findViewById(R.id.buttonPreviousPage);
        mapSecond = findViewById(R.id.mapSecond);
    }

    private void setupMap() {
        mapSecond.setTileSource(TileSourceFactory.MAPNIK);
        mapSecond.setMultiTouchControls(true);
        mapSecond.getController().setZoom(15.0);
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            return;
        }

        LocationRequest locationRequest = LocationRequest.create()
                        .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                                .setInterval(10000);

        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    GeoPoint point = new GeoPoint(location.getLatitude(), location.getLongitude());
                    addMarcador(point);
                    clientLocation.removeLocationUpdates(this);
                }
            }
        };

        clientLocation.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                GeoPoint point = new GeoPoint(location.getLatitude(), location.getLongitude());
                addMarcador(point);
            } else {
                clientLocation.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
            }
        })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erro ao obter localização: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void addMarcador(GeoPoint point) {
        Marker marker = new Marker(mapSecond);
        marker.setPosition(point);
        marker.setTitle("Você está aqui");
        mapSecond.getOverlays().add(marker);
        mapSecond.getController().setZoom(15.0);
        mapSecond.getController().setCenter(marker.getPosition());
    }

}