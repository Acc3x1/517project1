package com.iso11820.model;

/**
 * 试验设备信息
 */
public class Apparatus {
    private int apparatusid;
    private String innernumber;
    private String apparatusname;
    private String checkdatef;
    private String checkdatet;
    private String pidport;
    private String powerport;
    private Integer constpower;

    public Apparatus() {}

    public int getApparatusid() { return apparatusid; }
    public void setApparatusid(int apparatusid) { this.apparatusid = apparatusid; }

    public String getInnernumber() { return innernumber; }
    public void setInnernumber(String innernumber) { this.innernumber = innernumber; }

    public String getApparatusname() { return apparatusname; }
    public void setApparatusname(String apparatusname) { this.apparatusname = apparatusname; }

    public String getCheckdatef() { return checkdatef; }
    public void setCheckdatef(String checkdatef) { this.checkdatef = checkdatef; }

    public String getCheckdatet() { return checkdatet; }
    public void setCheckdatet(String checkdatet) { this.checkdatet = checkdatet; }

    public String getPidport() { return pidport; }
    public void setPidport(String pidport) { this.pidport = pidport; }

    public String getPowerport() { return powerport; }
    public void setPowerport(String powerport) { this.powerport = powerport; }

    public Integer getConstpower() { return constpower; }
    public void setConstpower(Integer constpower) { this.constpower = constpower; }
}