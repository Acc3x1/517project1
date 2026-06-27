package com.iso11820.model;

/**
 * 校准记录
 */
public class CalibrationRecord {
    private String id;
    private String calibrationDate;
    private String calibrationType;
    private int apparatusId;
    private String operator;
    private String temperatureData;
    private Double uniformityResult;
    private Double maxDeviation;
    private Double averageTemperature;
    private int passedCriteria;
    private String remarks;
    private String createdAt;

    // 炉壁9测温点
    private Double tempA1, tempA2, tempA3;
    private Double tempB1, tempB2, tempB3;
    private Double tempC1, tempC2, tempC3;

    // 计算结果
    private Double tAvg;
    private Double tAvgAxis1, tAvgAxis2, tAvgAxis3;
    private Double tAvgLevela, tAvgLevelb, tAvgLevelc;
    private Double tDevAxis1, tDevAxis2, tDevAxis3;
    private Double tDevLevela, tDevLevelb, tDevLevelc;
    private Double tAvgDevAxis, tAvgDevLevel;

    private String centerTempData;
    private String memo;

    public CalibrationRecord() {}

    // Getters and Setters (简化版，关键字段)
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCalibrationDate() { return calibrationDate; }
    public void setCalibrationDate(String calibrationDate) { this.calibrationDate = calibrationDate; }

    public String getCalibrationType() { return calibrationType; }
    public void setCalibrationType(String calibrationType) { this.calibrationType = calibrationType; }

    public int getApparatusId() { return apparatusId; }
    public void setApparatusId(int apparatusId) { this.apparatusId = apparatusId; }

    public String getOperator() { return operator; }
    public void setOperator(String operator) { this.operator = operator; }

    public String getTemperatureData() { return temperatureData; }
    public void setTemperatureData(String temperatureData) { this.temperatureData = temperatureData; }

    public Double getUniformityResult() { return uniformityResult; }
    public void setUniformityResult(Double uniformityResult) { this.uniformityResult = uniformityResult; }

    public Double getMaxDeviation() { return maxDeviation; }
    public void setMaxDeviation(Double maxDeviation) { this.maxDeviation = maxDeviation; }

    public Double getAverageTemperature() { return averageTemperature; }
    public void setAverageTemperature(Double averageTemperature) { this.averageTemperature = averageTemperature; }

    public int getPassedCriteria() { return passedCriteria; }
    public void setPassedCriteria(int passedCriteria) { this.passedCriteria = passedCriteria; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getMemo() { return memo; }
    public void setMemo(String memo) { this.memo = memo; }
}