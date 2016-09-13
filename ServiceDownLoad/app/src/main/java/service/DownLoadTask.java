package service;

import android.content.Context;
import android.content.Intent;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import db.ThreadDao;
import db.ThreadDaoImpl;
import entities.FileInfo;
import entities.ThreadInfo;

/**
 * Created by Stefan on 2016/9/12.
 */
public class DownLoadTask {
    private FileInfo fileInfo;
    private Context context;
    private ThreadDao threadDao;
    private int finished;
    public boolean isPause = false;

    public DownLoadTask(Context context, FileInfo fileInfo) {
        this.context = context;
        this.fileInfo = fileInfo;
        threadDao = new ThreadDaoImpl(context);
    }

    public  void downLoad(){
        ThreadInfo threadInfo = null;
      List<ThreadInfo> threadInfos = threadDao.getThread(fileInfo.getUrl());
        if (threadInfos.size() == 0){
             threadInfo = new ThreadInfo(0,fileInfo.getUrl(),0,fileInfo.getLength(),0);
        }else {
            threadInfo = threadInfos.get(0);
        }
        new DownLoadThread(threadInfo).start();
    }

    class DownLoadThread extends Thread{
        private ThreadInfo threadInfo;
        public DownLoadThread(ThreadInfo threadInfo){
            this.threadInfo = threadInfo;
        }

        @Override
        public void run() {
            HttpURLConnection conn= null;
            RandomAccessFile randomAccessFile = null;
            InputStream in = null;
            if (!threadDao.isExits(threadInfo.getUrl(),threadInfo.getId())){
                threadDao.insertThread(threadInfo);
            }
            try {
                URL url = new URL(threadInfo.getUrl());
                conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(50000);
                conn.setRequestMethod("GET");
                int start = threadInfo.getStart()+threadInfo.getFinished();
               conn.setRequestProperty("Range","bytes="+start+"-"+threadInfo.getEnd());
                File file = new File(DownLoadService.DOWNLOAD_PATH,fileInfo.getFileName());
                randomAccessFile = new RandomAccessFile(file,"rwd");
                randomAccessFile.seek(start);
                finished += threadInfo.getFinished();
                final Intent intent = new Intent();
                intent.setAction(DownLoadService.ACTION_UPDATE);
                int a = conn.getResponseCode();
               if (conn.getResponseCode() == HttpURLConnection.HTTP_OK){
                   in = conn.getInputStream();
                   byte[] buffer = new  byte[1024];
                   int len = -1;
                   float time = System.currentTimeMillis();
                   while ((len = in.read(buffer)) != -1){
                       randomAccessFile.write(buffer,0,len);
                       finished += len;
                       Timer timer = new Timer();
                       timer.schedule(new TimerTask(){
                           @Override
                           public void run() {
                               intent.putExtra("finished",finished*100/fileInfo.getLength());
                               context.sendBroadcast(intent);
                           }
                       },1000,1000);

                       if (isPause){
                           threadDao.updateThread(threadInfo.getUrl(),threadInfo.getId(),finished);
                           return;
                       }
                   }
                   threadDao.deleteThread(threadInfo.getUrl(),threadInfo.getId());
               }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                if (conn != null){
                    conn.disconnect();
                }
                if (randomAccessFile != null){
                    try {
                        randomAccessFile.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (in != null){
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        }
    }
}
