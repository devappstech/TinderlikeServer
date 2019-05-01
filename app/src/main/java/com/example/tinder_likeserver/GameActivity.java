package com.example.tinder_likeserver;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.KeyEvent;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class GameActivity extends AppCompatActivity {

    private BoardView boardView;
    private GameEngine gameEngine;
    private ServerSocket serverSocket;
    private static DataOutputStream dos;
    private DataInputStream dis;
    private static int x = -1;
    private static int y = -1;
    Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        boardView = (BoardView) findViewById(R.id.board);
        gameEngine = new GameEngine();
        boardView.setGameEngine(gameEngine);
        boardView.setMainActivity(this);

        start(3004);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            RegisterActivity.setInGame(false);
            Log.d(this.getClass().getName(), "back button pressed");
        }
        return super.onKeyDown(keyCode, event);
    }

    public static String setPoint(int xPos, int yPos){
        x = xPos;
        y = yPos;
        String ret = x + " " + y;
        return ret;
    }

    public void gameEnded(int c){
        String msg = (c==-1) ? "Game Ended in Tie" : "Game ended " + c + " win.";

        new AlertDialog.Builder(this).setTitle("Tic Tac Toe").
                setMessage(msg).setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                newGame();
            }
        }).show();
    }

    private void newGame(){
        gameEngine.newGame();
        boardView.invalidate();
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
                            dis = new DataInputStream(s.getInputStream());
                            dos = new DataOutputStream(s.getOutputStream());

                            // create a new thread object
                            Thread t = new GameActivity.ClientHandler(s, dis, dos, context);

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
            String tosend;
            String received;
            try{
                received = dis.readUTF();
                Log.d("received", received);
                String[] data = received.split(" ");
                if(data[0].equals("move")){
                    BoardView.makeAMove(Integer.parseInt(data[1]), Integer.parseInt(data[2]));
                }
                //if(x!=-1&&y!=-1){
                  //  tosend = "move " + x + " " + y;
                    //dos.writeUTF(tosend);
                //}
            } catch(IOException e){
                e.printStackTrace();
            }

            try {
                dis.close();
                dos.close();
            }catch(IOException e){
                e.printStackTrace();
            }
        }


    }
}
