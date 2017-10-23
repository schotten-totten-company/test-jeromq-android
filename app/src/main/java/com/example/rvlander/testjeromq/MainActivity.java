package com.example.rvlander.testjeromq;

import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.os.Handler;

import java.util.Random;

import org.zeromq.ZMQ;
import org.zeromq.ZContext;
import org.zeromq.ZFrame;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZMsg;

public class MainActivity extends AppCompatActivity {
    //
// Simple Pirate worker
// Connects REQ socket to tcp://*:5556
// Implements worker part of load-balancing queueing
//
    public class spworker implements Runnable {

        private final static String WORKER_READY = "\001";      //  Signals worker is ready

        private Handler h;

        public spworker(Handler h) {
            this.h = h;
        }

        public void run() {
            ZContext ctx = new ZContext();
            Socket worker = ctx.createSocket(ZMQ.REQ);

            //  Set random identity to make tracing easier
            Random rand = new Random(System.nanoTime());
            String identity = String.format("%04X-%04X", rand.nextInt(0x10000), rand.nextInt(0x10000));
            worker.setIdentity(identity.getBytes());
            worker.connect("tcp://83.152.93.57:5555");

            //  Tell broker we're ready for work
            System.out.printf("I: (%s) worker ready\n", identity);
            ZFrame frame = new ZFrame(WORKER_READY);
            frame.send(worker, 0);

            int cycles = 0;
            int c = 0;
            while (true) {
                ZMsg msg = ZMsg.recvMsg(worker);
                if (msg == null)
                    break;              //  Interrupted

                Message m = Message.obtain();
                Bundle b = new Bundle();
                b.putString("str", "Received Hello" + c);
                m.setData(b);
                h.sendMessage(m);

                //  Simulate various problems, after a few cycles
                cycles++;
                /*if (cycles > 3 && rand.nextInt(5) == 0) {
                    System.out.printf("I: (%s) simulating a crash\n", identity);
                    msg.destroy();
                    break;
                } else if (cycles > 3 && rand.nextInt(5) == 0) {
                    System.out.printf("I: (%s) simulating CPU overload\n", identity);
                    try {
                        Thread.sleep(3000);
                    } catch (Exception e) {
                        System.out.println("Can't sleep");
                    }
                }*/
                System.out.printf("I: (%s) normal reply\n", identity);
                try {
                    Thread.sleep(3000);
                } catch (Exception e) {
                    System.out.println("Can't sleep");
                }//  Do some heavy work
                msg.send(worker);
                c++;
            }
            ctx.destroy();
        }

    }

    public void handleMessageReceived(String message) {
        TextView textView = (TextView) findViewById(R.id.my_view);
        textView.setText(message);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Handler h = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                Bundle b = msg.getData();
                handleMessageReceived(b.getString("str"));
            }
        };

        new Thread(new spworker(h)).start();

    }
}
