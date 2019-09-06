package com.example.camerafindlaserposition;

import android.util.Log;

/**
 * Myantenna激光模块
 */
public class MyantennaLaser implements Laser{
    private final static String TAG = "MyantennaLaser";

    public static final int LASER_OFFSET = 1;
    public static final int LASER_RANGE = 2;
    public static final int LASER_BAUNTE = 3;
    public static final int LASER_PROTOCOL = 4;
    public static final int LASER_FORMAT= 5;
    public static final int LASER_ADDRESS = 6;
    public static final int LASER_FREQUENCY = 7;
    public static final int LASER_AUTMEAS = 8;

    /** error code */
    public static final int LASER_SUCCESS = 0;
    public static final int LASER_ERROR_PROTOCOL_FUNCTION = 140;
    public static final int LASER_ERROR_PROTOCOL_VERIFICATION = 141;
    public static final int LASER_ERROR_PROTOCOL_PARAMETER = 142;
    public static final int LASER_ERROR_EXCESSIVE_TEMPERATURE= 252;
    public static final int LASER_ERROR_LOW_TEMPERATURE= 253;
    public static final int LASER_ERROR_WEAK_REFLEX= 255;
    public static final int LASER_ERROR_STRONG_REFLEX= 256;
    public static final int LASER_ERROR_OUTOF_RANGE= 258;
    public static final int LASER_ERROR_PHOTOSENSITIVE = 285;
    public static final int LASER_ERROR_TUBE = 286;
    public static final int LASER_ERROR_HARDWARE = 290;

    /** 偏移量,-10000-10000mm  */
   private int offset;

    /** 量程,500~80000mm */
    private int range;

    /** 波特率,9600/19200/38400/57600/115200 */
    private int baunte;

    /** 协议格式类型 */
    private int protocol;

    /** 输出距离格式 */
    private int distanceFormat;

    /** 从机地址 */
    private int address;

    /** 输出速率 仅快速测量有效*/
    private int frequency;

    /** 上电自动测量标识 */
    private int autmeas;

    private int statusFlag = 0;
    private int modeFlag = -1;

    public MyantennaLaser(){
        offset = 0;
        range = 40000;
        baunte = 38400;
        protocol = 1;
        distanceFormat = 0;
        address = 1;
        frequency = 30;
        autmeas = 0;
    }

    public String getconfig(){
        String configParameter = "offset:"+offset
                +"\r\nrange:"+range
                +"\r\nbaunte:"+baunte
                +"\r\nprotocol:"+protocol
                +"\r\ndistanceFormat:"+distanceFormat
                +"\r\naddress:"+address
                +"\r\nfrequency:"+frequency
                +"\r\nautmeas:"+autmeas;

        if(statusFlag == 0){
            configParameter += "\r\n激光已关闭";
        }else {
            configParameter += "\r\n激光已打开";
            if(modeFlag == 0){
                configParameter += "\r\n单次测量";
            }else if(modeFlag == 1){
                configParameter += "\r\n连续测量";
            }else if(modeFlag == 2){
                configParameter += "\r\n快速连续测量";
            }else if(modeFlag == 3){
                configParameter += "\r\n停止测量";
                statusFlag = 0; //发送此命令后激光会关闭
            }else {
                configParameter += "\r\n未开始测量";
            }
        }
        return configParameter;
    }

    public String getBaseInfo(){

        String info = "测量范围:0.05~40m\r\n"
                +"分辨率: 1mm\r\n"
                +"测量精度:±(1.5mm+D*5%%%)\r\n"
                +"数据输出率:连续测量(1~10hz)\r\n    快速测量:10/20/30hz\r\n"
                +"激光类型: 630~670nm,red, <1mw\r\n";

        return info;
    }

    public String getParamrter(int index){
        String command = "iGET:"+index+"\r\n";
        if(index == LASER_BAUNTE){
            Log.e(TAG, "can not support get baunte");
           // return command+" failed";
        }
        return command;
    }

    public String setParamrter(int index, int value){
        String command = "iSET:"+index+","+value+"\r\n";

        switch (index){
            case LASER_OFFSET:
                if(value > 10000 || value < -10000){
                    Log.e(TAG, "set parameter(offset) error");
                    break;
                }
            case LASER_RANGE:
                if(value > 80000 || value < 500){
                    Log.e(TAG, "set parameter(range) error");
                    break;
                }
            case LASER_BAUNTE:
                if(value != 9600 && value != 19200 && value !=38400
                        && value != 57600 && value != 115200){
                    Log.e(TAG, "set parameter(baunte) error, not support");
                    break;
                }
            case LASER_PROTOCOL:
                if(value != 0 && value != 1 && value != 2){
                    Log.e(TAG, "set parameter(protocol) error, not support");
                    break;
                }
            case LASER_FORMAT:
                if(value != 0 && value != 1){
                    Log.e(TAG, "set parameter(format) error, not support");
                    break;
                }
            case LASER_ADDRESS:
                if(value<1 || value > 247){
                    Log.e(TAG, "set parameter(address) error, not support");
                    break;
                }
            case LASER_FREQUENCY:
                if(value != 10 && value != 20 && value != 30){
                    Log.e(TAG, "set parameter(frequency) error, not support");
                    break;
                }
            case LASER_AUTMEAS:
                if(value != 0 && value != 1 && value != 2){
                    Log.e(TAG, "set parameter(autmeas) error, not support");
                    break;
                }
                default:break;
        }

        return command;
    }

    @Override
    public String singleMeasure(){
        modeFlag = 0;
        String command = "iSM\r\n";
        return command;
    }

    @Override
    public String continuousMeasure(){
        modeFlag = 1;
        String command = "iACM\r\n";
        return command;
    }

    public String continuousFastMeasure(){
        modeFlag = 2;
        String command = "iFACM\r\n";
        return command;
    }

    @Override
    public String stopMeasure(){
        modeFlag = 3;
        String command = "iHALT\r\n";
        return command;
    }

    @Override
    public String open(){
        statusFlag = 1;
        String command = "iLD:1\r\n";
        return command;
    }

    @Override
    public String close(){
        statusFlag = 0;
        String command = "iLD:0\r\n";
        return command;
    }

    @Override
    public String handleMessage(String string){
        String dealString = "";
       if(string.equals("OK\r\n")){
           dealString = "设置成功!";
       }else if(string.contains("OFFSET")){
           String value = string.substring(7,string.indexOf("\r\n"));
           dealString = "偏移量为:"+value;
       }else if(string.contains("RANGE")){
           String value = string.substring(6,string.indexOf("\r\n"));
           dealString = "量程为:"+value;
       }else if(string.contains("PROTOCOL")){
           String value = string.substring(9,string.indexOf("\r\n"));
           dealString = "协议为:"+value;
       }else if(string.contains("DATATYPE")){
           String value = string.substring(9,string.indexOf("\r\n"));
           dealString = "距离数字格式为:"+value;
       }else if(string.contains("ADDRESS")){
           String value = string.substring(8,string.indexOf("\r\n"));
           dealString = "从机地址为:"+value;
       }else if(string.contains("FREQUENCY")){
           String value = string.substring(10,string.indexOf("\r\n"));
           dealString = "输出速率为:"+value;
       }else if(string.contains("AUTMEAS")){
           String value = string.substring(8,string.indexOf("\r\n"));
           dealString = "上电自动测量标识为:"+value;
       }else if(string.contains("STOP")){
           //String value = string.substring(8,string.indexOf("\r\n"));
           dealString = "停止测量";
       }else if(string.contains("LASER OPEN")){
           //String value = string.substring(8,string.indexOf("\r\n"));
           dealString = "激光已开启";
       }else if(string.contains("LASER CLOSE")){
           //String value = string.substring(8,string.indexOf("\r\n"));
           dealString = "激光已关闭";
       }
        return dealString;
    }

    @Override
    public String errMessage(String str){
        String callbackMsg;
        callbackMsg = str.substring(2, str.indexOf("\r\n"));
        int errCode = Integer.parseInt(callbackMsg);
        //;
        switch(errCode){
            case LASER_SUCCESS:
                callbackMsg = "无错误";break;
            case LASER_ERROR_PROTOCOL_FUNCTION:
                callbackMsg = "CUSTOM HEX协议功能码错误";break;
            case LASER_ERROR_PROTOCOL_VERIFICATION:
                callbackMsg = "CUSTOM HEX协议校验错误";break;
            case LASER_ERROR_PROTOCOL_PARAMETER:
                callbackMsg = "CUSTOM HEX协议参数错误";break;
            case LASER_ERROR_EXCESSIVE_TEMPERATURE:
                callbackMsg = "温度过高";break;
            case LASER_ERROR_LOW_TEMPERATURE:
                callbackMsg = "温度过低";break;
            case LASER_ERROR_WEAK_REFLEX:
                callbackMsg = "弱反射或计算失败";break;
            case LASER_ERROR_STRONG_REFLEX:
                callbackMsg = "强反射";break;
            case LASER_ERROR_OUTOF_RANGE:
                callbackMsg = "超出量程";break;
            case LASER_ERROR_PHOTOSENSITIVE:
                callbackMsg = "光敏器件异常";break;
            case LASER_ERROR_TUBE:
                callbackMsg = "激光管异常";break;
            case LASER_ERROR_HARDWARE:
                callbackMsg = "硬件异常";break;
            default:
                break;
        }
        return callbackMsg;
    }
}
