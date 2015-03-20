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
import android.os.Bundle;

import pl.openrnd.connection.rest.RestConnectionLog;
import pl.openrnd.connection.rest.view.ConnectionLogView;

//Activity that is displaying full details about selected communication log
public class LogActivity extends Activity {
    public static final String KEY_LOG = "KEY_LOG";

    private ConnectionLogView mConnectionLogView;
    private RestConnectionLog mConnectionLog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_log);

        initData();
        findViews();
        confViews();
    }

    private void initData() {
        mConnectionLog = (RestConnectionLog)getIntent().getSerializableExtra(KEY_LOG);
    }

    private void findViews() {
        mConnectionLogView = (ConnectionLogView)findViewById(R.id.connectionLog);
    }

    private void confViews() {
        mConnectionLogView.assignConnectionLog(mConnectionLog);
    }
}
