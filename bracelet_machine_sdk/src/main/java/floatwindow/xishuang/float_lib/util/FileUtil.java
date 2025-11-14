package floatwindow.xishuang.float_lib.util;

import android.util.Log;

import java.text.SimpleDateFormat;

public class FileUtil {
    public static boolean isimeOut(String name,long outTime){
        boolean isOut = false;
        try{
            if(name.indexOf("log")>=0){
                String logname = name.substring(0,8);
                long time = date2TimeStamp(logname,"yyyyMMdd");
                long nowTime=System.currentTimeMillis()/1000;
                if(nowTime-time>outTime){
                    Log.i("isimeOut","name:"+name);
                    isOut = true;
                }
            }else{
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return isOut;
    }
    static long date2TimeStamp(String date, String format) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            return sdf.parse(date).getTime() / 1000;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
}
