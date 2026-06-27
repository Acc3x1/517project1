package com.iso11820.model;

/**
 * 传感器通道配置
 */
public class Sensor {
    private int sensorid;
    private String sensorname;
    private String dispname;
    private String sensorgroup;
    private String unit;
    private String discription;
    private String flag;
    private double signalzero;
    private double signalspan;
    private double outputzero;
    private double outputspan;
    private double outputvalue;
    private double inputvalue;
    private int signaltype;

    public Sensor() {}

    // Getters and Setters
    public int getSensorid() { return sensorid; }
    public void setSensorid(int sensorid) { this.sensorid = sensorid; }

    public String getSensorname() { return sensorname; }
    public void setSensorname(String sensorname) { this.sensorname = sensorname; }

    public String getDispname() { return dispname; }
    public void setDispname(String dispname) { this.dispname = dispname; }

    public String getSensorgroup() { return sensorgroup; }
    public void setSensorgroup(String sensorgroup) { this.sensorgroup = sensorgroup; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public String getDiscription() { return discription; }
    public void setDiscription(String discription) { this.discription = discription; }

    public String getFlag() { return flag; }
    public void setFlag(String flag) { this.flag = flag; }

    public double getSignalzero() { return signalzero; }
    public void setSignalzero(double signalzero) { this.signalzero = signalzero; }

    public double getSignalspan() { return signalspan; }
    public void setSignalspan(double signalspan) { this.signalspan = signalspan; }

    public double getOutputzero() { return outputzero; }
    public void setOutputzero(double outputzero) { this.outputzero = outputzero; }

    public double getOutputspan() { return outputspan; }
    public void setOutputspan(double outputspan) { this.outputspan = outputspan; }

    public double getOutputvalue() { return outputvalue; }
    public void setOutputvalue(double outputvalue) { this.outputvalue = outputvalue; }

    public double getInputvalue() { return inputvalue; }
    public void setInputvalue(double inputvalue) { this.inputvalue = inputvalue; }

    public int getSignaltype() { return signaltype; }
    public void setSignaltype(int signaltype) { this.signaltype = signaltype; }
}