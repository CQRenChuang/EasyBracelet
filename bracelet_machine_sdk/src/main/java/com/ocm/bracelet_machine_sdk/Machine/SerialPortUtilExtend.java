package com.ocm.bracelet_machine_sdk.Machine;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;
import cn.shorr.serialport.SerialPortConfig;
import cn.shorr.serialport.SerialPortService;

import java.util.ArrayList;

public class SerialPortUtilExtend {
    private Context context;
    private boolean isDebug;
    private SerialPortService service;
    private ArrayList<SerialPortConfig> configs;
    private ServiceConnection serviceConnection;
    String TAG = "SerialPortUtilExtend";

    public SerialPortUtilExtend(Context context, SerialPortConfig... configs) {
        this.context = context;
        if (configs.length == 0) {
            throwNullException();
        }
        this.configs = new ArrayList<>();
        for (SerialPortConfig config : configs) {
            this.configs.add(config);
        }

    }

    /**
     * 设置调试模式
     * 开启后，可输出发送和接收的字节数据
     *
     * @param debug defalut: false
     */
    public void setDebug(boolean debug) {
        isDebug = debug;
    }

    /**
     * 绑定服务
     */
    public void bindService() {
        if (configs.size() == 0) {
            throwNullException();
        }
        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                service = ((SerialPortService.SerialBinder) iBinder).getService();
                Log.i(TAG,"connect");
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                Log.i(TAG,"disconnect");
            }
        };
        Intent intent = new Intent(context, SerialPortService.class);
        intent.putExtra("debug", isDebug);
        intent.putExtra("configs", configs);
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    /**
     * 解绑服务
     */
    public void unBindService() {
        if (serviceConnection != null) {
            context.unbindService(serviceConnection);
        }
    }

    /**
     * 抛出空异常
     */
    private void throwNullException() {
        throw new NullPointerException("Please configurate serial port at least one!");
    }
}
