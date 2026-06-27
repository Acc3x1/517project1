package com.iso11820.util;

import com.iso11820.model.TemperatureData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * CSV 文件写入工具
 */
public class CsvWriter {
    private static final Logger logger = LoggerFactory.getLogger(CsvWriter.class);

    /**
     * 写入温度数据CSV文件
     */
    public static void writeTemperatureData(String filePath, List<TemperatureData> data) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            // 写入表头
            writer.write("Time,Temp1,Temp2,TempSurface,TempCenter,TempCalibration\n");

            // 写入数据
            for (TemperatureData td : data) {
                writer.write(String.format("%d,%f,%f,%f,%f,%f\n",
                    td.getTime(), td.getTf1(), td.getTf2(), td.getTs(), td.getTc(), td.getTCal()));
            }

            logger.info("CSV文件写入成功: {}", filePath);
        } catch (IOException e) {
            logger.error("CSV文件写入失败", e);
        }
    }
}