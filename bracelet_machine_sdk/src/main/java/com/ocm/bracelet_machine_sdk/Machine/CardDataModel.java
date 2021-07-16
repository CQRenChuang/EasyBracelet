package com.ocm.bracelet_machine_sdk.Machine;

public class CardDataModel {
    public String CardNo;      //卡号
    public String cardNoHex;      //卡号
    public String CabinetNos; //柜号
    public boolean UsbCabine = false;
    public byte[] DataBuff; //柜号
    public byte[] sectorBuff;
    public CardDataModel(String CardNo, String CabinetNos, boolean use, byte[] sectorBuff){
        this.CardNo = CardNo;
        this.CabinetNos = CabinetNos;
        this.UsbCabine = use;
        this.sectorBuff = sectorBuff;
    }
    public CardDataModel(String CardNo, String CabinetNos, boolean use){
        this.CardNo = CardNo;
        this.CabinetNos = CabinetNos;
        this.UsbCabine = use;
    }
    public CardDataModel(String CardNo, String CabinetNos){
        this.CardNo = CardNo;
        this.CabinetNos = CabinetNos;
        this.UsbCabine = false;
    }

    public CardDataModel(String CardNo, byte[] buff){
        this.CardNo = CardNo;
        this.DataBuff = buff;
    }
}
