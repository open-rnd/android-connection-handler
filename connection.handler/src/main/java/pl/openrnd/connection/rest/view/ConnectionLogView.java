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
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;

import pl.openrnd.connection.rest.ConnectionLog;
import pl.openrnd.connection.rest.R;
import pl.openrnd.connection.rest.RestConnectionLog;

public class ConnectionLogView extends LinearLayout {
    private View mReqNameTitleView;
    private TextView mReqNameView;

    private View mReqTimeTitleView;
    private TextView mReqTimeView;

    private View mRespTimeTitleView;
    private TextView mRespTimeView;

    private View mUriTitleView;
    private TextView mUriView;

    private View mMethodTitleView;
    private TextView mMethodView;

    private View mStatusTitleView;
    private TextView mStatusView;

    private View mExceptionTitleView;
    private TextView mExceptionView;

    private View mRequestHeadersTitleView;
    private TextView mRequestHeadersView;

    private View mResponseHeadersTitleView;
    private TextView mResponseHeadersView;

    private View mCookieTitleView;
    private TextView mCookieView;

    private View mRequestEntityTitleView;
    private TextView mRequestEntityView;

    private View mResponseEntityTitleView;
    private TextView mResponseEntityView;

    private RestConnectionLog mConnectionLog;

    public ConnectionLogView(Context context) {
        super(context);

        initView(null);
    }

    public ConnectionLogView(Context context, AttributeSet attrs) {
        super(context, attrs);

        initView(attrs);
    }

    public ConnectionLogView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        initView(attrs);
    }

    private void initView(AttributeSet attrs) {
        setOrientation(VERTICAL);
        LayoutInflater.from(getContext()).inflate(R.layout.connection_view_log, this, true);

        findViews();
        confViews();
    }

    private void findViews() {
        mReqNameView = (TextView)findViewById(R.id.connectionLogRequest);
        mReqTimeView = (TextView)findViewById(R.id.connectionLogReqTime);
        mRespTimeView = (TextView)findViewById(R.id.connectionLogRespTime);
        mUriView = (TextView)findViewById(R.id.connectionLogUri);
        mMethodView = (TextView)findViewById(R.id.connectionLogMethod);
        mStatusView = (TextView)findViewById(R.id.connectionLogStatus);
        mExceptionView = (TextView)findViewById(R.id.connectionLogException);
        mRequestHeadersView = (TextView)findViewById(R.id.connectionLogReqHeaders);
        mResponseHeadersView = (TextView)findViewById(R.id.connectionLogRespHeaders);
        mCookieView = (TextView)findViewById(R.id.connectionCookie);
        mRequestEntityView = (TextView)findViewById(R.id.connectionLogReqEntity);
        mResponseEntityView = (TextView)findViewById(R.id.connectionLogRespEntity);

        mReqNameTitleView = findViewById(R.id.connectionLogRequestTitle);
        mReqTimeTitleView = findViewById(R.id.connectionLogReqTimeTitle);
        mRespTimeTitleView = findViewById(R.id.connectionLogRespTimeTitle);
        mUriTitleView = findViewById(R.id.connectionLogUriTitle);
        mMethodTitleView = findViewById(R.id.connectionLogMethodTitle);
        mStatusTitleView = findViewById(R.id.connectionLogStatusTitle);
        mExceptionTitleView = findViewById(R.id.connectionLogExceptionTitle);
        mRequestHeadersTitleView = findViewById(R.id.connectionLogReqHeadersTitle);
        mResponseHeadersTitleView = findViewById(R.id.connectionLogRespHeadersTitle);
        mCookieTitleView = findViewById(R.id.connectionLogCookieTitle);
        mRequestEntityTitleView = findViewById(R.id.connectionLogReqEntityTitle);
        mResponseEntityTitleView = findViewById(R.id.connectionLogRespEntityTitle);
    }

    private void confViews() {
        mReqNameTitleView.setOnClickListener(mOnClickListener);
        mReqTimeTitleView.setOnClickListener(mOnClickListener);
        mRespTimeTitleView.setOnClickListener(mOnClickListener);
        mUriTitleView.setOnClickListener(mOnClickListener);
        mMethodTitleView.setOnClickListener(mOnClickListener);
        mStatusTitleView.setOnClickListener(mOnClickListener);
        mExceptionTitleView.setOnClickListener(mOnClickListener);
        mRequestHeadersTitleView.setOnClickListener(mOnClickListener);
        mResponseHeadersTitleView.setOnClickListener(mOnClickListener);
        mCookieTitleView.setOnClickListener(mOnClickListener);
        mRequestEntityTitleView.setOnClickListener(mOnClickListener);
        mResponseEntityTitleView.setOnClickListener(mOnClickListener);
    }

    private void copyTextToClipboard(String caption, TextView textView) {
        int sdk = android.os.Build.VERSION.SDK_INT;
        if(sdk < android.os.Build.VERSION_CODES.HONEYCOMB) {
            android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setText(textView.getText().toString());
        } else {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText(caption, textView.getText().toString());
            clipboard.setPrimaryClip(clip);
        }

        Toast.makeText(getContext(), R.string.connection_log_text_copied_to_clipboard, Toast.LENGTH_SHORT).show();
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            //Resource id can't be used in switch

            int id = view.getId();
            if (id == R.id.connectionLogRequestTitle) {
                copyTextToClipboard("request name", mReqNameView);
            } else if (id == R.id.connectionLogReqTimeTitle) {
                copyTextToClipboard("request time", mReqTimeView);
            } else if (id == R.id.connectionLogRespTimeTitle) {
                copyTextToClipboard("response time", mRespTimeView);
            } else if (id == R.id.connectionLogUriTitle) {
                copyTextToClipboard("uri", mUriView);
            } else if (id == R.id.connectionLogExceptionTitle) {
                copyTextToClipboard("exception", mExceptionView);
            } else if (id == R.id.connectionLogMethodTitle) {
                copyTextToClipboard("method", mMethodView);
            } else if (id == R.id.connectionLogReqEntityTitle) {
                copyTextToClipboard("request entity", mRequestEntityView);
            } else if (id == R.id.connectionLogReqHeadersTitle) {
                copyTextToClipboard("request headers", mRequestHeadersView);
            } else if (id == R.id.connectionLogRespEntityTitle) {
                copyTextToClipboard("response entity", mResponseEntityView);
            } else if (id == R.id.connectionLogRespHeadersTitle) {
                copyTextToClipboard("response headers", mResponseHeadersView);
            } else if (id == R.id.connectionLogStatusTitle) {
                copyTextToClipboard("status", mStatusView);
            } else if (id == R.id.connectionLogCookieTitle) {
                copyTextToClipboard("cookies", mCookieView);
            }
        }
    };

    public void assignConnectionLog(RestConnectionLog connectionLog) {
        mConnectionLog = connectionLog;

        if (mConnectionLog == null) {
            resetViews();
        } else {
            confViewsWithConnectionLog();
        }
    }

    public ConnectionLog getConnectionLog() {
        return mConnectionLog;
    }

    private void resetViews() {
        String empty = getContext().getString(R.string.connection_log_not_available);

        mReqNameView.setText(empty);
        mReqTimeView.setText(empty);
        mRespTimeView.setText(empty);
        mUriView.setText(empty);
        mMethodView.setText(empty);
        mStatusView.setText(empty);
        mExceptionView.setText(empty);
        mRequestHeadersView.setText(empty);
        mResponseHeadersView.setText(empty);
        mRequestEntityView.setText(empty);
        mResponseEntityView.setText(empty);
        mCookieView.setText(empty);
    }

    private void confViewsWithConnectionLog() {
        mReqNameView.setText(getTextOrNA(mConnectionLog.getRequestName()));
        mReqTimeView.setText(getTextOrNA(mConnectionLog.getFormattedDate()));
        mRespTimeView.setText(getTextOrNA(mConnectionLog.getFormattedResponseDate()));
        mUriView.setText(getTextOrNA(mConnectionLog.getRequestUri()));
        mMethodView.setText(getTextOrNA(mConnectionLog.getRequestMethod()));
        mStatusView.setText(getTextOrNA(getStatus()));
        mExceptionView.setText(getTextOrNA(getException()));
        mRequestHeadersView.setText(getTextOrNA(getHeaders(mConnectionLog.getRequestHeaders())));
        mResponseHeadersView.setText(getTextOrNA(getHeaders(mConnectionLog.getResponseHeaders())));
        mRequestEntityView.setText(getTextOrNA(mConnectionLog.getRequestContent()));
        mResponseEntityView.setText(getTextOrNA(mConnectionLog.getResponseContent()));
        mCookieView.setText(getTextOrNA(getCookiesDescription(mConnectionLog.getCookies())));
    }

    private String getCookiesDescription(ArrayList<String> cookies) {
        StringBuilder builder = new StringBuilder();

        if (cookies != null) {
            for (String cookie : cookies) {
                if (builder.length() > 0) {
                    builder.append("\n");
                }
                builder.append(cookie);
            }
        }

        return builder.toString();
    }

    private String getTextOrNA(String text) {
        if ((text == null) || (text.length() == 0)) {
            return getResources().getString(R.string.connection_log_not_available);
        }
        return text;
    }

    private String getStatus() {
        StringBuilder result = new StringBuilder();

        Integer statusCode = mConnectionLog.getResponseStatusCode();
        String reasonPhrase = mConnectionLog.getResponseReasonPhrase();

        if (statusCode != null) {
            result.append(statusCode);
            result.append(" / ");
            result.append(reasonPhrase);
        }

        return result.toString();
    }

    private String getException() {
        StringBuilder result = new StringBuilder();

        Exception exception = mConnectionLog.getResponseException();
        if (exception != null) {
            result.append(exception.getMessage());
            StringWriter stackTrace = new StringWriter();
            exception.printStackTrace(new PrintWriter(stackTrace));
            result.append(stackTrace.toString());
        }

        return result.toString();
    }

    private String getHeaders(RestConnectionLog.Header[] headers) {
        StringBuilder result = new StringBuilder();

        if (headers != null) {
            for (RestConnectionLog.Header header : headers) {
                if (result.length() > 0) {
                    result.append("\n");
                }
                result.append(header.getKey());
                result.append("\n");
                result.append(header.getValue());
                result.append("\n");
            }
        }

        return result.toString();
    }
}




