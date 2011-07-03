package org.droidkit.net;

import org.apache.http.client.HttpClient;
import org.apache.http.client.params.HttpClientParams;
import org.droidkit.DroidKit;

import android.net.http.AndroidHttpClient;

public class FroyoHttpClientFactory extends HttpClientFactory {
    
    @Override
    protected HttpClient getNewThreadsafeHttpClient() {
        AndroidHttpClient client = AndroidHttpClient.newInstance(DEFAULT_USER_AGENT, DroidKit.getContext());
        HttpClientParams.setRedirecting(client.getParams(), true);
        return client;
    }

}
