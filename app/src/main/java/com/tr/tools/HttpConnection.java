package com.tr.tools;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.Scanner;

import org.apache.http.HttpHost;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by bpl2111 on 2014-05-26.
 */
public class HttpConnection {
    public static String HTTP_CONNECTION_TAG="Tr.HttpConnection";

    public static int CONNECTION_TIMEOUT = 5000;
    public static int DATARETRIEVAL_TIMEOUT = 5000;

    private Context _context;
    private Proxy _proxy;

    public HttpConnection(Context context) {
        _context = context;
        _proxy = null;
    }

    // http://developer.android.com/training/basics/network-ops/connecting.html

    public void setProxyServer(String proxyHost, int port) {
        _proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, port));
    }

    public void resetProxyServer() {
        _proxy = null;
    }

    public void setProxyAuthentication(String user, String password) {

        //<editor-fold desc="todo">
/*
            If your proxy requires authentication it will give you response 407.

            In this case you'll need the following code:

                Authenticator authenticator = new Authenticator() {

                    public PasswordAuthentication getPasswordAuthentication() {
                        return (new PasswordAuthentication("user",
                                "password".toCharArray()));
                    }
                };
                Authenticator.setDefault(authenticator);
*/
        //</editor-fold>
    }

    public boolean checkConnectivity() {
        ConnectivityManager connMgr = (ConnectivityManager) _context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        return (networkInfo != null && networkInfo.isConnected());
    }

    public String requestFromService(String serviceUrl) throws Exception {
        disableConnectionReuseIfNecessary();

        Log.d(HTTP_CONNECTION_TAG, "Url: " + serviceUrl);
        HttpURLConnection urlConnection = null;
        try {
            // create connection
            URL urlToRequest = new URL(serviceUrl);

            if (_proxy != null)
                urlConnection = (HttpURLConnection) urlToRequest.openConnection(_proxy);
            else
                urlConnection = (HttpURLConnection) urlToRequest.openConnection();

            urlConnection.setConnectTimeout(CONNECTION_TIMEOUT);
            urlConnection.setReadTimeout(DATARETRIEVAL_TIMEOUT);
            urlConnection.setDoInput(true); // set 'cause we want to allow input (must be set before connection use)

            // handle issues
            int statusCode = urlConnection.getResponseCode();

            if (statusCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                // handle unauthorized (if service requires user login)
                Log.d(HTTP_CONNECTION_TAG,"Request unauthorized");

                throw new Exception("Request unauthorized to:"+serviceUrl);
            } else if (statusCode != HttpURLConnection.HTTP_OK) {
                // handle any other errors, like 404, 500,..
                Log.d(HTTP_CONNECTION_TAG,"Request failed: " + Integer.toString(statusCode));

                throw new Exception("Request failed, status: "+Integer.toString(statusCode));
            }

            // create JSON object from content
            InputStream inStream = new BufferedInputStream(urlConnection.getInputStream());

            return getResponseText(inStream);

        } catch (MalformedURLException e) {
            // URL is invalid
            Log.d(HTTP_CONNECTION_TAG, e.toString());
            throw new Exception(e.toString());
        } catch (SocketTimeoutException e) {
            // data retrieval or connection timed out
            Log.d(HTTP_CONNECTION_TAG,e.toString());
            throw new Exception(e.toString());
        } catch (IOException e) {
            // could not read response body
            // (could not create input stream)
            Log.d(HTTP_CONNECTION_TAG,e.toString());
            throw new Exception(e.toString());
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }

    }

    //required in order to prevent issues in earlier Android version.
    private static void disableConnectionReuseIfNecessary() {
        // // Work around pre-Froyo bugs in HTTP connection reuse.
        // see HttpURLConnection API doc
        if (Integer.parseInt(Build.VERSION.SDK)
                < Build.VERSION_CODES.FROYO) {
            System.setProperty("http.keepAlive", "false");
        }
    }

    private static String getResponseText(InputStream inStream) {
        // very nice trick from
        // http://weblogs.java.net/blog/pat/archive/2004/10/stupid_scanner_1.html
        return new Scanner(inStream).useDelimiter("\\A").next();
    }
}