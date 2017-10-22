package com.example.rvlander.testjeromq;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import org.zeromq.ZMQ;
public class MainActivity extends AppCompatActivity {

    public class hwserver implements Runnable{

        public void run() {
            ZMQ.Context context = ZMQ.context(1);

            //  Socket to talk to clients
            ZMQ.Socket responder = context.socket(ZMQ.REP);
            responder.connect("tcp://192.168.1.89:5555");

            while (!Thread.currentThread().isInterrupted()) {
                // Wait for next request from the client
                byte[] request = responder.recv(0);
                System.out.println("Received Hello");

                try{
                    Thread.sleep(1000);
                } catch(InterruptedException ie) {
                    System.out.println("Could not sleep");
                }

                // Send reply back to client
                String reply = "World";
                responder.send(reply.getBytes(), 0);
            }
            responder.close();
            context.term();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new Thread(new hwserver()).start();

    }
}
