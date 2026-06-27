package com.iso11820;

import com.iso11820.config.AppConfig;
import com.iso11820.config.ConfigLoader;
import com.iso11820.db.DbHelper;
import com.iso11820.db.DatabaseInitializer;
import com.iso11820.model.Operator;
import com.iso11820.model.Apparatus;
import com.iso11820.model.TestMaster;
import com.iso11820.model.Sensor;
import com.iso11820.model.TemperatureData;
import com.iso11820.model.MasterMessage;
import com.iso11820.service.SimulationEngine;
import com.iso11820.service.DaqWorker;
import com.iso11820.service.ExportService;
import com.iso11820.controller.TestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 全局上下文 - 单例模式，持有所有核心对象
 */
public class GlobalContext {
    private static final Logger logger = LoggerFactory.getLogger(GlobalContext.class);
    private static GlobalContext instance;

    // 配置
    private AppConfig config;

    // 数据库
    private DbHelper dbHelper;

    // 当前用户
    private Operator currentOperator;

    // 当前设备
    private Apparatus currentApparatus;

    // 传感器列表
    private Map<Integer, Sensor> sensors;

    // 当前试验
    private TestMaster currentTest;

    // 温度数据缓存 (记录阶段使用)
    private List<TemperatureData> temperatureDataList;

    // 系统消息
    private List<MasterMessage> messages;

    // 仿真引擎
    private SimulationEngine simulationEngine;

    // 数据采集服务
    private DaqWorker daqWorker;

    // 试验控制器
    private TestController testController;

    // 导出服务
    private ExportService exportService;

    // 私有构造器
    private GlobalContext() {
        temperatureDataList = new CopyOnWriteArrayList<>();
        messages = new CopyOnWriteArrayList<>();
        sensors = new HashMap<>();
    }

    /**
     * 获取单例实例
     */
    public static GlobalContext getInstance() {
        if (instance == null) {
            instance = new GlobalContext();
        }
        return instance;
    }

    /**
     * 初始化全局上下文
     */
    public void initialize() {
        // 加载配置
        config = ConfigLoader.load();

        // 初始化数据库
        String dbPath = config.getFileStorage().getBaseDirectory() + "/" + config.getDatabase().getSqlitePath();
        DatabaseInitializer initializer = new DatabaseInitializer(dbPath);
        initializer.initialize();

        // 创建 DbHelper
        dbHelper = new DbHelper(dbPath);

        // 加载传感器配置
        loadSensors();

        // 加载设备信息
        loadApparatus();

        // 创建仿真引擎
        simulationEngine = new SimulationEngine(config.getSimulation());

        // 创建试验控制器
        testController = new TestController(this);

        // 创建数据采集服务
        daqWorker = new DaqWorker(this, 800); // 800ms 间隔

        // 创建导出服务
        exportService = new ExportService(config);

        logger.info("全局上下文初始化完成");
    }

    /**
     * 加载传感器配置
     */
    private void loadSensors() {
        List<Object[]> sensorList = dbHelper.querySensors();
        for (Object[] row : sensorList) {
            Sensor sensor = new Sensor();
            sensor.setSensorid((Integer) row[0]);
            sensor.setSensorname((String) row[1]);
            sensor.setDispname((String) row[2]);
            sensor.setSensorgroup((String) row[3]);
            sensor.setUnit((String) row[4]);
            sensor.setDiscription((String) row[5]);
            sensor.setFlag((String) row[6]);
            sensor.setSignalzero((Double) row[7]);
            sensor.setSignalspan((Double) row[8]);
            sensor.setOutputzero((Double) row[9]);
            sensor.setOutputspan((Double) row[10]);
            sensor.setSignaltype((Integer) row[11]);
            sensors.put(sensor.getSensorid(), sensor);
        }
        logger.info("加载 {} 个传感器配置", sensors.size());
    }

    /**
     * 加载设备信息
     */
    private void loadApparatus() {
        List<Object[]> apparatusList = dbHelper.queryApparatus();
        if (!apparatusList.isEmpty()) {
            Object[] row = apparatusList.get(0);
            currentApparatus = new Apparatus();
            currentApparatus.setApparatusid((Integer) row[0]);
            currentApparatus.setInnernumber((String) row[1]);
            currentApparatus.setApparatusname((String) row[2]);
            currentApparatus.setCheckdatef((String) row[3]);
            currentApparatus.setCheckdatet((String) row[4]);
            currentApparatus.setConstpower((Integer) row[5]);
        }
        logger.info("加载设备信息: {}", currentApparatus != null ? currentApparatus.getApparatusname() : "无");
    }

    // Getters
    public AppConfig getConfig() { return config; }
    public DbHelper getDbHelper() { return dbHelper; }
    public Operator getCurrentOperator() { return currentOperator; }
    public Apparatus getCurrentApparatus() { return currentApparatus; }
    public Map<Integer, Sensor> getSensors() { return sensors; }
    public TestMaster getCurrentTest() { return currentTest; }
    public List<TemperatureData> getTemperatureDataList() { return temperatureDataList; }
    public List<MasterMessage> getMessages() { return messages; }
    public SimulationEngine getSimulationEngine() { return simulationEngine; }
    public DaqWorker getDaqWorker() { return daqWorker; }
    public TestController getTestController() { return testController; }
    public ExportService getExportService() { return exportService; }

    // Setters
    public void setCurrentOperator(Operator operator) { this.currentOperator = operator; }
    public void setCurrentTest(TestMaster test) { this.currentTest = test; }

    /**
     * 清空温度数据
     */
    public void clearTemperatureData() {
        temperatureDataList.clear();
    }

    /**
     * 清空消息
     */
    public void clearMessages() {
        messages.clear();
    }

    /**
     * 添加消息
     */
    public void addMessage(String time, String message) {
        messages.add(new MasterMessage(time, message));
    }

    /**
     * 添加温度数据
     */
    public void addTemperatureData(TemperatureData data) {
        temperatureDataList.add(data);
    }
}