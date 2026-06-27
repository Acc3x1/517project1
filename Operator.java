package com.iso11820.model;

/**
 * 操作员/用户账号
 */
public class Operator {
    private String userid;
    private String username;
    private String pwd;
    private String usertype;

    public Operator() {}

    public Operator(String userid, String username, String pwd, String usertype) {
        this.userid = userid;
        this.username = username;
        this.pwd = pwd;
        this.usertype = usertype;
    }

    public String getUserid() { return userid; }
    public void setUserid(String userid) { this.userid = userid; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPwd() { return pwd; }
    public void setPwd(String pwd) { this.pwd = pwd; }

    public String getUsertype() { return usertype; }
    public void setUsertype(String usertype) { this.usertype = usertype; }
}