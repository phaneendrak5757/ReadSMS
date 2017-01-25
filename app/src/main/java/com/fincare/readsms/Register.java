package com.fincare.readsms;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaCodec;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.regex.Pattern;


/**
 * Created by Phaneendra on 25-Nov-16.
 */

public class Register extends AppCompatActivity {

    private EditText name,mobile,email;
    private Button sub;

    public static final int MY_PERMISSIONS_REQUEST_GET_ACCOUNTS=3;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);


        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(Register.this,Manifest.permission.GET_ACCOUNTS)) {

            } else {

                ActivityCompat.requestPermissions(Register.this, new String[]{Manifest.permission.GET_ACCOUNTS},MY_PERMISSIONS_REQUEST_GET_ACCOUNTS);

            }
        }

        name=(EditText) findViewById(R.id.name);
        mobile=(EditText) findViewById(R.id.mobile);
        email=(EditText) findViewById(R.id.email);


        String gmail = "";
        try {
            Pattern gmailPattern = Patterns.EMAIL_ADDRESS; // API level 8+
            Account[] accounts = AccountManager.get(this).getAccounts();
            for (Account account : accounts) {
                if (gmailPattern.matcher(account.name).matches()) {
                    gmail = account.name;
                }
            }
            email.setText(gmail);
        }
        catch (SecurityException e)
        {
            e.printStackTrace();
        }

        sub=(Button) findViewById(R.id.Submit);
        sub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(name.getText().toString().length()==0)
                {
                    Toast.makeText(Register.this, "Please give name", Toast.LENGTH_SHORT).show();
                }
                else if(mobile.getText().toString().length()==0)
                {
                    Toast.makeText(Register.this,"Please Give Mobile No.",Toast.LENGTH_SHORT).show();
                }
                else if(email.getText().toString().length()==0)
                {
                    Toast.makeText(Register.this,"Please Give eMail",Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Rege r=new Rege();
                    r.setName(name.getText().toString());
                    r.setEmail(email.getText().toString());
                    r.setPhone_number(mobile.getText().toString());
                    TelephonyManager telephonyManager =(TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

                    r.setImie(telephonyManager.getDeviceId());



                    DbHandler db=new DbHandler(getApplicationContext());
                    db.delte();
                    db.InsReg(r);
                    dataSync(r);
                    Intent i=new Intent(Register.this,MainActivity.class);
                    startActivity(i);
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {


            case MY_PERMISSIONS_REQUEST_GET_ACCOUNTS :
            {

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                }
                //return;
            }
            break;


        }
    }

    public void dataSync(Rege r) {
        class UploadImage extends AsyncTask<String, Void, String> {

            //ProgressDialog loading;
            RequestHandler rh = new RequestHandler();

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                //loading = ProgressDialog.show(getApplicationContext(), "Uploading Image", "Please wait...",true,true);
                Toast.makeText(getApplicationContext(),"successfull",Toast.LENGTH_SHORT).show();
            }


            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                JSONObject jsonResp = null;
            }

            @Override
            protected String doInBackground(String... params) {
                //String bitmap = params[0];
                HashMap<String, String> data = new HashMap<>();

                data.put("name",params[0] );
                data.put("username",params[3]);
                data.put("password",params[3]);
                data.put("mob",params[2]);
                data.put("email",params[1]);


                String result = rh.sendPostRequest("http://mel-los.fincare.com/SMS_repo_web/login_reg.php", data);

                return result;
            }

        }
        UploadImage ui = new UploadImage();
        String[] s = {r.getName(),r.getEmail(),r.getPhone_number(),r.getImie()};
        ui.execute(s);
    }




}
