/******************************************************************************
 *
 *  2015 (C) Copyright Open-RnD Sp. z o.o.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 ******************************************************************************/

package pl.openrnd.connection.rest.sample;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import java.util.ArrayList;

import pl.openrnd.connection.rest.OnUiRequestResultListener;
import pl.openrnd.connection.rest.request.Request;
import pl.openrnd.connection.rest.response.Response;
import pl.openrnd.connection.rest.sample.google.GeocodeResult;
import pl.openrnd.connection.rest.sample.google.Geometry;
import pl.openrnd.connection.rest.sample.google.GoogleGeocodeRequest;
import pl.openrnd.connection.rest.sample.google.GoogleGeocodeResponse;
import pl.openrnd.connection.rest.sample.google.Location;
import pl.openrnd.connection.rest.sample.utils.Utils;

public class MainActivity extends ActionBarActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private EditText mAddressView;
    private View mGeoCodeView;
    private View mRestLogsView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        findViews();
        confViews();
    }

    private void findViews() {
        mAddressView = (EditText)findViewById(R.id.address);
        mGeoCodeView = findViewById(R.id.geocode);
        mRestLogsView = findViewById(R.id.restLogs);
    }

    private void confViews() {
        mGeoCodeView.setOnClickListener(mOnClickListener);
        mRestLogsView.setOnClickListener(mOnClickListener);
    }

    private void onGeocodeRequested(String address) {
        Log.v(TAG, String.format("onGeocodeRequested(): address[%s]", address));

        //Request is created and passed to ConnectionHandlerAsync.
        //The request is added to the requests queue, and if the queue is empty the request is executed immediately.
        //Besides the request, the ConnectionHandler required to provide a callback that will be notified about
        //request execution status. The notification can be performed on the request execution thread or
        //the UI thread. To select desired behavior OnRequestResultListener or OnUiRequestResultListener object has to be
        //provided.

        GoogleGeocodeRequest request = new GoogleGeocodeRequest(address);
        SampleApplication.getConnectionHandler().addRequest(request, mOnUiRequestResultListener);
    }

    private void onRestLogsShowRequested() {
        Intent intent = new Intent(this, LogsActivity.class);
        startActivity(intent);
    }

    private void showBestLocationOnMap(ArrayList<GeocodeResult> geocodeResults) {
        for (GeocodeResult geocodeResult : geocodeResults) {
            Geometry geometry = geocodeResult.getGeometry();
            if (geometry != null) {
                Location location = geometry.getLocation();
                if (location != null) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);

                    String lat = Double.toString(location.getLat()).replace(",", ".");
                    String lng = Double.toString(location.getLng()).replace(",", ".");

                    intent.setData(Uri.parse(String.format("geo:%s,%s", lat, lng)));
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivity(intent);
                    } else {
                        Utils.showMessageToast(this, getString(R.string.no_app));
                    }
                    return;
                }
            }
        }
        Utils.showMessageToast(this, getString(R.string.geocode_failed_no_coords));
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            Log.v(TAG, "onClick()");

            switch (v.getId()) {
                case R.id.geocode:
                    onGeocodeRequested(mAddressView.getText().toString());
                    break;

                case R.id.restLogs:
                    onRestLogsShowRequested();
                    break;
            }
        }
    };

    private OnUiRequestResultListener mOnUiRequestResultListener = new OnUiRequestResultListener() {

        @Override
        public void onRequestResultReady(Request request, Response response) {
            if (response != null) {
                if (response.hasException()) {
                    Utils.showMessageToast(MainActivity.this, response.getException().getMessage());
                } else {
                    GoogleGeocodeResponse geocodeResponse = (GoogleGeocodeResponse)response;
                    if ("OK".equalsIgnoreCase(geocodeResponse.getStatus())) {
                        showBestLocationOnMap(geocodeResponse.getGeocodeResults());
                    } else {
                        Utils.showMessageToast(MainActivity.this, getString(R.string.geocode_failed, geocodeResponse.getStatus()));
                    }
                }
            } else {
                //Not possible in this demo application.
                //Response is a null when request was cancelled.
                //This case can be additionally checked by calling isCanceled() method of request.
            }
        }
    };
}
