package com.example.stefan.servicedownload;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import entities.FileInfo;
import service.DownLoadService;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private ProgressBar progressBar;
    private Button stop,begin;
    private FileInfo fileInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setMax(100);
        stop = (Button) findViewById(R.id.button);
        begin = (Button) findViewById(R.id.button2);
        stop.setOnClickListener(this);
        begin.setOnClickListener(this);
        fileInfo = new FileInfo(0,"https://github.com/StefanLiu007/StefanLiu007.github.io.git","kkk.txt",0,0);
        IntentFilter i = new IntentFilter();
        i.addAction(DownLoadService.ACTION_UPDATE);
        registerReceiver(broad,i);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.button:
                Intent intent1 = new Intent(MainActivity.this, DownLoadService.class);
                intent1.setAction(DownLoadService.ACTION_STOP);
                intent1.putExtra("fileInfo",fileInfo);
                startService(intent1);

                break;
            case R.id.button2:
                //通过intent传递给service
                Intent intent = new Intent(MainActivity.this, DownLoadService.class);
                intent.setAction(DownLoadService.ACTION_START);
                intent.putExtra("fileInfo",fileInfo);
                startService(intent);
                break;
        }
    }
    BroadcastReceiver broad = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(DownLoadService.ACTION_UPDATE)){
                int finished = intent.getIntExtra("finished",0);
                Log.i("mmm",finished+"finished");
                progressBar.setProgress(finished);
            }
        }
    };

    @Override
    protected void onDestroy() {
        unregisterReceiver(broad);
        super.onDestroy();
    }
}
