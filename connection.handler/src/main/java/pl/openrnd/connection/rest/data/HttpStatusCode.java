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

package pl.openrnd.connection.rest.data;

public interface HttpStatusCode {
	public static final int SUCCESS_200_OK = 200;
	public static final int SUCCESS_201_CREATED = 201;
	public static final int SUCCESS_202_ACCEPTED = 202;
	public static final int SUCCESS_204_NO_CONTENT = 204;
	public static final int REDIRECT_303_SEE_OTHER = 303;
    public static final int REDIRECT_304_NOT_MODIFIED = 304;
	public static final int CLIENT_ERROR_400_BAD_REQUEST = 400;
	public static final int CLIENT_ERROR_401_UNAUTHORIZED = 401;
	public static final int CLIENT_ERROR_403_FORBIDDEN = 403;
	public static final int CLIENT_ERROR_404_NOT_FOUND = 404;
	public static final int CLIENT_ERROR_406_NOT_ACCEPTABLE = 406;
	public static final int CLIENT_ERROR_409_CONFLICT = 409;
	public static final int SERVER_ERROR_500_INTERNAL_SERVER_ERROR = 500;
	public static final int SERVER_ERROR_501_NOT_IMPLEMENTED = 501;
}
