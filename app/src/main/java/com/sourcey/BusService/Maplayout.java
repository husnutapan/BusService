package com.sourcey.BusService;

/**
 * Created by Ozgen on 6/29/16.
 */


import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.g;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.sourcey.BusService.com.sourcey.mapfunc.GISMapFunc;
import com.sourcey.BusService.com.sourcey.mapfunc.GPSTracker;
import com.sourcey.BusService.com.sourcey.mapfunc.JSONParser;

import android.app.ProgressDialog;
import android.content.Context;

import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Maplayout extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, LocationListener {

    private GoogleMap googleMap;
    public TextView text;

    public GPSTracker gps;
    public double latitude;
    public double longitude;

    public GISMapFunc gisMapFunc;

    Marker marker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.maplayout);


        gps = new GPSTracker(Maplayout.this);

        if (gps.canGetLocation()) {

            latitude = gps.getLatitude();
            longitude = gps.getLongitude();

            // \n is for new line
            Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + latitude + "\nLong: "
                    + longitude, Toast.LENGTH_SHORT).show();


        } else {
            // Can't get location.
            // GPS or network is not enabled.
            // Ask user to enable GPS/network in settings.
            gps.showSettingsAlert();
        }


        try {
            if (googleMap == null) {

                googleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();

            }


            LatLng userLocation = new LatLng(latitude, longitude);

            googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);


            LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 100, this);
            MarkerOptions marker = new MarkerOptions().position(userLocation)
                    .title("Here I am");

            marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));

            googleMap.addMarker(marker);

            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);
            gisMapFunc = new GISMapFunc();
            String url = gisMapFunc.makeURL(latitude, longitude, 39.921433, 32.835023);
            connectAsyncTask connectAsyncTask = new connectAsyncTask(url);
            connectAsyncTask.execute();

        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }

    }


    @Override
    public void onConnected(Bundle connectionHint) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onMyLocationChange(Location location) {
        // Creating a LatLng object for the current location
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        // Showing the current location in Google Map
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                latLng, 15);
        googleMap.animateCamera(cameraUpdate);
        marker = googleMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title("Current Location(You)")
                .snippet("Current")
                .icon(BitmapDescriptorFactory
                        .fromResource(R.drawable.markericon))
                .draggable(true));
    }


    @Override
    public void onConnectionSuspended(int cause) {
        // TODO Auto-generated method stub

    }


    @Override
    public void onLocationChanged(Location location) {
        // TODO Auto-generated method stub

    }


    @Override
    public void onProviderDisabled(String provider) {
        // TODO Auto-generated method stub

    }


    @Override
    public void onProviderEnabled(String provider) {
        // TODO Auto-generated method stub

    }


    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub

    }

    private class connectAsyncTask extends AsyncTask<Void, Void, String> {
        private ProgressDialog progressDialog;
        String url;

        connectAsyncTask(String urlPass) {
            url = urlPass;

        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
            progressDialog = new ProgressDialog(Maplayout.this);
            progressDialog.setMessage("Fetching route, Please wait...");
            progressDialog.setIndeterminate(true);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(Void... params) {
            JSONParser jParser = new JSONParser();
            String json = jParser.getJSONFromUrl(url);
            return json;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            progressDialog.hide();

            drawPath(result);

        }
    }

    private void drawPath(String result) {

        try {
            //Tranform the string into a json object
            final JSONObject json = new JSONObject(result);
            JSONArray routeArray = json.getJSONArray("routes");
            JSONObject routes = routeArray.getJSONObject(0);
            JSONObject overviewPolylines = routes.getJSONObject("overview_polyline");
            String encodedString = overviewPolylines.getString("points");
            List<LatLng> list = decodePoly(encodedString);
            Polyline line = googleMap.addPolyline(new PolylineOptions()
                    .addAll(list)
                    .width(12)
                    .color(Color.parseColor("#05b1fb"))//Google maps blue color
                    .geodesic(true)
            );
           /*
           for(int z = 0; z<list.size()-1;z++){
                LatLng src= list.get(z);
                LatLng dest= list.get(z+1);
                Polyline line = mMap.addPolyline(new PolylineOptions()
                .add(new LatLng(src.latitude, src.longitude), new LatLng(dest.latitude,   dest.longitude))
                .width(2)
                .color(Color.BLUE).geodesic(true));
            }
           */
        } catch (JSONException e) {
            Log.d("draw", e.getMessage());
        }
    }

    public List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }
}

