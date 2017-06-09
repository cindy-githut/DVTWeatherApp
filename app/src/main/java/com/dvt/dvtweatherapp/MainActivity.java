package com.dvt.dvtweatherapp;

import android.location.Address;
import android.location.Geocoder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private TextView max;
    private TextView min;
    private TextView currentLocation;
    private ProgressBar progressBar;
    String currentAddress;
    String todaysDate;
    OkHttpClient client;
    String url;
    double latitude;
    double longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView txtTodaysDate = (TextView) findViewById(R.id.date);
        max = (TextView) findViewById(R.id.max);
        min = (TextView) findViewById(R.id.min);
        currentLocation = (TextView) findViewById(R.id.location);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        client = new OkHttpClient();

        try {

            //get todays date
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat df = new SimpleDateFormat("dd MMMM yyyy");
            todaysDate = df.format(cal.getTime());

            txtTodaysDate.setText("TODAY, " + todaysDate);

            getCurrentLocation();

            url = "http://api.openweathermap.org/data/2.5/weather?lat="+latitude+"&lon="+longitude+"&APPID=792da03bbab5f16d21c114557788130d";
            getWeatherInformation(url);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getWeatherInformation(String url){

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

                try{
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(MainActivity.this, "No internet connection", Toast.LENGTH_SHORT).show();
                        }
                    });
                }catch(Exception exc){
                    Log.d("Error:", exc.getLocalizedMessage());
                }

            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {

                final String responseString = response.body().string();

                try{

                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            progressBar.setVisibility(View.GONE);

                            if (response.isSuccessful()) {

                                try {

                                    JSONObject object = new JSONObject(responseString);
                                    handleServerResponse(object);

                                } catch (JSONException es) {
                                    es.printStackTrace();
                                }

                            }else{

                                Toast.makeText(MainActivity.this, "Something went wrong please, try again later.", Toast.LENGTH_SHORT).show();

                            }
                        }
                    });

                }catch(Exception exc){
                    Log.d("Error:", exc.getLocalizedMessage());
                }

            }
        });
    }

    private void handleServerResponse(JSONObject object) throws JSONException {

        if(object.has("main")){

            JSONObject mainObject = object.getJSONObject("main");

            if(mainObject.has("temp_min") && mainObject.has("temp_max") && mainObject.getString("temp_min") != null && mainObject.getString("temp_max") != null){

                max.setText("max " + doTheConvertions((Double.parseDouble(mainObject.getString("temp_max")))));
                min.setText("min " + doTheConvertions((Double.parseDouble(mainObject.getString("temp_min")))));

            }

        }

    }

    private String doTheConvertions(double Tk){

        /*convert Kelvin to Celcius
         * 0 degrees Kelvin is equal to -273.15 degrees Celsius:
         * The temperature Tc in degrees Celsius (°C) is equal to the temperature Tk in Kelvin (K) minus 273.15:
         * Celcius degree symbol \u2103
         */

        return Tk - 273.15 + " \u2103";

    }

    private void getCurrentLocation() throws IOException {

        // create class object
        GPSTracker gps = new GPSTracker(MainActivity.this);

        // check if GPS enabled
        if(gps.canGetLocation()){

            latitude = gps.latitude;
            longitude = gps.longitude;

            getAddress(latitude, longitude);

        }else{
            // can't get location
            // GPS or Network is not enabled
            // Ask user to enable GPS/network in settings
            gps.showSettingsAlert();
        }

    }
    private void getAddress(double latitude, double longitude) throws IOException {

        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());

        addresses = geocoder.getFromLocation(latitude, longitude, 1);

        String city = "";
        String country = "";

        if(addresses.get(0).getLocality() != null){
            city = addresses.get(0).getLocality();
        }

        if(addresses.get(0).getCountryName() != null){
            country = addresses.get(0).getCountryName();
        }

        currentAddress = city + ", " + country;
        currentLocation.setText(currentAddress);

    }

}
