package com.iso11820.config;

/**
 * 应用配置类 - 对应 appsettings.json
 */
public class AppConfig {
    private DatabaseConfig database;
    private HardwareConfig hardware;
    private SimulationConfig simulation;
    private FileStorageConfig fileStorage;
    private ReportConfig report;
    private CriteriaConfig criteria;

    public AppConfig() {}

    // Getters and Setters
    public DatabaseConfig getDatabase() { return database; }
    public void setDatabase(DatabaseConfig database) { this.database = database; }

    public HardwareConfig getHardware() { return hardware; }
    public void setHardware(HardwareConfig hardware) { this.hardware = hardware; }

    public SimulationConfig getSimulation() { return simulation; }
    public void setSimulation(SimulationConfig simulation) { this.simulation = simulation; }

    public FileStorageConfig getFileStorage() { return fileStorage; }
    public void setFileStorage(FileStorageConfig fileStorage) { this.fileStorage = fileStorage; }

    public ReportConfig getReport() { return report; }
    public void setReport(ReportConfig report) { this.report = report; }

    public CriteriaConfig getCriteria() { return criteria; }
    public void setCriteria(CriteriaConfig criteria) { this.criteria = criteria; }

    // 子配置类
    public static class DatabaseConfig {
        private String provider;
        private String sqlitePath;

        public String getProvider() { return provider; }
        public void setProvider(String provider) { this.provider = provider; }

        public String getSqlitePath() { return sqlitePath; }
        public void setSqlitePath(String sqlitePath) { this.sqlitePath = sqlitePath; }
    }

    public static class HardwareConfig {
        private int constPower;
        private int pidTemperature;
        private String sensorProtocol;

        public int getConstPower() { return constPower; }
        public void setConstPower(int constPower) { this.constPower = constPower; }

        public int getPidTemperature() { return pidTemperature; }
        public void setPidTemperature(int pidTemperature) { this.pidTemperature = pidTemperature; }

        public String getSensorProtocol() { return sensorProtocol; }
        public void setSensorProtocol(String sensorProtocol) { this.sensorProtocol = sensorProtocol; }
    }

    public static class SimulationConfig {
        private boolean enableSimulation;
        private boolean simulateSensors;
        private boolean simulatePidController;
        private double initialFurnaceTemp;
        private double targetFurnaceTemp;
        private double heatingRatePerSecond;
        private double tempFluctuation;
        private double stableThreshold;
        private boolean simulateFlame;

        public boolean isEnableSimulation() { return enableSimulation; }
        public void setEnableSimulation(boolean enableSimulation) { this.enableSimulation = enableSimulation; }

        public boolean isSimulateSensors() { return simulateSensors; }
        public void setSimulateSensors(boolean simulateSensors) { this.simulateSensors = simulateSensors; }

        public boolean isSimulatePidController() { return simulatePidController; }
        public void setSimulatePidController(boolean simulatePidController) { this.simulatePidController = simulatePidController; }

        public double getInitialFurnaceTemp() { return initialFurnaceTemp; }
        public void setInitialFurnaceTemp(double initialFurnaceTemp) { this.initialFurnaceTemp = initialFurnaceTemp; }

        public double getTargetFurnaceTemp() { return targetFurnaceTemp; }
        public void setTargetFurnaceTemp(double targetFurnaceTemp) { this.targetFurnaceTemp = targetFurnaceTemp; }

        public double getHeatingRatePerSecond() { return heatingRatePerSecond; }
        public void setHeatingRatePerSecond(double heatingRatePerSecond) { this.heatingRatePerSecond = heatingRatePerSecond; }

        public double getTempFluctuation() { return tempFluctuation; }
        public void setTempFluctuation(double tempFluctuation) { this.tempFluctuation = tempFluctuation; }

        public double getStableThreshold() { return stableThreshold; }
        public void setStableThreshold(double stableThreshold) { this.stableThreshold = stableThreshold; }

        public boolean isSimulateFlame() { return simulateFlame; }
        public void setSimulateFlame(boolean simulateFlame) { this.simulateFlame = simulateFlame; }
    }

    public static class FileStorageConfig {
        private String baseDirectory;
        private String testDataDirectory;

        public String getBaseDirectory() { return baseDirectory; }
        public void setBaseDirectory(String baseDirectory) { this.baseDirectory = baseDirectory; }

        public String getTestDataDirectory() { return testDataDirectory; }
        public void setTestDataDirectory(String testDataDirectory) { this.testDataDirectory = testDataDirectory; }
    }

    public static class ReportConfig {
        private String outputDirectory;
        private boolean enablePdfExport;

        public String getOutputDirectory() { return outputDirectory; }
        public void setOutputDirectory(String outputDirectory) { this.outputDirectory = outputDirectory; }

        public boolean isEnablePdfExport() { return enablePdfExport; }
        public void setEnablePdfExport(boolean enablePdfExport) { this.enablePdfExport = enablePdfExport; }
    }

    public static class CriteriaConfig {
        private double maxTemperatureDriftPerTenMinutes;
        private int targetDurationSeconds;
        private boolean standardMode;

        public double getMaxTemperatureDriftPerTenMinutes() { return maxTemperatureDriftPerTenMinutes; }
        public void setMaxTemperatureDriftPerTenMinutes(double maxTemperatureDriftPerTenMinutes) { this.maxTemperatureDriftPerTenMinutes = maxTemperatureDriftPerTenMinutes; }

        public int getTargetDurationSeconds() { return targetDurationSeconds; }
        public void setTargetDurationSeconds(int targetDurationSeconds) { this.targetDurationSeconds = targetDurationSeconds; }

        public boolean isStandardMode() { return standardMode; }
        public void setStandardMode(boolean standardMode) { this.standardMode = standardMode; }
    }
}