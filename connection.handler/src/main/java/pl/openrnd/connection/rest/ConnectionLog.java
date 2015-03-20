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

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Base class containing data related to network communication.
 */
public class ConnectionLog implements Serializable {
    protected static final SimpleDateFormat sSimpleDateFormat = new SimpleDateFormat("yyyy MMM dd HH:mm:ss.SSS");
    private Date mDate;

    /**
     * Class constructor.
     *
     * The constructor initializes log time with current time.
     */
    public ConnectionLog() {
        mDate = Calendar.getInstance().getTime();
    }

    /**
     * Class constructor.
     *
     * @param date Log time
     */
    public ConnectionLog(Date date) {
        mDate = (Date)date.clone();
    }

    /**
     * Gets log time.
     *
     * @return Date object with log time.
     */
    public Date getDate() {
        return (Date)mDate.clone();
    }

    /**
     * Sets log time.
     *
     * @param date Date object with log time.
     */
    protected void setDate(Date date) {
        mDate = (Date)date.clone();
    }

    /**
     * Gets log time as a String object in "yyyy MMM dd HH:mm:ss.SSS" format.
     *
     * @return Log time as a String object
     */
    public String getFormattedDate() {
        return sSimpleDateFormat.format(getDate());
    }
}
