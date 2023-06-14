package com.ocm.bracelet_machine_sdk.Machine;

import android.util.Log;

import com.ocm.bracelet_machine_sdk.utils.StringHelper;

/**
 * Created by ocm on 2018-09-21.
 */

public class RobotData {
    private static String TAG = "RobotData";

    private static byte[] HEAD = new byte[]{intToByte(0x0f)};
    private static byte[] ADDR = new byte[]{intToByte(0x01), intToByte(0x02)};
    public static class HOST{
        public static byte[] NULL = new byte[]{intToByte(0x00),intToByte(0x00)};
//        public static byte[] SUCCESS = new byte[]{intToByte(0x0A),intToByte(0xA0)};
        public static byte[] TEST = new byte[]{intToByte(0x1A),intToByte(0xA1)};
        public static byte[] RECIVE = new byte[]{intToByte(0x2A),intToByte(0xA2)}; //回收手环
        public static byte[] RECIVENABLE = new byte[]{intToByte(0x3A),intToByte(0xA3)};//可以回收
        public static byte[] RECIVEDISABLE = new byte[]{intToByte(0x4A),intToByte(0xA4)};//不可回收 丢弃
//        public static byte[] TAKEBRAND = new byte[]{intToByte(0x5A),intToByte(0xA5)}; //取手环
        public static byte[] TAKEBRAND = new byte[]{intToByte(0x0B),intToByte(0xB0)}; //取手环
        public static byte[] PUSHBRAND = new byte[]{intToByte(0x6A),intToByte(0xA6)}; //放入手环 用于系统设置
        public static byte[] PUSHBRANDOVER = new byte[]{intToByte(0x7A),intToByte(0xA7)}; //直放入手环完成
        public static byte[] READCARD = new byte[]{intToByte(0x8A),intToByte(0xA8)}; //读扇区、卡号、密码
        public static byte[] WRITECARD = new byte[]{intToByte(0x9A),intToByte(0xA9)}; //写扇区、卡号、密码
//        public static byte[] ERRORERCOVER = new byte[]{intToByte(0xAA),intToByte(0xAA)}; //直接吐手环
        public static byte[] RESET = new byte[]{intToByte(0xBA),intToByte(0xAB)}; //复位
        public static byte[] FAIL = new byte[]{intToByte(0xFA),intToByte(0xAF)};
        public static byte[] SETCARDTYPE = new byte[]{intToByte(0xCA),intToByte(0xAC)};//设置卡类型 ID/IC
        public static byte[] STOP = new byte[]{intToByte(0xDA),intToByte(0xAD)};//告诉下位机 已无手环
        public static byte[] START = new byte[]{intToByte(0xEA),intToByte(0xAE)};//告诉下位机 已无手环
    }
    public static class ROBOT{
        public static byte[] SUCCESS = new byte[]{intToByte(0x10),intToByte(0x01)};
        public static byte[] RECIVE = new byte[]{intToByte(0x20),intToByte(0x02)};//回收手环
        public static byte[] FAIL = new byte[]{intToByte(0x30),intToByte(0x03)};
        public static byte[] TAKE = new byte[]{intToByte(0x40),intToByte(0x04)};//发送手环
        public static byte[] DOOVER = new byte[]{intToByte(0x50),intToByte(0x05)};// 执行成功 清空手环或 写手环
        public static byte[] READCARDNO = new byte[]{intToByte(0x60),intToByte(0x06)};//返回卡号和扇区
        public static byte[] ONLYCARDNO = new byte[]{intToByte(0x70),intToByte(0x07)};//只读卡号
        public static byte[] CARDNOSECTOR = new byte[]{intToByte(0x80),intToByte(0x08)};//卡号和扇区
        public static byte[] BUSY = new byte[]{intToByte(0xA0),intToByte(0x0A)};//忙碌
        public static byte[] STOPROLL = new byte[]{intToByte(0xB0),intToByte(0x0B)};//下位机停转
    }

    public static byte[] testRobotData = new byte[]{intToByte(0x0f),intToByte(0x00)
            ,intToByte(0x30),intToByte(0x03),intToByte(0x31),intToByte(0x57),intToByte(0x00),intToByte(0x00),intToByte(0x00),intToByte(0x00)};

    public static byte[] testRobotData2 = new byte[]{intToByte(0x0f),intToByte(0x00)
            ,intToByte(0x40),intToByte(0x04),intToByte(0x31),intToByte(0x57),intToByte(0x00),intToByte(0x00),intToByte(0x00),intToByte(0x00)};

    private static byte intToByte(int x) {
        return (byte) x;
    }

    public static byte[] getPackage(byte[] CMD, String conent, int addr){
        if(conent.length()%2!=0)conent = "0"+conent;
        byte[] contentByte = StringHelper.hexStringToBytes(conent);
        int contentLen = contentByte==null?0:contentByte.length;
//        byte[] pack = new byte[84];
        byte[] pack = new byte[contentLen+4];
        pack[0] = HEAD[0];
        pack[1] = ADDR[addr];
        pack[2] = CMD[0];
        pack[3] = CMD[1];
        if(contentByte!=null){
            for(int i=0,len = contentLen;i<len;++i){
                pack[i+4] = contentByte[i];
            }
        }
        //校验码
//        pack = CRC16Util.appendCrc16(pack);
//        byte tmp = pack[contentLen+5];
//        pack[contentLen+5] =pack[contentLen+4];
//        pack[contentLen+4] = tmp;
        return pack;
    }

    public static boolean validCRC(byte[] pack,int len){
        byte[] buff = new byte[len-2];
        System.arraycopy(pack, 0, buff, 0, buff.length);
        byte[] crc = CRC16Util.getCrc16(buff);
        Log.i("validCRC","crc:"+ StringHelper.bytesToHexFun3(crc)+"pack:"+StringHelper.bytesToHexFun3(pack));
        if(crc[0]==pack[len-1]&&crc[1]==pack[len-2])return true;
//        return false;
        return true;
    }

    public static boolean byteEqule(byte[] b1,byte[] b2){
        if(b1.length!=b2.length)return false;
        for(int i =0;i<b1.length;++i){
            if(b1[i] != b2[i])return false;
        }
        return true;
    }


    /// <summary>
    /// 设置水泵
    /// </summary>
    /// <param name="type">0：无效  1：自动充值 需要激活（即每刷一次水控机置为0）  2：不限次数  3：人工充值 按次数每刷一次减1 </param>
    /// <param name="count">充值次数</param>
    /// <param name="munute">充值时间</param>
    /// <returns></returns>
    public static String getSetWaterPumpData(int type, int count, int minute){
        //水控写块10
        String data="";
        String sec = (minute * 60)+"";
        byte[] numbyte = StringHelper.intToByteArray(count);
        byte[] buff = new byte[16];
        byte[] tmp = new byte[14];
        buff[2] = tmp[0]= (byte)type; //类型1byte
        buff[3] = tmp[1] = numbyte[1];
        buff[4] = tmp[2] = numbyte[0];//次数 2byte
        buff[5] = tmp[3] = (byte)minute;
        buff[6] = 0;
        buff[7] = 0;
        byte[] crc = CRC16Util.appendCrc16(tmp);
        buff[0] =crc[15];
        buff[1] = crc[14];
        return StringHelper.bytesToHexFun3(buff);
    }

    public static String getWaterPumpData(int type, String count, int minute){
        String data="";
        String sec = (minute * 60)+"";
//        byte[] remainSec = StringHelper.hexStringToBytes(sec);
        byte[] numbyte = StringHelper.hexStringToBytes(count);
        byte[] buff = new byte[16];
        byte[] tmp = new byte[15];
        buff[2] = tmp[0]= (byte)type; //类型1byte
        buff[3] = tmp[1] = numbyte[1];
        buff[4] = tmp[2] = numbyte[0];//次数 2byte
        buff[5] = tmp[3] = (byte)minute;
        buff[6] = 0;
        buff[7] = 0;
        buff = CRC16Util.appendCrc16(buff);
        byte change = buff[7];
        buff[7] =buff[6];
        buff[6] = change;
        return StringHelper.bytesToHexFun3(buff);
    }


    public static String getCabinetRecord(byte[] buff){
        String data = StringHelper.bytesToHexFun3(buff);
        String str="";
        String cupno="无";
        if (data.length() == 0)
        {
            str = "0";
        }
        else {
            //租柜柜号
            String rentno = GetNo(data.substring(10, 14));
//            //临时柜柜号
//            cupno = GetNo(data.substring(6, 10));
            //临时柜记录
            String no1 = GetNo(data.substring(14, 18));
            String no2 = GetNo(data.substring(18, 22));
            String no3 = GetNo(data.substring(22, 26));
            boolean userent = data.substring(26, 30).equals("A5");
            str = String.format("%s,%s,%s,%s", no1, no2, no3, userent?rentno:"0");
        }
        Log.i(TAG,"临时柜号:"+cupno);
        Log.i(TAG,"柜号记录:"+str);
        return str;
    }
    public static boolean UseCabinet(byte[] buff){
        String data = StringHelper.bytesToHexFun3(buff);
        String cupno="0";
        if (data.length() == 0)
        {
            cupno = "0";
        }
        else {
            //租柜柜号
            String rentno = GetNo(data.substring(10, 14));
            //临时柜柜号
            cupno = GetNo(data.substring(6, 10));
        }
        Log.i(TAG,"临时柜号:"+cupno);
        return cupno.equals("0")?false:true;
    }

    private static String GetNo(String hex)
    {
        if (hex.equals("0000")) return "0";
        String tmp =  Long.parseLong(hex,16)+"";
        while (tmp.length() != 5) tmp = "0" + tmp;
        String noTmp = tmp.substring(2,5);
        int no =  Integer.parseInt(noTmp);
        String noStr = no+"";
        while (noStr.length() != 3) noStr = "0" + noStr;
        return GetZone(tmp.charAt(1)+"")+noStr;
    }
    private static String GetZone(String restmp)
    {
        switch (restmp)
        {
            case "0": return "女";
            case "1": return "男";
            case "2": return "C";
            case "3": return "D";
            case "4": return "E";
            case "5": return "F";
            case "6": return "G";
            default: return "未知";
        }
    }
}
