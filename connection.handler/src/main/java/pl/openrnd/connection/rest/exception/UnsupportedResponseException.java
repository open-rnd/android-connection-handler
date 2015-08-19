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

package pl.openrnd.connection.rest.exception;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import pl.openrnd.connection.rest.RestConnectionLog;
import pl.openrnd.connection.rest.utils.Utils;

/**
 * Exception thrown by ConnectionHandler where server returns status code.
 *
 * @see pl.openrnd.connection.rest.request.Request
 * @see pl.openrnd.connection.rest.response.Response
 */
public class UnsupportedResponseException extends Exception {
	private static final long serialVersionUID = -5776937298456362123L;
	
	private Integer mHttpStatusCode;
	private String mHttpReasonPhrase;
	private RestConnectionLog.Header[] mHeaders;
	private String mContent;

    /**
     * Class constructor
     *
     * @param statusCode Server response status code
     * @param reasonPhrase Server response reason phrase
     * @param headers Server response headers
     * @param entityInputStream Opened input stream to response content entity. No need to close it.
     */
	public UnsupportedResponseException(Integer statusCode, String reasonPhrase, Map<String, List<String>> headers, InputStream entityInputStream) {
		super(String.format("UnsupportedResponseException: %d/%s", statusCode, reasonPhrase));
		
		mContent = Utils.streamToString(entityInputStream);
	}

    /**
     * Gets server response status code
     *
     * @return Server response status code
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
     * Gets server response headers
     *
     * @return Response headers
     */
	public RestConnectionLog.Header[] getHeaders() {
		return mHeaders;
	}

    /**
     * Gets response entity content as a String object
     * @return Response entity content as a String object
     */
	public String getContent() {
		return mContent;
	}
	
}
