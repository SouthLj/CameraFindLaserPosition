package com.example.camerafindlaserposition;

public class LaserFactory {
/*
    private String laserName;

    public LaserFactory(String laserName){
        this.laserName = laserName;
    }
*/
    public  Laser getLaserInstance(String laserName){
        Laser instance = null;
        if(laserName.contains("myantenna")){
            instance = new MyantennaLaser();
        }else if(laserName.contains("south")){

        }

        return instance;
    }
}
