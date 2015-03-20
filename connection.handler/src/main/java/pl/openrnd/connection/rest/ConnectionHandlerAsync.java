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

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantLock;

import pl.openrnd.connection.rest.request.Request;
import pl.openrnd.connection.rest.response.Response;
import pl.openrnd.connection.rest.utils.ObjectListenerHandler;

/**
 * Class that processes requests asynchronously
 */
public class ConnectionHandlerAsync {
	
	public static String TAG = ConnectionHandlerAsync.class.getSimpleName();
	
	private ObjectListenerHandler<OnRequestResultListener> mGlobalRequestResultListeners;
	private LinkedList<RequestDataHolder> mRequestDataHolders;
	private ReentrantLock mRequestDataHoldersLock;
	private ConnectionHandler mConnectionHandler;
	private RequestsHandlerThread mRequestsHandlerThread;
	private Handler mUiHandler;
	private ConnectionConfig mConnectionConfig;

    /**
     * Class constructor
     *
     * @param context Application context.
     * @param connectionConfig ConnectionConfig object with configuration data.
     */
	public ConnectionHandlerAsync(Context context, ConnectionConfig connectionConfig) {
		mConnectionConfig = connectionConfig;

        mGlobalRequestResultListeners = new ObjectListenerHandler<OnRequestResultListener>();
		mConnectionHandler = new ConnectionHandler(context, connectionConfig);
		mRequestDataHolders = new LinkedList<RequestDataHolder>();
		mRequestDataHoldersLock = new ReentrantLock();
        mUiHandler = new Handler(Looper.getMainLooper());
	}

    /**
     * Gets ConnectionLogger related to the handler.
     *
     * @see pl.openrnd.connection.rest.ConnectionLogger
     *
     * @return ConnectionLogger object.
     */
	public ConnectionLogger getConnectionLogger() {
		return mConnectionHandler.getConnectionLogger();
	}

    /**
     * Method for clearing cookies
     */
	public void clearCookie() {
		mConnectionHandler.clearCookie();
	}

    /**
     * Registers global OnRequestResultListener object.
     *
     * This listener will be notified about status of each request processing.
     *
     * The listener is stored internally as a weak reference object. Please keep
     * its strong reference as long as the object is suppose to receive notifications.
     *
     * @param listener OnRequestResultListener object.
     */
	public void registerGlobalRequestResultWeakListener(OnRequestResultListener listener) {
        mGlobalRequestResultListeners.registerObjectWeakListener(listener);
	}

    /**
     * Removes global OnRequestResultListener object.
     *
     * @param listener
     */
	public void removeGlobalRequestResultWeakListener(OnRequestResultListener listener) {
        mGlobalRequestResultListeners.unregisterObjectWeakListener(listener);
	}

    private void notifyListenerResponse(final Request request, final Response response, final OnRequestResultListener requestResultListener) {
        if (requestResultListener != null) {
            if (requestResultListener instanceof OnUiRequestResultListener) {
                mUiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        requestResultListener.onRequestResultReady(request, response);
                    }
                });
            } else {
                requestResultListener.onRequestResultReady(request, response);
            }
        }
    }

	private void notifyResponse(final Request request, final Response response, final OnRequestResultListener requestResultListener) {
        notifyListenerResponse(request, response, requestResultListener);

		mGlobalRequestResultListeners.notifyObjectChange(new ObjectListenerHandler.NotificationHandler<OnRequestResultListener>() {
            @Override
            public void runOnListener(final OnRequestResultListener listener) {
                notifyListenerResponse(request, response, listener);
            }
        });
	}

    /**
     * Sets OnRequestConnectionListener object that will be notified when performing requests
     * will takes too long.
     *
     * @param listener OnRequestConnectionListener object.
     */
	public void setRequestConnectionListener(OnRequestConnectionListener listener){
		mConnectionHandler.setRequestConnectionListener(listener);
	}

    /**
     * Adds requests to the requests queue.
     *
     * @param request Request object to be executed.
     * @param requestResultListener OnRequestResultListener object to be notified about execution status.
     */
	public void addRequest(Request request, OnRequestResultListener requestResultListener) {
		Log.d(TAG, "addRequest()");
		
		if (request != null) {
			mRequestDataHoldersLock.lock();
			mRequestDataHolders.addLast(new RequestDataHolder(request, requestResultListener));
			mRequestDataHoldersLock.unlock();
			
			if (mConnectionConfig.isFullAsync()) {
				Thread thread = new Thread(getRequestExecutionRunnable());
				thread.start();
			} else {
				if ((mRequestsHandlerThread == null) || (mRequestsHandlerThread.isFinished())) {
					mRequestsHandlerThread = new RequestsHandlerThread();
					mRequestsHandlerThread.start();
				}
			}
		}
	}

    /**
     * Cancels all requests execution.
     *
     * When request is canceled no response object is created.
     *
     * @see pl.openrnd.connection.rest.OnRequestResultListener
     */
	public void cancelAllRequests() {
		Log.d(TAG, "cancelAllRequests()");
		
		mRequestDataHoldersLock.lock();
		
		LinkedList<RequestDataHolder> requestDataHolders = new LinkedList<ConnectionHandlerAsync.RequestDataHolder>(mRequestDataHolders);
		
		mRequestDataHolders.clear();
		
		mRequestDataHoldersLock.unlock();
		
		for (RequestDataHolder requestDataHolder : requestDataHolders) {
			Request request = requestDataHolder.getRequest();
			request.cancel();
			
			notifyResponse(request, null, requestDataHolder.getRequestResultListener());
		}
	}

	private Runnable getRequestExecutionRunnable() {
		return new Runnable() {
			@Override
			public void run() {
				Log.d(TAG, "getRequestExecutionRunnable(): run()");
				
				mRequestDataHoldersLock.lock();
				
				if (!mRequestDataHolders.isEmpty()) {
					RequestDataHolder requestDataHolder = mRequestDataHolders.removeFirst();
					mRequestDataHoldersLock.unlock();
					
					Request request = requestDataHolder.getRequest();
					OnRequestResultListener requestResultListener = requestDataHolder.getRequestResultListener();

                    Response response = null;
					if (!request.isCanceled()) {
                        response = mConnectionHandler.handleRequest(request);
                    }
                    notifyResponse(request, response, requestResultListener);
				} else {
					mRequestDataHoldersLock.unlock();
				}
			}
		};
	}
	
	private class RequestsHandlerThread extends Thread {
		private boolean mIsFinished;
		
		public RequestsHandlerThread() {
			mIsFinished = false;
		}
		
		public synchronized void setFinished(boolean isFinished) {
			mIsFinished = isFinished;
		}
		
		public synchronized boolean isFinished() {
			return mIsFinished;
		}
		
		public void run() {
			while (!isFinished()) {
				getRequestExecutionRunnable().run();
				
				mRequestDataHoldersLock.lock();
				setFinished(mRequestDataHolders.isEmpty());
				mRequestDataHoldersLock.unlock();
			}
		}
	}
	
	private class RequestDataHolder {
		private Request mRequest;
		private OnRequestResultListener mRequestResultListener;
		
		public RequestDataHolder(Request request, OnRequestResultListener requestResultListener) {
			mRequest = request;
			mRequestResultListener = requestResultListener;
		}
		
		public Request getRequest() {
			return mRequest;
		}
		
		public OnRequestResultListener getRequestResultListener() {
			return mRequestResultListener;
		}
	}
}
