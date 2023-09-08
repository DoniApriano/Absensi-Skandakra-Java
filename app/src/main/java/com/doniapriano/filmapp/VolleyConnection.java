package com.doniapriano.filmapp;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class VolleyConnection {

    private static VolleyConnection volleyConnection;
    private RequestQueue requestQueue;
    private static Context vContext;

    private VolleyConnection(Context context) {
        vContext = context;
        requestQueue = getRequestQueue();
    }

    private RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(vContext.getApplicationContext());
        }
        return requestQueue;
    }

    public static synchronized VolleyConnection getInstance(Context context) {
        if (volleyConnection == null) {
            volleyConnection = new VolleyConnection(context);
        }
        return volleyConnection;
    }

    public<T> void addRequestQueue(Request<T> request) {
        getRequestQueue().add(request);
    }

}
