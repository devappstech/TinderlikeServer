package com.example.tinder_likeserver;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class LeaderboardActivity extends AsyncTask<String, Void, String> {

    private Context context;

    public LeaderboardActivity(Context context) {
        this.context = context;
    }

    @Override
    protected String doInBackground(String... arg0) {
        try {

            String link = "http://euclid.nmu.edu/~dmiranda/SeniorProject/getLeaderBoard.php";

            URL url = new URL(link);
            URLConnection conn = url.openConnection();

            conn.setDoOutput(true);

            BufferedReader reader = new BufferedReader(new
                    InputStreamReader(conn.getInputStream()));

            StringBuilder sb = new StringBuilder();
            String line = null;

            // Read Server Response
            while ((line = reader.readLine()) != null) {
                sb.append(line);
                break;
            }

            return sb.toString();
        } catch (Exception e) {
            return new String("Exception: " + e.getMessage());
        }

    }

    @Override
    protected void onPreExecute() {
    }

    @Override
    protected void onPostExecute(String result) {
        if (result != null) {
            Log.d("myTag", result);
        } else {
            Log.d("myTag", "error");
        }
    }
}

