package org.droidkit.net;

import org.apache.http.client.HttpClient;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.scheme.SocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.droidkit.DroidKit;

import android.net.SSLCertificateSocketFactory;

public class HttpClientFactory {
    
    public static final int SOCKET_OPERATION_TIMEOUT = 60 * 1000;
    public static final String DEFAULT_USER_AGENT = "DroidKit-HTTPClient-for-Android";

    private static HttpClientFactory sFactoryInstance = null;
    private static HttpClient sClientInstance = null;
    
    public static HttpClientFactory getInstance() {
        if (sFactoryInstance == null) {
            if (DroidKit.isFroyo())
                sFactoryInstance = new FroyoHttpClientFactory();
            else
                sFactoryInstance = new HttpClientFactory();
        }
        return sFactoryInstance;
    }
    
    public synchronized HttpClient getSharedClient() {
        if (sClientInstance == null)
            sClientInstance = getNewThreadsafeHttpClient(SOCKET_OPERATION_TIMEOUT);
        return sClientInstance;
    }
    
    public HttpClient getNewThreadsafeHttpClient(int timeout) {
//        HttpParams params = new BasicHttpParams();

        DefaultHttpClient badClient = new DefaultHttpClient();
        
        HttpParams params = badClient.getParams();
        SchemeRegistry schemeRegistry = badClient.getConnectionManager().getSchemeRegistry();
        
        // Turn off stale checking.  Our connections break all the time anyway,
        // and it's not worth it to pay the penalty of checking every time.

        HttpConnectionParams.setStaleCheckingEnabled(params, false);

        HttpConnectionParams.setConnectionTimeout(params, timeout);
        HttpConnectionParams.setSoTimeout(params, timeout);
        HttpConnectionParams.setSocketBufferSize(params, 8192);

        HttpClientParams.setRedirecting(params, true);
        
        HttpProtocolParams.setUserAgent(params, DEFAULT_USER_AGENT);

        // Set the specified user agent and register standard protocols.
//        SchemeRegistry schemeRegistry = new SchemeRegistry();
//        schemeRegistry.register(new Scheme("http",
//                PlainSocketFactory.getSocketFactory(), 80));
//        schemeRegistry.register(new Scheme("https", 
//                (SocketFactory) SSLCertificateSocketFactory.getDefault(SOCKET_OPERATION_TIMEOUT), 443));

        ClientConnectionManager manager =
                new ThreadSafeClientConnManager(params, schemeRegistry);
        
        return new DefaultHttpClient(manager, params);
    }
    
}
