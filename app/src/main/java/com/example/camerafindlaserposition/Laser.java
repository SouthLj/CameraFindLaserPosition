package com.example.camerafindlaserposition;

public interface Laser {
    public String singleMeasure();
    public String continuousMeasure();
    public String open();
    public String close();
    public String stopMeasure();
    public String continuousFastMeasure();
    public String errMessage(String string);
    public String handleMessage(String string);
}
