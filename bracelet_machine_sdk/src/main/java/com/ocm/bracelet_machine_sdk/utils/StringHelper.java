package com.ocm.bracelet_machine_sdk.utils;

import android.text.TextUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by ocm on 2017-06-20.
 */

public class StringHelper {
    public static String replaceCardno(String cardno){
        String regEx = "[^A-Za-z0-9_]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(cardno);
        return m.replaceAll("").trim();
    }

    public static byte[] hmacSha1(String base, String key) throws NoSuchAlgorithmException, InvalidKeyException {
        if (TextUtils.isEmpty(base) || TextUtils.isEmpty(key)) {
            return null;
        }
        String type = "sha1";
        SecretKeySpec secret = new SecretKeySpec(key.getBytes(), type);
        Mac mac = Mac.getInstance(type);
        mac.init(secret);
        byte[] digest = mac.doFinal(base.getBytes());
        return digest;
//        hideboard Base64.encodeToString(digest, Base64.DEFAULT);

    }
    public static String getNumFromString(String str){
        int index = 0;
        for (int i =0,len=str.length();i<len;i++){
            if (!Character.isDigit(str.charAt(i))){
                index = i;
                break;
            }
        }
        return str.substring(0,index);
    }

    public static String str2HexStr(String str) throws Exception {
        int num = Integer.parseInt(str);
        if(num<10)return "0"+num;
        else{
            if(num==10)return "0A";
            else if(num==11)return "0B";
            else if(num==12)return "0C";
            else if(num==13)return "0D";
            else if(num==14)return "0E";
            else if(num==15)return "0F";
            else if(num==44)return "2C";
            else if(num==45)return "2D";
            else if(num==46)return "2E";
            else if(num==40)return "28";
            else if(num==41)return "29";
            else if(num==42)return "2A";
            else throw new Exception("格式错误");
        }
    }

    //字节数组转字符串
    public static String bytesToHexFun3(byte[] bytes) {
        StringBuilder buf = new StringBuilder(bytes.length * 2);
        for(byte b : bytes) { // 使用String的format方法进行转换
            buf.append(String.format("%02x", new Integer(b & 0xff)));
        }
        return buf.toString();
    }
    public static String hexStr2Str(String hexStr)
    {
        int len = hexStr.length();
        if(len==1)hexStr= "0"+hexStr;
        String str = "0123456789ABCDEF";
        char[] hexs = hexStr.toCharArray();
        byte[] bytes = new byte[len/ 2];
        int n;

        for (int i = 0; i < bytes.length; i++)
        {
            n = str.indexOf(hexs[2 * i]) * 16;
            n += str.indexOf(hexs[2 * i + 1]);
            bytes[i] = (byte) (n & 0xff);
        }
        return new String(bytes);
    }
    public static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
//        hexString = hexString.toUpperCase();
//        int length = hexString.length() / 2;
        int length = hexString.length() >> 1;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
//            int pos = i * 2;
            int pos = i << 1;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }
    private static byte charToByte(char c) {
        int dex =  "0123456789ABCDEF".indexOf(c);
        if(dex<0)dex ="0123456789abcdef".indexOf(c);
        return (byte) dex;
    }

    public static long date2TimeStamp(String date, String format) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            return sdf.parse(date).getTime() / 1000;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static String GetTimeStamp(){
        String res;
        res = String.valueOf(Calendar.getInstance().getTime());
        return res;
    }

    public static String stringToHexString(String strPart) {
        String hexString = "";
        for (int i = 0; i < strPart.length(); i++) {
            int ch = (int) strPart.charAt(i);
            String strHex = Integer.toHexString(ch);
            hexString = hexString + strHex;
        }
        return hexString;
    }

    private static String hexString="0123456789ABCDEF";
    /*
    * 将字符串编码成16进制数字,适用于所有字符（包括中文）
    */
    public static String encode(String str)
    {
// 根据默认编码获取字节数组
        byte[] bytes=str.getBytes();
        StringBuilder sb=new StringBuilder(bytes.length*2);
// 将字节数组中每个字节拆解成2位16进制整数
        for(int i=0;i<bytes.length;i++)
        {
            sb.append(hexString.charAt((bytes[i]&0xf0)>>4));
            sb.append(hexString.charAt((bytes[i]&0x0f)>>0));
        }
        return sb.toString();
    }

    /*
    * 将16进制数字解码成字符串,适用于所有字符（包括中文）
    */
    public static String decode(String bytes)
    {
        ByteArrayOutputStream baos=new ByteArrayOutputStream(bytes.length()/2);
// 将每2位16进制整数组装成一个字节
        for(int i=0;i<bytes.length();i+=2)
            baos.write((hexString.indexOf(bytes.charAt(i))<<4 |hexString.indexOf(bytes.charAt(i+1))));
        return new String(baos.toByteArray());
    }

    private static byte uniteBytes(byte src0, byte src1) {
        byte _b0 = Byte.decode("0x" + new String(new byte[]{src0})).byteValue();
        _b0 = (byte) (_b0 << 4);
        byte _b1 = Byte.decode("0x" + new String(new byte[]{src1})).byteValue();
        byte ret = (byte) (_b0 | _b1);
        return ret;
    }
//    public static byte[] HexString2Bytes(String src)
//    {
//        byte[] ret = new byte[6];
//        byte[] tmp = src.getBytes();
//        for(int i=0; i<6; ++i )
//        {
//            ret[i] = uniteBytes(tmp[i*2], tmp[i*2+1]);
//        }
//        hideboard ret;
//    }

    public static String hexStrReverse(String tmp){
        if(tmp.length()%2!=0)tmp="0"+tmp;
        int len = tmp.length();
        char[] newchar = new char[len];
        try{
            for(int i=0;i<len;i+=2){
                newchar[i] = tmp.charAt(len-i-2);
                newchar[i+1] = tmp.charAt(len-i-1);
//                Log.i("reverse",i+":"+tmp.charAt(len-i-2));
//                Log.i("reverse",(i+1)+":"+tmp.charAt(len-i-1));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return String.valueOf(newchar);
    }

    public static String unicodeToString(String unicode) {
        String str = unicode.replace("0x", "\\");

        StringBuffer string = new StringBuffer();
        String[] hex = str.split("\\\\u");
        for (int i = 1; i < hex.length; i++) {
            int data = Integer.parseInt(hex[i], 16);
            string.append((char) data);
        }
        return string.toString();
    }

    public static String getTime(){
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int min = calendar.get(Calendar.MINUTE);
        final String time = (hour<10?"0"+hour:hour)+":"+(min<10?"0"+min:min);
        return time;
    }

    public static String getDateStr(){
        Date date=new Date();
        SimpleDateFormat format=new SimpleDateFormat("yyyy/MM/dd/  E");
        return format.format(date);
    }

    public static String convertPsw(String tmp){
        if(tmp==null)tmp = "000000000000";
        while(tmp.length()<12)tmp="0"+tmp;
//        Log.i(TAG,"convertPsw:"+tmp);
        char[] cardChar = new char[12];
        try{
            cardChar[0] =tmp.charAt(10);
            cardChar[1] =tmp.charAt(11);
            cardChar[2] =tmp.charAt(8);
            cardChar[3] =tmp.charAt(9);
            cardChar[4] =tmp.charAt(6);
            cardChar[5] =tmp.charAt(7);
            cardChar[6] =tmp.charAt(4);
            cardChar[7] =tmp.charAt(5);
            cardChar[8] =tmp.charAt(2);
            cardChar[9] =tmp.charAt(3);
            cardChar[10] =tmp.charAt(0);
            cardChar[11] =tmp.charAt(1);
        }catch (Exception e){
            e.printStackTrace();
        }
        return String.valueOf(cardChar);
    }

    public static byte[] longToByteArray(long s) {
        byte[] targets = new byte[8];
        for (int i = 0; i < 8; i++) {
            int offset = (targets.length - 1 - i) * 8;
            targets[i] = (byte) ((s >>> offset) & 0xff);
        }
        return targets;
    }

    public static byte[] intToByteArray(int value) {
        byte[] buffer = new byte[4];
        int offset=0;
        buffer[offset++] = (byte)value;
        buffer[offset++] = (byte)(value>>8);
        buffer[offset++] = (byte)(value>>16);
        buffer[offset++] = (byte)(value>>24);
        return buffer;
    }
}
