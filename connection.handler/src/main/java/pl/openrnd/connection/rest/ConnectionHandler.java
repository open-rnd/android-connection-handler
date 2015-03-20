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

package pl.openrnd.connection.rest;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.loopj.android.http.PersistentCookieStore;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;
import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;

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
	private HttpClient mHttpClient = null;
	private ConnectionLogger mConnectionLogger;
	
	private ConnectionConfig mConnectionConfig;
	private PersistentCookieStore mCookieStore;
	private HttpContext mHttpContext;
	
	private Object mClientLock = new Object();
	private Object mCookieLock = new Object();

    private Context mApplicationContext;

    /**
     * Class constructor.
     *
     * @param context Application context.
     * @param connectionConfig ConnectionConfig object with configuration data.
     */
	public ConnectionHandler(Context context, ConnectionConfig connectionConfig) {
		Log.d(TAG, "ConnectionHandler()");

        mApplicationContext = context.getApplicationContext();
		
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
            mCookieStore.clear();
			mCookieStore = null;
			mHttpContext = null;
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

        HttpEntity httpEntity = null;
        InputStream inputStream = null;
        RestConnectionLog.Builder builder = null;
        if (mConnectionLogger.areLogsEnabled()) {
            builder = new RestConnectionLog.Builder();
            builder.request(request);
        }

        Log.d(TAG, String.format("handleRequest(%d): ---> [%s]", requestNumber, request.getClass().getSimpleName()));

        try {
            HttpUriRequest httpUriRequest = request.getHttpUriRequest();

            Log.d(TAG, String.format("handleRequest(%d): uri[%s]", requestNumber, httpUriRequest.getURI().toString()));

            logHeaders(requestNumber, httpUriRequest.getAllHeaders());

            HttpResponse httpResponse;

            timer = startRequestTimer(request);
            if (builder != null) {
                builder.request(httpUriRequest);

                httpResponse = execute(httpUriRequest, request.getConnectionTimeout(), request.getReadTimeout());

                builder.response(httpResponse);
            } else {
                httpResponse = execute(httpUriRequest, request.getConnectionTimeout(), request.getReadTimeout());
            }
            stopRequestTimer(timer);

            int statusCode = httpResponse.getStatusLine().getStatusCode();
            String reasonPhrase = httpResponse.getStatusLine().getReasonPhrase();

            Log.d(TAG, String.format("handleRequest(%d): http response[%d / %s]", requestNumber, statusCode, reasonPhrase));

            logHeaders(requestNumber, httpResponse.getAllHeaders());

            httpEntity = httpResponse.getEntity();
            inputStream = httpEntity.getContent();

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
                result = request.getResponse(statusCode, reasonPhrase, httpResponse.getAllHeaders(), inputStream);
            } else {
                throw new UnsupportedResponseException(statusCode, reasonPhrase, httpResponse.getAllHeaders(), inputStream);
            }
        } catch (Exception exc) {
            Log.e(TAG, String.format("handleRequest(%d): ", requestNumber), exc);

            stopRequestTimer(timer);

            result = request.getResponse(exc);
        } finally {
            if (httpEntity != null) {
                try {
                    httpEntity.consumeContent();
                } catch (Exception exc) {
                    Log.e(TAG, String.format("handleRequest(%d): ", requestNumber), exc);
                }
            }

            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception exc) {
                    Log.e(TAG, String.format("handleRequest(%d): ", requestNumber), exc);
                }
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
				mCookieStore = new PersistentCookieStore(mApplicationContext);
				mHttpContext = new BasicHttpContext();
				mHttpContext.setAttribute(ClientContext.COOKIE_STORE, mCookieStore);
			}
		}
	}
	
	private boolean hasCookie() {
		synchronized (mCookieLock) {
			return (mCookieStore != null) && (mHttpContext != null);
		}
	}
	
	private HttpContext getHttpContext() {
		synchronized (mCookieLock) {
			return mHttpContext;
		}
	}
	
	private HttpClient getHttpClient() {
		synchronized (mClientLock) {
			if ((null == mHttpClient) || mConnectionConfig.isFullAsync()) {			
				HttpParams httpParameters = new BasicHttpParams();
				HttpConnectionParams.setConnectionTimeout(httpParameters, mConnectionConfig.getConnectionTimeout());
				HttpConnectionParams.setSoTimeout(httpParameters, mConnectionConfig.getReadTimeout());
				HttpClientParams.setRedirecting(httpParameters, false);

                if (mConnectionConfig.getSchemeRegistry() != null) {
                    ClientConnectionManager connectionManager = new ThreadSafeClientConnManager(httpParameters, mConnectionConfig.getSchemeRegistry());
                    mHttpClient = new DefaultHttpClient(connectionManager, httpParameters);
                } else {
                    mHttpClient = new DefaultHttpClient(httpParameters);
                }
			}
			
			return mHttpClient;
		}
	}

    private void notifyTakingTooLong(final Request request) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (mOnRequestConnectionListener != null){
                    mOnRequestConnectionListener.onRequestTakingTooLong(request);
                }
            }
        });
    }

	private Timer startRequestTimer(final Request request){
		Timer result = new Timer();
		result.schedule(new TimerTask() {
			@Override
			public void run() {
                notifyTakingTooLong(request);
			}
		}, mConnectionConfig.getRequestWarningTime());
		return result;
	}
	
	private void stopRequestTimer(Timer timer){
		if(timer == null){
			return;
		}
		timer.purge();
		timer.cancel();
	}
	
	private HttpResponse execute(HttpUriRequest request, Integer connectionTimeout, Integer readTimeout) throws ClientProtocolException, IOException {
        HttpResponse response = null;
        HttpClient httpClient = getHttpClient();
        HttpParams params = httpClient.getParams();

        Integer connectionTimeoutOriginal = null;
        Integer readTimeoutOriginal = null;

        if (connectionTimeout != null) {
            connectionTimeoutOriginal = HttpConnectionParams.getConnectionTimeout(params);
            HttpConnectionParams.setConnectionTimeout(params, connectionTimeout);
        }

        if (readTimeout != null) {
            readTimeoutOriginal = HttpConnectionParams.getSoTimeout(params);
            HttpConnectionParams.setSoTimeout(params, readTimeout);
        }

        try {
            if (mConnectionConfig.isUsingCookies()) {
                createCookieIfNotSet();
                response = httpClient.execute(request, getHttpContext());
            } else {
                response = httpClient.execute(request);
            }
        } finally {
            if (connectionTimeoutOriginal != null) {
                HttpConnectionParams.setConnectionTimeout(params, connectionTimeout);
            }

            if (readTimeoutOriginal != null) {
                HttpConnectionParams.setSoTimeout(params, readTimeoutOriginal);
            }
        }

        return response;
    }

	private void logHeaders(int id, Header[] headers) {
		if (headers != null) {
			for (Header header : headers) {
				Log.w(TAG, String.format("handleRequest(%d):    key[%s], value[%s]", id, header.getName(), header.getValue()));
			}
		}
	}
}
