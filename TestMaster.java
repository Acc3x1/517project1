package com.iso11820.model;

/**
 * 试验记录 - 核心表
 */
public class TestMaster {
    // 基本信息
    private String productid;
    private String testid;
    private String testdate;
    private double ambtemp;
    private double ambhumi;
    private String according;
    private String operator;
    private String apparatusid;
    private String apparatusname;
    private String apparatuschkdate;
    private String rptno;

    // 质量数据
    private double preweight;
    private double postweight;
    private double lostweight;
    private double lostweightPer;

    // 试验过程
    private int totaltesttime;
    private int constpower;
    private String phenocode;
    private int flametime;
    private int flameduration;

    // 各通道温度最大值
    private double maxtf1;
    private double maxtf2;
    private double maxts;
    private double maxtc;
    private int maxtf1Time;
    private int maxtf2Time;
    private int maxtsTime;
    private int maxtcTime;

    // 各通道温度最终值
    private double finaltf1;
    private double finaltf2;
    private double finalts;
    private double finaltc;
    private int finaltf1Time;
    private int finaltf2Time;
    private int finaltsTime;
    private int finaltcTime;

    // 温升
    private double deltatf1;
    private double deltatf2;
    private double deltatf;
    private double deltats;
    private double deltatc;

    // 备注
    private String memo;
    private String flag;

    public TestMaster() {}

    // Getters and Setters
    public String getProductid() { return productid; }
    public void setProductid(String productid) { this.productid = productid; }

    public String getTestid() { return testid; }
    public void setTestid(String testid) { this.testid = testid; }

    public String getTestdate() { return testdate; }
    public void setTestdate(String testdate) { this.testdate = testdate; }

    public double getAmbtemp() { return ambtemp; }
    public void setAmbtemp(double ambtemp) { this.ambtemp = ambtemp; }

    public double getAmbhumi() { return ambhumi; }
    public void setAmbhumi(double ambhumi) { this.ambhumi = ambhumi; }

    public String getAccording() { return according; }
    public void setAccording(String according) { this.according = according; }

    public String getOperator() { return operator; }
    public void setOperator(String operator) { this.operator = operator; }

    public String getApparatusid() { return apparatusid; }
    public void setApparatusid(String apparatusid) { this.apparatusid = apparatusid; }

    public String getApparatusname() { return apparatusname; }
    public void setApparatusname(String apparatusname) { this.apparatusname = apparatusname; }

    public String getApparatuschkdate() { return apparatuschkdate; }
    public void setApparatuschkdate(String apparatuschkdate) { this.apparatuschkdate = apparatuschkdate; }

    public String getRptno() { return rptno; }
    public void setRptno(String rptno) { this.rptno = rptno; }

    public double getPreweight() { return preweight; }
    public void setPreweight(double preweight) { this.preweight = preweight; }

    public double getPostweight() { return postweight; }
    public void setPostweight(double postweight) { this.postweight = postweight; }

    public double getLostweight() { return lostweight; }
    public void setLostweight(double lostweight) { this.lostweight = lostweight; }

    public double getLostweightPer() { return lostweightPer; }
    public void setLostweightPer(double lostweightPer) { this.lostweightPer = lostweightPer; }

    public int getTotaltesttime() { return totaltesttime; }
    public void setTotaltesttime(int totaltesttime) { this.totaltesttime = totaltesttime; }

    public int getConstpower() { return constpower; }
    public void setConstpower(int constpower) { this.constpower = constpower; }

    public String getPhenocode() { return phenocode; }
    public void setPhenocode(String phenocode) { this.phenocode = phenocode; }

    public int getFlametime() { return flametime; }
    public void setFlametime(int flametime) { this.flametime = flametime; }

    public int getFlameduration() { return flameduration; }
    public void setFlameduration(int flameduration) { this.flameduration = flameduration; }

    public double getMaxtf1() { return maxtf1; }
    public void setMaxtf1(double maxtf1) { this.maxtf1 = maxtf1; }

    public double getMaxtf2() { return maxtf2; }
    public void setMaxtf2(double maxtf2) { this.maxtf2 = maxtf2; }

    public double getMaxts() { return maxts; }
    public void setMaxts(double maxts) { this.maxts = maxts; }

    public double getMaxtc() { return maxtc; }
    public void setMaxtc(double maxtc) { this.maxtc = maxtc; }

    public int getMaxtf1Time() { return maxtf1Time; }
    public void setMaxtf1Time(int maxtf1Time) { this.maxtf1Time = maxtf1Time; }

    public int getMaxtf2Time() { return maxtf2Time; }
    public void setMaxtf2Time(int maxtf2Time) { this.maxtf2Time = maxtf2Time; }

    public int getMaxtsTime() { return maxtsTime; }
    public void setMaxtsTime(int maxtsTime) { this.maxtsTime = maxtsTime; }

    public int getMaxtcTime() { return maxtcTime; }
    public void setMaxtcTime(int maxtcTime) { this.maxtcTime = maxtcTime; }

    public double getFinaltf1() { return finaltf1; }
    public void setFinaltf1(double finaltf1) { this.finaltf1 = finaltf1; }

    public double getFinaltf2() { return finaltf2; }
    public void setFinaltf2(double finaltf2) { this.finaltf2 = finaltf2; }

    public double getFinalts() { return finalts; }
    public void setFinalts(double finalts) { this.finalts = finalts; }

    public double getFinaltc() { return finaltc; }
    public void setFinaltc(double finaltc) { this.finaltc = finaltc; }

    public int getFinaltf1Time() { return finaltf1Time; }
    public void setFinaltf1Time(int finaltf1Time) { this.finaltf1Time = finaltf1Time; }

    public int getFinaltf2Time() { return finaltf2Time; }
    public void setFinaltf2Time(int finaltf2Time) { this.finaltf2Time = finaltf2Time; }

    public int getFinaltsTime() { return finaltsTime; }
    public void setFinaltsTime(int finaltsTime) { this.finaltsTime = finaltsTime; }

    public int getFinaltcTime() { return finaltcTime; }
    public void setFinaltcTime(int finaltcTime) { this.finaltcTime = finaltcTime; }

    public double getDeltatf1() { return deltatf1; }
    public void setDeltatf1(double deltatf1) { this.deltatf1 = deltatf1; }

    public double getDeltatf2() { return deltatf2; }
    public void setDeltatf2(double deltatf2) { this.deltatf2 = deltatf2; }

    public double getDeltatf() { return deltatf; }
    public void setDeltatf(double deltatf) { this.deltatf = deltatf; }

    public double getDeltats() { return deltats; }
    public void setDeltats(double deltats) { this.deltats = deltats; }

    public double getDeltatc() { return deltatc; }
    public void setDeltatc(double deltatc) { this.deltatc = deltatc; }

    public String getMemo() { return memo; }
    public void setMemo(String memo) { this.memo = memo; }

    public String getFlag() { return flag; }
    public void setFlag(String flag) { this.flag = flag; }

    /**
     * 判断试验是否已保存完成
     */
    public boolean isSaved() {
        return "10000000".equals(flag);
    }
}