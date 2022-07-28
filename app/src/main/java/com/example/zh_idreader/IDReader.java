package com.example.zh_idreader;

import android.os.Build;
import android.util.Log;

import java.util.Arrays;

public class IDReader {
    static {
        if (Build.BRAND.toLowerCase().contains("renchuang")) {
            System.loadLibrary("RfidDevCtl_rc");

        } else {
            System.loadLibrary("RfidDevCtl");
        }
    }

    private static final String TAG = "jniNfcDev";
    private static final String NFC_PATH = "/dev/mcu_user_rfid";

    private  native int RfidOpenDevice();
    private  native int RfidCloseDevice();
    private  native int RfidReadEnable();
    private  native int RfidReadDisable();
    private  native int RfidSetKeyAndMode(int[] arr);
    private  native int RfidSetSecNumAndBlockNum(int[] arr);
    private  native int RfidReadCardIDSecAndBlock(int[] arr);

    // 打开RFID /dev/mcu_user_rfid ,返回值: 成功-0； 失败：-1
    public int openRFID(){
        int ret = -1;
        ret  = RfidOpenDevice();

        return ret;
    }

    // 关闭RFID /dev/mcu_user_rfid 返回值: 成功-0； 失败：-1
    public int closeRFID(){
        int ret = -1;
        ret = RfidCloseDevice();

        return ret;
    }

    // 使能读扇区功能 ,写成功-0； 写失败：-1
    public int Sector_read_enable()
    {
        int ret = -1;
        Log.e("dxx","Sector_read_enable");

        ret = RfidReadEnable();

        return ret;
    }

    // 关闭读扇区功能,写成功-0； 写失败：-1
    public int Sector_read_disable()
    {
        int ret = -1;
        Log.e("dxx","Sector_read_disenable");

        ret = RfidReadDisable();

        return ret;
    }

    // 设置读扇区的密钥和密钥模式,成功返回0；失败：返回-1
    public int Sector_set_key_and_mode(int[] arr)
    {
        int ret = -1;
        Log.e("dxx","Sector_set_key_and_mode");

        ret = RfidSetKeyAndMode(arr);

        return ret;
    }

    // 设置读取哪个扇区哪个块 成功： 返回0；失败：返回-1
    public int Sector_set_read_sectorNum_and_blockNum(int[] arr)
    {
        int ret= -1;
        Log.e("dxx","Sector_set_read_sectorNum_and_blockNum");

        ret = RfidSetSecNumAndBlockNum(arr);

        Log.d("dxx","arr:" + Arrays.toString(arr));

        return ret;
    }

    // 读RFID 数据（卡号或卡号+扇区内容）
    // 注意：1：该函数在JNI会阻塞等待内核NFC数据；有数据或出错才会返回
    // 2：  注意： 传递给JNI so库的数组长度最大不能超过32
    public int Read_rfid_data(int[] arr)
    {
        int ret= -1;
        Log.e("dxx","Read_rfid_data");

        ret = RfidReadCardIDSecAndBlock(arr);

        //Log.d("Read_rfid_data","arr:" + Arrays.toString(arr));

        return ret;
    }
}