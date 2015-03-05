package com.nobroker.nbassignment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;


public class MainActivity extends Activity {
    private Spinner citySpinner;
    private String citySelected;
    private AutoCompleteTextView actv;
    private String[] localityNames;
    private String[] latlng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        addCitySpinner();
        addListenerOnSpinnerItemSelection();
        addSearchButton();
    }

    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first
        if (actv != null) {
            actv.setText("");
        }
    }

    public void addCitySpinner() {
        citySpinner = (Spinner) findViewById(R.id.cities_spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.cities_array, R.layout.spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        citySpinner.setAdapter(adapter);
    }

    public void addSearchButton() {
        final Button button = (Button) findViewById(R.id.findbutton);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String locality = actv.getText().toString();
                if (!locality.equals("")) {
                    int idx = Arrays.asList(localityNames).indexOf(locality);
                    if (idx >= 0) {
                        Intent myIntent = new Intent(v.getContext(), SearchDisplayActivity.class);
                        myIntent.putExtra("CURRENT_CITY", citySelected);
                        myIntent.putExtra("LOCALITY_NAME", locality);
                        myIntent.putExtra("LAT_LNG", latlng[idx]);
                        startActivityForResult(myIntent, 0);
                    }
                }
            }
        });
    }

    public void addAutoCompleteLocalitySelector() {
        ArrayAdapter adapter = new ArrayAdapter
                (this, android.R.layout.simple_list_item_1, localityNames);
        actv = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView1);
        actv.setAdapter(adapter);
    }

    public void addListenerOnSpinnerItemSelection() {
        citySpinner = (Spinner) findViewById(R.id.cities_spinner);
        citySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                citySelected = parent.getItemAtPosition(position).toString().toLowerCase();
                new FetchLocality().execute(citySelected);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.
                INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        return true;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {

        View v = getCurrentFocus();
        boolean ret = super.dispatchTouchEvent(event);

        if (v instanceof EditText) {
            View w = getCurrentFocus();
            int scrcoords[] = new int[2];
            w.getLocationOnScreen(scrcoords);
            float x = event.getRawX() + w.getLeft() - scrcoords[0];
            float y = event.getRawY() + w.getTop() - scrcoords[1];

            Log.d("Activity", "Touch event " + event.getRawX() + "," + event.getRawY() + " " + x + "," + y + " rect " + w.getLeft() + "," + w.getTop() + "," + w.getRight() + "," + w.getBottom() + " coords " + scrcoords[0] + "," + scrcoords[1]);
            if (event.getAction() == MotionEvent.ACTION_UP && (x < w.getLeft() || x >= w.getRight() || y < w.getTop() || y > w.getBottom())) {

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getWindow().getCurrentFocus().getWindowToken(), 0);
            }
        }
        return ret;
    }


    public class FetchLocality extends AsyncTask<String, Void, String[]> {

        private final String LOG_TAG = FetchLocality.class.getSimpleName();

        private String[] getLocalityDataFromJson(String forecastJsonStr, String city)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String LOCALITY_LIST = city;
            final String LOCALITY_NAME = "name";
            final String LOCALITY_LOCATION = "temp";
            ArrayList<String> resultStr = new ArrayList<>();
            JSONObject forecastJson = new JSONObject(forecastJsonStr);
            JSONArray localityArray = forecastJson.getJSONArray(LOCALITY_LIST);
            String localityName;
            String latlng;
            for (int i = 0; i < localityArray.length(); i++) {
                // Get the JSON object representing the locality
                JSONObject locality = localityArray.getJSONObject(i);
                localityName = locality.getString(LOCALITY_NAME);
                JSONObject location = locality.getJSONObject("location");
                latlng = location.getString("ob") + "," + location.getString("pb");

                resultStr.add(localityName + "#" + latlng);
            }
            return resultStr.toArray(new String[resultStr.size()]);

        }

        @Override
        protected String[] doInBackground(String... params) {

            // If there's no city name, there's nothing to look up.  Verify size of params.
            if (params.length == 0) {
                return null;
            }

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String localitiesJsonStr = null;

            try {
                // Construct the URL for the fetch locality in given city
                final String LOCALITY_BASE_URL =
                        "http://nobroker.in/static/" + params[0] + ".json";

                Uri builtUri = Uri.parse(LOCALITY_BASE_URL).buildUpon()
                        .build();

                URL url = new URL(builtUri.toString());

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                localitiesJsonStr = buffer.toString();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            try {
                return getLocalityDataFromJson(localitiesJsonStr, params[0]);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            // This will only happen if there was an error getting or parsing the forecast.
            return null;
        }

        @Override
        protected void onPostExecute(String[] result) {
            if (result != null) {
                localityNames = new String[result.length];
                latlng = new String[result.length];
                for (int i = 0; i < result.length; i++) {
                    String[] val = result[i].split("#");
                    localityNames[i] = val[0];
                    latlng[i] = val[1];
                }
                addAutoCompleteLocalitySelector();
                // New data is back from the server.  Hooray!
            }
        }
    }

}
