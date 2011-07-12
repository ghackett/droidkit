package org.droidkit.net;

import org.apache.http.client.HttpClient;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.params.HttpConnectionParams;
import org.droidkit.DroidKit;

import android.net.http.AndroidHttpClient;

public class FroyoHttpClientFactory extends HttpClientFactory {
    
    @Override
    public HttpClient getNewThreadsafeHttpClient(int timeout) {
        AndroidHttpClient client = AndroidHttpClient.newInstance(DEFAULT_USER_AGENT, DroidKit.getContext());
        HttpClientParams.setRedirecting(client.getParams(), true);
        if (timeout != SOCKET_OPERATION_TIMEOUT) {
            HttpConnectionParams.setConnectionTimeout(client.getParams(), timeout);
            HttpConnectionParams.setSoTimeout(client.getParams(), timeout);
        }
        return client;
    }

}
