package com.example.animation.memorableplaces;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    static ArrayList<String> places = new ArrayList<String>();
    static ArrayList<LatLng> locations = new ArrayList<LatLng>();
    ListView placesListView;
    static ArrayAdapter<String> placeadapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences sharedPref = this.getSharedPreferences("com.example.animation.memorableplaces", Context.MODE_PRIVATE);
        ArrayList<String> Latitude = new ArrayList<String>();
        ArrayList<String> Longitude = new ArrayList<String>();
        places.clear();
        Latitude.clear();
        Longitude.clear();
        locations.clear();

        try {
            places = (ArrayList) ObjectSerializer.deserialize(sharedPref.getString("places", ObjectSerializer.serialize(new ArrayList<String>())));
            Latitude = (ArrayList)ObjectSerializer.deserialize(sharedPref.getString("lats", ObjectSerializer.serialize(new ArrayList<String>())));
            Longitude = (ArrayList)ObjectSerializer.deserialize(sharedPref.getString("longs", ObjectSerializer.serialize(new ArrayList<String>())));

        }catch(Exception e){
            e.printStackTrace();
        }
        if(places.size() > 0 && Latitude.size() > 0 && Longitude.size() > 0 && places.size() == Latitude.size() && places.size() == Longitude.size()){
            for(int i = 0; i < Latitude.size(); i ++){
                locations.add(new LatLng(Double.parseDouble(Latitude.get(i)), Double.parseDouble(Longitude.get(i))));
            }
        }else {

            places.add("Add a New Place....");
            locations.add(new LatLng(0, 0));
        }
        placesListView = (ListView)findViewById(R.id.placesListView);
        placeadapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,places);
        placesListView.setAdapter(placeadapter);

        placesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getApplicationContext(),MapsActivity.class);
                intent.putExtra("placeNumber", i);
                startActivity(intent);

            }
        });



    }
}
