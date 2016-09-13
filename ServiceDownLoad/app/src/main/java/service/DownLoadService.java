package service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import entities.FileInfo;

/**
 * Created by Stefan on 2016/9/12.
 */
public class DownLoadService extends Service {
//    public static final String DOWNLOAD_PATH = Environment.getExternalStorageDirectory().getAbsolutePath()
//            +"/down/";
    public static String DOWNLOAD_PATH =null;
    public static final String ACTION_START = "action_start";
    public static final String ACTION_STOP = "action_stop";
    public static final String ACTION_UPDATE = "action_update";
    private static  final int  MSG_INIT = 0;
    private DownLoadTask downLoadTask = null;
    String path = null;
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (ACTION_START.equals(intent.getAction())){
            FileInfo fileInfo = (FileInfo) intent.getSerializableExtra("fileInfo");
            Log.i("test","start"+fileInfo.toString());
            new InitThread(fileInfo).start();
        }else if (ACTION_STOP.equals(intent.getAction())){
            FileInfo fileInfo = (FileInfo) intent.getSerializableExtra("fileInfo");
            Log.i("test","stop"+fileInfo.toString());
            if (downLoadTask != null){
                downLoadTask.isPause = true;
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case MSG_INIT:
                    FileInfo fileInfo = (FileInfo) msg.obj;
                    DOWNLOAD_PATH = path;
                    downLoadTask = new DownLoadTask(DownLoadService.this,fileInfo);
                    downLoadTask.downLoad();
                    break;
            }
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private class InitThread extends Thread{
        private FileInfo fileInfo;

        public InitThread(FileInfo fileInfo){
            this.fileInfo = fileInfo;
        }
        @Override
        public void run() {
            RandomAccessFile raf = null;
            HttpURLConnection conn = null;
            try {
                URL url = new URL(fileInfo.getUrl());
                conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(5000);
                conn.setRequestMethod("GET");
                int length = -1;

                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK){
                    length = conn.getContentLength();
                };
                if (length <0){
                    return;
                }
                path = getFilesDir().getAbsolutePath()+"/down/";

                File dir = new File(path);
                if (!dir.exists()){
                    dir.mkdir();
                }
                boolean exit = dir.exists();
                //本地创建文件
                File file1 = new File(dir,fileInfo.getFileName());
                if (!file1.exists()){
                    file1.createNewFile();
                }
                boolean name = file1.exists();
                raf = new RandomAccessFile(file1,"rwd");
                raf.setLength(length);
                fileInfo.setLength(length);
                handler.obtainMessage(MSG_INIT,fileInfo).sendToTarget();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                if (conn != null){
                    conn.disconnect();
                }
                if (raf != null){
                    try {
                        raf.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
