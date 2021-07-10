package com.ocm.bracelet_machine_sdk.Machine;

import android.content.Context;
import android.util.Log;

import com.ocm.bracelet_machine_sdk.BraceletMachineManager;
import com.ocm.bracelet_machine_sdk.utils.StringHelper;
import com.ocm.bracelet_machine_sdk.utils.LocalLogger;

import cn.shorr.serialport.SerialPortConfig;
import cn.shorr.serialport.SerialRead;
import cn.shorr.serialport.SerialWrite;

import java.lang.ref.WeakReference;
import java.util.concurrent.Executors;

public class SerialPortHelper {
    SerialPortUtilExtend serialPortUtil;
    SerialRead serial0Read;
    SerialWrite serial0Write;
    WeakReference<Context> contextReference;

    String TAG = "SPH";
    MachineInterface machineInterface;
    public SerialPortHelper(Context c, MachineInterface mi){
        this.contextReference = new WeakReference<>(c);
        this.machineInterface = mi;
    }

    public void Connect() {
        //配置串口参数
        serialPortUtil = new SerialPortUtilExtend(contextReference.get(),new SerialPortConfig("/dev/ttyS3", 115200));
        //设置为调试模式，打印收发数据
        serialPortUtil.setDebug(true);
        //绑定串口服务
        serialPortUtil.bindService();
        //串口0数据读取监听（可在不同Activity中同时设置监听）
        serial0Read = new SerialRead(contextReference.get());
        serial0Read.registerListener(0, new Serial0ReadListener());
        Log.i(TAG,"Connect");
        Executors.newSingleThreadExecutor().submit(new Runnable() {
            @Override
            public void run() {
                machineInterface.onConnect();
            }
        },2000);
    }

    RobotInterface robotLisenter;
    RobotSysInterface sysLisenter;
    public void setOnMsg(RobotInterface ri){
        robotLisenter = ri;
    }
    public void setOnSysMsg(RobotSysInterface lisenter){
        sysLisenter =  lisenter;
    }

    int buffOffSet = 16;
    public void setBlock(int block){
        if(block%4==0)buffOffSet = 0;
        else if(block%4==1)buffOffSet = 16;
        else buffOffSet = 32;
        buffOffSet += 8;
        Log.i(TAG,"setBlock:"+block+","+buffOffSet);
    }

    long lastSendCmdTime= 0;
    private final long WAITOUTTIME = 0;
    public boolean SendCmd(final byte[] cmdBuf, final String hexContent){
        if(System.currentTimeMillis()-lastSendCmdTime<WAITOUTTIME){
            writeLog("SendCmd: 发送间隔过短");
            return false;
        };
        return simpleSendCmd(cmdBuf,hexContent);
    }

    byte[] lastSendCmd = new byte[2];
    boolean reciveNotifyable = false;
    public void setReciveNotify(boolean val){
        reciveNotifyable = val;
    }
    public boolean simpleSendCmd(byte[] cmdBuf, String content){
        lastSendCmd = cmdBuf;
        lastSendCmdTime = System.currentTimeMillis();
        byte[] pack = RobotData.getPackage(cmdBuf,content);
        long t1 = System.currentTimeMillis();
        write(pack);
        Log.i(TAG,"WriteData spend time:"+(System.currentTimeMillis()-t1));
        lastSendCmdTime = System.currentTimeMillis()-WAITOUTTIME;
        String str = "发送指令:"+ StringHelper.bytesToHexFun3(pack)+",content:"+content;
        writeLog(str);
        return true;
    }
    public void ReciveSendSuccess(){
        robotLisenter.OnMsg(RobotMsg.ReciveSendSuccess,"");
        lastSendCmd = new byte[2];
    }

    public boolean noRecordSendCmd(byte[] cmdBuf, String content){
        byte[] pack = RobotData.getPackage(cmdBuf,content);
        long t1 = System.currentTimeMillis();
        write(pack);
        Log.i(TAG,"WriteData spend time:"+(System.currentTimeMillis()-t1));
        String str = "发送指令:"+StringHelper.bytesToHexFun3(pack)+",content:"+content;
        writeLog(str);
        return true;
    }
    void ReciveCallBack(RobotMsg msg){
        if(robotLisenter!=null){
            if(RobotData.byteEqule(lastSendCmd,RobotData.HOST.RECIVE)){
                robotLisenter.OnMsg(msg,"");
                lastSendCmd=new byte[2];
            }
        }
    }
    void GetCallBack(RobotMsg msg){
        if(robotLisenter!=null){
            if(RobotData.byteEqule(lastSendCmd,RobotData.HOST.TAKEBRAND))
                robotLisenter.OnMsg(msg,"");
        }
    }

    private void write(byte[] data){
        SerialWrite.sendData(contextReference.get(), 0, data);
    }

    class Serial0ReadListener implements SerialRead.ReadDataListener {
        byte[] recv_buff = new byte[63];
        byte[] tmp_buff = new byte[0];
        int recv_len;
        boolean RecvThreadFlag = false;
        @Override
        public void onReadData(byte[] recv_buff) {
            Log.i(TAG, "onRead:" + toHexString(recv_buff, recv_buff.length));
            try{
                recv_len = recv_buff.length;
            }catch (Exception e){
                e.printStackTrace();
                recv_len = 0;
            }
            if (recv_len > 0) {
                try{
                    String read_string = toHexString(recv_buff, recv_len);
                    writeLog(recv_len+",收到数据:"+read_string);
//                    if(IsSector(recv_buff)){
//                        tmp_buff = new byte[recv_len];
//                        System.arraycopy(recv_buff,0,tmp_buff,0,recv_len);
//                    }else{
                    if(tmp_buff.length>0){
                        byte[] buff = new byte[63];
                        System.arraycopy(tmp_buff,0,buff,0,tmp_buff.length);
                        System.arraycopy(recv_buff,0,buff,tmp_buff.length,recv_len);
                        read_string = toHexString(buff, buff.length);
                        writeLog(recv_len+",扇区数据合并:"+read_string);
                        analyData(buff,read_string);
                        tmp_buff = new byte[0];
                    }else{
                        if(RobotData.validCRC(recv_buff,recv_len)){
                            analyData(recv_buff,read_string);
                        }else{
                            Log.i(TAG,"校验失败");
                        }
                    }
//                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            Log.i(TAG,"RecvThread stop");
        }
    }

    long lastRecevConfirmTime = 0;
    byte[] lastBuff = null;
    void analyData(byte[] buff, String buffStr){
        if(robotRecvEqual(buff,RobotData.ROBOT.SUCCESS)){
            successDeal();
        }else if(robotRecvEqual(buff,RobotData.ROBOT.BUSY)){
            busyDeal();
        }else if(robotRecvEqual(buff,RobotData.ROBOT.FAIL)){
            failDeal();
        }else if(robotRecvEqual(buff,RobotData.ROBOT.STOPROLL)){
            robotLisenter.OnMsg(RobotMsg.RobotStatusChange,true);
        }
        else if(recevConfirm(buff)){
            //收发手环
            if(System.currentTimeMillis()-lastRecevConfirmTime<1000&&lastBuff!=null&&RobotData.byteEqule(lastBuff,buff)){
                writeLog("距离上次通信时间间隔太短");
                return;
            }
            lastBuff = buff.clone();
            lastRecevConfirmTime = System.currentTimeMillis();
            String tmp = buffStr.replace(" ","").substring(8,16);
            if(BraceletMachineManager.INSTANCE.isIC())tmp = convertCardNo(tmp);
            String cardno = String.valueOf(Long.parseLong(tmp,16));
            while(cardno.length()<10)
                cardno ="0"+cardno;
            writeLog("收到手环:"+cardno+",最后发送的指令:"+StringHelper.bytesToHexFun3(lastSendCmd));
            if(robotRecvEqual(buff,RobotData.ROBOT.CARDNOSECTOR)){
                //块数据拼装
                byte[] blockData = new byte[16];
                byte[] sectorData = new byte[48];
                System.arraycopy(buff,buffOffSet,blockData,0,16);//0-16 块8 块44 16-31 块9 块45  32-47 块10 块46
                try {
                    System.arraycopy(buff, 8, sectorData, 0, 48);//扇区
                }catch (Exception e) {
                    e.printStackTrace();
                }
                CardDataModel data = new CardDataModel(cardno,RobotData.getCabinetRecord(blockData),RobotData.UseCabinet(blockData), sectorData);
                if(RobotData.byteEqule(lastSendCmd,RobotData.HOST.TAKEBRAND)){
                    //发手环
                    Log.i(TAG,"发手环："+data.CardNo+",柜号:"+data.CabinetNos);
                    robotLisenter.OnMsg(RobotMsg.GetSuccess,data);
                }else{
                    //收手环 等待服务器判断手环是否合法
                    writeLog(TAG+"收手环："+data.CardNo+",柜号:"+data.CabinetNos+",reciveNotifyable:"+reciveNotifyable + ", sector: " + StringHelper.bytesToHexFun3(sectorData));
                    if(reciveNotifyable)
                        robotLisenter.OnMsg(RobotMsg.ReciveWait,data);
                }
            }else {
                CardDataModel cardnodata = new CardDataModel(cardno,"0,0,0,0",false);
                if(RobotData.byteEqule(lastSendCmd,RobotData.HOST.TAKEBRAND)){
                    //发手环
                    robotLisenter.OnMsg(RobotMsg.GetSuccess,cardnodata);
                }else{
                    writeLog(TAG+"收手环：reciveNotifyable:"+reciveNotifyable);
                    //收手环 等待服务器判断手环是否合法
                    if(reciveNotifyable)
                        robotLisenter.OnMsg(RobotMsg.ReciveWait,cardnodata);
                }
            }
        }else if(robotRecvEqual(buff,RobotData.ROBOT.RECIVE)){
//            robotLisenter.OnMsg(RobotMsg.ReciveSendSuccess,"");
//            lastSendCmd=new byte[2];
        }else if(robotRecvEqual(buff,RobotData.ROBOT.BUSY)){
            robotLisenter.OnMsg(RobotMsg.Busy,"");
        }
    }
    boolean recevConfirm(byte[] pack){
        byte[] cmd = new byte[2];
        System.arraycopy(pack,2,cmd,0,2);
        if(RobotData.byteEqule(cmd,RobotData.ROBOT.ONLYCARDNO)||
                RobotData.byteEqule(cmd,RobotData.ROBOT.CARDNOSECTOR))
            return true;
        else
            return false;
    }
    void busyDeal(){
        robotLisenter.OnMsg(RobotMsg.Busy,"");
    }

    void successDeal(){
        RobotMsg msg = null;
        //收到指令成功
        if(RobotData.byteEqule(lastSendCmd,RobotData.HOST.RECIVENABLE)){
            msg = RobotMsg.ReciveSuccess;
        }else if(RobotData.byteEqule(lastSendCmd,RobotData.HOST.PUSHBRAND)||RobotData.byteEqule(lastSendCmd,RobotData.HOST.PUSHBRANDOVER)){
            sysLisenter.OnSysMsg(RobotSysMsg.Success,"");
        }else if(RobotData.byteEqule(lastSendCmd,RobotData.HOST.SETCARDTYPE)){
            msg = RobotMsg.InitSuccess;
//            RoadRoller();
        }else if(RobotData.byteEqule(lastSendCmd,RobotData.HOST.TEST)){
            msg = RobotMsg.Success;
        }
        Log.i(TAG,"成功处理:"+msg);
        if(msg!=null)robotLisenter.OnMsg(msg,"");
    }
    byte[] emptyCmd = new byte[2];
    void failDeal(){
        RobotMsg msg = null;
        if(RobotData.byteEqule(lastSendCmd,RobotData.HOST.RECIVE)){
//            msg = RobotMsg.ReciveSendSuccess;
//            lastSendCmd = new byte[2];
        }else if(RobotData.byteEqule(lastSendCmd,RobotData.HOST.RECIVENABLE)){
            //发送可回收后超时
            msg = RobotMsg.ReciveFail;
        }else if(RobotData.byteEqule(lastSendCmd,RobotData.HOST.TAKEBRAND)){
            msg = RobotMsg.GetFail;
        }else if(RobotData.byteEqule(lastSendCmd,RobotData.HOST.TEST)){
            msg = RobotMsg.Fail;
        }else if(RobotData.byteEqule(lastSendCmd,emptyCmd)){
            msg = RobotMsg.ReciveSendRoll;
        }
        Log.i(TAG,"失败处理:"+msg);
        robotLisenter.OnMsg(msg,"");
    }
    private String convertCardNo(String tmp){
        if(tmp==null)tmp = "00000000";
        while(tmp.length()<8)tmp="0"+tmp;
        char[] cardChar = new char[8];
        try{
            cardChar[0] =tmp.charAt(6);
            cardChar[1] =tmp.charAt(7);
            cardChar[2] =tmp.charAt(4);
            cardChar[3] =tmp.charAt(5);
            cardChar[4] =tmp.charAt(2);
            cardChar[5] =tmp.charAt(3);
            cardChar[6] =tmp.charAt(0);
            cardChar[7] =tmp.charAt(1);
        }catch (Exception e){
            e.printStackTrace();
        }
        return String.valueOf(cardChar);
    }
    boolean IsSector(byte[] pack){
        byte[] cmd = new byte[2];
        System.arraycopy(pack,2,cmd,0,2);
        if(RobotData.byteEqule(cmd,RobotData.ROBOT.CARDNOSECTOR))
            return true;
        else
            return false;
    }
    boolean robotRecvEqual(byte[] pack,byte[] robotCMD){
        byte[] cmd = new byte[2];
        System.arraycopy(pack,2,cmd,0,2);
        if(RobotData.byteEqule(cmd,robotCMD))
            return true;
        else
            return false;
    }

    public void Close(){
        stopSerialPortConnect();
    }

    private void stopSerialPortConnect() {
        serial0Read.unRegisterListener();
        serialPortUtil.unBindService();
    }

    private String toHexString(byte[] arg, int length) {
        String result = new String();
        if (arg != null) {
            for (int i = 0; i < length; i++) {
                result = result
                        + (Integer.toHexString(
                        arg[i] < 0 ? arg[i] + 256 : arg[i]).length() == 1 ? "0"
                        + Integer.toHexString(arg[i] < 0 ? arg[i] + 256
                        : arg[i])
                        : Integer.toHexString(arg[i] < 0 ? arg[i] + 256
                        : arg[i])) + " ";
            }
            return result;
        }
        return "";
    }

    public void close(){
        serialPortUtil.unBindService();
        serial0Read.unRegisterListener();
    }

    private void writeLog(String msg){
        LocalLogger.INSTANCE.write(msg);
    }
}
