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

package pl.openrnd.connection.rest.utils;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public final class Utils {
	private static final String TAG = Utils.class.getSimpleName();
	private static final int BUFFER_SIZE = 8192;
	
	private Utils() {}

    /**
     * Tries to read data from provided input stream as a String object.
     *
     * @param inputStream InputStream from which data has to be read.
     * @return String object from the stream or null in case of error.
     */
	public static String streamToString(InputStream inputStream) {
		String result = null;

		Reader reader = null;
	    try {
	        reader = new BufferedReader(new InputStreamReader(inputStream));
	        StringBuilder builder = new StringBuilder();
	        char[] buffer = new char[BUFFER_SIZE];
	        int read;
	        while ((read = reader.read(buffer, 0, buffer.length)) > 0) {
	            builder.append(buffer, 0, read);
	        }
	        result = builder.toString();
	    } catch (IOException exc) {
	    	Log.e(TAG, "streamToString(): EXC");
	    	exc.printStackTrace();
	    	
		} finally {
	        if (reader != null) {
	        	try {
	        		reader.close();
	        	} catch (Exception exc) {
	        		Log.e(TAG, "streamToString(): EXC");
	    	    	exc.printStackTrace();
	        	}
	        }
	    }    
		return result;
	}
}
