package com.example.reincarnation;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends ActionBarActivity {
    WebView body;
    ImageButton load;
    View v;
    String filename = "index.html";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String filedir;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        load = (ImageButton) findViewById(R.id.load);
        load.setBackgroundResource(R.drawable.custom_button);
        body = (WebView) findViewById(R.id.body);
        body.setBackgroundColor(0);
        body.getSettings().setJavaScriptEnabled(true);
        body.setBackgroundResource(R.drawable.gradient_green);
        body.setWebChromeClient(new WebChromeClient());
        body.reload();
        try {
            FileInputStream inputStream =  openFileInput(filename);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            reader.close();
            inputStream.close();
            filedir = "file:///" + getFilesDir().getPath() + "/"+ filename;
            body.loadUrl(filedir);
            Log.v("storage app", "data loaded..");

        } catch (Exception e) {
            e.printStackTrace();
            Log.v("storage app", "Error: data is not loaded.." + e.getMessage());

        }
        //load.setOnTouchListener(TouchListener);

        load.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
                String currentDateandTime = sdf.format(new Date());
                ConnectivityManager connMgr = (ConnectivityManager)
                        getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.isConnected()) {
                    DownloadWebpageText task = new DownloadWebpageText();
                    task.execute("http://md5.jsontest.com/?text=" + currentDateandTime);
                } else {
                    // display error
                }
            }
        });

    }

    final View.OnTouchListener TouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    v.setBackgroundResource(R.drawable.refresh_yellow);
                    v.invalidate();
                    break;
                }
                case MotionEvent.ACTION_UP: {
                    v.setBackgroundResource(R.drawable.refresh_blue);
                    v.invalidate();
                    break;
                }
            }


            return false;
        }
    };

    private class DownloadWebpageText extends AsyncTask {
        // arguments are given by execute() method call (defined in the parent): params[0] is the url.
        protected String doInBackground(Object... urls) {
            try {
                String st = downloadUrl((String) urls[0]);
                return st;
            } catch (IOException e) {
                return "Unable to retrieve web page. URL may be invalid.";
            }
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(Object result) {
            String sd = (String) result;
            FileOutputStream outputStream;
            String filename = "index.html";
            String filedir;
            try {
                JSONObject jObject = new JSONObject(sd);
                String aJsonString = jObject.getString("original");
                String template = String.format("<!DOCTYPE html><html><body><div style='text-align:justify; width:100%%; display:block; font-size:18;'>\n" +
                        "        It seems that You have requested <b>http://www.jsontest.com/</b> to return in JSON format the MD5 hash of the <b><u>%1$s</u></b> string. <p>Here is Your response ;-)</p> \n" +
                        "        <span style='text-align:left; width:100%%; display:block; background-color:#020203; color:#0EFF0A; white-space: normal; word-wrap:break-word; font-size:20px; padding-top:10px; padding-bottom:10px; margin-top:10'><code>%2$s</code></span>\n" +
                        "        </div></body></html>", TextUtils.htmlEncode(aJsonString) , TextUtils.htmlEncode(sd));
                Log.i("+++++++++++++++++++++++++++++++++++++++++++++", template);


                try {
                    outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
                    outputStream.write(template.getBytes());
                    outputStream.close();
                    Log.v("storage app", "data saved..");
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.v("storage app", "Error: data is not saved");

                }
                filedir = "file:///" + getFilesDir().getPath() + "/"+ filename;
                body.loadUrl(filedir);
                //template = String.format(getString(R.string.template), TextUtils.htmlEncode(aJsonString) , TextUtils.htmlEncode(sd));

            } catch (JSONException e) {
                Log.e("JSON Parser", "Error parsing data " + e.toString());
            }


        }

    }

    // Given a URL, this method establishes an HttpUrlConnection and retrieves
    // the web page content as an InputStream, which it returns as
    // a string.
    private String downloadUrl(String myurl) throws IOException {
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
            Log.d("network", "The response is: " + response);
            is = conn.getInputStream();

            // Convert the InputStream into a string
            String contentAsString = convertStreamToString(is);
            return contentAsString;

            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    private String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
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
