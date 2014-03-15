package com.example.chuckin;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class CreateActivity extends ActionBarActivity {
    Button post;
    TextView latitude, longitude;
    EditText name, description;
    Map<String, String> thedata = new HashMap<String, String>(2);
    Map<String, Double> thedata2 = new HashMap<String, Double>(2);
    JSONObject JsonData, JsonData1, JsonData2 = null;
    String MyURL;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);
        Bundle arguments = getIntent().getExtras();
        MyURL = arguments.getString("URL");
        post = (Button) CreateActivity.this.findViewById(R.id.post);
        post.setBackgroundResource(R.drawable.custom_button2);
        latitude = (TextView) CreateActivity.this.findViewById(R.id.latitude);
        longitude = (TextView) CreateActivity.this.findViewById(R.id.longitude);
        name = (EditText) CreateActivity.this.findViewById(R.id.name);
        description = (EditText) CreateActivity.this.findViewById(R.id.description);
        LocationManager locationManager = (LocationManager) getSystemService(android.content.Context.LOCATION_SERVICE);
        // Define a listener that responds to location updates

        MyLocationListener myloc1 = new MyLocationListener();

        // Register the listener with the Location Manager to receive location updates
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, myloc1);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, myloc1);

        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Double thelatitude = null;
                Double thelongitude = null;
                String thename = name.getText().toString();
                String thedescription = description.getText().toString();
                try{
                    String temp = latitude.getText().toString();
                    thelatitude = Double.parseDouble(temp);
                    temp = longitude.getText().toString();
                    thelongitude = Double.parseDouble(temp);
                } catch (NumberFormatException e) {
                    //do nothing
                }
                if (thename.length() > 0 && thedescription.length() > 0 && thelatitude != null && thelongitude != null){
                    thename = thename.replaceAll(" ", "+");
                    thedata.put("name",thename);
                    thedata.put("description",thedescription);
                    thedata2.put("latitude", thelatitude);
                    thedata2.put("longitude", thelongitude);
                    try {
                        Map[] theData = {thedata, thedata2};
                        JsonData = getJsonObjectFromMap(theData);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    if (JsonData != null && JsonData.length() > 0){
                        PosttoWebPage task = new PosttoWebPage();
                        task.execute(JsonData);
                    }else{
                        Toast.makeText(getApplicationContext(), "All the fields must be filled correctly", Toast.LENGTH_SHORT).show();
                    }

                }else{
                    Toast.makeText(getApplicationContext(), "Please fill all the fields appropriately", Toast.LENGTH_SHORT).show();
                }
            }
        });



    }


    class PosttoWebPage extends AsyncTask<JSONObject, Void, Integer> {

        // arguments are given by execute() method call (defined in the parent): params[0] is the url.
        @Override
        protected Integer doInBackground(JSONObject... params) {
            Integer response = 0;
            try {
                URL url = new URL(MyURL + "php/create.php");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestMethod("POST");
                OutputStreamWriter request = new OutputStreamWriter(connection.getOutputStream());
                Log.e("Post", params[0].toString());
                request.write(params[0].toString());
                request.flush();
                request.close();
                connection.connect();
                response = connection.getResponseCode();

            } catch (Exception e){
                e.printStackTrace();
            }
            return response;
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(Integer result) {
            if (result == 200)
                Toast.makeText(getApplicationContext(), "Your request have been successfully completed.", Toast.LENGTH_SHORT).show();
            else{
                Toast.makeText(getApplicationContext(), "Something went wrong with Your request, please try another credentials", Toast.LENGTH_SHORT).show();
            }

        }

    }

    private JSONObject getJsonObjectFromMap(Map[] params)
            throws JSONException {
        int i;
        JSONObject data = new JSONObject();
        for (i=0; i < params.length; i++)
        {
            Iterator iter = params[i].entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry pairs = (Map.Entry) iter.next();
                String key = (String) pairs.getKey();
                Object value = pairs.getValue();
                data.put((String) key, value);
            }
        }
        return data;
    }

    public class MyLocationListener implements LocationListener {

        public void onLocationChanged(Location location) {
            // Called when a new location is found by the network location provider.
            if(location.getProvider().equals(LocationManager.NETWORK_PROVIDER)){
                latitude.setText(String.valueOf(location.getLatitude()));
                longitude.setText(String.valueOf(location.getLongitude()));
            }
            else if(location.getProvider().equals(LocationManager.GPS_PROVIDER)){
                latitude.setText(String.valueOf(location.getLatitude()));
                longitude.setText(String.valueOf(location.getLongitude()));
            }else{
                Toast.makeText(getApplicationContext(), "Unable to retrieve your location", Toast.LENGTH_SHORT).show();
            }

        }

        public void onStatusChanged(String provider, int status, Bundle extras) {}

        public void onProviderEnabled(String provider) {}

        public void onProviderDisabled(String provider) {}
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.create, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_create, container, false);
            return rootView;
        }
    }

}
