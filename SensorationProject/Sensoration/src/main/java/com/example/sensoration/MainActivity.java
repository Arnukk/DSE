package com.example.sensoration;

import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.Location;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.SensorEventListener;
import android.hardware.SensorEvent;
import android.widget.Toast;


public class MainActivity extends ActionBarActivity {

    private final static int NUM_PAGES = 3;
    private ViewPager mPager;
    private ScreenSlidePagerAdapter mAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Instantiate a ViewPager and a PagerAdapter.
        mPager = (ViewPager) findViewById(R.id.pager);
        mAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mAdapter);
    }


    /**
     * A simple pager adapter that represents 5 ScreenSlidePageFragment objects,
     * in sequence.
     */
    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {

            Fragment frag = null;

            switch (i) {
                case 0:
                    frag = new PageThreeFragment();
                    break;
                case 1:
                    frag = new PageOneFragment();
                    break;
                case 2:
                    frag = new PageTwoFragment();
                    break;

            }
            return frag;
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
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


    public class PageOneFragment extends Fragment {

        Button GetLocation;
        TextView latitude,longitude,location_text;
        private RelativeLayout tempLayout;
        ProgressBar progressBar;
        private String Mylongitude, Mylatitude;



        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

            tempLayout = (RelativeLayout) inflater.inflate(R.layout.fragment_main, container, false);
            latitude = (TextView) tempLayout.findViewById(R.id.latitude);
            longitude = (TextView) tempLayout.findViewById(R.id.longitude);
            progressBar = (ProgressBar) tempLayout.findViewById(R.id.progressBar);
            location_text = (TextView) tempLayout.findViewById(R.id.location_bottom_text);


            GetLocation = (Button) tempLayout.findViewById(R.id.getlocation);
            GetLocation.setOnTouchListener(TouchListener);
            GetLocation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    GetLocation.setEnabled(false);
                    location_text.setVisibility(View.INVISIBLE);
                    progressBar.setVisibility(View.VISIBLE);
                    new GetMyLocation().execute();
                }
            });


            return tempLayout;
        }


        final View.OnTouchListener TouchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        v.getBackground().setColorFilter(0xe0FFFD0C,android.graphics.PorterDuff.Mode.SRC_ATOP);
                        v.invalidate();
                        break;
                    }
                    case MotionEvent.ACTION_UP: {
                        v.getBackground().clearColorFilter();
                        v.invalidate();
                        break;
                    }
                }


                return false;
            }
        };

        public class GetMyLocation extends AsyncTask<Void, Integer, Void> {
            int myProgress;

            @Override
            protected void onPostExecute(Void result) {
                // TODO Auto-generated method stub
                if ((Mylatitude != "") && (Mylongitude != "")){
                    Toast.makeText(MainActivity.this,"Done, thank You for the patience !", Toast.LENGTH_SHORT).show();
                    new android.os.Handler().postDelayed(
                            new Runnable() {
                                public void run() {
                                    GetLocation.setEnabled(true);
                                    progressBar.setVisibility(View.INVISIBLE);
                                    latitude.setText(Mylatitude);
                                    longitude.setText(Mylongitude);
                                    location_text.setText(Html.fromHtml(getString(R.string.location_text)));
                                    location_text.setVisibility(View.VISIBLE);
                                }
                            }, 800);

                }else{
                    Toast.makeText(MainActivity.this,"Sorry, I am not able to locate You :(", Toast.LENGTH_SHORT).show();
                    new android.os.Handler().postDelayed(
                            new Runnable() {
                                public void run() {
                                    GetLocation.setEnabled(true);
                                    progressBar.setVisibility(View.INVISIBLE);
                                    latitude.setText("Unknown");
                                    longitude.setText("Unknown");
                                }
                            }, 800);

                }

            }

            @Override
            protected void onPreExecute() {
                // TODO Auto-generated method stub
                Toast.makeText(MainActivity.this,"Please wait, I am trying to locate You.", Toast.LENGTH_SHORT).show();
                myProgress = 0;
                Mylongitude = "";
                Mylatitude = "";
                LocationManager locationManager = (LocationManager) getSystemService(android.content.Context.LOCATION_SERVICE);
                // Define a listener that responds to location updates

                MyLocationListener myloc1 = new MyLocationListener();

                // Register the listener with the Location Manager to receive location updates
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, myloc1);
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, myloc1);


            }

            @Override
            protected Void doInBackground(Void... params) {
                // TODO Auto-generated method stub

                while(myProgress<50){
                    myProgress++;
                    publishProgress(myProgress);
                    if ((Mylatitude != "") && (Mylongitude != "")){
                        myProgress = 100;
                    }
                    SystemClock.sleep(100);
                }
                return null;
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                // TODO Auto-generated method stub
                progressBar.setProgress(values[0]);
            }

        }

        public class MyLocationListener implements LocationListener{

            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
                if(location.getProvider().equals(LocationManager.NETWORK_PROVIDER)){
                    Mylatitude = String.valueOf(location.getLatitude());
                    Mylongitude = String.valueOf(location.getLongitude());
                }
                else if(location.getProvider().equals(LocationManager.GPS_PROVIDER)){
                    Mylatitude = String.valueOf(location.getLatitude());
                    Mylongitude = String.valueOf(location.getLongitude());
                }

            }

            public void onStatusChanged(String provider, int status, Bundle extras) {}

            public void onProviderEnabled(String provider) {}

            public void onProviderDisabled(String provider) {}
        }

    }

    public class PageTwoFragment extends Fragment {
        TextView lightness, ligthnessbody;
        private RelativeLayout tempLayout;
        public int holdon = -1;
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
            tempLayout = (RelativeLayout) inflater.inflate(R.layout.fragment_second, container, false);
            lightness = (TextView) tempLayout.findViewById(R.id.lightness);
            ligthnessbody = (TextView) tempLayout.findViewById(R.id.lightness_body);
            SensorManager mSensorManager = (SensorManager) getSystemService(android.content.Context.SENSOR_SERVICE);
            MySensorListener mysensor = new MySensorListener();
            Sensor m1Sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
            mSensorManager.registerListener(mysensor, m1Sensor, 0);


            lightness.setText(Html.fromHtml(String.format(getString(R.string.lightness_header), holdon)));
            if (holdon > 50){
                ligthnessbody.setText(Html.fromHtml(String.format(getString(R.string.lightness), "DECREASE", "let Your eyes to relax a little ;-)")));
            }else{
                ligthnessbody.setText(Html.fromHtml(String.format(getString(R.string.lightness), "INCREASE", "otherwise You will get blind !!!")));
            }

            return tempLayout;
        }

        public class MySensorListener implements SensorEventListener {

            @Override
            public void onSensorChanged(SensorEvent event) {
                holdon = (int) event.values[0];
            }
            @Override
            public void onAccuracyChanged(Sensor arg0, int arg1) {
            }

        }
    }

    public class PageThreeFragment extends Fragment{
        private RelativeLayout tempLayout;
        TextView welcome;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
            tempLayout = (RelativeLayout) inflater.inflate(R.layout.fragment_third, container, false);

            welcome = (TextView) tempLayout.findViewById(R.id.welcome_body);
            welcome.setText(Html.fromHtml(getString(R.string.welcome)));

            return tempLayout;
        }
    }

}
