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

package pl.openrnd.connection.rest.sample.google;

import android.net.Uri;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;

import pl.openrnd.connection.rest.request.Request;

public class GoogleGeocodeRequest extends Request {

    private static final String API = "http://maps.googleapis.com/maps/api/geocode/json";

    public GoogleGeocodeRequest(String address) {
        //GoogleGeocodeResponse class is responsible for handling the response received from server.
        //It need to be provided during request creation.

        super(GoogleGeocodeResponse.class, new Object[] { address });
    }

    @Override
    protected HttpUriRequest onHttpUriRequestCreate(Object... params) {
        //params are the all parameters provided to the super constructor except the Response class object.
        //In that case there is only an address provided.

        String address = (String)params[0];

        Uri.Builder uriBuilder = Uri.parse(API).buildUpon();
        uriBuilder.appendQueryParameter("address", address);

        return new HttpGet(uriBuilder.build().toString());
    }
}
