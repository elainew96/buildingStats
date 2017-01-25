package com.example.elaine.buildingstats;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.content.Context;
import android.location.Criteria;
import android.location.LocationListener;
import android.os.Looper;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.VolleyError;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BuildingStats extends AppCompatActivity {
    private TextView buildingname;
    private ListView buildinginfo;
    private Button b;
    private LocationManager locationManager;
    private Criteria crit = new Criteria();
    private Looper loop = null;
    private ArrayList<String> listItems = new ArrayList<String>();
    private ArrayAdapter<String> adapter;
    private RequestQueue requestQueue;

    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            double arcpav_longitude = -121.760069;
            double arcpav_latitude = 38.5418086;
            double arcpav_radius = 0.01;

            if (Math.abs(location.getLongitude() - arcpav_longitude) < arcpav_radius &&
                    Math.abs(location.getLatitude() - arcpav_latitude) < arcpav_radius) {
                buildingname.setText("ARC Pavilion");
                try {
                    displayARCPavilion();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                buildingname.setText("lon: " + location.getLongitude() +
                        "lat: " + location.getLatitude());
            }
        }
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}
        @Override
        public void onProviderEnabled(String provider) {}
        @Override
        public void onProviderDisabled(String provider) {}
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_building_stats);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        buildingname = (TextView) findViewById(R.id.header);
        buildinginfo = (ListView) findViewById(R.id.infolist);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        crit.setAccuracy(Criteria.ACCURACY_FINE);
        b = (Button) findViewById(R.id.ChangeText);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,listItems);
        buildinginfo.setAdapter(adapter);
        requestQueue = Volley.newRequestQueue(this.getApplicationContext());

        requestPermission();

        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    locationManager.requestSingleUpdate(crit, locationListener, loop);
                } catch (SecurityException s) {
                    s.printStackTrace();
                }
            }
        });
    }

    private void requestPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_building_stats, menu);
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

    private void displayARCPavilion() throws JSONException {
        String url = "https://bldg-pi-api.ou.ad3.ucdavis.edu/piwebapi/streamsets/E0bgZy4oKQ9kiBiZJTW7eugwb1fCH91qBEWKCMCkgV3FwQVVRJTC1BRlxDRUZTXFVDREFWSVNcQlVJTERJTkdTXEFSQyBQQVZJTElPTlxFTEVDVFJJQ0lUWQ/value";
        String creds = String.format("%s:%s","ou\\pi-api-public", "M53$dx7,d3fP8");
        String auth = "Basic " + Base64.encodeToString(creds.getBytes(), Base64.NO_WRAP);
        JsonObjectRequest jsObjRequest = new JsonObjectRequest(url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject resp) {
                        try {
                            System.out.println(resp.toString(4));
                            JSONArray items = resp.getJSONArray("Items");
                            listItems.clear();

                            for (int i = 0; i < items.length(); i++) {
                                JSONObject o = items.getJSONObject(i);
                                String valuename = o.getString("Name");

                                JSONObject values = o.getJSONObject("Value");
                                Object value = values.get("Value");
                                String units = values.getString("UnitsAbbreviation");

                                listItems.add(valuename + ": " + value + " " + units);
                            }

                            adapter.notifyDataSetChanged();
                        } catch (JSONException e) {
                            System.out.println("json exception intercepted");
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError e) {
                        System.out.println("volley error");
                        e.printStackTrace();
                    }
                })
                {
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        HashMap<String, String> params = new HashMap<String, String>();
                        String creds = String.format("%s:%s","ou\\pi-api-public", "M53$dx7,d3fP8");
                        String auth = "Basic " + Base64.encodeToString(creds.getBytes(), Base64.NO_WRAP);
                        params.put("Authorization", auth);
                        return params;
                    }
                };
        requestQueue.add(jsObjRequest);
    }
}

    /* JSON structure
     * {
     *   "Database name": CEFS,
     *   "Items": [
     *     {
     *       name: "building 1"
     *       ...
     *     },
     *     {
     *       name: "building 2"
     *       ...
     *     },
     *     ...
     *   ]
     * }
     */
