package com.example.tinder_likeserver;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

public class updateLossDBActivity extends AsyncTask<String, Void, String> {

    private Context context;

    public updateLossDBActivity(Context context) {
        this.context = context;
    }

    @Override
    protected String doInBackground(String... arg0) {
        try{
            String username = (String)arg0[0];


            String link="http://euclid.nmu.edu/~dmiranda/SeniorProject/updateLossDB.php";
            String data  = URLEncoder.encode("username", "UTF-8") + "=" +
                    URLEncoder.encode(username, "UTF-8");

            URL url = new URL(link);
            URLConnection conn = url.openConnection();

            conn.setDoOutput(true);
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());

            wr.write( data );
            wr.flush();

            BufferedReader reader = new BufferedReader(new
                    InputStreamReader(conn.getInputStream()));

            StringBuilder sb = new StringBuilder();
            String line = null;

            // Read Server Response
            while((line = reader.readLine()) != null) {
                sb.append(line);
                break;
            }

            return sb.toString();
        } catch(Exception e){
            return new String("Exception: " + e.getMessage());
        }


    }

    @Override
    protected void onPreExecute(){
    }

    @Override
    protected void onPostExecute(String result){
        if(result != null){
            Log.d("myTag", result);
        }
        else{
            Log.d("mTag","error updating db");
        }
    }

}


