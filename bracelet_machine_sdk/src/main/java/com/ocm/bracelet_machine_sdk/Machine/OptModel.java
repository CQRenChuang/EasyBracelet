package com.ocm.bracelet_machine_sdk.Machine;

import com.ocm.bracelet_machine_sdk.utils.StringHelper;
import com.ocm.bracelet_machine_sdk.utils.StringHelper;

import java.util.HashMap;

public class OptModel {
    public static final String DefaultPwdStr = "281474976710655";
    public static final String DefaultPwd = "FFFFFFFFFFFF";

    public String key="02";
    public String type = "01";
    public String block = "09";
    public String sectorContent = "";

//    public String getPwd() {
//        return pwd;
//    }
//
//    public void setPwd(String pwd) {
//        while(pwd.length()<12)pwd ="0"+pwd;
//        try{
//            this.pwd = StringHelper.convertPsw(Long.toHexString(Long.parseLong(pwd)));
//        }catch (Exception e){
//            e.printStackTrace();
//            this.pwd= DefaultPwdStr;
//        }
//    }

    public String pwd = DefaultPwd;
    public HashMap<String, String> optMap = new HashMap<>();

    public String Content(){
        String content =optMap.get(key);
        return content==null?"":content;
    }
}
