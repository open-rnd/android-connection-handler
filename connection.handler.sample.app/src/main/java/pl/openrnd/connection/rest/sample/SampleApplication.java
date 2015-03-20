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

import android.app.Application;
import android.util.Log;

import pl.openrnd.connection.rest.ConnectionConfig;
import pl.openrnd.connection.rest.ConnectionHandlerAsync;
import pl.openrnd.connection.rest.OnRequestConnectionListener;
import pl.openrnd.connection.rest.OnUiRequestResultListener;
import pl.openrnd.connection.rest.request.Request;
import pl.openrnd.connection.rest.response.Response;
import pl.openrnd.connection.rest.sample.utils.Utils;

public class SampleApplication extends Application {
    private static final String TAG = SampleApplication.class.getSimpleName();

    private ConnectionHandlerAsync mConnectionHandler;

    private static SampleApplication mInstance;

    public SampleApplication() {
        mInstance = this;
    }

    public static ConnectionHandlerAsync getConnectionHandler() {
        if (mInstance.mConnectionHandler == null) {
            ConnectionConfig.Builder configBuilder = new ConnectionConfig.Builder();
            configBuilder.logsEnabled(true);
            configBuilder.logsSize(20);
            configBuilder.requestWarningTime(2000);

            mInstance.mConnectionHandler = new ConnectionHandlerAsync(mInstance, configBuilder.build());
            mInstance.mConnectionHandler.registerGlobalRequestResultWeakListener(mInstance.mGlobalUiRequestResultListener);
            mInstance.mConnectionHandler.setRequestConnectionListener(mInstance.mOnRequestConnectionListener);
        }
        return mInstance.mConnectionHandler;
    }

    //Optional global listener that will receive notification when executing some request takes too long.
    //Time is configured in ConnectionConfig provided during ConnectionHandlerAsync creation.
    private OnRequestConnectionListener mOnRequestConnectionListener = new OnRequestConnectionListener() {

        @Override
        public void onRequestTakingTooLong(Request request) {
            Log.v(TAG, "onRequestTakingTooLong()");

            Utils.showMessageToast(SampleApplication.this, getString(R.string.too_long_warning));
        }
    };

    //Optional global listener that will receive notification about every request that is executed.
    private OnUiRequestResultListener mGlobalUiRequestResultListener = new OnUiRequestResultListener() {

        @Override
        public void onRequestResultReady(Request request, Response response) {
            Log.v(TAG, "onRequestResultReady()");
        }
    };
}
