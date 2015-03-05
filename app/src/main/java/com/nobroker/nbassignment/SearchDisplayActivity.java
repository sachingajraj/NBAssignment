package com.nobroker.nbassignment;

import android.app.ProgressDialog;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.nobroker.nbassignment.adapter.CustomListAdapter;
import com.nobroker.nbassignment.app.AppController;
import com.nobroker.nbassignment.model.PropertyListItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class SearchDisplayActivity extends ActionBarActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private ProgressDialog pDialog;
    private List<PropertyListItem> propertyList = new ArrayList<PropertyListItem>();
    private ListView listView;
    private CustomListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_display);

        listView = (ListView) findViewById(R.id.list);
        adapter = new CustomListAdapter(this, propertyList);
        listView.setAdapter(adapter);

        pDialog = new ProgressDialog(this);
        // Showing progress dialog before making http request
        pDialog.setMessage("Loading...");
        pDialog.show();

        Bundle extras = getIntent().getExtras();
        String localityName = "koramangala";
        String lat_lng = 12.931662 + "," + 77.622687;
        String city = "bangalore";
        if (extras != null) {
            localityName = extras.getString("LOCALITY_NAME");
            lat_lng = extras.getString("LAT_LNG");
            city = extras.getString("CURRENT_CITY");
        }

        try {
            final String NOBROKER_BASE_URL =
                    "http://www.nobroker.in/property/json/rent/";
            final String IMAGE_BASE_URL = "http://d3snwcirvb4r88.cloudfront.net/images/";
            final String RENT_PARAM = "rent";
            final String ORDER_BY_PARAM = "orderBy";
            final String LAT_LNG_PARAM = "lat_lng";
            final String RADIUS_PARAM = "radius";
            final String SHOW_MAP_PARAM = "showMap";

            Uri builtUri = Uri.parse(NOBROKER_BASE_URL).buildUpon()
                    .appendPath(city)
                    .appendPath(localityName + "/")
                    .appendQueryParameter(RENT_PARAM, 0 + "," + 200000)
                    .appendQueryParameter(ORDER_BY_PARAM, "lastUpdateDate" + "," + "desc")
                    .appendQueryParameter(LAT_LNG_PARAM, lat_lng)
                    .appendQueryParameter(RADIUS_PARAM, "4.00")
                    .appendQueryParameter(SHOW_MAP_PARAM, "true")
                    .build();

            String url = builtUri.toString();

            // Creating volley request obj
            JsonObjectRequest propertyReq = new JsonObjectRequest(Request.Method.GET, url, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            // display response
                            Log.d("Response", response.toString());
                            hidePDialog();
                            JSONArray jsonArray;
                            try {
                                jsonArray = response.getJSONArray("data");
                                // Parsing json
                                for (int i = 0; i < jsonArray.length(); i++) {

                                    JSONObject obj = jsonArray.getJSONObject(i);
                                    PropertyListItem property = new PropertyListItem();
                                    String title = obj.getString("type") + " " + obj.getString("furnishing") + " at " + obj.getString("locality");
                                    property.setTitle(title);
                                    String url = "http://d3snwcirvb4r88.cloudfront.net/static/img/na_m.jpg";

                                    JSONArray photos = obj.getJSONArray("photos");
                                    if (photos.length() > 0) {
                                        String imagePath = photos.getJSONObject(0).getJSONObject("imagesMap").getString("medium");
                                        url = IMAGE_BASE_URL + obj.getString("id") + "/" + imagePath;
                                    }

                                    property.setThumbnailUrl(url);
                                    property.setRent(((Number) obj.get("rent"))
                                            .doubleValue());
                                    property.setSize(obj.getInt("propertySize"));

                                    propertyList.add(property);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            // notifying list adapter about data changes
                            // so that it renders the list view with updated data
                            adapter.notifyDataSetChanged();
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d("Error.Response", error.getMessage());
                        }
                    }

            );
            // Adding request to request queue
            AppController.getInstance().addToRequestQueue(propertyReq);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void hidePDialog() {
        if (pDialog != null) {
            pDialog.dismiss();
            pDialog = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search_display, menu);
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
}
