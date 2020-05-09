package ru.intercom.testJava;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.gson.JsonObject;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.annotation.OnSymbolClickListener;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;
import com.mapbox.mapboxsdk.style.sources.GeoJsonOptions;
import com.mapbox.mapboxsdk.utils.ColorUtils;

import java.util.List;
import java.util.Objects;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textField;

public class MainActivity extends AppCompatActivity implements PermissionsListener {

    String MAKI_ICON_CIRCLE = "jopa";
    MapView mapView;
    PermissionsManager permissionsManager;
   MapboxMap mapboxMap;
    SymbolManager symbolManager;
    BottomSheetBehavior<View> mBottomSheetBehavior;
    BottomSheetBehavior<View> mBottomSheetBehavior2;

    TextView addCode2;
    TextView addCoordinates2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getString(R.string.access_token));
        setContentView(R.layout.activity_main);

        
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
                                @Override
                                public void onMapReady(@NonNull MapboxMap mapboxmap) {
                                    mapboxMap = mapboxmap;
                                    mapboxmap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
                                        @Override
                                        public void onStyleLoaded(@NonNull Style style) {
                                            style.addImage("jopa", getDrawable(R.drawable.ic_marker));
                                            //val mapText: Layer? = it.getLayer("country-label")
                                            // mapText!!.setProperties(textField("{name_ru}"))
                                         //   style.layers.forEach {
                                          //      style.setProperties(textField("{name_ru}"))
                                          //  }
                                            enableLocationPlugin(style);
                                            GeoJsonOptions geoJsonOptions = new GeoJsonOptions().withTolerance(0.4f);
                                            symbolManager = new SymbolManager(mapView, mapboxMap, style, null, geoJsonOptions);

                                            // loadFromDatabase()

                                            JsonObject jsonObject = new JsonObject();
                                            jsonObject.addProperty("number", "228");

                                            SymbolOptions nearbyOptions = new SymbolOptions()
                                                    .withLatLng(new LatLng(6.626384, 0.367099))
                                                    .withIconImage(MAKI_ICON_CIRCLE)
                                                    .withIconColor(ColorUtils.colorToRgbaString(Color.YELLOW))
                                                    .withIconSize(0.1f)
                                                    .withSymbolSortKey(5.0f)
                                                    .withDraggable(false)
                                                    .withData(jsonObject);
                                            symbolManager.create(nearbyOptions);
                                            symbolManager.addClickListener(new OnSymbolClickListener() {
                                                @Override
                                                public void onAnnotationClick(Symbol symbol) {
                                                    mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                                                    TextView textView= findViewById(R.id.number);
                                                    JsonObject jsonObject= Objects.requireNonNull(symbol.getData()).getAsJsonObject();
                                                    textView.setText(jsonObject.get("number").getAsString());
                                                    CameraPosition position = new CameraPosition.Builder()
                                                            .target(
                                                                    new LatLng(
                                                                            symbol.getLatLng().getLatitude(),
                                                                            symbol.getLatLng().getLongitude()
                                                                    )
                                                            ) // Sets the new camera position
                                                            .zoom(17.0) // Sets the zoom
                                                            .bearing(0.0) // Rotate the camera
                                                            .tilt(0.0) // Set the camera tilt
                                                            .build(); // Creates a CameraPosition from the builder


                                                    mapboxMap.animateCamera(
                                                            CameraUpdateFactory
                                                                    .newCameraPosition(position), 3000
                                                    );
                                                }
                                            });

                                        }
                                    });

                                }
                            });

                }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
       switch(requestCode)
        {
            case 1:{
            Log.d("jopa",data.toString());
            if (data != null) {
                Log.d("jopa",data.getStringExtra("pincode"));
                addCode2.setText(data.getStringExtra("pincode"));
            }
        break;}
            case 2:{
            Log.d("jopa",data.toString());
            if (data != null) {
                addCoordinates2.setText(data.getStringExtra("latitude")+", "+data.getStringExtra("longitude"));
            }
        break;}
        }
    }

    @SuppressWarnings( {"MissingPermission"})
    public void enableLocationPlugin(@NonNull Style loadedMapStyle) {
        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(this)) {

            // Get an instance of the component. Adding in LocationComponentOptions is also an optional
            // parameter
            LocationComponent locationComponent = mapboxMap.getLocationComponent();
            locationComponent.activateLocationComponent(LocationComponentActivationOptions.builder(
                    this, loadedMapStyle).build());
            locationComponent.setLocationComponentEnabled(true);

            // Set the component's camera mode
            locationComponent.setCameraMode(CameraMode.TRACKING);
            locationComponent.setRenderMode(RenderMode.NORMAL);

        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(this, "jopa", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted && mapboxMap != null) {
            Style style = mapboxMap.getStyle();
            if (style != null) {
                enableLocationPlugin(style);
            }
        } else {
            Toast.makeText(this,"jopa", Toast.LENGTH_LONG).show();
            finish();
        }
    }
}
