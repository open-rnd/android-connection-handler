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

package pl.openrnd.connection.rest.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

import pl.openrnd.connection.rest.ConnectionLog;
import pl.openrnd.connection.rest.R;
import pl.openrnd.connection.rest.RestConnectionLog;

public class ConnectionLogItemView extends RelativeLayout {
    private TextView mTimeView;
    private TextView mNameView;
    private TextView mMethodView;
    private TextView mStatusView;
    private TextView mExceptionView;

    private RestConnectionLog mConnectionLog;

    public ConnectionLogItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public ConnectionLogItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ConnectionLogItemView(Context context) {
        super(context);
        init();
    }

    private void init(){
        findViews();
    }

    private void findViews() {
        LayoutInflater.from(getContext()).inflate(R.layout.connection_view_log_item, this, true);
        mTimeView = (TextView)findViewById(R.id.connectionLogTime);
        mNameView = (TextView)findViewById(R.id.connectionLogName);
        mMethodView = (TextView)findViewById(R.id.connectionLogMethod);
        mStatusView = (TextView)findViewById(R.id.connectionLogStatus);
        mExceptionView = (TextView)findViewById(R.id.connectionLogException);
    }

    public void assignConnectionLog(RestConnectionLog connectionLog) {
        mConnectionLog = connectionLog;

        mTimeView.setText(mConnectionLog.getFormattedDate());
        mNameView.setText(mConnectionLog.getRequestName());
        mMethodView.setText(mConnectionLog.getRequestMethod());
        mStatusView.setText(getStatus(mConnectionLog));
        mExceptionView.setText(getException(mConnectionLog));
    }

    public ConnectionLog getConnectionLog() {
        return mConnectionLog;
    }

    private String getStatus(RestConnectionLog connectionLog) {
        StringBuilder result = new StringBuilder();

        Integer statusCode = connectionLog.getResponseStatusCode();
        String reasonPhrase = connectionLog.getResponseReasonPhrase();

        if (statusCode != null) {
            result.append(statusCode);
            result.append(" / ");
            result.append(reasonPhrase);
        }

        return result.toString();
    }

    private String getException(RestConnectionLog connectionLog) {
        StringBuilder result = new StringBuilder();

        Exception exc = connectionLog.getResponseException();
        if (exc != null) {
            result.append(exc.getClass().getSimpleName());
        }

        return result.toString();
    }
}
