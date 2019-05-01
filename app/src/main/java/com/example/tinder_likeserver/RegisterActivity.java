package com.example.tinder_likeserver;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class RegisterActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, LocationListener {

    private ServerSocket serverSocket;
    private DataInputStream dis;
    private DataOutputStream dos;
    Context context;
    private Handler handler;
    private LinearLayout users;
    private static LoginActivity loginActivity;
    private static SigninActivity signinActivity;
    protected LocationManager locationManager;
    private static double latitude;
    private static double longitude;
    private static RegisterActivity main;
    private static int radius = 0;
    //private static Map<Integer, Socket> clients;
    private static ArrayList<Pair<Integer, Socket>> clients;
    private static ArrayList<Pair<String, Pair<Double, Double>>> usersOnline;
    private static ArrayList<Pair<String, String>> usersReady;
    private static boolean ingame = false;
    private static boolean accepted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        handler = new Handler();
        users = findViewById(R.id.users);
        loginActivity = new LoginActivity(context);
        signinActivity = new SigninActivity(context);
        main = RegisterActivity.this;
        //clients = new HashMap<Integer, Socket>();
        clients = new ArrayList<>();
        usersOnline = new ArrayList<>();
        usersReady = new ArrayList<>();

        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(this);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.radius, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        //get the location of the client
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //check if the user allows access to the location
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "First enable LOCATION ACCESS in settings.", Toast.LENGTH_LONG).show();
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        Log.d("position", latitude + " " + longitude);
    }

    public static double getLatitude(){
        return latitude;
    }

    public static double getLongitude(){
        return longitude;
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d("Latitude","disable");
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d("Latitude","enable");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d("Latitude","status");
    }

    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        radius = Integer.valueOf(parent.getItemAtPosition(pos).toString());
        Log.d("radius", ""+radius);
    }

    public void onNothingSelected(AdapterView<?> parent) {}

    public static void setInGame(boolean status){
        ingame = status;
    }

    public TextView textView(String message) {
        if (null == message || message.trim().isEmpty()) {
            message = "<Empty Message>";
        }
        TextView tv = new TextView(this);

        tv.setText(message);
        tv.setTextSize(20);
        tv.setPadding(0, 5, 0, 0);
        return tv;
    }

    public void showMessage(final String message) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                users.addView(textView(message));
            }
        });
    }

    public static boolean isClientInsideRadius(String username){
        double clientLat = 0.0;
        double clientLon = 0.0;
        float[] result = new float[1];
        for(int i = 0; i < usersOnline.size(); i ++){
            if(usersOnline.get(i).first.equals(username)){
                clientLat = usersOnline.get(i).second.first;
                clientLon = usersOnline.get(i).second.second;
            }
        }
        Log.d("client lat", Double.toString(clientLat));
        Log.d("client lon", Double.toString(clientLon));
        Log.d("server lat", Double.toString(latitude));
        Log.d("server lon", Double.toString(longitude));
        Location.distanceBetween(getLatitude(), getLongitude(), clientLat, clientLon, result);
        Log.d("distance", Float.toString(result[0]));
        if(result[0] <= radius){
            return true;
        }
        return false;
    }

    public static void setAccepted(boolean accept){
        accepted = accept;
    }

    public static int getUsernameIndex(ArrayList<Pair<String, Pair<Double, Double>>> array, String username){
        for(int i = 0; i < array.size(); i ++){
            if(username.equals(array.get(i).first)){
                return i;
            }
        }
        return -1;
    }

    public static void printArray(ArrayList<Pair<String, Pair<Double, Double>>> array){
        for(int i = 0; i < array.size(); i ++){
            Log.d("array", array.get(i).first + " " + array.get(i).second.first.toString() + " " + array.get(i).second.second.toString());
        }
    }

    public static String getUsersReady(){
        String ret = "";
        for(int i = 0; i < usersReady.size(); i ++){
            ret += usersReady.get(i).first + " " + usersReady.get(i).second + ", ";
        }
        return ret;
    }

    public void startServer(View view){
        start(3003);
        findViewById(R.id.start_server).setVisibility(View.GONE);
    }

    public void start(final int port){

        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try  {
                    serverSocket = new ServerSocket(port);
                    while (true){
                        try{
                            Socket s = null;
                            s = serverSocket.accept();
                            //clients.put(s.getPort(), s);
                            clients.add(new Pair<Integer, Socket>(s.getPort(), s));
                            dis = new DataInputStream(s.getInputStream());
                            dos = new DataOutputStream(s.getOutputStream());

                            // create a new thread object
                            Thread t = new ClientHandler(s, dis, dos, context);

                            // Invoking the start() method
                            t.start();
                        } catch(IOException e){
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
        Log.d("myTag", "Server started correctly");
    }

    public class ClientHandler extends Thread implements Runnable{
        private Socket s;
        private DataInputStream dis;
        private DataOutputStream dos;
        Context context;

        public ClientHandler(Socket s, DataInputStream dis, DataOutputStream dos, Context context) {
            this.s = s;
            this.dis = dis;
            this.dos = dos;
            this.context = context;
        }

        @Override
        public void run() {
            String received = "";
            String toreturn = "";

            while (true) {
                try {
                    received = dis.readUTF();
                    Log.d("received", received);
                    final String[] receivedData = received.split(" ");
                    if(receivedData[0].equals("quit")){
                        System.out.println("Client " + this.s + " sends exit...");
                        System.out.println("Closing this connection.");
                        this.s.close();
                        System.out.println("Connection closed");
                        int index = getUsernameIndex(usersOnline, receivedData[1]);
                        //usersOnline.remove(index);
                        clients.remove(s.getPort());
                        printArray(usersOnline);
                        main.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                main.showMessage(receivedData[1] + " disconnected");
                            }
                        });
                        break;
                    }
                    if (receivedData.length == 5) {
                        Log.d("received", receivedData[0] + " " + receivedData[1] + " " + receivedData[2] + " " + receivedData[3] + " " + receivedData[4]);
                        //we are doing log in
                        if (receivedData[0].equals("0")) {
                            Log.d("userpass", receivedData[1] + " " + receivedData[2]);

                            try {
                                toreturn = new LoginActivity(this.context).execute(receivedData[1], receivedData[2]).get();
                            } catch (ExecutionException e) {
                                e.printStackTrace();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            //toreturn = loginActivity.getResult();
                            Log.d("toreturn", toreturn);
                            dos.writeUTF(toreturn);
                            if(toreturn.equals("error")){
                                break;
                            }
                            else {
                                usersOnline.add(new Pair<String, Pair<Double, Double>>(receivedData[1], new Pair<Double, Double>(Double.valueOf(receivedData[3]), Double.valueOf(receivedData[4]))));
                                printArray(usersOnline);

                                main.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        main.showMessage(receivedData[1] + " connected");
                                }
                                });
                                break;
                            }

                        } else if (receivedData[0].equals("1")) {
                            Log.d("userpass", receivedData[1] + " " + receivedData[2]);

                            try {
                                toreturn = new SigninActivity(this.context).execute(receivedData[1], receivedData[2]).get();
                            } catch (ExecutionException e) {
                                e.printStackTrace();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            Log.d("toreturn", toreturn);
                            dos.writeUTF(toreturn);

                            if (!toreturn.equals("error")) {
                                usersOnline.add(new Pair<String, Pair<Double, Double>>(receivedData[1], new Pair<Double, Double>(Double.valueOf(receivedData[3]), Double.valueOf(receivedData[4]))));
                                printArray(usersOnline);
                                main.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        main.showMessage(receivedData[1] + " connected");
                                    }
                                });
                                break;
                            }
                        }

                    }
                    else if(receivedData[0].equals("leaderboard")){
                        try {
                            toreturn = new LeaderboardActivity(this.context).execute().get();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        Log.d("toreturn", toreturn);
                        dos.writeUTF(toreturn);
                        break;
                    }
                    else if(receivedData[0].equals("win")){
                        try {
                            toreturn = new updateWinDBActivity(RegisterActivity.this).execute(receivedData[1]).get();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        Log.d("return", toreturn);
                        dos.writeUTF(toreturn);
                        break;
                    }
                    else if(receivedData[0].equals("loss")){
                        try {
                            toreturn = new updateLossDBActivity(RegisterActivity.this).execute(receivedData[1]).get();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        Log.d("return", toreturn);
                        dos.writeUTF(toreturn);
                        break;
                    }
                    else if(receivedData[0].equals("ready")) {
                        if(isClientInsideRadius(receivedData[1])){
                            Log.d("client", "inside");
                            //usersReady.add(new Pair<String, String>(receivedData[1], receivedData[2]));
                            if(ingame == false) {
                                main.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        new AlertDialog.Builder(RegisterActivity.this)
                                                .setTitle(R.string.game_title)
                                                .setMessage(receivedData[1] + " wants to challenge you")
                                                .setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        setAccepted(true);
                                                        Intent intent = new Intent(RegisterActivity.this, GameActivity.class);
                                                        startActivity(intent);
                                                        ingame = true;

                                                    }
                                                })
                                                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        setAccepted(false);
                                                    }
                                                }).show();
                                    }
                                });
                                try {
                                    Thread.sleep(2000);
                                } catch (Exception e) {
                                    System.out.println(e);
                                }
                                if (accepted) {
                                    dos.writeUTF("okay");
                                } else {
                                    dos.writeUTF("notnow");
                                }
                            }
                            else{
                                dos.writeUTF("busy");
                            }
                        }
                        else {
                            dos.writeUTF("outside");
                        }
                        break;
                    }
                    else if(received.equals("users")){
                        String tosend = getUsersReady();
                        //send to all clients connected the list of users ready
                        for (int i = 0; i < clients.size(); i ++) {
                            Socket client = clients.get(i).second;
                            if(!client.isClosed()) {
                                // Sending the response back to the client.
                                DataOutputStream dos = new DataOutputStream(client.getOutputStream());
                                dos.writeUTF(tosend);
                            }
                        }

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                // closing resources
                this.dis.close();
                this.dos.close();
            }catch(IOException e){
                e.printStackTrace();
            }
        }
    }
}
