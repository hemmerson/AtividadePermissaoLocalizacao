package com.example.atividadepermissaolocalizacao;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatTextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.switchmaterial.SwitchMaterial;

import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private AppCompatEditText editTextLatitude, editTextLongitude;
    private AppCompatButton buttonVer;
    private SwitchMaterial switchMap;
    private AppCompatTextView textViewLatitude, textViewLongitude;
    private FloatingActionButton buttonNextPage;
    private MapView map;
    private FrameLayout mapContainer;
    private GeoPoint lastPosition; // Para armazenar a última posição
    private MapListener mapListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));

        setupViews();
        setupMap();
        setupSwitch();
        setupButtonVer();
        setupButtonNextPage();
    }

    private void setupButtonVer() {
        buttonVer.setOnClickListener(v -> {
            try {
                String latText = editTextLatitude.getText().toString().trim();
                String lonText = editTextLongitude.getText().toString().trim();

                if (latText.isEmpty() || lonText.isEmpty()) {
                    Toast.makeText(this, "Por favor, preencha latitude e longitude", Toast.LENGTH_SHORT).show();
                    return;
                }

                double latitude = Double.parseDouble(latText);
                double longitude = Double.parseDouble(lonText);

                // Atualiza os TextViews
                textViewLatitude.setText(String.valueOf(latitude));
                textViewLongitude.setText(String.valueOf(longitude));

                // Armazena a última posição
                lastPosition = new GeoPoint(latitude, longitude);

                updateMapPosition(lastPosition);

                Toast.makeText(this, "Localização atualizada!", Toast.LENGTH_SHORT).show();
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Digite valores válidos para latitude e longitude", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, "Erro ao atualizar o mapa: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupSwitch() {
        if (switchMap == null || map == null) return;

        // Criar o MapListener uma única vez
        mapListener = new MapListener() {
            @Override
            public boolean onScroll(ScrollEvent event) {
                if (lastPosition != null && map != null) {
                    map.getController().setCenter(lastPosition);
                }
                return true;
            }

            @Override
            public boolean onZoom(ZoomEvent event) {
                return false;
            }
        };

        switchMap.setOnCheckedChangeListener((buttonView, isChecked) -> {
            try {
                if (isChecked) {
                    lockMap();
                } else {
                    unlockMap();
                }
            } catch (Exception e) {
                Toast.makeText(this,
                        "Erro ao " + (isChecked ? "travar" : "destravar") + " o mapa",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void lockMap() {
        if (map == null) return;

        // Desabilita controles de interação
        map.setMultiTouchControls(false);
        map.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);
        map.setHorizontalMapRepetitionEnabled(false);
        map.setVerticalMapRepetitionEnabled(false);

        // Configura limites de zoom
        map.setMinZoomLevel(map.getZoomLevelDouble());
        map.setMaxZoomLevel(map.getZoomLevelDouble());

        // Retorna à posição salva com animação
        if (lastPosition != null) {
            map.getController().animateTo(lastPosition,
                    map.getZoomLevelDouble(),
                    500L // animação mais longa para transição inicial
            );
        }

        // Adiciona o listener
        map.addMapListener(mapListener);
    }

    private void unlockMap() {
        if (map == null) return;

        // Remove o listener primeiro
        map.removeMapListener(mapListener);

        // Reabilita controles
        map.setMultiTouchControls(true);
        map.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.SHOW_AND_FADEOUT);
        map.setHorizontalMapRepetitionEnabled(true);
        map.setVerticalMapRepetitionEnabled(true);

        // Restaura limites de zoom padrão
        map.setMinZoomLevel(null);
        map.setMaxZoomLevel(null);
    }

    private void updateMapPosition(GeoPoint point) {
        // Limpa marcadores anteriores
        map.getOverlays().clear();

        // Adiciona novo marcador
        Marker marker = new Marker(map);
        marker.setPosition(point);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setTitle("Sua Pesquisa");
        marker.setDraggable(true);
        marker.setOnMarkerDragListener(new Marker.OnMarkerDragListener() {
            @Override
            public void onMarkerDrag(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                lastPosition = marker.getPosition();
                textViewLatitude.setText(String.valueOf(lastPosition.getLatitude()));
                textViewLongitude.setText(String.valueOf(lastPosition.getLongitude()));
            }

            @Override
            public void onMarkerDragStart(Marker marker) {

            }
        });
        map.getOverlays().add(marker);

        // Centraliza o mapa no ponto
        map.getController().setCenter(point);
        map.getController().setZoom(15.0);

        // Força o redesenho do mapa
        map.invalidate();
    }

    private void setupButtonNextPage() {
        buttonNextPage.setOnClickListener(v -> {
            Intent intent = new Intent(this, SecondActivity.class);
            startActivity(intent);
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (map != null) {
            map.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (map != null) {
            map.onPause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (map != null) {
            map.removeMapListener(mapListener);
            mapListener = null;
        }
    }

    private void setupViews() {
        editTextLatitude = findViewById(R.id.editTextLatitude);
        editTextLongitude = findViewById(R.id.editTextLongitude);
        buttonVer = findViewById(R.id.buttonVer);
        switchMap = findViewById(R.id.switch1);
        textViewLatitude = findViewById(R.id.textViewLatitude);
        textViewLongitude = findViewById(R.id.textViewLongitude);
        buttonNextPage = findViewById(R.id.buttonNextPage);
        mapContainer = findViewById(R.id.fragmentMap);
    }

    private void setupMap() {
        map = new MapView(this);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);

        // Configuração inicial do mapa
        map.getController().setZoom(15.0);

        // Define uma posição inicial (São Paulo)
        lastPosition = new GeoPoint(-23.550520, -46.633308);
        map.getController().setCenter(lastPosition);

        // Adiciona o MapView ao FrameLayout
        mapContainer.addView(map);

        // Adiciona um marcador inicial
        updateMapPosition(lastPosition);
    }
}