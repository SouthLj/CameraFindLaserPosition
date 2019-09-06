package com.example.camerafindlaserposition;

import android.graphics.Point;
import java.util.HashMap;

public class LaserPositonBuilder {

    private final static String TAG = "LaserPositonBuilder";

    private int laserToCaremaX;//激光距离摄像头x方向距离(mm)
    private int laserToCaremaY;//激光距离摄像头y方向距离(mm)

    private float targetDistance;//目标距离
    private float caremaFocal;  //摄像头焦距

    private int horizontalViewAngle;//水平视场角
    private int verticalViewAngle;//垂直视场角

    private int pixelWidth;//像素宽度
    private int pixelHeight;//像素高度

    private int screenWidth;//显示画面宽度
    private int screenHeight;//显示画面高度

    private float minDistance;//测距最小距离

    private int entryDirection;//激光进入摄像头方向（相对与摄像的位置）

    public class caremaViewQuadrant{
        public  static final int RIGHTUPPER = 0;
        public  static final int LEFTUPPER = 1;
        public  static final int LEFTLOWER = 3;
        public  static final int RIGHTLOWER = 4;
    }

    LaserPositonBuilder(){
        laserToCaremaX = 0;
        laserToCaremaY = 0;
        caremaFocal = 0;
        horizontalViewAngle = 0;
        verticalViewAngle = 0;
        screenWidth = 0;
        screenHeight = 0;
        entryDirection = caremaViewQuadrant.RIGHTUPPER;
    }

    public HashMap<String, Object> getDevicesInfomation(){
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("laserToCaremaX", laserToCaremaX);
        map.put("laserToCaremaY", laserToCaremaY);
        map.put("caremaFocal", caremaFocal);
        map.put("horizontalViewAngle", horizontalViewAngle);
        map.put("verticalViewAngle", verticalViewAngle);
        map.put("pixelWidth", pixelWidth);
        map.put("pixelHeight", pixelHeight);
        map.put("screenWidth", screenWidth);
        map.put("screenHeight", screenHeight);

        switch (entryDirection){
            case caremaViewQuadrant.RIGHTUPPER:
                map.put("entryDirection", "upright");break;
            case caremaViewQuadrant.LEFTUPPER:
                map.put("entryDirection", "upleft");break;
            case caremaViewQuadrant.LEFTLOWER:
                map.put("entryDirection", "downleft");break;
            case caremaViewQuadrant.RIGHTLOWER:
                map.put("entryDirection", "downright");break;
        }

        return map;
    }

    //得到最小距离
    /*
    float getMinDistance(){

        if((laserToCaremaY/laserToCaremaX)/((screenHeight/2)/(screenWidth/2)))
            minDistance = laserToCaremaX/qTan(M_PI/180 * (horizontalViewAngle/2))-caremaFocal;
        else
            minDistance = laserToCaremaY /qTan(M_PI/180 * (verticalViewAngle/2))-caremaFocal;

        return minDistance;
        return 0.0f;
    }
*/

    /**
     * 模拟如下结构用于测试
     */
    public void test(){
        laserToCaremaX = 30;
        laserToCaremaY = 30;
        caremaFocal = 1;
        horizontalViewAngle = 60;
        verticalViewAngle = 60;
        screenWidth = 800;
        screenHeight = 600;
        entryDirection = caremaViewQuadrant.RIGHTUPPER;
    }
    //得到激光在图像中的坐标
    Point getCurrentDistancePosition(float distance){

        int x = 0, y = 0;
        double proportion_x, proportion_y;
        Point pos = new Point();

        // 激光从右上进入，且摄像头据激光的角度小于屏幕高宽的角度时，proportion_x=1，激光正好进入摄像头画面
        proportion_x = laserToCaremaX / ((distance*1000 + caremaFocal)*Math.tan(Math.PI * (horizontalViewAngle/2)/180));//点在x方向距离与实际物体在x方向的比例
        proportion_y = laserToCaremaY / ((distance*1000 + caremaFocal)*Math.tan(Math.PI * (verticalViewAngle/2)/180));//点在y方向距离与实际物体在y方向的比例

        switch (entryDirection)
        {
            case caremaViewQuadrant.RIGHTUPPER:
                x = (int)((1+proportion_x) * (screenWidth/2));//点在感光片上的x坐标
                y = (int)((1+proportion_y) * (screenHeight/2));//点在感光片上的y坐标
                break;
            case caremaViewQuadrant.LEFTUPPER:
                x = (int)((1-proportion_x) * (screenWidth/2));//点在感光片上的x坐标
                y = (int)((1+proportion_y) * (screenHeight/2));//点在感光片上的y坐标
                break;
            case caremaViewQuadrant.LEFTLOWER:
                x = (int)((1-proportion_x) * (screenWidth/2));//点在感光片上的x坐标
                y = (int)((1-proportion_y) * (screenHeight/2));//点在感光片上的y坐标
                break;
            case caremaViewQuadrant.RIGHTLOWER:
                x = (int)((1+proportion_x) * (screenWidth/2));//点在感光片上的x坐标
                y = (int)((1-proportion_y) * (screenHeight/2));//点在感光片上的y坐标
                break;
            default:
                break;
        }

        pos.x = x;
        pos.y = y;

        return pos;
    }

}
