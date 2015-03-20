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

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.cookie.Cookie;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import pl.openrnd.connection.rest.request.Request;
import pl.openrnd.connection.rest.response.Response;

/**
 * Class containing communication log data.
 */
public class RestConnectionLog extends ConnectionLog implements Serializable {
    private String mRequestName;
    private String mRequestUri;
    private String mRequestMethod;
    private String mRequestContent;
    private Header[] mRequestHeaders;

    private String mResponseContent;
    private Header[] mResponseHeaders;
    private Integer mResponseStatusCode;
    private String mResponseReasonPhrase;
    private Exception mResponseException;
    private Date mResponseDate;
    private ArrayList<String> mCookies;

    private RestConnectionLog(Builder builder) {
        super(builder.mRequestDate);
        mRequestName = builder.mRequestName;
        mRequestUri = builder.mRequestUri;
        mRequestMethod = builder.mRequestMethod;
        mRequestContent = builder.mRequestContent;
        mRequestHeaders = builder.mRequestHeaders;

        mResponseContent = builder.mResponseContent;
        mResponseHeaders = builder.mResponseHeaders;
        mResponseStatusCode = builder.mResponseStatusCode;
        mResponseReasonPhrase = builder.mResponseReasonPhrase;
        mResponseException = builder.mResponseException;
        mResponseDate = builder.mResponseDate;
        mCookies = builder.mCookie;
    }

    /**
     * Gets request name.
     *
     * @see pl.openrnd.connection.rest.request.Request
     *
     * @return Request name or null when not available.
     */
    public String getRequestName() {
        return mRequestName;
    }

    /**
     * Gets request uri.
     *
     * @return Request uri as a String object or null when not available.
     */
    public String getRequestUri() {
        return mRequestUri;
    }

    /**
     * Gets request method (.e.g. POST, GET).
     *
     * @return Request method or null when not available.
     */
    public String getRequestMethod() {
        return mRequestMethod;
    }

    /**
     * Gets request content description.
     *
     * @see pl.openrnd.connection.rest.request.Request
     *
     * @return Request content description or null when not available.
     */
    public String getRequestContent() {
        return mRequestContent;
    }

    /**
     * Gets request headers.
     *
     * @return Request headers or null when not available.
     */
    public Header[] getRequestHeaders() {
        return mRequestHeaders != null ? mRequestHeaders.clone() : null;
    }

    /**
     * Gets response content description.
     *
     * @see pl.openrnd.connection.rest.response.Response
     *
     * @return Response content description or null when not available.
     */
    public String getResponseContent() {
        return mResponseContent;
    }

    /**
     * Gets server response headers.
     *
     * @return Server response headers or null when not available.
     */
    public Header[] getResponseHeaders() {
        return mResponseHeaders != null ? mResponseHeaders.clone() : null;
    }

    /**
     * Gets server response status code.
     *
     * @return Server response status code or null when not available.
     */
    public Integer getResponseStatusCode() {
        return mResponseStatusCode;
    }

    /**
     * Gets server response reason phrase.
     *
     * @return Server response reason phrase or null when not available.
     */
    public String getResponseReasonPhrase() {
        return mResponseReasonPhrase;
    }

    /**
     * Gets request execution exception.
     *
     * @return Request execution exception or null when not available.
     */
    public Exception getResponseException() {
        return mResponseException;
    }

    /**
     * Gets response time.
     *
     * @return Date object or null when not available.
     */
    public Date getResponseDate() {
        return mResponseDate != null ? (Date)mResponseDate.clone() : null;
    }

    /**
     * Gets list of cookies.
     *
     * The list is taken after request execution.
     *
     * @return List of cookies or null when not available.
     */
    public ArrayList<String> getCookies() {
        return mCookies;
    }

    /**
     * Gets response time as String object in "yyyy MMM dd HH:mm:ss.SSS" format.
     *
     * @return Response time as String object or null when not available.
     */
    public String getFormattedResponseDate() {
        return mResponseDate != null ? sSimpleDateFormat.format(mResponseDate) : null;
    }

    static class Builder {
        private String mRequestName;
        private String mRequestUri;
        private String mRequestMethod;
        private String mRequestContent;
        private Header[] mRequestHeaders;
        private Date mRequestDate;

        private String mResponseContent;
        private Header[] mResponseHeaders;
        private Integer mResponseStatusCode;
        private String mResponseReasonPhrase;
        private Exception mResponseException;
        private Date mResponseDate;
        private ArrayList<String> mCookie;

        Builder() {}

        Builder cookies(CookieStore cookieStore){
            mCookie = null;
            if (cookieStore != null) {
                List<Cookie> cookies = cookieStore.getCookies();
                if (cookies != null) {
                    int size = cookies.size();

                    mCookie = new ArrayList<>(size);
                    for (int i = 0; i < size; ++i) {
                        mCookie.add(cookies.get(i).toString());
                    }
                }
            }
            return this;
        }

        Builder request(HttpUriRequest request) {
            mRequestUri = request.getURI().toString();
            mRequestMethod = request.getMethod();
            mRequestHeaders = createHeaders(request.getAllHeaders());
            mRequestDate = Calendar.getInstance().getTime();
            return this;
        }

        Builder response(HttpResponse response) {
            mResponseHeaders = createHeaders(response.getAllHeaders());
            mResponseDate = Calendar.getInstance().getTime();

            StatusLine statusLine = response.getStatusLine();
            if (statusLine != null) {
                mResponseStatusCode = statusLine.getStatusCode();
                mResponseReasonPhrase = statusLine.getReasonPhrase();
            }

            return this;
        }

        Builder request(Request request) {
            mRequestName = request.getName();
            mRequestContent = request.getContentDescription();
            return this;
        }

        Builder response(Response response) {
            mResponseException = response.getException();
            mResponseContent = response.getContentDescription();

            if (mRequestDate == null) {
                mResponseDate = Calendar.getInstance().getTime();
            }
            return this;
        }

        RestConnectionLog build() {
            return new RestConnectionLog(this);
        }
    }

    private static Header[] createHeaders(org.apache.http.Header[] headers) {
        Header[] result = null;

        if ((headers != null) && (headers.length > 0)) {
            result = new Header[headers.length];

            for (int i = 0; i < headers.length; ++i) {
                org.apache.http.Header header = headers[i];
                result[i] = new Header(header.getName(), header.getValue());
            }
        }

        return result;
    }

    //org.apache.http.Header does not implement Serializable interface
    public static class Header implements Serializable {
        private static final long serialVersionUID = -4073612454166266286L;
        private String mKey;
        private String mValue;

        public Header(String key, String value) {
            mKey = key;
            mValue = value;
        }

        public String getKey() {
            return mKey;
        }

        public String getValue() {
            return mValue;
        }
    }
}
