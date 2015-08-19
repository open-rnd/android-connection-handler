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

package pl.openrnd.connection.rest.request;

import android.util.Log;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;

import pl.openrnd.connection.rest.data.HttpStatusCode;
import pl.openrnd.connection.rest.response.Response;

/**
 * Base request class.
 *
 * Main tasks for extending classes are:
 * - provide valid HttpURLConnection object,
 * - provide Response class object to be used for handling the response
 */
public abstract class Request {
	private static final String TAG = Request.class.getSimpleName();
	
	private HttpURLConnection mHttpUriRequest;
	protected Class<? extends Response> mResponseClass;
	private boolean mIsCanceled;
    private Integer mConnectionTimeout;
    private Integer mReadTimeout;
    private String mContentDescription;
    private Object mTag;

    /**
     * Class constructor
     *
     * @param responseClass Response class object that handles the response
     * @param params All parameters required to create valid request object
     */
	public Request(Class<? extends Response> responseClass, Object... params) {
		mResponseClass = responseClass;
		
		mHttpUriRequest = onHttpUrlConnectionCreate(params);
		
		mIsCanceled = false;
	}

    /**
     * Method that is required to create valid HttpURLConnection object to be executed.
     *
     * @param params Parameters required to create HttpURLConnection. Those are the same parameters as provided in constructor.
     * @return HttpUriRequest object
     */
	protected abstract HttpURLConnection onHttpUrlConnectionCreate(Object... params);

	/**
	 * In case of requests with body, this method should write body to URLConnection's output stream.
	 *
	 * @param outputStream Output stream to which request body should be written
	 */
	public void writeOutputStream(OutputStream outputStream) { }

    /**
     * Creates response object from registered Response class object.
     *
     * @param httpStatusCode Http status code (e.g., 200)
     * @param httpReasonPhrase Http reason phrase (e.g., "200 OK")
     * @param headers Response headers
     * @param entityContentStream Opened input stream for response entity
     * @return Response object
     */
	public Response getResponse(Integer httpStatusCode, String httpReasonPhrase, Map<String, List<String>> headers, InputStream entityContentStream) {
		Response response = null;
		if (mResponseClass != null) {
			@SuppressWarnings("rawtypes")
			Class[] paramTypes = { Integer.class, String.class, Map.class, InputStream.class };
			Object[] paramValues = { httpStatusCode, httpReasonPhrase, headers, entityContentStream };
			
			try {
				response = (Response) mResponseClass.getConstructor(paramTypes).newInstance(paramValues);
                response.setTag(mTag);
			} catch (Exception exc) {
				Log.e(TAG, String.format("getResponse(): exc[%s]", exc.getMessage()));
			}
		}
		return response;
	}

    /**
     * Creates response object from registered Response class object.
     *
     * @param exception Exception that was thrown during request executing.
     * @return Response object
     */
	public Response getResponse(Exception exception) {
		Response response = null;
		if (mResponseClass != null) {
			@SuppressWarnings("rawtypes")
			Class[] paramTypes = { Exception.class };
			Object[] paramValues = { exception };
			
			try {
				response = (Response) mResponseClass.getConstructor(paramTypes).newInstance(paramValues);
                response.setTag(mTag);
			} catch (Exception exc) {
				Log.e(TAG, String.format("getResponse(): exc[%s]", exc.getMessage()));
			}
		}
		return response;
	}

    /**
     * Gets HttpUriRequest object created by onHttpUrlConnectionCreate() method
     *
     * @return HttpUriRequest object
     */
	public HttpURLConnection getHttpUrlConnection() {
		return mHttpUriRequest;
	}

    /**
     * Cancels the request.
     *
     * When request is canceled null response is returned in OnRequestResultListener.
     */
	public void cancel() {
		mIsCanceled = true;
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					mHttpUriRequest.disconnect();
				} catch (UnsupportedOperationException exc) {
					exc.printStackTrace();
				}
			}
		}).start();
	}

    /**
     * Gets request cancellation status
     *
     * @return True if request was canceled, false otherwise
     */
	public boolean isCanceled() {
		return mIsCanceled;
	}

    /**
     * Add request header.
     *
     * @param name Header key name
     * @param value Header value
     */
	protected void addHeader(String name, String value) {
		mHttpUriRequest.addRequestProperty(name, value);
	}

    /**
     * Removes header with provided key name from the request
     * @param name Header key name
     */
    protected void removeHeader(String name) {
		mHttpUriRequest.getRequestProperty(name);
	}

    /**
     * Gets information if all status codes are supported by the requests response
     *
     * If not all status codes are supported, the list of supported ones has to be
     * provided by getSupportedStatusCodes() method.
     * If not supported status code will be returned returned from server, UnsupportedResponseException
     * will be raised.
     *
     * @return True if all status codes are supported by the request response.
     */
	public boolean supportsAllStatusCodes() {
		return true;
	}

    /**
     * Gets the list of supported status codes.
     *
     * @return The list of supported status codes.
     */
	public int[] getSupportedStatusCodes() {
		int[] result = new int[1];
		
		result[0] = HttpStatusCode.SUCCESS_200_OK;
		
		return result;
	}

    /**
     * Gets the content description.
     *
     * This is description used for logging purposes.
     *
     * @return The content description.
     */
    public String getContentDescription() {
        return mContentDescription;
    }

    /**
     * Gets the request name.
     *
     * By default this method returns request class name.
     *
     * This is used for logging purposes.
     *
     * @return The request name.
     */
    public String getName() {
        return getClass().getSimpleName();
    }

    /**
     * Sets the content description.
     *
     * This is description used for logging purposes.
     *
     * @param contentDescription The content description.
     */
    protected void setContentDescription(String contentDescription) {
        mContentDescription = contentDescription;
    }

    /**
     * Sets request connection timeout in milliseconds
     *
     * @param timeout Connection timeout in milliseconds.
     */
    public void setConnectionTimeout(int timeout){
        mConnectionTimeout = timeout;
    }

    /**
     * Gets request connection timeout in milliseconds
     *
     * @return Connection timeout in milliseconds.
     */
    public Integer getConnectionTimeout(){
        return mConnectionTimeout;
    }

    /**
     * Sets request read timeout in milliseconds
     *
     * @param timeout Read timeout in milliseconds.
     */
    public void setReadTimeout(int timeout){
        mReadTimeout = timeout;
    }

    /**
     * Gets read timeout in milliseconds
     *
     * @return Read timeout in milliseconds.
     */
    public Integer getReadTimeout(){
        return mReadTimeout;
    }

    /**
     * Sets tag object related to the request.
     *
     * This object is passed to response created.
     * @param tag Tag object.
     */
    public void setTag(Object tag) {
        mTag = tag;
    }

    /**
     * Gets tag object related to the request.
     *
     * @return Tag object related to the request.
     */
    public Object getTag() {
        return mTag;
    }

	public String getUrl() {
		return mHttpUriRequest.getURL().toString();
	}

	public String getMethod() {
		return mHttpUriRequest.getRequestMethod();
	}

	public Map<String, List<String>> getHeaders() {
		return mHttpUriRequest.getRequestProperties();
	}
}
