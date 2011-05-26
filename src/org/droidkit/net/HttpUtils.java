/*
 * Copyright 2011 droidkit.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.droidkit.net;

import android.os.Bundle;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.entity.StringEntity;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * @author mrn
 */
public class HttpUtils {
    public static final int METHOD_GET = 0x1;
    public static final int METHOD_POST = 0x2;
    public static final int METHOD_PUT = 0x3;
    public static final int METHOD_DELETE = 0x4;

    public static String executeRequest(String url, int method, String body, Bundle params, Bundle headers) throws HttpConnectionException, HttpResponseException {
        DefaultHttpClient client = new DefaultHttpClient();
        HttpResponse httpResponse;
        String response = null;

        try {
            switch (method) {
            case METHOD_GET:
                url = url + "?" + encodeParams(params);
                HttpGet get = new HttpGet(url);

                if (headers != null) {
                    for (String key : headers.keySet()) {
                        get.addHeader(key, headers.getString(key));
                    }
                }

                httpResponse = client.execute(get);
                break;
            case METHOD_POST:
                if (body != null) {
                    url = url + "?" + encodeParams(params);
                }

                HttpPost post = new HttpPost(url);

                if (headers != null) {
                    for (String key : headers.keySet()) {
                        post.addHeader(key, headers.getString(key));
                    }
                }

                if (body == null) {
                    post.setEntity(new UrlEncodedFormEntity(bundleToList(params), HTTP.UTF_8));
                } else {
                    post.setEntity(new StringEntity(body));
                }
    
                httpResponse = client.execute(post);
                break;
            case METHOD_PUT:
                url = url + "?" + encodeParams(params);
                HttpPut put = new HttpPut(url);

                if (headers != null) {
                    for (String key : headers.keySet()) {
                        put.addHeader(key, headers.getString(key));
                    }
                }

                httpResponse = client.execute(put);
                break;
            case METHOD_DELETE:
                url = url + "?" + encodeParams(params);
                HttpDelete delete = new HttpDelete(url);

                if (headers != null) {
                    for (String key : headers.keySet()) {
                        delete.addHeader(key, headers.getString(key));
                    }
                }

                httpResponse = client.execute(delete);
                break;
            default:
                throw new IllegalArgumentException("Unknown http method: " + method);
            }

            int statusCode = httpResponse.getStatusLine().getStatusCode();
            response = read(httpResponse.getEntity().getContent());

            if (statusCode != HttpStatus.SC_OK) {
                throw new HttpResponseException(statusCode, response);
            }
        } catch (ClientProtocolException e) {
            throw new HttpConnectionException(e);
        } catch (IOException e) {
            throw new HttpConnectionException(e);
        }

        return response;
    }

    public static String read(InputStream in) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader r = new BufferedReader(new InputStreamReader(in), 1000);

        for (String line = r.readLine(); line != null; line = r.readLine()) {
            sb.append(line);
        }

        in.close();
        return sb.toString();
    }

    public static String encodeParams(Bundle params) {
        if (params == null || params.isEmpty()) {
            return "";
        }
        
        StringBuilder sb = new StringBuilder();
        boolean first = true;

        for (String key : params.keySet()) {
            if (first) {
                first = false;
            } else {
                sb.append("&");
            }

            try {
                sb.append(URLEncoder.encode(key, "utf-8")).append("=").append(URLEncoder.encode(params.getString(key), "utf-8"));
            } catch (UnsupportedEncodingException e) {
                return null;
            }
        }

        return sb.toString();
    }

    public static Bundle decodeParams(String query) {
        Bundle params = new Bundle();

        if (query != null) {
            String[] parts = query.split("&");

            for (String param : parts) {
                String[] item = param.split("=");
                params.putString(URLDecoder.decode(item[0]), URLDecoder.decode(item[1]));
            }
        }

        return params;
    }

    public static List<NameValuePair> bundleToList(Bundle params) {
        List<NameValuePair> pairs = new ArrayList<NameValuePair>();

        for (String key : params.keySet()) {
            pairs.add(new BasicNameValuePair(key, params.getString(key)));
        }

        return pairs;
    }
}

