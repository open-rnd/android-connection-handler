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

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import org.apache.http.Header;

import java.io.InputStream;
import java.util.ArrayList;

import pl.openrnd.connection.rest.response.Response;
import pl.openrnd.connection.rest.utils.Utils;

public class GoogleGeocodeResponse extends Response {

    @SerializedName("results")
    private ArrayList<GeocodeResult> mGeocodeResults;

    @SerializedName("status")
    private String mStatus;

    public GoogleGeocodeResponse(Exception exception) {
        super(exception);
    }

    public GoogleGeocodeResponse(Integer httpStatusCode, String httpReasonPhrase, Header[] headers, InputStream entityContentStream) {
        super(httpStatusCode, httpReasonPhrase, headers, entityContentStream);
    }

    @Override
    protected void handleContent(InputStream inputStream) throws Exception {
        String content = Utils.streamToString(inputStream);

        //Sets response content description for logging purposes
        setContentDescription(content);

        GoogleGeocodeResponse response = new Gson().fromJson(content, GoogleGeocodeResponse.class);

        mGeocodeResults = response.mGeocodeResults;
        mStatus = response.mStatus;
    }

    public ArrayList<GeocodeResult> getGeocodeResults() {
        return mGeocodeResults;
    }

    public String getStatus() {
        return mStatus;
    }
}
