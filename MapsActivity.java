package com.example.animation.memorableplaces;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    LocationManager locationManager;
    LocationListener locationListener;
    int index = 0;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == 1){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);

                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

//        CheckEnableGPS();
    }


    private void CheckEnableGPS(){

        int GPSoff = 0;
        try {
            GPSoff = Settings.Secure.getInt(getContentResolver(),Settings.Secure.LOCATION_MODE);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        if (GPSoff == 1) {
            Toast.makeText(MapsActivity.this, "You Need to Turn ON your GPS",
                    Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }


    }

    public void pinLocation(Location location, String Address){

        LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.addMarker(new MarkerOptions().position(userLocation).title(Address));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(userLocation));

        if(index == 0) {

            MainActivity.places.add(Address);
            MainActivity.locations.add(userLocation);

            SharedPreferences sharePref = this.getSharedPreferences("com.example.animation.memorableplaces", Context.MODE_PRIVATE);

            ArrayList<String> Latitude = new ArrayList<String>();
            ArrayList<String> Longitude = new ArrayList<String>();
            for(LatLng cord:MainActivity.locations){
                Latitude.add(Double.toString(cord.latitude));
                Longitude.add(Double.toString(cord.longitude));
            }
            try {
                sharePref.edit().putString("places", ObjectSerializer.serialize(MainActivity.places)).apply();
                sharePref.edit().putString("lats",ObjectSerializer.serialize(Latitude)).apply();
                sharePref.edit().putString("longs", ObjectSerializer.serialize(Longitude)).apply();

            }catch (Exception e){
                e.printStackTrace();
            }

            MainActivity.placeadapter.notifyDataSetChanged();
        }
;    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        Intent intent = getIntent();
        index = intent.getIntExtra("placeNumber", 0);
        if (index == 0) {

            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    mMap.clear();
                    String address = "";

                    Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                    try {
                        List<Address> listAddresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                        if (listAddresses != null && listAddresses.size() > 0) {


                            if (listAddresses.get(0).getThoroughfare() != null) {
                                address += listAddresses.get(0).getThoroughfare();
                            }
                            if (listAddresses.get(0).getLocality() != null) {
                                address += " " + listAddresses.get(0).getLocality();
                            }
                            if (listAddresses.get(0).getAdminArea() != null) {
                                address += " " + listAddresses.get(0).getAdminArea();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if (address.equals("")) {
                        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm yyyy-MM--dd");
                        address += sdf.format(new Date());
                    }

                    pinLocation(location, address);
                }

                @Override
                public void onStatusChanged(String s, int i, Bundle bundle) {

                }

                @Override
                public void onProviderEnabled(String s) {

                }

                @Override
                public void onProviderDisabled(String s) {

                }
            };

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 10, locationListener);
//            mMap.clear();
//            Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//            LatLng userLocation = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
//
//            pinLocation(userLocation);

            }
        }else{
            Location placeLocation = new Location(LocationManager.GPS_PROVIDER);
            placeLocation.setLatitude(MainActivity.locations.get(intent.getIntExtra("placeNumber",0)).latitude);
            placeLocation.setLongitude(MainActivity.locations.get(intent.getIntExtra("placeNumber", 0)).longitude);

            pinLocation(placeLocation, MainActivity.places.get(intent.getIntExtra("placeNumber", 0)));
        }
    }

}
