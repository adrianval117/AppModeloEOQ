package com.example.modeloeoq;

import android.app.Application;
import android.content.Context;

public class Contexter extends Application {
    public static Context context;

    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }
    public static Context getcontext(){
        return context;
    }

}