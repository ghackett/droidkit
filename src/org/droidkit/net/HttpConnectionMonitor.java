package org.droidkit.net;

import java.net.HttpURLConnection;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.http.client.methods.HttpRequestBase;

import android.os.AsyncTask;

/**
 * A static singleton monitoring object (managed by the DroidKit lifecycle)
 * that keeps track of all connections being used using the DroidKit libraries
 * @author ghackett
 *
 */
public class HttpConnectionMonitor {

	protected LinkedBlockingQueue<HttpURLConnection> mUrlConnections = new LinkedBlockingQueue<HttpURLConnection>();
	protected LinkedBlockingQueue<HttpRequestBase> mHttpRequests = new LinkedBlockingQueue<HttpRequestBase>();
	protected int mContiguousConnectionErrorCount = 0;
	
	public void onRequestStart(HttpRequestBase request) {
		mHttpRequests.add(request);
	}
	
	public void onRequestFinished(HttpRequestBase request) {
		mHttpRequests.remove(request);
		resetContiguousConnectionErrorCount();
	}
	
	public void onRequestFinished(HttpRequestBase request, Throwable t) {
		mHttpRequests.remove(request);
		if (t != null)
			incrementContiguousConnectionErrorCount();
		else
			resetContiguousConnectionErrorCount();
	}
	
	public void onRequestStart(HttpURLConnection connection) {
		mUrlConnections.add(connection);
	}
	
	public void onRequestFinished(HttpURLConnection connection) {
		mUrlConnections.remove(connection);
		resetContiguousConnectionErrorCount();
	}
	
	public void onRequestFinished(HttpURLConnection connection, Throwable t) {
		mUrlConnections.remove(connection);
		if (t != null)
			incrementContiguousConnectionErrorCount();
		else
			resetContiguousConnectionErrorCount();
	}	
	
	private synchronized void resetContiguousConnectionErrorCount() {
		mContiguousConnectionErrorCount = 0;
	}
	
	private synchronized void incrementContiguousConnectionErrorCount() {
		mContiguousConnectionErrorCount++;
	}
	
	public synchronized int getContiguousConnectionErrorCount() {
		return mContiguousConnectionErrorCount;
	}
	
	public int getCurrentConnectionCount() {
		return mUrlConnections.size() + mHttpRequests.size();
	}
	
	public void stopAllConnectionsAsync() {
		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				stopAllConnections();
				return null;
			}
			
		}.execute((Void)null);
	}
	
	public void stopAllConnections() {
		for (HttpURLConnection conn : mUrlConnections) {
			try {
				conn.disconnect();
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
		
		for (HttpRequestBase req : mHttpRequests) {
			try {
				req.abort();
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
	}
}
