/**
 * ***************************************************************************
 * <p/>
 * 2015 (C) Copyright Open-RnD Sp. z o.o.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p/>
 * ****************************************************************************
 */

package pl.openrnd.connection.rest;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.InputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.CookieStore;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.net.ssl.HttpsURLConnection;

import pl.openrnd.connection.rest.exception.UnsupportedResponseException;
import pl.openrnd.connection.rest.request.Request;
import pl.openrnd.connection.rest.response.Response;

/**
 * Class that handles requests.
 */
public class ConnectionHandler {

    private static final String TAG = ConnectionHandler.class.getSimpleName();
    private static int mRequestCounter = 0;

    private OnRequestConnectionListener mOnRequestConnectionListener;
    private ConnectionLogger mConnectionLogger;

    private ConnectionConfig mConnectionConfig;
    private CookieStore mCookieStore;

    private Object mCookieLock = new Object();

    /**
     * Class constructor.
     *
     * @param context Application context.
     * @param connectionConfig ConnectionConfig object with configuration data.
     */
    public ConnectionHandler(Context context, ConnectionConfig connectionConfig) {
        Log.d(TAG, "ConnectionHandler()");

        mConnectionConfig = connectionConfig;
        mConnectionLogger = new ConnectionLogger(connectionConfig.getInitialLogsState(), connectionConfig.getInitialLogsSize());
    }

    /**
     * Sets OnRequestConnectionListener.
     *
     * @see pl.openrnd.connection.rest.OnRequestConnectionListener
     *
     * @param listener OnRequestConnectionListener object.
     */
    public void setRequestConnectionListener(OnRequestConnectionListener listener) {
        mOnRequestConnectionListener = listener;
    }

    /**
     * Gets ConnectionLogger related to the handler.
     *
     * @see pl.openrnd.connection.rest.ConnectionLogger
     *
     * @return ConnectionLogger object.
     */
    public ConnectionLogger getConnectionLogger() {
        return mConnectionLogger;
    }

    /**
     * Method for clearing cookies
     */
    public void clearCookie() {
        synchronized (mCookieLock) {
            mCookieStore.removeAll();
            mCookieStore = null;
        }
    }

    /**
     * Handles provided request and returns response related to it.
     *
     * The method creates a log if logging mechanism is enabled.
     *
     * Request is executed in a caller thread.
     *
     * @param request Request object
     * @return Response object
     */
    public Response handleRequest(Request request) {
        Response result = null;

        int requestNumber = mRequestCounter++;
        Timer timer = null;

        InputStream inputStream = null;
        RestConnectionLog.Builder builder = null;
        if (mConnectionLogger.areLogsEnabled()) {
            builder = new RestConnectionLog.Builder();
            builder.request(request);
        }

        Log.d(TAG, String.format("handleRequest(%d): ---> [%s]", requestNumber, request.getClass().getSimpleName()));

        HttpURLConnection httpURLConnection = null;
        try {
            httpURLConnection = request.getHttpUrlConnection();

            Log.d(TAG, String.format("handleRequest(%d): uri[%s]", requestNumber, httpURLConnection.getURL().toString()));

            logHeaders(requestNumber, httpURLConnection.getRequestProperties());

            timer = startRequestTimer(request);
            if (builder != null) {
                builder.request(request);
            }
            httpURLConnection.setConnectTimeout(request.getConnectionTimeout() != null ? request.getConnectionTimeout().intValue() : mConnectionConfig.getConnectionTimeout());
            httpURLConnection.setReadTimeout(request.getReadTimeout() != null ? request.getReadTimeout().intValue() : mConnectionConfig.getReadTimeout());

            if (httpURLConnection.getDoOutput()) {
                request.writeOutputStream(httpURLConnection.getOutputStream());
            }

            if (mConnectionConfig.isUsingCookies()) {
                createCookieIfNotSet();
            }
            if (httpURLConnection instanceof HttpsURLConnection && mConnectionConfig.getSSLSocketFactory() != null) {
                ((HttpsURLConnection)httpURLConnection).setSSLSocketFactory(mConnectionConfig.getSSLSocketFactory());
            }

            int statusCode = httpURLConnection.getResponseCode();
            String reasonPhrase = httpURLConnection.getResponseMessage();
            if (statusCode >= 200 && statusCode < 300) {
                inputStream = httpURLConnection.getInputStream();
            } else {
                inputStream = httpURLConnection.getErrorStream();
            }

            Log.d(TAG, String.format("handleRequest(%d): http response[%d / %s]", requestNumber, statusCode, reasonPhrase));
            stopRequestTimer(timer);
            logHeaders(requestNumber, httpURLConnection.getHeaderFields());

            boolean isStatusCodeSupported = request.supportsAllStatusCodes();
            if (!isStatusCodeSupported) {
                int[] supportedStatusCodes = request.getSupportedStatusCodes();
                for (int supportedStatusCode : supportedStatusCodes) {
                    if (statusCode == supportedStatusCode) {
                        isStatusCodeSupported = true;
                        break;
                    }
                }
            }

            if (isStatusCodeSupported) {
                result = request.getResponse(statusCode, reasonPhrase, httpURLConnection.getHeaderFields(), inputStream);
            } else {
                throw new UnsupportedResponseException(statusCode, reasonPhrase, httpURLConnection.getHeaderFields(), inputStream);
            }
        } catch (Exception exc) {
            Log.e(TAG, String.format("handleRequest(%d): ", requestNumber), exc);

            stopRequestTimer(timer);

            result = request.getResponse(exc);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception exc) {
                    Log.e(TAG, String.format("handleRequest(%d): ", requestNumber), exc);
                }
            }
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        }

        if (builder != null) {
            builder.cookies(mCookieStore);
            builder.response(result);

            mConnectionLogger.addConnectionLog(builder.build());
        }

        Log.d(TAG, String.format("handleRequest(%d): <---", requestNumber));

        return result;
    }

    private void createCookieIfNotSet() {
        synchronized (mCookieLock) {
            if (!hasCookie()) {
                CookieManager manager = new CookieManager( null, CookiePolicy.ACCEPT_ALL);
                CookieHandler.setDefault(manager);
                mCookieStore = manager.getCookieStore();
            }
        }
    }

    private boolean hasCookie() {
        synchronized (mCookieLock) {
            return (mCookieStore != null);
        }
    }

    private void notifyTakingTooLong(final Request request) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (mOnRequestConnectionListener != null) {
                    mOnRequestConnectionListener.onRequestTakingTooLong(request);
                }
            }
        });
    }

    private Timer startRequestTimer(final Request request) {
        Timer result = new Timer();
        result.schedule(new TimerTask() {
            @Override
            public void run() {
                notifyTakingTooLong(request);
            }
        }, mConnectionConfig.getRequestWarningTime());
        return result;
    }

    private void stopRequestTimer(Timer timer) {
        if (timer == null) {
            return;
        }
        timer.purge();
        timer.cancel();
    }

    private void logHeaders(int id, Map<String, List<String>> headers) {
        if (headers != null) {
            for (Map.Entry<String, List<String>> header : headers.entrySet()) {
                Log.w(TAG, String.format("handleRequest(%d):    key[%s], value[%s]", id, header.getKey(), header.getValue()));
            }
        }
    }
}
