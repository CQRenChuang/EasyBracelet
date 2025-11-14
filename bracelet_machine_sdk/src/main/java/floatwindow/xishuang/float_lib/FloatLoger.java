package floatwindow.xishuang.float_lib;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;

import floatwindow.xishuang.float_lib.service.FloatMonkService;

import static android.content.ContentValues.TAG;

/**
 * Author:xishuang
 * Date:2017.08.01
 * Des:与悬浮窗交互的控制类，真正的实现逻辑不在这
 */
public class FloatLoger {

    static LocalLoger loger;
    Context context;
    public static boolean ShowFloat = false;

    private FloatLoger() {
    }

    public static FloatLoger getInstance() {
        if (loger == null) loger = LocalLoger.getInstance();
        return LittleMonkProviderHolder.sInstance;
    }

    public static void setSize(long size) {
        LocalLoger.setSize(size);
    }

    // 静态内部类
    private static class LittleMonkProviderHolder {
        private static final FloatLoger sInstance = new FloatLoger();
    }

    private FloatCallBack mFloatCallBack;

    /**
     * 开启服务悬浮窗
     */
    public void startServer(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            path = context.getExternalFilesDir("").getAbsolutePath()
                    + "/LocalLoger/";
        }
        loger.startLogcatManager(context);
        Intent intent = new Intent(context, FloatMonkService.class);
        context.startService(intent);
    }

    public void startLoger() {
        loger = LocalLoger.getInstance();
        loger.startLogcatManager(context);
    }


    public static boolean uploading = false;
    private String path = Environment
            .getExternalStorageDirectory()
            .getAbsolutePath()
            + "/LocalLoger/";
    LinkedList<String> logNames = new LinkedList<>();
    boolean ready = false;
    boolean readyFlag = true;

    public interface UpLoadLogLisenter {
        void callback(List<String> msg, int now, int total);
    }

    UpLoadLogLisenter upLoadLogLisenter;

    public void SetUpLoadLogLisenter(UpLoadLogLisenter li) {
        upLoadLogLisenter = li;
    }

    String keyDir = "";

    public boolean upLoadLog(String name, String dir) throws Exception {
        if (dir.lastIndexOf("/") != dir.length() - 1) throw new Exception("dir parmas is error");
        keyDir = dir;
        return upLoadLog(name);
    }

    public boolean upLoadLog(String name) {
        if (ready) return false;
        ready = true;
        readyFlag = true;
        readyNum = 0;
        num = 0;
        msgStrs.clear();
        logNames.clear();
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdir();
        }
        File[] files = dir.listFiles();
        for (File file : files) {
            if (file.isFile()) {
                String fileName = file.getName();
                Log.i(TAG, "文件:" + fileName);
                if (fileName.indexOf(name) >= 0) {
                    logNames.offer(fileName);
                }
            }
        }
        num = logNames.size();
        RunableUpload();
        return true;
    }

    void RunableUpload() {
        Executors.newSingleThreadExecutor().submit(new Runnable() {
            @Override
            public void run() {
                while (readyFlag) {
                    try {
                        String logname = logNames.poll();
                        if (logname != null) {
                            Thread.sleep(5 * 1000);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                ready = false;
            }
        });
    }

    int num = 0;
    int readyNum = 0;
    List<String> msgStrs = new ArrayList<>();

    public void clearOutLog(int day) {
        loger.clearOutTime(day);
    }

    /**
     * 关闭悬浮窗
     */
    public void stopServer(Context context) {
        Intent intent = new Intent(context, FloatMonkService.class);
        context.stopService(intent);
        loger.stopLogcatManager();
        readyFlag = false;
    }

    public void writeLog(String msg) {
        loger.Write(msg);
    }

    /**
     * 注册监听
     */
    public void registerCallLittleMonk(FloatCallBack callLittleMonk) {
        mFloatCallBack = callLittleMonk;
        if (!ShowFloat) mFloatCallBack.hide();
    }

    /**
     * 调用引导的方法
     */
    public void callGuide(int type) {
        if (mFloatCallBack == null) return;
        mFloatCallBack.guideUser(type);
    }

    /**
     * 悬浮窗的显示
     */
    public void show() {
        if (mFloatCallBack == null) return;
        mFloatCallBack.show();
    }

    /**
     * 悬浮窗的隐藏
     */
    public void hide() {
        if (mFloatCallBack == null) return;
        mFloatCallBack.hide();
    }

    /**
     * 增加数量
     */
    public void addObtainNumer() {
    }

    /**
     * 设置数量
     */
    public void setObtainNumber(int number) {
    }
}
