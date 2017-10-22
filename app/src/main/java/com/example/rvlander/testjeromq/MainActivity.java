package com.example.rvlander.testjeromq;

import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import org.zeromq.ZMQ;
import android.widget.TextView;
import android.os.Handler;

public class MainActivity extends AppCompatActivity {


    public class hwserver implements Runnable{

        private Handler h;

        public hwserver(Handler h) {
            this.h = h;
        }
        public void run() {
            ZMQ.Context context = ZMQ.context(1);

            //  Socket to talk to clients
            ZMQ.Socket responder = context.socket(ZMQ.REP);
            responder.connect("tcp://192.168.1.89:5555");

            int c = 0;

            while (!Thread.currentThread().isInterrupted()) {
                // Wait for next request from the client
                byte[] request = responder.recv(0);
                System.out.println("Received Hello");

                Message m = Message.obtain();
                Bundle b = new Bundle();
                b.putString("str", "Received Hello" + c);
                m.setData(b);
                h.sendMessage(m);

                try{
                    Thread.sleep(1000);
                } catch(InterruptedException ie) {
                    System.out.println("Could not sleep");
                }

                // Send reply back to client
                String reply = "World";
                responder.send(reply.getBytes(), 0);
                c++;
            }
            responder.close();
            context.term();
        }
    }

    public void handleMessageReceived(String message) {
        TextView textView = (TextView)findViewById(R.id.my_view);
        textView.setText(message);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Handler h = new Handler(){
            @Override
            public void handleMessage(Message msg){
                Bundle b = msg.getData();
                handleMessageReceived(b.getString("str"));
            }
        };

        new Thread(new hwserver(h)).start();

    }
}
