package me.b1ackange1.noder;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startBut = (Button) findViewById(R.id.but_start);
        infoTv = (TextView) findViewById(R.id.tv_info);

        startBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent i = new Intent(MainActivity.this, StarterService.class);
                if (!Utils.isServiceRunning(MainActivity.this, StarterService.class))
                    startService(i);
                else
                    stopService(i);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter intentFilter = new IntentFilter(StarterService.BROADCAST_STARTED);
        intentFilter.addAction(StarterService.BROADCAST_FINISHED);
        registerReceiver(nodeStatusReceiver, intentFilter);
        updateView();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(nodeStatusReceiver);
    }

    private BroadcastReceiver nodeStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if ( action.equals(StarterService.BROADCAST_FINISHED) ||
                    action.equals(StarterService.BROADCAST_STARTED) ) {
                updateView();
            }
        }
    };

    private void updateView() {
        if (Utils.isServiceRunning(this, StarterService.class)) {
            startBut.setText(R.string.but_stop);
            // Try to guess the ip of the device on local network
            String ip = Utils.getIP();
            if (ip != null) {
                infoTv.setText(getString(R.string.address_info)  + ip.replace("/", "") + ":" + StarterService.PORT);
            }
            else
                infoTv.setText(R.string.unknown_ip_port);
        }
        else {
            startBut.setText(R.string.but_start);
            infoTv.setText(R.string.node_not_running);
        }
    }

    private Button startBut;
    private TextView infoTv;
}