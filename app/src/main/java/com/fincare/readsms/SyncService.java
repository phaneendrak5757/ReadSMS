package com.fincare.readsms;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
/**
 * Created by Phaneendra on 06-Sep-16.
 */
public class SyncService extends Service{

    private static final String TAG = "SyncService";
    private static final Object sAdapterLock = new Object();
    private static SyncAdapter sAdapter = null;
    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "onCreate()");
        synchronized (sAdapterLock) {
            if (sAdapter == null) {
                sAdapter = new SyncAdapter(getApplicationContext(), true);
            }
        }
    }
    @Override
    public IBinder onBind(Intent intent) {
        return sAdapter.getSyncAdapterBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                startJob();
            }
        });
        t.start();
        return START_STICKY;
    }
    private void startJob(){
        //do job here

        synchronized (sAdapterLock) {
            if (sAdapter == null) {
                sAdapter = new SyncAdapter(getApplicationContext(), true);
            }
        }

        //job completed. Rest for 5 second before doing another one
        try {
            Thread.sleep(24*60*60*1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //do job again
        synchronized (sAdapterLock) {
            if (sAdapter == null) {
                sAdapter = new SyncAdapter(getApplicationContext(), true);
            }
        }
        startJob();
    }
}


