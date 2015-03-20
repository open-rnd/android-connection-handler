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

import pl.openrnd.connection.rest.request.Request;
import pl.openrnd.connection.rest.response.Response;

/**
 * Interface used for getting notification about request execution finish status.
 *
 * Notification is performed on the thread that the request was executed on.
 * This is not UI thread. If it is desired for notification to be performed
 * on the UI thread please use OnUiRequestResultListener instead.
 */
public interface OnRequestResultListener {

    /**
     * Method called when request execution finished.
     *
     * @param request Request that was executed.
     * @param response Response object with response data. If request was canceled
     *                 this parameter will be null.
     */
	void onRequestResultReady(Request request, Response response);
}
