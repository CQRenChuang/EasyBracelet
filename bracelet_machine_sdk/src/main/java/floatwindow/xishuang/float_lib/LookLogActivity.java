package floatwindow.xishuang.float_lib;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.ocm.bracelet_machine_sdk.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;

public class LookLogActivity extends Activity implements View.OnClickListener{

    RelativeLayout close;
    TextView logContent;
    Spinner dateList;
    Button btnUpload;
    Button btnRefresh;
    ScrollView scroll;
    private String path = Environment
            .getExternalStorageDirectory()
            .getAbsolutePath()
            + "/LocalLoger/";
    private String currentName = "";

    private SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("yyyyMMdd");
    Handler mHandler = new Handler();
    List<String> logNameList;
    Activity act;
    boolean uploading = false;
    boolean firstSelect = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_look_log);
        act = this;
        initView();
        Executors.newSingleThreadExecutor().submit(new Runnable() {
            @Override
            public void run() {
                openLog();
            }
        },0);
    }

    void initView(){
        close = (RelativeLayout)findViewById(R.id.close);
        logContent = (TextView)findViewById(R.id.log_content);
        dateList = (Spinner)findViewById(R.id.date_list);
        btnUpload = (Button)findViewById(R.id.upload);
        btnRefresh = (Button)findViewById(R.id.refresh);
        scroll = (ScrollView)findViewById(R.id.scroll_view);
        btnUpload.setOnClickListener(this);
        btnRefresh.setOnClickListener(this);
        close.setOnClickListener(this);
        logNameList = getLogList(path);
        SpinnerAdapter adapter = new ArrayAdapter<String>(this,R.layout.simple_list_item,logNameList);
        dateList.setAdapter(adapter);
        dateList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(firstSelect){
                    firstSelect = false;
                }else{
                    currentName =logNameList.get(i)+".log";
                    refreshLog(currentName);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        if(logNameList.size()>=1){
            int index = logNameList.size()-1;
            dateList.setSelection(index);
            currentName =logNameList.get(index)+".log";
            refreshLog(currentName);
        }
    }

    void openLog(){
        File dir = new File(path);
        if(!dir.exists()){
            dir.mkdirs();
        }
        String logFileName =  simpleDateFormat1.format(new Date())+".log";
        if(currentName.length()==0)currentName = logFileName;
        refreshLog(logFileName);
    }

    void refreshLog(String logFileName){
        Log.i("looklog","refreshLog"+logFileName);
        File logFile = new File(path+logFileName);
        if(logFile.exists()){
            InputStream is = null;
            try{
                is= new FileInputStream(logFile);
                int cnt =0;
                Long filelength = logFile.length();
                byte[] filecontent = new byte[filelength.intValue()];
                is.read(filecontent);
                postContent(new String(filecontent,"UTF-8"));
            }catch (Exception e){
            }finally {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }else{
            postContent("");
        }
    }

    List<String> getLogList(String path){
        List<String> list = new ArrayList();
        File dir = new File(path);
        File[] files = dir.listFiles();
        for(File file:files){
            if(file.isFile()){
                if(!isimeOut(file.getName())){
                    String name = file.getName().substring(0,file.getName().indexOf(".log"));
                    list.add(name);
                }
            }
        }
        Collections.sort(list, new Comparator<String>() {
                    @Override
                    public int compare(String o1, String o2) {
                return timeCompare(o1,o2);
                }
        });
        return list;
    }

    int timeCompare(String t1,String t2){
        long time1 = date2TimeStamp(t1,"yyyyMMdd");
        long time2 = date2TimeStamp(t2,"yyyyMMdd");
        Log.i("compare","t1:"+time1+",t2:"+time2);
        return time1 > time2 ? 1 : (time1 == time2) ? 0 : -1;
    }

    long outTime = 60*60*24*5;
    boolean isimeOut(String name){
        boolean isOut = false;
        Log.i("isimeOut","name:"+name);
        try{
            if(name.indexOf("log")>=0){
                String logname;
                if(name.indexOf("_")>=0){
                    logname = name.substring(0,name.indexOf("_"));
                }else{
                    logname = name.substring(0,name.indexOf(".log"));
                }
                long time = date2TimeStamp(logname,"yyyyMMdd");
                long nowTime=System.currentTimeMillis()/1000;
                if(nowTime-time>outTime){
                    isOut = true;
                }
            }else{
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return isOut;
    }
    long date2TimeStamp(String date, String format) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            return sdf.parse(date).getTime() / 1000;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    void postContent(final  String msg){
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                logContent.setText(msg);
            }
        },0);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                scroll.fullScroll(ScrollView.FOCUS_DOWN);
            }
        },500);
    }

    void uploadLog(){
        if(!uploading){
            showToast("开始上传");
            uploading = true;
        }else{
            showToast("正在上传,,请稍等");
        }
    }

    void showToast(final String msg){
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(act,msg,Toast.LENGTH_SHORT).show();
            }
        },0);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if(id==R.id.close){
            finish();
        }else if(id == R.id.upload){
            uploadLog();
        }else if(id == R.id.refresh){
            refreshLog(currentName);
        }
    }
}
