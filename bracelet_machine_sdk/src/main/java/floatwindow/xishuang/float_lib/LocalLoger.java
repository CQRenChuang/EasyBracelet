package floatwindow.xishuang.float_lib;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;

import floatwindow.xishuang.float_lib.util.FileUtil;

import static android.content.ContentValues.TAG;

public class LocalLoger {
    private static LocalLoger INSTANCE = null;
    private static String PATH_LOG;
    private LogDumper mLogDumper = null;
    private SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("yyyyMMdd");
    private SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private Date initDate;

    private String LogDirName = "LocalLoger";
    private LinkedList<String> waitWriteLog = new LinkedList<>();
    long writedLength = 0;
    long splitIndex = 1;
    static long splitLen = 500*1024;

    public static void setSize(long size){
        splitLen = size;
    }

    public static LocalLoger getInstance()
    {
        if (INSTANCE == null)
        {
            INSTANCE = new LocalLoger();
        }
        return INSTANCE;
    }

    public void Write(String log){
        waitWriteLog.offer(log);
    }

    public void startLogcatManager(Context context)
    {
        String folderPath = null;
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O_MR1 && Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
        {
            folderPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + LogDirName;
        }
        else
        {
            folderPath = context.getExternalFilesDir("").getAbsolutePath() + File.separator + LogDirName;
        }
        LocalLoger.getInstance().start(folderPath);
    }


    public void stopLogcatManager()
    {
        LocalLoger.getInstance().stop();
    }


    private void setFolderPath(String folderPath)
    {
        File folder = new File(folderPath);
        if (!folder.exists())
        {
            folder.mkdirs();
        }
        if (!folder.isDirectory())
        {
            throw new IllegalArgumentException("The logcat folder path is not a directory: " + folderPath);
        }

        PATH_LOG = folderPath.endsWith("/") ? folderPath : folderPath + "/";
    }


    public void start(String saveDirectoy)
    {
        setFolderPath(saveDirectoy);
        if (mLogDumper == null)
        {
            mLogDumper = new LogDumper(PATH_LOG);
        }
        try{
            mLogDumper.start();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void clearOutTime(int outDay){
        try{
            long time = outDay*24*60*60;
            File dir = new File(PATH_LOG);
            File[] files = dir.listFiles();
            for(File file:files){
                if(file.isFile()){
                    if(FileUtil.isimeOut(file.getName(),time)){
                        Log.i(TAG,"清除过期文件:"+file.getName());
                        file.delete();
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void stop()
    {
        if (mLogDumper != null)
        {
            mLogDumper.stopLogs();
            mLogDumper = null;
        }
    }


    private class LogDumper extends Thread
    {
        private Process logcatProc;
        private BufferedReader mReader = null;
        public boolean mRunning = true;
        private FileOutputStream out = null;
        private boolean haveSplit = false;

        void resetInitDate(){
            initDate = new Date();
            initDate.setHours(0);
            initDate.setMinutes(0);
            initDate.setSeconds(0);
        }
        public LogDumper(String dir)
        {
            try
            {
                resetInitDate();
                String name = simpleDateFormat1.format(initDate);
                File file = new File(dir);
                if (file.exists() && file.listFiles() != null) {
                    System.out.println("日志文件夹:" + file.getAbsolutePath());
                    File[] files = file.listFiles();
                    for (File file2 : files) {
                        if (file2.isDirectory()) {
                            System.out.println("文件夹:" + file2.getName());
                        } else {
                            if (file2.getName().contains(name)){
                                String[] splits = file2.getName().split("_");
                                if(splits.length>1){
                                    splitIndex++;
                                    haveSplit = true;
                                }
                            }
                            System.out.println("文件:" + file2.getAbsolutePath());
                        }
                    }
                }
                System.out.println("start index" +splitIndex);
                String logFileName =  name+".log";
                if(haveSplit)
                    logFileName =  name+"_"+(splitIndex-1)+".log";
                File fileout = new File(dir, logFileName);
                out = new FileOutputStream(fileout, true);
                writedLength = fileout.length();
                System.out.println("writedLength" +writedLength);
            }
            catch (FileNotFoundException e)
            {
                e.printStackTrace();
            }
        }

        long oneDay = 1000*60*60*24;
        private void arriveTomorrow(){
            if(System.currentTimeMillis()-initDate.getTime()>=oneDay){
                Log.i(TAG,"arriveTomorrow");
                try{
                    synchronized (out){
                        out.close();
                        resetInitDate();
                        String logFileName =  simpleDateFormat1.format(initDate)+".log";
                        out = new FileOutputStream(new File(PATH_LOG, logFileName), true);
                        splitIndex = 1;
                        Write("日期变化 新建文件");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        void sizeIsFull(){
            if(writedLength>=splitLen){
                Log.i(TAG,"sizeIsFull");
                try{
                    synchronized (out){
                        out.close();
                        String logFileName =  simpleDateFormat1.format(initDate)+"_"+(splitIndex++)+".log";
                        out = new FileOutputStream(new File(PATH_LOG, logFileName), true);
                        writedLength = 0;
                        Write("到达容量上限 分片");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


        public void stopLogs()
        {
            mRunning = false;
        }


        @Override
        public void run()
        {
            try
            {
                while (mRunning)
                {
                    if (!mRunning)
                    {
                        break;
                    }
                    try {

                        arriveTomorrow();
                        if (out != null)
                        {
                            sizeIsFull();
                            try{
                                while (waitWriteLog.size()>0&&!FloatLoger.uploading){
                                    String line = waitWriteLog.poll();
                                    if(line!=null){
                                        byte[] waitWrite =(simpleDateFormat2.format(new Date()) + "  " + line + "\n").getBytes();
                                        writedLength+=waitWrite.length;
                                        out.write(waitWrite);
                                    }
                                }
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            finally
            {
                if (logcatProc != null)
                {
                    logcatProc.destroy();
                    logcatProc = null;
                }
                if (mReader != null)
                {
                    try
                    {
                        mReader.close();
                        mReader = null;
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
                if (out != null)
                {
                    try
                    {
                        out.close();
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                    out = null;
                }
            }
            mRunning = false;
        }


    }
}
