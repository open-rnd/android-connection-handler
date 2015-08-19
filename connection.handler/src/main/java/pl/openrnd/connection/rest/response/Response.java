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

package pl.openrnd.connection.rest.response;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import pl.openrnd.connection.rest.RestConnectionLog;

/**
 * Base response class
 *
 * Each class that extends Response must provide all its constructors.
 */
public abstract class Response {
	private Exception mException;
	private Integer mHttpStatusCode;
	private String mHttpReasonPhrase;
	private String mContentDescription;
	private Map<String, List<String>> mHeaders;

    private Object mTag;

    /**
     * Class constructor.
     *
     * @param httpStatusCode Http status code (e.g., 200)
     * @param httpReasonPhrase Http reason phrase (e.g., "200 OK")
     * @param headers Response headers
     * @param entityContentStream Opened input stream for response entity. No need to close it.
     */
	public Response(Integer httpStatusCode, String httpReasonPhrase, Map<String, List<String>> headers, InputStream entityContentStream) {
		mHttpStatusCode = httpStatusCode;
		mHttpReasonPhrase = httpReasonPhrase;
		mHeaders = headers;
		
		try {
			handleContent(entityContentStream);
		} catch (Exception exc) {
			mException = exc;
			
			exc.printStackTrace();
		}
	}

    /**
     * Class constructor.
     *
     * @param exception Exception that was thrown during request executing.
     */
	public Response(Exception exception) {
		mException = exception;
	}

    /**
     * Gets information if exception was thrown during request execution.
     *
     * @return True if exception was thrown, false otherwise.
     */
	public boolean hasException() {
		return mException != null;
	}

    /**
     * Gets the exception that was raised during request execution.
     *
     * @return The exception that was raised during request execution.
     */
	public Exception getException() {
		return mException;
	}

    /**
     * Gets server status code
     *
     * @return Server status code
     */
	public Integer getHttpStatusCode() {
		return mHttpStatusCode;
	}

    /**
     * Gets server response reason phrase
     *
     * @return Server response reason phrase
     */
	public String getHttpReasonPhrase() {
		return mHttpReasonPhrase;
	}

    /**
     * Gets response headers
     *
     * @return Response headers
     */
	public Map<String, List<String>> getHeaders() {
		return mHeaders;
	}

    /**
     * Gets response header
     *
     * @param headerKey Header key name
     * @return Header value or null if header with provided key is not found
     */
    public String getHeaderValue(String headerKey) {
        String result = null;

        if (mHeaders != null) {
            int length = mHeaders.size();
            for (Map.Entry<String, List<String>> header : mHeaders.entrySet()) {
                if (header.getKey().equalsIgnoreCase(headerKey)) {
                    result = header.getValue().toString();
                    break;
                }
            }
        }

        return result;
    }

    /**
     * Method to be overridden by extended class.
     *
     * The method is suppose to handle the response entity content.
     *
     * @param entityContentStream Opened input stream to response entity content.
     * @throws Exception Exception if content can not be successfully handled.
     */
	protected abstract void handleContent(InputStream entityContentStream) throws Exception;

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
     * Sets tag object related to the response.
     *
     * @param tag Tag object.
     */
    public void setTag(Object tag) {
        mTag = tag;
    }

    /**
     * Gets tag object related to the response.
     *
     * Initially this is the object provided to the request that the response is
     * related to.
     *
     * @return Tag object related to the response.
     */
    public Object getTag() {
        return mTag;
    }
}
