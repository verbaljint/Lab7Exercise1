package th.ac.tu.siit.its333.lab7exercise1;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();
        WeatherTask w = new WeatherTask();
        w.execute("http://ict.siit.tu.ac.th/~cholwich/bangkok.json", "Bangkok Weather");
    }

    long past_visited = 0;
    int pastClickId = 0;

    public void reloadweather(int id) {
        WeatherTask w = new WeatherTask();
        switch (id) {
            case R.id.btBangkok:
                if (pastClickId != R.id.btBangkok || System.currentTimeMillis() - past_visited > 60000) {
                    w.execute("http://ict.siit.tu.ac.th/~cholwich/bangkok.json", "Bangkok Weather");
                    past_visited = System.currentTimeMillis();
                }
                break;
            case R.id.btNon:
                if (pastClickId != R.id.btNon || System.currentTimeMillis() - past_visited > 60000) {
                    w.execute("http://ict.siit.tu.ac.th/~cholwich/nonthaburi.json", "Nonthaburi Weather");
                    past_visited = System.currentTimeMillis();
                }
                break;
            case R.id.btPathum:
                if (pastClickId != R.id.btPathum || System.currentTimeMillis() - past_visited > 60000) {
                    w.execute("http://ict.siit.tu.ac.th/~cholwich/pathumthani.json", "Pathumthani Weather");
                    past_visited = System.currentTimeMillis();
                }
        }
        pastClickId = id;
    }

    public void buttonClicked(View v){
        int id = v.getId();
        reloadweather(id);
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

    class WeatherTask extends AsyncTask<String, Void, Boolean> {
        String errorMsg = "";
        ProgressDialog pDialog;
        String title;

        double windSpeed;
        double temp;
        double temp_min;
        double temp_max;
        double humid;
        String cloud;

        @Override
        protected void onPreExecute() {
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Loading weather data ...");
            pDialog.show();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            BufferedReader reader;
            StringBuilder buffer = new StringBuilder();
            String line;
            try {
                title = params[1];
                URL u = new URL(params[0]);
                HttpURLConnection h = (HttpURLConnection)u.openConnection();
                h.setRequestMethod("GET");
                h.setDoInput(true);
                h.connect();

                int response = h.getResponseCode();
                if (response == 200) {
                    reader = new BufferedReader(new InputStreamReader(h.getInputStream()));
                    while((line = reader.readLine()) != null) {
                        buffer.append(line);
                    }
                    //Start parsing JSON
                    JSONObject jWeather = new JSONObject(buffer.toString());
                    JSONObject jWind = jWeather.getJSONObject("wind");
                    JSONObject jMain = jWeather.getJSONObject("main");
                    JSONArray  jWeat = jWeather.getJSONArray("weather");

                    temp = jMain.getDouble("temp")-273;
                    cloud = jWeat.getJSONObject(0).getString("main");
                    temp_max = jMain.getDouble("temp_max")-273;
                    temp_min = jMain.getDouble("temp_min")-273;
                    humid = jMain.getDouble("humidity");
                    windSpeed = jWind.getDouble("speed");
                    errorMsg = "";
                    return true;
                }
                else {
                    errorMsg = "HTTP Error";
                }
            } catch (MalformedURLException e) {
                Log.e("WeatherTask", "URL Error");
                errorMsg = "URL Error";
            } catch (IOException e) {
                Log.e("WeatherTask", "I/O Error");
                errorMsg = "I/O Error";
            } catch (JSONException e) {
                Log.e("WeatherTask", "JSON Error");
                errorMsg = "JSON Error";
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            TextView tvTitle, tvWeather, tvWind, tvTemp, tvHumid;
            if (pDialog.isShowing()) {
                pDialog.dismiss();
            }

            tvTitle = (TextView)findViewById(R.id.tvTitle);
            tvWeather = (TextView)findViewById(R.id.tvWeather);
            tvWind = (TextView)findViewById(R.id.tvWind);
            tvTemp = (TextView)findViewById(R.id.tvTemp);
            tvHumid = (TextView)findViewById(R.id.tvHumid);

            if (result) {
                tvTitle.setText(title);
                tvWind.setText(String.format("%.1f", windSpeed));
                tvTemp.setText(String.format("%.1f", temp) + "(max = "+String.format("%.1f", temp_max) + "," + "min = "+String.format("%.1f",temp_min)+")");
                tvHumid.setText(humid+"%");

            }
            else {
                tvTitle.setText(errorMsg);
                tvWeather.setText("");
                tvWind.setText("");
            }
        }
    }
}
