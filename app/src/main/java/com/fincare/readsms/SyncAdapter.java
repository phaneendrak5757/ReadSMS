package com.fincare.readsms;


import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentResolver;
import android.content.Context;

import android.accounts.Account;
import android.content.ContentProviderClient;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by Phaneendra on 06-Sep-16.
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter {

    private static final int NET_CONNECT_TIMEOUT_MILLIS = 1500000;  // 1500 seconds

    /**
     * Network read timeout, in milliseconds.
     */
    private static final int NET_READ_TIMEOUT_MILLIS = 1000000;  // 1000 seconds

    public static final String TAG = "SyncAdapter";

    public Context c;
    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        c=context;
    }

    @Override
    public void onPerformSync(Account account, Bundle bundle, String s, ContentProviderClient contentProviderClient, SyncResult syncResult) {
        Log.i(TAG, "Beginning network synchronization");

        try {
            final URL location = new URL(Globals.syncService);
            InputStream stream = null;

            try {
                Log.i(TAG, "Streaming data from network: " + location);
                //stream = downloadUrl(location);
                DbHandler db = new DbHandler(getContext());
                Cursor c=db.getReg();
                if (c.getCount()>0)
                {
                    c.moveToFirst();
                    String name=c.getString(1);
                    String date=c.getString(5);

                    dataSync(name,date);
                }
                else
                {
                    Toast.makeText(getContext(),"Please Register ",Toast.LENGTH_SHORT).show();
                }

                //String resource = getStr(stream);
                //return resource;
                Log.d(TAG+"-stream","Sync Completed");


                // Makes sure that the InputStream is closed after the app is
                // finished using it.
            } finally {
                if (stream != null) {
                    stream.close();
                }
            }
        } catch (MalformedURLException e) {
            Log.e(TAG, "Feed URL is malformed", e);
            syncResult.stats.numParseExceptions++;
            return;
        } catch (IOException e) {
            Log.e(TAG, "Error reading from network: " + e.toString());
            syncResult.stats.numIoExceptions++;
            return;
        }
        Log.i(TAG, "Network synchronization complete");
    }




    private String getDataToSync(final String date)
    {

        Uri inboxURI = Uri.parse("content://sms/inbox");


        String[] reqCols = new String[]{"_id", "address", "body", "date_sent"};

        ContentResolver cr = getContext().getContentResolver();

        //Log.d("data",date);
        //Cursor c = cr.query(inboxURI, reqCols, " length(address) < 10 and date_sent > ?", new String[]{date}, null);
        Cursor c = cr.query(inboxURI, reqCols, " length(address) < 10 ", null, null);

        JSONArray tot=new JSONArray();

        if (c.moveToFirst()) {
            do {
                JSONObject one = new JSONObject();
                try {
                    DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date startDate = (Date)formatter.parse(date);


                    //Log.d("testdate",c.getString(3).toString());
                    Date startDate1 = new Date(Long.parseLong(c.getString(3).toString()));


                    one.put(c.getColumnName(0).toString(), c.getString(0).toString());
                    one.put(c.getColumnName(1).toString(), c.getString(1).toString());
                    one.put(c.getColumnName(2).toString(), c.getString(2).toString());
                    one.put(c.getColumnName(3).toString(), c.getString(3).toString());
                    if(startDate.before(startDate1)) {
                        tot.put(one);
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }



            } while (c.moveToNext());

        }
        return tot.toString();
    }

    private void dataSync(final String user,final String date) {
        class UploadImage extends AsyncTask<String, Void, String> {

            //ProgressDialog loading;
            RequestHandler rh = new RequestHandler();

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                //loading = ProgressDialog.show(getApplicationContext(), "Uploading Image", "Please wait...",true,true);

            }


            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                DbHandler db=new DbHandler(getContext());
                db.update_sts();
            }

            @Override
            protected String doInBackground(String... params) {
                //String bitmap = params[0];
                HashMap<String, String> data = new HashMap<>();

                data.put("uname", params[0]);
                data.put("smss",getDataToSync(date));


                String result = rh.sendPostRequest("http://106.51.250.202:8034/sms_repo_web/sms_get.php", data);

                return result;
            }

        }
        UploadImage ui = new UploadImage();
        String[] s = {user,date};
        ui.execute(s);
    }
}

