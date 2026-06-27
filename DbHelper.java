package com.iso11820.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * SQLite 数据库操作封装类
 */
public class DbHelper {
    private static final Logger logger = LoggerFactory.getLogger(DbHelper.class);
    private final String connectionString;

    public DbHelper(String dbPath) {
        this.connectionString = "jdbc:sqlite:" + dbPath;
    }

    /**
     * 获取数据库连接
     */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(connectionString);
    }

    /**
     * 登录验证
     */
    public boolean login(String username, String pwd, String[] result) {
        try (Connection conn = getConnection()) {
            String sql = "SELECT userid, usertype FROM operators WHERE username = ? AND pwd = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, pwd);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                result[0] = rs.getString("userid");
                result[1] = rs.getString("usertype");
                return true;
            }
        } catch (SQLException e) {
            logger.error("登录查询失败", e);
        }
        return false;
    }

    /**
     * 查询设备信息
     */
    public List<Object[]> queryApparatus() {
        List<Object[]> list = new ArrayList<>();
        try (Connection conn = getConnection()) {
            String sql = "SELECT apparatusid, innernumber, apparatusname, checkdatef, checkdatet, constpower FROM apparatus";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                list.add(new Object[]{
                    rs.getInt("apparatusid"),
                    rs.getString("innernumber"),
                    rs.getString("apparatusname"),
                    rs.getString("checkdatef"),
                    rs.getString("checkdatet"),
                    rs.getObject("constpower")
                });
            }
        } catch (SQLException e) {
            logger.error("查询设备失败", e);
        }
        return list;
    }

    /**
     * 查询传感器配置
     */
    public List<Object[]> querySensors() {
        List<Object[]> list = new ArrayList<>();
        try (Connection conn = getConnection()) {
            String sql = "SELECT sensorid, sensorname, dispname, sensorgroup, unit, discription, flag, signalzero, signalspan, outputzero, outputspan, signaltype FROM sensors";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                list.add(new Object[]{
                    rs.getInt("sensorid"),
                    rs.getString("sensorname"),
                    rs.getString("dispname"),
                    rs.getString("sensorgroup"),
                    rs.getString("unit"),
                    rs.getString("discription"),
                    rs.getString("flag"),
                    rs.getDouble("signalzero"),
                    rs.getDouble("signalspan"),
                    rs.getDouble("outputzero"),
                    rs.getDouble("outputspan"),
                    rs.getInt("signaltype")
                });
            }
        } catch (SQLException e) {
            logger.error("查询传感器失败", e);
        }
        return list;
    }

    /**
     * 新建试验 - 初始插入
     */
    public boolean insertTest(String productId, String testId, String operator,
                              double preweight, double ambtemp, double ambhumi,
                              String apparatusId, String apparatusName, String apparatusChkDate) {
        try (Connection conn = getConnection()) {
            String sql = "INSERT INTO testmaster (" +
                "productid, testid, testdate, operator, ambtemp, ambhumi, " +
                "according, apparatusid, apparatusname, apparatuschkdate, rptno, " +
                "preweight, postweight, lostweight, lostweight_per, " +
                "totaltesttime, constpower, phenocode, flametime, flameduration, " +
                "maxtf1, maxtf2, maxts, maxtc, maxtf1_time, maxtf2_time, maxts_time, maxtc_time, " +
                "finaltf1, finaltf2, finalts, finaltc, finaltf1_time, finaltf2_time, finalts_time, finaltc_time, " +
                "deltatf1, deltatf2, deltatf, deltats, deltatc, memo, flag) " +
                "VALUES (?, ?, date('now'), ?, ?, ?, 'ISO 11820:2022', ?, ?, ?, ?, " +
                "?, 0, 0, 0, 0, 0, '', 0, 0, " +
                "0, 0, 0, 0, 0, 0, 0, 0, " +
                "0, 0, 0, 0, 0, 0, 0, 0, " +
                "0, 0, 0, 0, 0, NULL, NULL)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, productId);
            stmt.setString(2, testId);
            stmt.setString(3, operator);
            stmt.setDouble(4, ambtemp);
            stmt.setDouble(5, ambhumi);
            stmt.setString(6, apparatusId);
            stmt.setString(7, apparatusName);
            stmt.setString(8, apparatusChkDate);
            stmt.setString(9, productId); // rptno
            stmt.setDouble(10, preweight);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("插入试验记录失败", e);
        }
        return false;
    }

    /**
     * 更新试验结果 - 试验完成后保存
     */
    public boolean updateTestResult(String productId, String testId, double postweight,
                                    double lostWeight, double lostPer, double deltaTf, int totalTime,
                                    String phenocode, int flameTime, int flameDuration,
                                    double maxtf1, double maxtf2, double maxts, double maxtc,
                                    int maxtf1Time, int maxtf2Time, int maxtsTime, int maxtcTime,
                                    double finaltf1, double finaltf2, double finalts, double finaltc,
                                    int finaltf1Time, int finaltf2Time, int finaltsTime, int finaltcTime,
                                    double deltatf1, double deltatf2, double deltats, double deltatc,
                                    String memo) {
        try (Connection conn = getConnection()) {
            String sql = "UPDATE testmaster SET " +
                "postweight = ?, lostweight = ?, lostweight_per = ?, " +
                "deltatf = ?, totaltesttime = ?, phenocode = ?, flametime = ?, flameduration = ?, " +
                "maxtf1 = ?, maxtf2 = ?, maxts = ?, maxtc = ?, " +
                "maxtf1_time = ?, maxtf2_time = ?, maxts_time = ?, maxtc_time = ?, " +
                "finaltf1 = ?, finaltf2 = ?, finalts = ?, finaltc = ?, " +
                "finaltf1_time = ?, finaltf2_time = ?, finalts_time = ?, finaltc_time = ?, " +
                "deltatf1 = ?, deltatf2 = ?, deltats = ?, deltatc = ?, " +
                "memo = ?, flag = '10000000' " +
                "WHERE productid = ? AND testid = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setDouble(1, postweight);
            stmt.setDouble(2, lostWeight);  // 失重量 = 试验前质量 - 试验后质量
            stmt.setDouble(3, lostPer);
            stmt.setDouble(4, deltaTf);
            stmt.setInt(5, totalTime);
            stmt.setString(6, phenocode);
            stmt.setInt(7, flameTime);
            stmt.setInt(8, flameDuration);
            stmt.setDouble(9, maxtf1);
            stmt.setDouble(10, maxtf2);
            stmt.setDouble(11, maxts);
            stmt.setDouble(12, maxtc);
            stmt.setInt(13, maxtf1Time);
            stmt.setInt(14, maxtf2Time);
            stmt.setInt(15, maxtsTime);
            stmt.setInt(16, maxtcTime);
            stmt.setDouble(17, finaltf1);
            stmt.setDouble(18, finaltf2);
            stmt.setDouble(19, finalts);
            stmt.setDouble(20, finaltc);
            stmt.setInt(21, finaltf1Time);
            stmt.setInt(22, finaltf2Time);
            stmt.setInt(23, finaltsTime);
            stmt.setInt(24, finaltcTime);
            stmt.setDouble(25, deltatf1);
            stmt.setDouble(26, deltatf2);
            stmt.setDouble(27, deltats);
            stmt.setDouble(28, deltatc);
            stmt.setString(29, memo);
            stmt.setString(30, productId);
            stmt.setString(31, testId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("更新试验结果失败", e);
        }
        return false;
    }

    /**
     * 查询试验记录 — 支持日期、样品编号和操作员筛选
     */
    public List<Object[]> queryTests(String fromDate, String toDate, String productId, String operator) {
        List<Object[]> list = new ArrayList<>();
        try (Connection conn = getConnection()) {
            String sql = "SELECT testid, productid, testdate, operator, totaltesttime, flag, " +
                "preweight, postweight, lostweight_per, deltatf " +
                "FROM testmaster WHERE testdate BETWEEN ? AND ? " +
                "AND (? = '' OR productid LIKE '%' || ? || '%') " +
                "AND (? = '' OR operator = ?) ORDER BY testdate DESC";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, fromDate);
            stmt.setString(2, toDate);
            stmt.setString(3, productId);
            stmt.setString(4, productId);
            stmt.setString(5, operator);
            stmt.setString(6, operator);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(new Object[]{
                    rs.getString("testid"),
                    rs.getString("productid"),
                    rs.getString("testdate"),
                    rs.getString("operator"),
                    rs.getInt("totaltesttime"),
                    rs.getString("flag"),
                    rs.getDouble("preweight"),
                    rs.getDouble("postweight"),
                    rs.getDouble("lostweight_per"),
                    rs.getDouble("deltatf")
                });
            }
        } catch (SQLException e) {
            logger.error("查询试验记录失败", e);
        }
        return list;
    }

    /**
     * 查询试验详情
     */
    public Object[] queryTestDetail(String productId, String testId) {
        try (Connection conn = getConnection()) {
            String sql = "SELECT * FROM testmaster WHERE productid = ? AND testid = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, productId);
            stmt.setString(2, testId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Object[]{
                    rs.getString("productid"),
                    rs.getString("testid"),
                    rs.getString("testdate"),
                    rs.getString("operator"),
                    rs.getDouble("ambtemp"),
                    rs.getDouble("ambhumi"),
                    rs.getString("apparatusid"),
                    rs.getString("apparatusname"),
                    rs.getDouble("preweight"),
                    rs.getDouble("postweight"),
                    rs.getDouble("lostweight_per"),
                    rs.getDouble("deltatf"),
                    rs.getInt("totaltesttime"),
                    rs.getString("phenocode"),
                    rs.getInt("flametime"),
                    rs.getInt("flameduration"),
                    rs.getDouble("maxtf1"),
                    rs.getDouble("maxtf2"),
                    rs.getDouble("maxts"),
                    rs.getDouble("maxtc"),
                    rs.getDouble("finaltf1"),
                    rs.getDouble("finaltf2"),
                    rs.getDouble("finalts"),
                    rs.getDouble("finaltc"),
                    rs.getString("memo")
                };
            }
        } catch (SQLException e) {
            logger.error("查询试验详情失败", e);
        }
        return null;
    }

    /**
     * 查询是否存在未保存的试验
     */
    public boolean hasUnsavedTest(String productId, String testId) {
        try (Connection conn = getConnection()) {
            String sql = "SELECT totaltesttime, flag FROM testmaster WHERE productid = ? AND testid = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, productId);
            stmt.setString(2, testId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int totalTime = rs.getInt("totaltesttime");
                String flag = rs.getString("flag");
                return totalTime > 0 && !"10000000".equals(flag);
            }
        } catch (SQLException e) {
            logger.error("查询未保存试验失败", e);
        }
        return false;
    }

    /**
     * 插入样品信息
     */
    public boolean insertProduct(String productId, String productName, String specific,
                                 double diameter, double height) {
        try (Connection conn = getConnection()) {
            String sql = "INSERT INTO productmaster (productid, productname, specific, diameter, height, flag) " +
                "VALUES (?, ?, ?, ?, ?, NULL)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, productId);
            stmt.setString(2, productName);
            stmt.setString(3, specific);
            stmt.setDouble(4, diameter);
            stmt.setDouble(5, height);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("插入样品信息失败", e);
        }
        return false;
    }

    /**
     * 插入校准记录
     */
    public boolean insertCalibration(String id, String date, String type, int apparatusId,
                                     String operator, String tempData, String remarks) {
        try (Connection conn = getConnection()) {
            String sql = "INSERT INTO CalibrationRecords (Id, CalibrationDate, CalibrationType, " +
                "ApparatusId, Operator, TemperatureData, Remarks, PassedCriteria, CreatedAt) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, 1, datetime('now'))";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, id);
            stmt.setString(2, date);
            stmt.setString(3, type);
            stmt.setInt(4, apparatusId);
            stmt.setString(5, operator);
            stmt.setString(6, tempData);
            stmt.setString(7, remarks);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("插入校准记录失败", e);
        }
        return false;
    }

    /**
     * 查询校准历史
     */
    public List<Object[]> queryCalibrationHistory() {
        List<Object[]> list = new ArrayList<>();
        try (Connection conn = getConnection()) {
            String sql = "SELECT Id, CalibrationDate, CalibrationType, Operator, Remarks FROM CalibrationRecords ORDER BY CalibrationDate DESC";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                list.add(new Object[]{
                    rs.getString("Id"),
                    rs.getString("CalibrationDate"),
                    rs.getString("CalibrationType"),
                    rs.getString("Operator"),
                    rs.getString("Remarks")
                });
            }
        } catch (SQLException e) {
            logger.error("查询校准历史失败", e);
        }
        return list;
    }
}