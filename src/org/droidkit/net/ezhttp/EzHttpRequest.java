package org.droidkit.net.ezhttp;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.impl.cookie.DateUtils;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.droidkit.DroidKit;
import org.droidkit.net.HttpClientFactory;
import org.droidkit.util.tricks.Base64;
import org.droidkit.util.tricks.ExceptionTricks;
import org.droidkit.util.tricks.IOTricks;
import org.droidkit.util.tricks.IOTricks.ProgressListener;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.media.AudioRecord.OnRecordPositionUpdateListener;
import android.os.AsyncTask;
import android.os.Handler;

public class EzHttpRequest implements ProgressListener {
//	private static final String TAG = "EzHttpRequest";
	
	public static final int REQ_GET = 1;
	public static final int REQ_PUT = 2;
	public static final int REQ_DEL = 3;
	public static final int REQ_HEAD = 4;
	public static final int REQ_POST = 5;
	public static final int REQ_POST_MULTIPART = 6;
	public static final int REQ_POST_STRING_ENT = 7;
	
	private static final String VAL_LAST_MOD_HEADER = "Last-Modified";
	private static final String VAL_USER_AGENT_HEADER = "User-Agent";
	
	private static final String VAL_AUTHORIZATION_HEADER = "Authorization";
	private static final String VAL_BASIC = "Basic ";
	
	public static final int DEFAULT_TIMEOUT_SECONDS = 60;
	
//	private static HttpClient sClient = null;
	

//	public synchronized static HttpClient getDefaultHttpClient() {
//	    if (sClient == null) {
//            sClient = getNewThreadsafeHttpClient(DEFAULT_TIMEOUT_SECONDS);
//        }
//        return sClient;
//	}
//	
//	public static HttpClient getNewThreadsafeHttpClient(int timeoutInSecs) {
//        DefaultHttpClient client = new DefaultHttpClient();
//        HttpParams connParams = client.getParams();
//        ClientConnectionManager mgr = client.getConnectionManager();
//        
//        HttpConnectionParams.setConnectionTimeout(connParams, timeoutInSecs*1000);
//        HttpConnectionParams.setSoTimeout(connParams, timeoutInSecs*1000);
//        return new DefaultHttpClient(new ThreadSafeClientConnManager(connParams, mgr.getSchemeRegistry()), connParams);
//	}
	
	
	public interface EzHttpRequestListener {
		public void onHttpRequestSucceeded(EzHttpResponse response);
		public void onHttpRequestFailed(EzHttpResponse response);
		public void onHttpRequestSucceededInBackground(EzHttpResponse response) throws Exception;
		public void onHttpRequestFailedInBackground(EzHttpResponse response);
	}
	
	public interface EzHttpRequestProgressListener {
		public void onHttpDownloadProgressUpdated(EzHttpRequest request, int percentComplete);
		public void onHttpUploadProgressUpdated(EzHttpRequest request, int percentComplete, int currentFile, int totalFiles);
	}
	
	public interface EzHttpPostUploadEntity {
		public String getParamName();
		public String getPostFileName();
		public String getContentType();
		public InputStream getInputStream();
		public long getSize();
	}
	
	public static class EzRequestFactory {
		public static EzHttpRequest createGetRequest(String url, boolean isRaw, int requestCode) {
			EzHttpRequest req = new EzHttpRequest(url, REQ_GET, isRaw, requestCode);
			return req;
		}
		
		public static EzHttpRequest createPutRequest(String url, boolean isRaw, int requestCode) {
			EzHttpRequest req = new EzHttpRequest(url, REQ_PUT, isRaw, requestCode);
			return req;
		}

		public static EzHttpRequest createDeleteRequest(String url, boolean isRaw, int requestCode) {
			EzHttpRequest req = new EzHttpRequest(url, REQ_DEL, isRaw, requestCode);
			return req;
		}
		
		public static EzHttpRequest createHeadRequest(String url, boolean isRaw, int requestCode) {
			EzHttpRequest req = new EzHttpRequest(url, REQ_HEAD, isRaw, requestCode);
			return req;
		}
		
		public static EzHttpRequest createPostRequest(String url, boolean isRaw, int requestCode) {
			EzHttpRequest req = new EzHttpRequest(url, REQ_POST, isRaw, requestCode);
			return req;
		}
		
		public static EzHttpRequest createMultipartPostRequest(String url, boolean isRaw, int requestCode) {
			EzHttpRequest req = new EzHttpRequest(url, REQ_POST_MULTIPART, isRaw, requestCode);
			req.addHeader(HTTP.CONTENT_TYPE, VAL_POST_MULTIPART_CONTENT_TYPE);
			req.addHeader(HTTP.CONN_DIRECTIVE, HTTP.CONN_KEEP_ALIVE);
			return req;
		}

		public static EzHttpRequest createPostStringEntityRequest(String url, boolean isRaw, int requestCode, String entity, String entityType, String entityEncoding) {
			EzHttpRequest req = new EzHttpRequest(url, REQ_POST_STRING_ENT, isRaw, requestCode);
			req.setStringEntity(entity, entityType, entityEncoding);
			return req;
		}
		
		
		public static EzHttpRequest createGetRequest(String url, boolean isRaw) {
		    return createGetRequest(url, isRaw, -1);
		}

		public static EzHttpRequest createPutRequest(String url, boolean isRaw) {
		    return createPutRequest(url, isRaw, -1);
		}

		public static EzHttpRequest createDeleteRequest(String url, boolean isRaw) {
		    return createDeleteRequest(url, isRaw, -1);
		}

		public static EzHttpRequest createHeadRequest(String url, boolean isRaw) {
		    return createHeadRequest(url, isRaw, -1);
		}

		public static EzHttpRequest createPostRequest(String url, boolean isRaw) {
		    return createPostRequest(url, isRaw, -1);
		}

		public static EzHttpRequest createMultipartPostRequest(String url, boolean isRaw) {
		    return createMultipartPostRequest(url, isRaw, -1);
		}

		public static EzHttpRequest createPostStringEntityRequest(String url, boolean isRaw, String entity, String entityType, String entityEncoding) {
		    return createPostStringEntityRequest(url, isRaw, -1, entity, entityType, entityEncoding);
		}
	}
	
	
	private static final String TMP_FILE_PREFIX = "ez_http_response";
	
	private String mUrl;
	private int mReqType;
//	private int mTimeoutSecs;
	private boolean mIsRaw;
	private int mNumberOfRetrysToAttempt;
	
	private String mStringEntity;
	private String mStringEntityType;
	private String mStringEntityEncoding;

	private int mRequestCode;
	private Object mTag;
	
	private ArrayList<NameValuePair> mParams;
	private ArrayList<EzHttpPostUploadEntity> mPostFiles;
	private HashMap<String, String> mHeaders;
	
	private EzHttpRequestListener mFinishedListener;
	private EzHttpRequestProgressListener mProgressListener;
	
	private Handler mResponseHandler;
	
	
	
	private long mTotalBytes;
	private int mCurrentFile;
	private boolean mUploadingFiles;
	
	protected EzHttpRequest(String url, int reqType, boolean isRaw, int requestCode) {
		mUrl = url;
		mReqType = reqType;
		mIsRaw = isRaw;
//		mTimeoutSecs = DEFAULT_TIMEOUT_SECS;
		
		mStringEntity = null;
		mStringEntityType = null;
		mStringEntityEncoding = null;
		
		mRequestCode = requestCode;
		mTag = null;

		mHeaders = null;
		mParams = null;
		mPostFiles = null;
		
		mFinishedListener = null;
		mProgressListener = null;
		
		mResponseHandler = null;
		
		mTotalBytes = 1;
		mCurrentFile = 0;
		mUploadingFiles = false;
		mNumberOfRetrysToAttempt = 1;
	}
	
	
	
	public void setUrl(String url) {
		mUrl = url;
	}
	public void setRequestType(int requestType) {
		mReqType = requestType;
	}
//	public void setTimeoutSecs(int timeoutSecs) {
//		mTimeoutSecs = timeoutSecs;
//	}
	public void setIsRaw(boolean isRaw) {
		mIsRaw = isRaw;
	}
	public void setNumberOfRetrysToAttempt(int numberOfRetrysToAttempt) {
	    mNumberOfRetrysToAttempt = numberOfRetrysToAttempt;
	}
	
	public String getUrl() {
		return mUrl;
	}
	public int getRequestType() {
		return mReqType;
	}
//	public int getTimeoutSecs() {
//		return mTimeoutSecs;
//	}
	public boolean isRaw() {
		return mIsRaw;
	}
	public int getNumberOfRetrysToAttempt() {
	    return mNumberOfRetrysToAttempt;
	}
	
//	public HttpClient getHttpClient() {
//	    if (mTimeoutSecs == DEFAULT_TIMEOUT_SECONDS)
//	        return getDefaultHttpClient();
//	    return getNewThreadsafeHttpClient(mTimeoutSecs);
//	}
	
	
	public void setStringEntity(String entity, String type, String encoding) {
		mStringEntity = entity;
		mStringEntityType = type;
		mStringEntityEncoding = encoding;
	}
	
	public String getStringEntity() {
		return mStringEntity;
	}
	public String getStringEntityType() {
		return mStringEntityType;
	}
	public String getStringEntityEncoding() {
		return mStringEntityEncoding;
	}
	
	
//	public void setResponseProcessor(EzHttpResponseProcessor processor) {
//		mResponseProcessor = processor;
//	}
	public void setHandler(Handler handler) {
		mResponseHandler = handler;
	}
	
	
	public void setHeaders(HashMap<String, String> headers) {
		mHeaders = headers;
	}
	public void setParams(ArrayList<NameValuePair> postParams) {
		mParams = postParams;
	}
	public void setPostFiles(ArrayList<EzHttpPostUploadEntity> postFiles) {
		mPostFiles = postFiles;
	}
	
	public HashMap<String, String> getHeaders() {
		return mHeaders;
	}
	public ArrayList<NameValuePair> getParams() {
		return mParams;
	}
	public ArrayList<EzHttpPostUploadEntity> getPostFiles() {
		return mPostFiles;
	}
	
	public void addHeader(String name, String value) {
		if (mHeaders == null) {
			mHeaders = new HashMap<String, String>();
		}
		mHeaders.put(name, value);
	}
	public void addParam(String name, String value) {
		if (mParams == null) {
			mParams = new ArrayList<NameValuePair>();
		}
		mParams.add(new BasicNameValuePair(name, value));
	}
	public void addPostFile(EzHttpPostUploadEntity postFile) {
		if (mPostFiles == null) {
			mPostFiles = new ArrayList<EzHttpRequest.EzHttpPostUploadEntity>();
		}
		mPostFiles.add(postFile);
	}
	public void addBasicAuth(String username, String password) {
		String auth = Base64.encodeString(username + ":" + password);
		addHeader(VAL_AUTHORIZATION_HEADER, VAL_BASIC + auth);
	}
	public void addUserAgent(String userAgent) {
		addHeader(VAL_USER_AGENT_HEADER, userAgent);
	}

	
	
	
	public void setRequestCode(int requestCode) {
		mRequestCode = requestCode;
	}
	public void setTag(Object tag) {
		mTag = tag;
	}
	
	public int getRequestCode() {
		return mRequestCode;
	}
	public Object getTag() {
		return mTag;
	}
	
	
	public EzHttpRequestListener getRequestFinishedListener() {
		return mFinishedListener;
	}
	
	
	
	
	public void setFinishedListener(EzHttpRequestListener listener) {
		mFinishedListener = listener;
	}
	
	public void setResponseHandler(Handler responseHandler) {
	    mResponseHandler = responseHandler;
	}
	
	public void setProgressListener(EzHttpRequestProgressListener listener) {
		mProgressListener = listener;
	}
	
	
   protected void processMessage(HttpUriRequest message) throws Exception{
        //overridable method added for OAuth support
    }
	

	
	public EzHttpResponse generateExceptionResponse(Throwable e) {
		EzHttpResponse response = new EzHttpResponse(this);
		response.mResponseCode = -1;
		response.mSuccess = false;
		response.mResponseReasonPhrase = e.getMessage();
		response.mResponseText = ExceptionTricks.getThrowableTraceAsString(e);
		return response;
	}
	

	

	
    public void executeAsync(EzHttpRequestListener listener, Handler responseHandler) {
        setFinishedListener(listener);
        setResponseHandler(responseHandler);
        executeAsync();
    }
    
   public void executeAsync(EzHttpRequestListener listener) {
        setFinishedListener(listener);
        executeAsync();
    }
    
    public void executeAsync(Handler responseHandler) {
        setResponseHandler(responseHandler);
        executeAsync();
    }
    
    public void executeAsync() {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                executeInSync();
                return null;
            }
        }.execute((Void)null);
    }
	
	public EzHttpResponse executeInSync(EzHttpRequestListener listener, Handler responseHandler) {
		setFinishedListener(listener);
		setResponseHandler(responseHandler);
		return executeInSync();
	}
	
   public EzHttpResponse executeInSync(EzHttpRequestListener listener) {
        setFinishedListener(listener);
        return executeInSync();
    }
	
	public EzHttpResponse executeInSync(Handler responseHandler) {
	    setResponseHandler(responseHandler);
		return executeInSync();
	}
	
	public EzHttpResponse executeInSync() {
	    if (mNumberOfRetrysToAttempt <= 0)
	        throw new RuntimeException("Could not execute EzHttpRequest to " + mUrl + " because the numberOfRetrysToAttempt was set to " + mNumberOfRetrysToAttempt);
	    
	    
		EzHttpResponse response = null;
		long time = System.currentTimeMillis();
		
		int tryCount = 0;
		while (tryCount < mNumberOfRetrysToAttempt && (response == null || response.wasSuccess() == false)) {
		    tryCount++;
		    
		    try {
		        response = execute();
		    } catch (Throwable t) {
		        t.printStackTrace();
		        
		        if (tryCount == mNumberOfRetrysToAttempt) {
		            response = generateExceptionResponse(t);
		        }
		    }
		}

		
		
		response.mRequestTime = System.currentTimeMillis() - time;
		response.onExecuteComplete();
		return response;
	}
	
	private EzHttpResponse execute() {
		if (mReqType == REQ_POST_MULTIPART)
			return executeMultipartPostRequest();
		
		EzHttpResponse ezResponse = new EzHttpResponse(this);
		
		HttpRequestBase message = null;
		
		String url = mUrl;
		
		if (mReqType != REQ_POST && mReqType != REQ_PUT && mParams != null && mParams.size() > 0) {
			for (NameValuePair param : mParams) {
				if (url.contains("?")) {
					url += "&";
				} else {
					url += "?";
				}
				url += param.getName() + "=" + param.getValue();
			}
		}
		
		try {
			switch(mReqType) {
				case REQ_GET: {
					message = new HttpGet(url);
					break;
				}
				case REQ_PUT: {
					message = new HttpPut(url);
					if (mParams != null)
						((HttpPut)message).setEntity(new UrlEncodedFormEntity(mParams, HTTP.UTF_8));
					break;
				}
				case REQ_DEL: {
					message = new HttpDelete(url);
					break;
				}
				case REQ_HEAD: {
					message = new HttpHead(url);
					break;
				}
				case REQ_POST: {
					message = new HttpPost(url);
					if (mParams != null)
						((HttpPost)message).setEntity(new UrlEncodedFormEntity(mParams, HTTP.UTF_8));
					break;
				}
				case REQ_POST_STRING_ENT: {
					message = new HttpPost(url);
					StringEntity ent = new StringEntity(mStringEntity, mStringEntityEncoding);
					ent.setContentType(mStringEntityType);
					((HttpPost)message).setEntity(ent);
					break;
				}
			}
			
			
			if (mHeaders != null) {
				for (String headerName : mHeaders.keySet()) {
					message.addHeader(headerName, mHeaders.get(headerName));
				}
			}
			
			processMessage(message);
			
			DroidKit.getHttpConnectionMonitor().onRequestStart(message);
			HttpResponse response = HttpClientFactory.getInstance().getSharedClient().execute(message);
			HttpEntity entity = response.getEntity();
			
			ezResponse.setResponseCode(response.getStatusLine().getStatusCode());
			ezResponse.mResponseReasonPhrase = response.getStatusLine().getReasonPhrase();
			
			if (entity != null) {
    			ezResponse.mResponseContentLength = entity.getContentLength();
    			if (entity.getContentEncoding() != null)
    				ezResponse.mResponseContentEncoding = entity.getContentEncoding().getValue();
    			if (entity.getContentType() != null)
    				ezResponse.mResponseContentType = entity.getContentType().getValue();
			}
			
			Header lastMod = response.getFirstHeader(VAL_LAST_MOD_HEADER);
			if (lastMod != null) {
				try {
					ezResponse.mResponseLastModTime = DateUtils.parseDate(lastMod.getValue()).getTime();
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}

			if (entity != null)
			    handleResponseInputStream(ezResponse, entity.getContent());
			
			DroidKit.getHttpConnectionMonitor().onRequestFinished(message);
			
			
		} catch (Throwable e) {
			e.printStackTrace();
			if (message != null) {
				DroidKit.getHttpConnectionMonitor().onRequestFinished(message, e);
			}
			ezResponse.mSuccess = false;
			ezResponse.mResponseCode = -1;
			ezResponse.mResponseReasonPhrase = e.getMessage();
			ezResponse.mResponseText = ExceptionTricks.getThrowableTraceAsString(e);
			ezResponse.deleteRawFile();
		}
		
		return ezResponse;
	}
	
	private void handleResponseInputStream(EzHttpResponse ezResponse, InputStream httpInputStream) throws IOException {
		if ((!ezResponse.wasSuccess()) || (!mIsRaw)) {
			ezResponse.mResponseText = IOTricks.getTextFromStream(httpInputStream, true, null);
		} else {
			mUploadingFiles = false;
			mTotalBytes = ezResponse.getResponseContentLength();
			ezResponse.mResponseFile = File.createTempFile(TMP_FILE_PREFIX, null, DroidKit.getContext().getCacheDir());
			IOTricks.copyInputStreamToFile(httpInputStream, ezResponse.mResponseFile, IOTricks.DEFAULT_BUFFER_SIZE, true, true, this);
		}
	}
	
	
	//post file string constants
	private static final String VAL_HTTP_POST = "POST";
	private static final String VAL_LINE_END = "\r\n";
	private static final String VAL_TWO_HYPHENS = "--";
	private static final String VAL_BOUNDRY = "***MIMEFileUpload***";
	private static final String VAL_POST_MULTIPART_CONTENT_TYPE = "multipart/form-data;boundary=" + VAL_BOUNDRY;
	private static final String VAL_FILE_CONTENT_DISPOSITION_FORMATER = "Content-Disposition: form-data; name=\"%s\"; filename=\"%s\"\r\n";
	private static final String VAL_FILE_CONTENT_TYPE_FORMATTER = "Content-Type: %s\r\n";
	private static final String VAL_FILE_CONTENT_TRANSFER_ENCODING = "Content-Transfer-Encoding: binary\r\n\r\n";
	private static final String VAL_PARAM_CONTENT_DISPOSITION_FORMATER = "Content-Disposition: form-data; name=\"%s\"\r\n\r\n";
	private static final String VAL_POST_SEPERATOR = VAL_TWO_HYPHENS + VAL_BOUNDRY + VAL_LINE_END;
	private static final String VAL_POST_CLOSE = VAL_TWO_HYPHENS + VAL_BOUNDRY + VAL_TWO_HYPHENS + VAL_LINE_END;	
	
	private EzHttpResponse executeMultipartPostRequest() {
		EzHttpResponse ezResponse = new EzHttpResponse(this);
		mUploadingFiles = true;
		
		HttpURLConnection conn = null;
		try {
			//setup connection
			conn = (HttpURLConnection)(new URL(mUrl).openConnection());
			DroidKit.getHttpConnectionMonitor().onRequestStart(conn);
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setUseCaches(false);
//			conn.setConnectTimeout(mTimeoutSecs*1000);
			conn.setConnectTimeout(DEFAULT_TIMEOUT_SECONDS*1000);
			conn.setRequestMethod(VAL_HTTP_POST);
			
			if (mHeaders != null) {
				for (String headerName : mHeaders.keySet()) {
					conn.setRequestProperty(headerName, mHeaders.get(headerName));
				}
			}
			
			boolean hasFiles = mPostFiles != null && mPostFiles.size() > 0;
			
			//START OUTPUT
			DataOutputStream outputStream = new DataOutputStream(conn.getOutputStream());
			outputStream.writeBytes(VAL_POST_SEPERATOR);
			
			if (mParams != null) {
				for (int i = 0; i<mParams.size(); i++) {
					NameValuePair param = mParams.get(i);
					outputStream.writeBytes(String.format(VAL_PARAM_CONTENT_DISPOSITION_FORMATER, param.getName()));
					outputStream.writeBytes(param.getValue());
					outputStream.writeBytes(VAL_LINE_END);
					
					if (hasFiles || i < mParams.size()-1) {
						outputStream.writeBytes(VAL_POST_SEPERATOR);
					}
				}
			}
			
			if (hasFiles) {
				for (int i = 0; i<mPostFiles.size(); i++) {
					EzHttpPostUploadEntity uploadFile = mPostFiles.get(i);
					mCurrentFile = i;
					mTotalBytes = uploadFile.getSize();
					
					outputStream.writeBytes(String.format(VAL_FILE_CONTENT_DISPOSITION_FORMATER, uploadFile.getParamName(), uploadFile.getPostFileName()));
					outputStream.writeBytes(String.format(VAL_FILE_CONTENT_TYPE_FORMATTER, uploadFile.getContentType()));
					outputStream.writeBytes(VAL_FILE_CONTENT_TRANSFER_ENCODING);
					IOTricks.copyInputStreamToOutputStream(uploadFile.getInputStream(), outputStream, IOTricks.DEFAULT_BUFFER_SIZE, true, false, this);
					outputStream.writeBytes(VAL_LINE_END);
					
					if (i < mPostFiles.size()-1) {
						outputStream.writeBytes(VAL_POST_SEPERATOR);
					}
				}
			}
			
			outputStream.writeBytes(VAL_POST_CLOSE);
			outputStream.flush();
			outputStream.close();
			
			
			//HANDLE RESPONSE
			ezResponse.setResponseCode(conn.getResponseCode());
			ezResponse.mResponseReasonPhrase = conn.getResponseMessage();
			ezResponse.mResponseContentLength = conn.getContentLength();
			ezResponse.mResponseContentEncoding = conn.getContentEncoding();
			ezResponse.mResponseContentType = conn.getContentType();
			ezResponse.mResponseLastModTime = conn.getLastModified();
			
			handleResponseInputStream(ezResponse, conn.getInputStream());
			
			DroidKit.getHttpConnectionMonitor().onRequestFinished(conn);
			
		} catch (Throwable e) {
			e.printStackTrace();
			if (conn != null)
				DroidKit.getHttpConnectionMonitor().onRequestFinished(conn, e);
			ezResponse.mSuccess = false;
			ezResponse.mResponseCode = -1;
			ezResponse.mResponseReasonPhrase = e.getMessage();
			ezResponse.mResponseText = ExceptionTricks.getThrowableTraceAsString(e);
			ezResponse.deleteRawFile();
		}
		
		return ezResponse;
	}
	
	
	
	@Override
	public void onProgressUpdate(int totalBytesRead) {
		if (mProgressListener != null) {
			if (mUploadingFiles)
				mProgressListener.onHttpUploadProgressUpdated(this, (int)(((float)totalBytesRead / Math.max((float)mTotalBytes, 1f)) * 100f), mCurrentFile, mPostFiles.size());
			else 
				mProgressListener.onHttpDownloadProgressUpdated(this, (int)(((float)totalBytesRead / Math.max((float)mTotalBytes, 1f)) * 100f));
		}
	}
	
	
	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append("EzHttpRequest: " + mUrl + "\n");
		b.append("RequestCode: " + mRequestCode + "\n");
		b.append("Request Type: ");
		switch(mReqType) {
		case REQ_GET: {
			b.append("GET");
			break;
		}
		case REQ_PUT: {
			b.append("PUT");
			break;
		}
		case REQ_DEL: {
			b.append("DELETE");
			break;
		}
		case REQ_HEAD: {
			b.append("HEAD");
			break;
		}
		case REQ_POST: {
			b.append("POST");
			break;
		}
		case REQ_POST_MULTIPART: {
			b.append("POST-MULTIPART");
			break;
		}
		case REQ_POST_STRING_ENT: {
			b.append("POST-STRING-ENTITY\n");
			b.append("String Entity: \n");
			b.append("\tType: ");
			b.append(mStringEntityType);
			b.append("\n\tEncoding: ");
			b.append(mStringEntityEncoding);
			b.append("\n\tContent: \n");
			try {
			    b.append(new JSONObject(mStringEntity).toString(4));
			} catch (JSONException e) {
			    b.append(mStringEntity);
			}
			break;
		}
		}
		b.append("\nIs Raw: " + (mIsRaw ? "Yes" : "No") + "\n");
		
		if (mParams != null) {
			b.append("\nParameters: \n");
			for (NameValuePair param : mParams) {
				b.append("\t" + param.getName() + " = " + param.getValue() + "\n");
			}
			b.append("\n");
		}
		return b.toString();
	}
	
	
	public static class EzHttpResponse {
		private EzHttpRequest mRequest;
		private boolean mSuccess;
		private int mResponseCode;
		private String mResponseReasonPhrase;
		private String mResponseContentType;
		private String mResponseContentEncoding;
		private long mResponseLastModTime;
		private long mResponseContentLength;
		private String mResponseText;
		private File mResponseFile;
		private long mRequestTime;

		
		private EzHttpResponse(EzHttpRequest request) {
			mRequest = request;
			mSuccess = false;
			mResponseCode = 0;
			mResponseReasonPhrase = null;
			mResponseContentType = null;
			mResponseContentEncoding = null;
			mResponseLastModTime = 0;
			mResponseContentLength = 0;
			mResponseText = null;
			mResponseFile = null;
		}
		
		public EzHttpRequest getRequest() {
			return mRequest;
		}
		public boolean wasSuccess() {
			return mSuccess;
		}
		private void setResponseCode(int responseCode) {
			mResponseCode = responseCode;
			mSuccess = (mResponseCode >= 200 && mResponseCode < 300);
		}
		public int getResponseCode() {
			return mResponseCode;
		}
		public String getResponseReasonPhrase() {
			return mResponseReasonPhrase;
		}
		public String getResponseContentType() {
			return mResponseContentType;
		}
		public String getResponseContentEncoding() {
			return mResponseContentEncoding;
		}
		public long getResponseLastModTime() {
			return mResponseLastModTime;
		}
		public long getResponseContentLength() {
			return mResponseContentLength;
		}
		public String getResponseText() {
			return mResponseText;
		}
		public JSONObject getResponseTextAsJson() throws JSONException {
		    return new JSONObject(mResponseText);
		}
		public long getRequestTime() {
			return mRequestTime;
		}
		public File getResponseFile() {
			return mResponseFile;
		}
		public boolean isRaw() {
			return mRequest.isRaw();
		}
		public void deleteRawFile() {
			if (mResponseFile != null && mResponseFile.exists()) {
				mResponseFile.delete();
			}
		}
		public int getRequestCode() {
			return mRequest.getRequestCode();
		}
		public Object getTag() {
			return mRequest.getTag();
		}
		public void setTag(Object tag) {
			mRequest.setTag(tag);
		}
		
		@Override
		public String toString() {
			StringBuilder b = new StringBuilder(mRequest.toString());
			b.append("\n\nEzHttpResponse: " + (mSuccess ? "Success: " : "Failure: ") + mResponseCode + "\n");
			b.append("ReasonPhrase: " + cnull(mResponseReasonPhrase) + "\n");
			b.append("ContentType: " + cnull(mResponseContentType) + "\n");
			b.append("ContentEncoding: " + cnull(mResponseContentEncoding) + "\n");
			b.append("LastModTime: " + mResponseLastModTime + "\n");
			b.append("Response: \n");
			if (!mSuccess || !isRaw()) {
				try {
					b.append(new JSONObject(mResponseText).toString(4));
				} catch (Throwable e) {
					try {
						b.append(new JSONArray(mResponseText).toString(4));
					} catch (Throwable e2) {
						b.append(cnull(mResponseText));
					}
				}
			} else {
				b.append("BINARY FILE");
			}
			return b.toString();
		}
		private String cnull(String str) {
			if (str == null)
				return "";
			return str;
		}
		
		protected void onExecuteComplete() {
			if (mRequest.getRequestFinishedListener() != null) {
				respondInBackground();
				respondInForeground();
			}
		}
		
		protected void respondInBackground() {
		    if (wasSuccess()) {
                try {
                    mRequest.getRequestFinishedListener().onHttpRequestSucceededInBackground(this);
                } catch (Throwable t) {
                    t.printStackTrace();
                    mSuccess = false;
                    mRequest.getRequestFinishedListener().onHttpRequestFailedInBackground(this);
                } 
            } else {
                mRequest.getRequestFinishedListener().onHttpRequestFailedInBackground(this);
            }
		}
		
		protected void respondInForeground() {
		    Runnable response = new Runnable() {
                
                @Override
                public void run() {
                    if (wasSuccess()) {
                        mRequest.getRequestFinishedListener().onHttpRequestSucceeded(EzHttpResponse.this);
                    } else {
                        mRequest.getRequestFinishedListener().onHttpRequestFailed(EzHttpResponse.this);
                    }
                }
            };
		    
		    
		    if (mRequest.mResponseHandler == null) {
                response.run();               
            } else {
                mRequest.mResponseHandler.post(response);
            }
		}	
	}
}
