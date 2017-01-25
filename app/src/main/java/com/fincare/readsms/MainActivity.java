package com.fincare.readsms;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity  {

    // GUI Widget
    Button  btnInbox,reg;

    TextView genid;
    public static final int MY_PERMISSIONS_REQUEST_READ_SMS=1;
    public static final int MY_PERMISSIONS_REQUEST_INTERNET=2;
    public static final int MY_PERMISSIONS_REQUEST_GET_ACCOUNTS=3;
    public static final int MY_PERMISSIONS_READ_PHONE_STATE=4;


    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        genid=(TextView) findViewById(R.id.genID);
        // Init GUI Widget
        btnInbox = (Button) findViewById(R.id.btnInbox);

        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,Manifest.permission.READ_SMS)) {

            } else {

                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_SMS,Manifest.permission.GET_ACCOUNTS,Manifest.permission.READ_PHONE_STATE},MY_PERMISSIONS_REQUEST_READ_SMS);

            }
        }



        btnInbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                DbHandler db = new DbHandler(getApplicationContext());
                Cursor c=db.getReg();
                if (c.getCount()>0)
                {
                    c.moveToFirst();
                    String name=c.getString(1);
                    String date=c.getString(5);
                    //Log.d("rowdatetime",date);
                    dataSync(name,date);
                }
                else
                {
                    Toast.makeText(getApplicationContext(),"Please Register ",Toast.LENGTH_SHORT).show();
                }
            }
        });

        reg = (Button) findViewById(R.id.reg);

        reg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this,Register.class);
                startActivity(i);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_SMS:
            {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                //return;
            }
            break;
            case MY_PERMISSIONS_REQUEST_INTERNET:
            {

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                }
                //return;
            }
            break;

            case MY_PERMISSIONS_REQUEST_GET_ACCOUNTS :
            {

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                }
                //return;
            }
            break;

            case MY_PERMISSIONS_READ_PHONE_STATE :
            {

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                }
                //return;
            }
            break;


        }
    }


    private String getDataToSync(final String date)
    {

        Uri inboxURI = Uri.parse("content://sms/inbox");


        String[] reqCols = new String[]{"_id", "address", "body", "date_sent"};

        ContentResolver cr = getContentResolver();

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
                DbHandler db=new DbHandler(getApplicationContext());
                db.update_sts();
                TelephonyManager telephonyManager =(TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

                genid.setText(telephonyManager.getDeviceId());


            }

            @Override
            protected String doInBackground(String... params) {
                //String bitmap = params[0];
                HashMap<String, String> data = new HashMap<>();

                data.put("uname", params[0]);
                data.put("smss",getDataToSync(date));


                String result = rh.sendPostRequest(Globals.syncService, data);

                return result;
            }

        }
        UploadImage ui = new UploadImage();
        String[] s = {user,date};
        ui.execute(s);
    }

}

