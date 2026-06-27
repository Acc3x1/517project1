package com.iso11820.model;

/**
 * 样品信息
 */
public class ProductMaster {
    private String productid;
    private String productname;
    private String specific;
    private double diameter;
    private double height;
    private String flag;

    public ProductMaster() {}

    public ProductMaster(String productid, String productname, String specific,
                         double diameter, double height) {
        this.productid = productid;
        this.productname = productname;
        this.specific = specific;
        this.diameter = diameter;
        this.height = height;
        this.flag = null;
    }

    public String getProductid() { return productid; }
    public void setProductid(String productid) { this.productid = productid; }

    public String getProductname() { return productname; }
    public void setProductname(String productname) { this.productname = productname; }

    public String getSpecific() { return specific; }
    public void setSpecific(String specific) { this.specific = specific; }

    public double getDiameter() { return diameter; }
    public void setDiameter(double diameter) { this.diameter = diameter; }

    public double getHeight() { return height; }
    public void setHeight(double height) { this.height = height; }

    public String getFlag() { return flag; }
    public void setFlag(String flag) { this.flag = flag; }
}