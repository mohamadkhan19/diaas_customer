package com.diaas.cust;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.test.mock.MockPackageManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONObject;

import java.sql.Array;

import io.socket.client.Socket;
import io.socket.client.IO;
import io.socket.emitter.Emitter;

public class MainActivity extends AppCompatActivity {

    Button sendLoc, delivered;
    private io.socket.client.Socket socket;
    private String BASE_URL = "http://52.53.210.161";//"https://node-socketio-diaas.herokuapp.com";
    Button btnShowLocation;
    private static final int REQUEST_CODE_PERMISSION = 2;
    String mPermission = Manifest.permission.ACCESS_FINE_LOCATION;
    double latitude =0.0, longitude=0.0;
    double lati =37.396080836921676, longi=-121.9783493131399;
    double[] loc = new double[2];

    // GPSTracker class
    GPSTracker gps;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sendLoc = (Button) findViewById(R.id.sendLoc);
        delivered = (Button) findViewById(R.id.confirm_button);
        try {
            if (ActivityCompat.checkSelfPermission(this, mPermission)
                    != MockPackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this, new String[]{mPermission},
                        REQUEST_CODE_PERMISSION);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        btnShowLocation = (Button) findViewById(R.id.button);

        // show location button click event
        btnShowLocation.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // create class object
                gps = new GPSTracker(MainActivity.this);

                // check if GPS enabled
                if(gps.canGetLocation()){

                    latitude = gps.getLatitude();
                    longitude = gps.getLongitude();

                    // \n is for new line
                    Toast.makeText(getApplicationContext(), "Your Location is - \nLat: "
                            + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();
                }else{
                    // can't get location
                    // GPS or Network is not enabled
                    // Ask user to enable GPS/network in settings
                    gps.showSettingsAlert();
                }

            }
        });


        sendLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendinfo();

            }
        });

        delivered.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deliveryinfo();

            }
        });
        connection();
    }

    private void sendinfo(){
        try{
            JSONObject obj = new JSONObject();
            obj.put("Lat", latitude);
            obj.put("Long", longitude);
            socket.emit("trigger", obj);
        }
        catch (Exception e){

        }
    }

    private void deliveryinfo(){
        try{
            socket.emit("triggerdelivery", "item delivered");
        }
        catch (Exception e){

        }
    }

    private void connection(){
        try{
            socket = IO.socket(BASE_URL);
            socket.on(Socket.EVENT_CONNECT,onConnect);
            socket.on("location_resp", location);
            socket.connect();

        }
        catch(Exception e){
            Log.d("Exception", e.toString());

        }
    }

    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            socket.emit("message", "hi");
            System.out.print("Connection successful");
            Log.d("CONNECTION CHECK", "Connection successful");
            //socket.disconnect();
        }

    };

    private Emitter.Listener location = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("Location Received", args.toString());
                }
            });
        }
    };
}

