package com.example.camerafindlaserposition;

import android.graphics.Point;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.SubMenu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private final static String TAG = "MainActivity";
    private Button laserOpen;
    private Button laserSimpleMeas;
    private Button laserContinuousMeas;
    private Button laserQuicksMeas;
    private Button laserStopMeas;
    private TextView display;
    private TextView point;
    private TextView info;
    private Sc60Uart mUart;
    private Laser laser;
    private boolean isflag = false;
    private LaserPositonBuilder laserPosition;
    private Point pixelPoint;

    public Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            String str = (String)msg.obj;
            switch (msg.what){
                case 1:
                    display.setText(str);
                    point.setText("x:" + pixelPoint.x + " y:" + pixelPoint.y);
                    break;
                 case 2:
                    info.setText(str);
                    break;
            }
        }
    };

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Example of a call to a native method
        mUart = new Sc60Uart("/dev/ttyHSL1",
                                            38400);
        laserPosition = new LaserPositonBuilder();
        laserPosition.test(); //测试，自定义了参数

        pixelPoint = new Point();

        LaserFactory laserFactory = new LaserFactory();
        laser = laserFactory.getLaserInstance("myantenna");
        isflag =true;

        initView();
        UartReceiceThread thread = new UartReceiceThread();
        Thread receiveUart = new Thread(thread);
        receiveUart.start();
    }

    private void initView(){
        laserOpen = findViewById(R.id.laser_open);
        laserSimpleMeas = findViewById(R.id.single_meas);
        laserContinuousMeas = findViewById(R.id.continuous_meas);
        laserQuicksMeas = findViewById(R.id.quick_meas);
        laserStopMeas = findViewById(R.id.stop_meas);
        display = findViewById(R.id.display);
        info = findViewById(R.id.laser_info);
        point = findViewById(R.id.point);

        MyOnClick click = new MyOnClick();
        laserOpen.setOnClickListener(click);
        laserSimpleMeas.setOnClickListener(click);
        laserContinuousMeas.setOnClickListener(click);
        laserQuicksMeas.setOnClickListener(click);
        laserStopMeas.setOnClickListener(click);
    }

    public class MyOnClick implements View.OnClickListener{

        String command;
        @Override
        public void onClick(View v){
            switch (v.getId()){
                case R.id.laser_open:
                    if(laserOpen.getText().equals("打开激光")){
                        laserOpen.setText("关闭激光");
                        command = laser.open();
                    }else{
                        laserOpen.setText("打开激光");
                        command = laser.close();
                    }
                    break;
                case R.id.single_meas:
                    command = laser.singleMeasure();break;
                case R.id.continuous_meas:
                    command = laser.continuousMeasure();break;
                case R.id.quick_meas:
                    command = laser.continuousFastMeasure();break;
                case R.id.stop_meas:
                    command = laser.stopMeasure();break;
            }
            Log.e(TAG, "command:"+command);
            mUart.write(command.getBytes(), command.length());
        }
    }

    public class UartReceiceThread implements Runnable{
        String delStr = "";
        @Override
        public void run(){
            byte[] buffer = new byte[1024];
            while(isflag){
                String mByte = mUart.read(buffer,buffer.length);
                Log.e(TAG, "uart receive:"+mByte);

                if(mByte.toCharArray()[0]=='D'){
                    int index = mByte.lastIndexOf("m");
                    delStr = mByte.substring(2,index);
                    pixelPoint = laserPosition.getCurrentDistancePosition(Float.parseFloat(delStr));

                    Message msg = mHandler.obtainMessage(1, delStr);
                    mHandler.sendMessage(msg);
                }else if(mByte.toCharArray()[0]=='E') {
                    delStr = laser.errMessage(mByte);

                    Message msg = mHandler.obtainMessage(1, delStr);
                    mHandler.sendMessage(msg);
                }else{
                    delStr = laser.handleMessage(mByte);

                    Message msg = mHandler.obtainMessage(2, delStr);
                    mHandler.sendMessage(msg);
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = new MenuInflater(this);
        inflater.inflate(R.menu.menu, menu);

        /*
        SubMenu fontMenu = menu.addSubMenu("设置");
        SubMenu baunte = fontMenu.addSubMenu("波特率");
        baunte.setGroupCheckable(0, true, false);
        baunte.setHeaderTitle("选择波特率");
        baunte.add(0,0x111, 0, "9600");
        baunte.add(0,0x112, 0, "19200");
        baunte.add(0,0x113, 0, "38400");
        baunte.add(0,0x114, 0, "57600");
        baunte.add(0,0x115, 0, "115200");

        SubMenu protocol = fontMenu.addSubMenu("协议格式");
        protocol.setGroupCheckable(0, true, true);
        protocol.setHeaderTitle("选择协议格式");
        protocol.add(0,0x200, 0, "MODBUS RTU");
        protocol.add(0,0x201, 0, "ASCLL");
        protocol.add(0,0x202, 0, "HEX");

        SubMenu format = fontMenu.addSubMenu("距离数字格式");
        format.setGroupCheckable(0, true, true);
        format.setHeaderTitle("选择距离数字格式");
        format.add(0,0x300, 0, "三位小数");
        format.add(0,0x301, 0, "四位小数");
       // range.add(0,0x203, 0, "20m");

        SubMenu frequency = fontMenu.addSubMenu("输出速率");
        frequency.setGroupCheckable(0, true, true);
        frequency.setHeaderTitle("选择输出速率");
        frequency.add(0,0x400, 0, "10HZ");
        frequency.add(0,0x401, 0, "20HZ");
        frequency.add(0,0x402, 0, "30HZ");

        SubMenu mark = fontMenu.addSubMenu("上电自动测量标识");
        mark.setGroupCheckable(0, true, true);
        mark.add(0,0x500, 0, "测量无效");
        mark.add(0,0x501, 0, "连续测量");
        mark.add(0,0x501, 0, "快速连续测量");

        menu.add(0, 0x11b, 0, "普通菜单项");
        SubMenu colorMenu = menu.addSubMenu("字体颜色");
        colorMenu.setHeaderTitle("选择字体颜色");
        colorMenu.add(0,0x116, 0, "红色");
        colorMenu.add(0,0x117, 0, "绿色");
        colorMenu.add(0,0x118, 0, "蓝色");*/
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}
