package org.droidkit.net;

import org.apache.http.client.HttpClient;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.params.HttpConnectionParams;
import org.droidkit.DroidKit;

import android.net.http.AndroidHttpClient;

public class FroyoHttpClientFactory extends HttpClientFactory {
    
    @Override
    public HttpClient getNewThreadsafeHttpClient(int timeout, boolean trustAllCerts) {
        AndroidHttpClient client = AndroidHttpClient.newInstance(DEFAULT_USER_AGENT, DroidKit.getContext());
        HttpClientParams.setRedirecting(client.getParams(), true);
        if (timeout != SOCKET_OPERATION_TIMEOUT) {
            HttpConnectionParams.setConnectionTimeout(client.getParams(), timeout);
            HttpConnectionParams.setSoTimeout(client.getParams(), timeout);
        }
        if (trustAllCerts) {
            client.getConnectionManager().getSchemeRegistry().unregister("https");
            client.getConnectionManager().getSchemeRegistry().register(new Scheme("https", new FakeSocketFactory(), 443));
        }
        return client;
    }

}
