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

import org.apache.http.conn.scheme.SchemeRegistry;

import javax.net.ssl.SSLSocketFactory;

import pl.openrnd.connection.rest.constant.ConnectionConstants;

/**
 * Class containing parameters used by ConnectionHandler|ConnectionHandlerAsync
 */
public class ConnectionConfig {
    private int mReadTimeout;
    private int mConnectionTimeout;
    private Integer mRequestWarningTime;
    private boolean mIsFullAsync;
    private boolean mAreLogsEnabled;
    private boolean mIsUsingCookies;
    private int mLogsSize;
    private SSLSocketFactory mSSLSocketFactory;

    /**
     * Gets connection read timeout in milliseconds
     *
     * @return Connection read timeout in milliseconds
     */
    public int getReadTimeout() {
        return mReadTimeout;
    }

    /**
     * Gets connection timeout in milliseconds
     *
     * @return Connection timeout in milliseconds
     */
    public int getConnectionTimeout() {
        return mConnectionTimeout;
    }

    /**
     * Gets full async mode status.
     * <p/>
     * If set, each request is executed in a separate thread.
     * <p/>
     * Functionality is deprecated.
     *
     * @return True if full async mode is enabled, false otherwise
     */
    public boolean isFullAsync() {
        return mIsFullAsync;
    }

    /**
     * Gets request warning time in milliseconds.
     * <p/>
     * When RequestConnectionListener object is registered in ConnectionHandler|ConnectionHandlerAsync,
     * the object will be notified if request is still executing after this time.
     *
     * @return Request warning time in milliseconds
     */
    public Integer getRequestWarningTime() {
        return mRequestWarningTime;
    }

    /**
     * Gets logs state
     *
     * @return True if logs are enabled, false otherwise
     */
    public boolean getInitialLogsState() {
        return mAreLogsEnabled;
    }

    /**
     * Gets logs queue size
     *
     * @return Logs queue size
     */
    public int getInitialLogsSize() {
        return mLogsSize;
    }

    /**
     * Gets SSL Socket Factory
     *
     * @return SSL Socket Factory
     */
    public SSLSocketFactory getSSLSocketFactory() {
        return mSSLSocketFactory;
    }

    /**
     * Gets cookies state
     *
     * @return True if connection is using cookies, false otherwise
     */
    public boolean isUsingCookies() {
        return mIsUsingCookies;
    }

    private ConnectionConfig(Builder builder) {
        mReadTimeout = validateTimeout(builder.mReadTimeout, ConnectionConstants.DEFAULT_HTTP_READ_TIMEOUT);
        mConnectionTimeout = validateTimeout(builder.mConnectionTimeout, ConnectionConstants.DEFAULT_HTTP_CONNECTION_TIMEOUT);
        mRequestWarningTime = validateTimeout(builder.mRequestWarningTime, ConnectionConstants.DEFAULT_HTTP_CONNECTION_WARNING_TIMEOUT);
        mLogsSize = validateNonNegative(builder.mLogsSize, ConnectionConstants.DEFAULT_LOG_SIZE);
        mAreLogsEnabled = builder.mAreLogsEnabled;
        mIsUsingCookies = builder.mIsUsingCookies;
        mIsFullAsync = builder.mIsFullAsync;
        mSSLSocketFactory = builder.mSSLSocketFactory;
    }

    private Integer validateTimeout(Integer timeout, Integer defaultValue) {
        if (timeout != null) {
            if (timeout <= 0) {
                return defaultValue;
            } else {
                return timeout;
            }
        } else {
            return defaultValue;
        }
    }

    private Integer validateNonNegative(Integer value, Integer defaultValue) {
        if (value != null) {
            if (value < 0) {
                return defaultValue;
            } else {
                return value;
            }
        } else {
            return defaultValue;
        }
    }

    /**
     * Builder of ConnectionConfig class
     */
    public static class Builder {
        private Integer mReadTimeout;
        private Integer mConnectionTimeout;
        private Integer mRequestWarningTime;
        private boolean mIsFullAsync;
        private boolean mAreLogsEnabled;
        private int mLogsSize;
        private boolean mIsUsingCookies;
        private SSLSocketFactory mSSLSocketFactory;

        /**
         * Base class constructor
         */
        public Builder() {
            mIsFullAsync = ConnectionConstants.DEFAULT_FULL_ASYNC;
            mAreLogsEnabled = ConnectionConstants.DEFAULT_LOG_STATE;
            mLogsSize = ConnectionConstants.DEFAULT_LOG_SIZE;
            mIsUsingCookies = ConnectionConstants.DEFAULT_USING_COOKIES;
        }

        /**
         * Sets connections and read timeouts in milliseconds
         *
         * @param connectionTimeout Connection timeout in milliseconds.
         * @param readTimeout       Read timeout in milliseconds.
         * @return Builder object
         */
        public Builder readTimeout(Integer connectionTimeout, Integer readTimeout) {
            mConnectionTimeout = connectionTimeout;
            mReadTimeout = readTimeout;
            return this;
        }

        /**
         * Sets request warning time in milliseconds.
         * <p/>
         * When RequestConnectionListener object is registered in ConnectionHandler|ConnectionHandlerAsync,
         * the object will be notified if request is still executing after this time.
         *
         * @param requestWarningTime Request warning time in milliseconds.
         * @return Builder object
         */
        public Builder requestWarningTime(Integer requestWarningTime) {
            mRequestWarningTime = requestWarningTime;
            return this;
        }

        /**
         * Sets full async mode. It that mode each request is executed in a separate thread.
         * <p/>
         * Functionality is deprecated.
         *
         * @param isFullAsync True to enable full async mode, false otherwise
         * @return Builder object
         */
        @Deprecated
        public Builder isFullAsync(boolean isFullAsync) {
            mIsFullAsync = isFullAsync;
            return this;
        }

        /**
         * Sets logs status.
         * <p/>
         * When enabled all network communication will be logged
         *
         * @param areLogsEnabled Logs status
         * @return Builder object
         */
        public Builder logsEnabled(boolean areLogsEnabled) {
            mAreLogsEnabled = areLogsEnabled;
            return this;
        }

        /**
         * Sets logs queue size
         *
         * @param logsSize Logs size
         * @return Builder object
         */
        public Builder logsSize(int logsSize) {
            mLogsSize = logsSize;
            return this;
        }

        /**
         * Sets cookies state
         *
         * @param usingCookies Cookies state
         * @return Builder object
         */
        public Builder usingCookies(boolean usingCookies) {
            mIsUsingCookies = usingCookies;
            return this;
        }

        /**
         * Sets ssl socket factory
         *
         * @param sslSocketFactory ssl socket factory
         * @return Builder object
         */
        public Builder sslSocketFactory(SSLSocketFactory sslSocketFactory) {
            mSSLSocketFactory = sslSocketFactory;
            return this;
        }

        /**
         * Creates new instance of ConnectionConfig class
         *
         * @return New ConnectionConfig object
         */
        public ConnectionConfig build() {
            return new ConnectionConfig(this);
        }
    }
}
