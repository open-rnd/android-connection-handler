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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import pl.openrnd.connection.rest.ConnectionLog;
import pl.openrnd.connection.rest.view.ConnectionLogsView;

//Activity that is displaying a list of communication logs
public class LogsActivity extends Activity {

    private ConnectionLogsView mConnectionLogsView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_logs);

        findViews();
        confViews();
    }

    private void findViews() {
        mConnectionLogsView = (ConnectionLogsView)findViewById(R.id.connectionLogs);
    }

    private void confViews() {
        mConnectionLogsView.setOnConnectionLogsListener(mOnConnectionLogsListener);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mConnectionLogsView.attachConnectionLogger(SampleApplication.getConnectionHandler().getConnectionLogger());
    }

    @Override
    protected void onPause() {
        super.onPause();

        mConnectionLogsView.detachConnectionLogger(SampleApplication.getConnectionHandler().getConnectionLogger());
    }

    private ConnectionLogsView.OnConnectionLogsListener mOnConnectionLogsListener = new ConnectionLogsView.OnConnectionLogsListener() {
        @Override
        public void onConnectionLogClicked(ConnectionLogsView connectionLogsView, ConnectionLog connectionLog) {
            Intent intent = new Intent(LogsActivity.this, LogActivity.class);
            intent.putExtra(LogActivity.KEY_LOG, connectionLog);
            startActivity(intent);
        }
    };
}
