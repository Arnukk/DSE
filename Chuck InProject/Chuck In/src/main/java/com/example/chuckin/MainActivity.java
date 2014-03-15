package com.example.chuckin;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.internal.widget.ActionBarContainer;
import android.text.TextUtils;
import android.util.JsonReader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends ActionBarActivity {
    ListView listView;
    ImageButton load;
    InputStream response;
    ArrayList<BriefLocation> locations;
    FrameLayout maincontainer;
    Button checkin;
    String URL = "http://ec2-54-82-37-1.compute-1.amazonaws.com/";
    //FrameLayout container = (FrameLayout) findViewById(R.id.container);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = (ListView) findViewById(R.id.listView);
        load = (ImageButton) findViewById(R.id.load);
        load.setBackgroundResource(R.drawable.custom_button);
        maincontainer = (FrameLayout) findViewById(R.id.container);
        checkin = ( Button) findViewById(R.id.checkin);
        checkin.setBackgroundResource(R.drawable.custom_button2);
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isConnected()){
            maincontainer.setVisibility(View.INVISIBLE);
            Toast.makeText(getApplicationContext(), "Unable to connect to the internet, please check your internet connection", Toast.LENGTH_SHORT).show();
        }

        checkin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCreateActivity();
            }
        });

        load.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    DownloadWebpage task = new DownloadWebpage();
                    task.execute(URL + "locations");
            }
        });

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }


    class DownloadWebpage extends AsyncTask<String, Void, ArrayList> {

        // arguments are given by execute() method call (defined in the parent): params[0] is the url.
        protected ArrayList doInBackground(String... urls) {
            ArrayList st = null;
            try {
                st = downloadUrl(urls[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return st;
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(ArrayList result) {
            if (result != null && result.size() > 0)
                createListing(result);
            else{
                Toast.makeText(getApplicationContext(), "Unfortunately, an error occurred while reading the data from server", Toast.LENGTH_SHORT).show();
            }

        }

    }


    // Given a URL, this method establishes an HttpUrlConnection and retrieves
    // the web page content as an InputStream, which it returns as
    // a string.
    private ArrayList downloadUrl(String myurl) throws IOException {
        InputStream is = null;
        // Only display the first 500 characters of the retrieved
        // web page content.
        int len = 500;

        try {
            URL url = new URL(myurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000); /* milliseconds */
            conn.setConnectTimeout(15000);/* milliseconds */
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();
            int response = conn.getResponseCode();
            is = conn.getInputStream();
            locations = readJSONLocs(is);
            return locations;

            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } finally {
            if (is != null) {
                is.close();
            }
        }

    }


    public ArrayList<BriefLocation> readJSONLocs(InputStream in) throws IOException {
        ArrayList<BriefLocation> locs = new ArrayList<BriefLocation>();
        JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
        try {
            reader.beginArray();
            while (reader.hasNext()) {
                locs.add(readLoc(reader));
            }
            reader.endArray();
        } catch (Exception ex) {
            String err = (ex.getMessage()==null)?"JSON ERROR UNPREDICTABLE":ex.getMessage();
            Log.e("JSON ERROR:",err);
        } finally {
            reader.close();
        }
        return locs;
    }


    public BriefLocation readLoc(JsonReader reader) throws IOException {
        BriefLocation loc = new BriefLocation();

        reader.beginObject();
        while (reader.hasNext()) {

            String name = reader.nextName();
            Log.e("Loc", name);
            if (name.equals("name"))
                loc.name = reader.nextString();
            else if (name.equals("longitude"))
                loc.longitude = reader.nextDouble();
            else if (name.equals("latitude"))
                loc.latitude = reader.nextDouble();
            else {
                reader.skipValue();
            }
        }
        reader.endObject();

        return loc;
    }

    public class BriefLocation {
        public String name;
        public Double longitude, latitude;

        public BriefLocation() {
            name = new String();
            longitude = 0.0;
            latitude = 0.0;
        }
    }

    public void createListing(final ArrayList<BriefLocation> locs) {

        // create a list containing a map of two strings, first corresponds to
        // location names, second to (x,y) coordinates
        List<Map<String, String>> data = new ArrayList<Map<String, String>>();
        for (int i = 0; i < locs.size(); i++) {
            Map<String, String> datum = new HashMap<String, String>(2);
            datum.put("Loc", locs.get(i).name);
            datum.put("coord", locs.get(i).latitude + "," + locs.get(i).longitude);
            data.add(datum);
        }

        // simple adapter takes our data, info about the ListView we are using
        // which uses text1, text2 internally
        SimpleAdapter adapter = new SimpleAdapter(this, data, android.R.layout.simple_list_item_2,
                new String[] { "Loc", "coord" }, new int[] { android.R.id.text1, android.R.id.text2 });

        // link the list with the adapter
        listView.setAdapter(adapter);

        // whenever item is clicked, call a function showDescActivity which
        // actually opens com.example.chuckin.DescActivity
        listView.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String item = locs.get(position).name;
                showDescActivity(item);
            }
        });

    }

    public void showDescActivity(String name) {
        Intent intent = new Intent(this, DescActivity.class);
        intent.putExtra("name", name);
        intent.putExtra("URL", URL + "locations/" + name);
        startActivity(intent);

    }

    public void showCreateActivity() {
        Intent intent = new Intent(this, CreateActivity.class);
        intent.putExtra("URL", URL);
        startActivity(intent);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }
    }

}
