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

package pl.openrnd.connection.rest.constant;

public final class ConnectionConstants {
	private ConnectionConstants() {}
	
	public static final int DEFAULT_HTTP_CONNECTION_WARNING_TIMEOUT = 20000;
	public static final int DEFAULT_HTTP_CONNECTION_TIMEOUT = 40000;
	public static final int DEFAULT_HTTP_READ_TIMEOUT = 40000;
	public static final int DEFAULT_LOG_SIZE = 50;
	public static final boolean DEFAULT_LOG_STATE = false;
	
	public static final boolean DEFAULT_USING_COOKIES = false;

	@Deprecated
	public final static boolean DEFAULT_FULL_ASYNC = false;
}
