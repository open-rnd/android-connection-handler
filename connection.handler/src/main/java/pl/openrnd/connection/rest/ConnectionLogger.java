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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import pl.openrnd.connection.rest.constant.ConnectionConstants;

/**
 * Connection logger class
 */
public class ConnectionLogger {
	private int mMaxQueueSize;
	
	private List<WeakReference<OnLogQueueChangeListener>> mQueueChangeListeners;
	private LinkedList<ConnectionLog> mLogQueue;
	private boolean mAreLogsEnabled;

    /**
     * Class constructor with default initialization.
     */
    public ConnectionLogger() {
        mAreLogsEnabled = ConnectionConstants.DEFAULT_LOG_STATE;
        mMaxQueueSize = ConnectionConstants.DEFAULT_LOG_SIZE;

        mQueueChangeListeners = new ArrayList<WeakReference<OnLogQueueChangeListener>>();
        mLogQueue = new LinkedList<ConnectionLog>();
    }

    /**
     * Class constructor
     *
     * @param logsEnabled Logs state. True when logs are enabled, false otherwise
     * @param maxQueueSize Logs max queue size.
     */
	public ConnectionLogger(boolean logsEnabled, int maxQueueSize) {
		mAreLogsEnabled = logsEnabled;
        mMaxQueueSize = maxQueueSize;

        mQueueChangeListeners = new ArrayList<WeakReference<OnLogQueueChangeListener>>();
        mLogQueue = new LinkedList<ConnectionLog>();
	}

    /**
     * Sets logs state.
     *
     * If logs are disabled all previously taken logs are cleared.
     *
     * @param enabled New logs state. True if logs are enabled, false otherwise.
     */
	public synchronized void setLogsState(boolean enabled) {
		if (mAreLogsEnabled != enabled) {
			mAreLogsEnabled = enabled;
			
			if (!mAreLogsEnabled) {
				clearLogs();
			}
		}
	}

    /**
     * Gets logs state.
     *
     * @return Logs state.  True if logs are enabled, false otherwise.
     */
	public synchronized boolean areLogsEnabled() {
		return mAreLogsEnabled;
	}

    /**
     * Gets max logs queue size.
     *
     * @return Max logs queue size.
     */
	public synchronized int getQueueMaxSize() {
		return mMaxQueueSize;
	}

    /**
     * Gets current logs queue size.
     *
     * @return Current logs queue size.
     */
	public synchronized int getQueueSize() {
		return mLogQueue.size();
	}

    /**
     * Clears logs.
     */
	public synchronized void clearLogs() {
		boolean notifyChange = mLogQueue.size() > 0;
		mLogQueue.clear();
		
		if (notifyChange) {
			notifyListeners();
		}
	}

    /**
     * Set logs queue max size.
     *
     * @param queueMaxSize Max logs queue size.
     */
	public synchronized void setQueueMaxSize(int queueMaxSize) {
		mMaxQueueSize = queueMaxSize;
		
		if (validateSize()) {
			notifyListeners();
		}
	}

    /**
     * Adds new connection log.
     *
     * New log notification is performed in a caller thread.
     *
     * @param connectionLog ConnectionLog object with log data.
     */
	public synchronized void addConnectionLog(ConnectionLog connectionLog) {
		if (mAreLogsEnabled) {
			mLogQueue.addLast(connectionLog);
			validateSize();
			notifyListeners();
		}
	}

    /**
     * Gets list of connection logs.
     */
	public synchronized List<ConnectionLog> getConnectionLogs() {
		return new ArrayList<ConnectionLog>(mLogQueue);
	}
	
	private synchronized boolean validateSize() {
		boolean result = false;
		
		int delta = getQueueSize() - getQueueMaxSize();
		if (delta > 0) {
			result = true;
			
			for (int i = 0; i < delta; ++i) {
				mLogQueue.removeFirst();
			}
		}
		
		return result;
	}
	
	private synchronized void notifyListeners() {
		Iterator<WeakReference<OnLogQueueChangeListener>> iterator = mQueueChangeListeners.iterator();
		
		while (iterator.hasNext()) {
			OnLogQueueChangeListener listener = iterator.next().get();
			if (listener == null) {
				iterator.remove();
			} else {
				listener.onQueueSizeChanged(this);
			}
		}
	}

    /**
     * Registers OnLogQueueChangeListener object that will receive notifications about logs queue changes.
     *
     * The listener is internally stored as a weak reference. Please keep
     * its strong reference as long as the object is suppose to receive notifications.
     *
     * @param changeListener
     */
	public synchronized void registerOnLogQueueChangeListener(OnLogQueueChangeListener changeListener) {
		Iterator<WeakReference<OnLogQueueChangeListener>> iterator = mQueueChangeListeners.iterator();
		
		while (iterator.hasNext()) {
			OnLogQueueChangeListener listener = iterator.next().get();
			if (listener == null) {
				iterator.remove();
			} else if (listener == changeListener) {
				return;
			}
		}
		
		mQueueChangeListeners.add(new WeakReference<ConnectionLogger.OnLogQueueChangeListener>(changeListener));
	}

    /**
     * Removes OnLogQueueChangeListener object from receiving logs queue change notifications.
     *
     * @param changeListener OnLogQueueChangeListener object.
     */
	public synchronized void unregisterOnLogChangeListener(OnLogQueueChangeListener changeListener) {
		Iterator<WeakReference<OnLogQueueChangeListener>> iterator = mQueueChangeListeners.iterator();
		
		while (iterator.hasNext()) {
			OnLogQueueChangeListener listener = iterator.next().get();
			if ((listener == null) || (listener == changeListener)) {
				iterator.remove();
			}
		}
	}

    /**
     * Interface used for getting notification about logs queue size changes.
     */
	public interface OnLogQueueChangeListener {
        /**
         * Method called when logs queue size changed.
         *
         * @param connectionLogger ConnectionLogger object whose queue size changed.
         */
		void onQueueSizeChanged(ConnectionLogger connectionLogger);
	}
}
