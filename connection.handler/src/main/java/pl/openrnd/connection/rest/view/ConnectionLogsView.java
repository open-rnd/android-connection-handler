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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

import pl.openrnd.connection.rest.ConnectionLog;
import pl.openrnd.connection.rest.ConnectionLogger;
import pl.openrnd.connection.rest.R;
import pl.openrnd.connection.rest.RestConnectionLog;

public class ConnectionLogsView extends RelativeLayout {
    private static final String TAG = ConnectionLogsView.class.getSimpleName();

    private ListView mListView;
    private LogsAdapter mLogsAdapter;

    private OnConnectionLogsListener mOnConnectionLogsListener;

    private boolean mIsLoggerAttached;

    public ConnectionLogsView(Context context) {
        super(context);

        initView(null);
    }

    public ConnectionLogsView(Context context, AttributeSet attrs) {
        super(context, attrs);

        initView(attrs);
    }

    public ConnectionLogsView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        initView(attrs);
    }

    private void initView(AttributeSet attrs) {
        LayoutInflater.from(getContext()).inflate(R.layout.connection_view_logs, this, true);
        initData();
        findViews();
        confViews();
    }

    private void initData() {
        mIsLoggerAttached = false;

        mLogsAdapter = new LogsAdapter();
    }

    private void findViews() {
        mListView = (ListView)findViewById(R.id.connectionLogsList);
    }

    private void confViews() {
        mListView.setAdapter(mLogsAdapter);
        mListView.setOnItemClickListener(mOnItemClickListener);
    }

    public void setOnConnectionLogsListener(OnConnectionLogsListener listener) {
        Log.v(TAG, "setOnConnectionLogsListener()");

        mOnConnectionLogsListener = listener;
    }

    public void attachConnectionLogger(ConnectionLogger connectionLogger) {
        Log.v(TAG, "attachConnectionLogger()");

        if (mIsLoggerAttached) {
            throw new IllegalStateException("Currently only one ConnectionLogger is supported");
        }

        mIsLoggerAttached = true;

        if (connectionLogger != null) {
            connectionLogger.registerOnLogQueueChangeListener(mOnLogQueueChangeListener);
        }

        refreshLogs(connectionLogger);
    }

    public void detachConnectionLogger(ConnectionLogger connectionLogger) {
        Log.v(TAG, "detachConnectionLogger()");

        mIsLoggerAttached = false;

        if (connectionLogger != null) {
            connectionLogger.unregisterOnLogChangeListener(mOnLogQueueChangeListener);
        }

        refreshLogs(null);
    }

    private void refreshLogs(final ConnectionLogger connectionLogger) {
        Log.v(TAG, "refreshLogs()");

        if (connectionLogger != null) {
            post(new Runnable() {
                @Override
                public void run() {
                    mLogsAdapter.setConnectionLogs(connectionLogger.getConnectionLogs());
                }
            });
        }
    }

    private AdapterView.OnItemClickListener mOnItemClickListener = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Log.v(TAG, "OnItemClickListener.onItemClick()");

            final ConnectionLog connectionLog = ((ConnectionLogItemView)view).getConnectionLog();

            post(new Runnable() {
                @Override
                public void run() {
                    if (mOnConnectionLogsListener != null) {
                        mOnConnectionLogsListener.onConnectionLogClicked(ConnectionLogsView.this, connectionLog);
                    }
                }
            });
        }
    };

    private ConnectionLogger.OnLogQueueChangeListener mOnLogQueueChangeListener = new ConnectionLogger.OnLogQueueChangeListener() {

        @Override
        public void onQueueSizeChanged(ConnectionLogger connectionLogger) {
            Log.v(TAG, "onQueueSizeChanged()");

            refreshLogs(connectionLogger);
        }
    };

    public interface OnConnectionLogsListener {
        void onConnectionLogClicked(ConnectionLogsView view, ConnectionLog connectionLog);
    }

    private class LogsAdapter extends BaseAdapter {
        private List<ConnectionLog> mConnectionLogs;

        public LogsAdapter() {
            mConnectionLogs = new ArrayList<ConnectionLog>();
        }

        public void setConnectionLogs(final List<ConnectionLog> logs) {
            mConnectionLogs = logs != null ? logs : new ArrayList<ConnectionLog>();
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mConnectionLogs.size();
        }

        @Override
        public Object getItem(int position) {
            return mConnectionLogs.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ConnectionLogItemView connectionLogView;
            if (convertView != null) {
                connectionLogView = (ConnectionLogItemView)convertView;
            } else {
                connectionLogView = new ConnectionLogItemView(getContext());
            }

            connectionLogView.assignConnectionLog((RestConnectionLog)getItem(position));

            return connectionLogView;
        }
    }
}
