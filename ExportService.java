package com.iso11820.service;

import com.iso11820.config.AppConfig;
import com.iso11820.model.TestMaster;
import com.iso11820.model.TemperatureData;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 数据导出服务 - CSV/Excel/PDF
 */
public class ExportService {
    private static final Logger logger = LoggerFactory.getLogger(ExportService.class);

    private final AppConfig config;

    // 缓存的中文字体
    private PDFont chineseFont = null;
    private PDFont chineseFontBold = null;
    private boolean fontLoaded = false;

    // Windows系统字体路径（按优先级尝试）
    private static final String[] CHINESE_FONT_PATHS = {
        "C:/Windows/Fonts/msyh.ttc",      // 微软雅黑（Win10/11首选）
        "C:/Windows/Fonts/msyhbd.ttc",     // 微软雅黑粗体
        "C:/Windows/Fonts/simhei.ttf",     // 黑体
        "C:/Windows/Fonts/simsun.ttc",     // 宋体
        "C:/Windows/Fonts/simfang.ttf"     // 仿宋
    };

    private static final String[] CHINESE_FONT_BOLD_PATHS = {
        "C:/Windows/Fonts/msyhbd.ttc",     // 微软雅黑粗体
        "C:/Windows/Fonts/simhei.ttf",     // 黑体（可作为粗体替代）
        "C:/Windows/Fonts/simsun.ttc"      // 宋体
    };

    public ExportService(AppConfig config) {
        this.config = config;
    }

    /**
     * 加载中文字体 — 尝试多个系统字体路径
     */
    private PDFont loadChineseFont(PDDocument document, String[] fontPaths) {
        for (String path : fontPaths) {
            try {
                File fontFile = new File(path);
                if (fontFile.exists()) {
                    PDFont font = PDType0Font.load(document, fontFile);
                    logger.info("成功加载中文字体: {}", path);
                    return font;
                }
            } catch (IOException e) {
                logger.warn("加载字体 {} 失败: {}", path, e.getMessage());
            }
        }
        logger.warn("未找到可用的中文字体，将使用Helvetica（中文可能无法显示）");
        return PDType1Font.HELVETICA;
    }

    /**
     * 确保字体已加载
     */
    private void ensureFonts(PDDocument document) {
        if (!fontLoaded) {
            chineseFont = loadChineseFont(document, CHINESE_FONT_PATHS);
            chineseFontBold = loadChineseFont(document, CHINESE_FONT_BOLD_PATHS);
            fontLoaded = true;
        }
    }

    /**
     * 导出CSV文件
     */
    public String exportCsv(String productId, String testId, List<TemperatureData> data) {
        String basePath = config.getFileStorage().getTestDataDirectory();
        String dirPath = basePath + "/" + productId + "/" + testId;
        String filePath = dirPath + "/sensor_data.csv";

        try {
            // 创建目录
            Files.createDirectories(Paths.get(dirPath));

            // 写入CSV
            BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
            writer.write("Time(s),Temp1(°C),Temp2(°C),TempSurface(°C),TempCenter(°C),TempCalibration(°C)\n");
            for (TemperatureData td : data) {
                writer.write(td.toCsvLine() + "\n");
            }
            writer.close();

            logger.info("CSV文件导出成功: {}", filePath);
            return filePath;
        } catch (IOException e) {
            logger.error("CSV导出失败", e);
            return null;
        }
    }

    /**
     * 导出Excel文件
     */
    public String exportExcel(String testId, TestMaster test, List<TemperatureData> data) {
        String basePath = config.getReport().getOutputDirectory();
        String filePath = basePath + "/" + testId + "_报告.xlsx";

        try {
            Files.createDirectories(Paths.get(basePath));

            Workbook workbook = new XSSFWorkbook();

            // Sheet1: 试验信息
            Sheet infoSheet = workbook.createSheet("试验信息");
            createInfoSheet(infoSheet, test);

            // Sheet2: 温度数据
            Sheet dataSheet = workbook.createSheet("温度数据");
            createDataSheet(dataSheet, data);

            // Sheet3: 判定结论
            Sheet resultSheet = workbook.createSheet("判定结论");
            createResultSheet(resultSheet, test);

            // 保存文件
            FileOutputStream fos = new FileOutputStream(filePath);
            workbook.write(fos);
            fos.close();
            workbook.close();

            logger.info("Excel文件导出成功: {}", filePath);
            return filePath;
        } catch (IOException e) {
            logger.error("Excel导出失败", e);
            return null;
        }
    }

    /**
     * 创建试验信息Sheet
     */
    private void createInfoSheet(Sheet sheet, TestMaster test) {
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("项目");
        headerRow.createCell(1).setCellValue("内容");

        int rowNum = 1;
        addRow(sheet, rowNum++, "样品编号", test.getProductid());
        addRow(sheet, rowNum++, "试验编号", test.getTestid());
        addRow(sheet, rowNum++, "试验日期", test.getTestdate());
        addRow(sheet, rowNum++, "操作员", test.getOperator());
        addRow(sheet, rowNum++, "环境温度", String.valueOf(test.getAmbtemp()) + " °C");
        addRow(sheet, rowNum++, "环境湿度", String.valueOf(test.getAmbhumi()) + " %");
        addRow(sheet, rowNum++, "设备名称", test.getApparatusname());
        addRow(sheet, rowNum++, "试验前质量", String.valueOf(test.getPreweight()) + " g");
        addRow(sheet, rowNum++, "试验后质量", String.valueOf(test.getPostweight()) + " g");
        addRow(sheet, rowNum++, "失重量", String.format("%.2f g", test.getLostweight()));
        addRow(sheet, rowNum++, "失重率", String.format("%.2f%%", test.getLostweightPer()));
        addRow(sheet, rowNum++, "温升(ΔTs)", String.format("%.2f °C", test.getDeltats()));
        addRow(sheet, rowNum++, "试验时长", String.valueOf(test.getTotaltesttime()) + " 秒");
    }

    private void addRow(Sheet sheet, int rowNum, String label, String value) {
        Row row = sheet.createRow(rowNum);
        row.createCell(0).setCellValue(label);
        row.createCell(1).setCellValue(value);
    }

    /**
     * 创建温度数据Sheet
     */
    private void createDataSheet(Sheet sheet, List<TemperatureData> data) {
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("时间(秒)");
        headerRow.createCell(1).setCellValue("炉温1(°C)");
        headerRow.createCell(2).setCellValue("炉温2(°C)");
        headerRow.createCell(3).setCellValue("表面温度(°C)");
        headerRow.createCell(4).setCellValue("中心温度(°C)");

        for (int i = 0; i < data.size(); i++) {
            TemperatureData td = data.get(i);
            Row row = sheet.createRow(i + 1);
            row.createCell(0).setCellValue(td.getTime());
            row.createCell(1).setCellValue(td.getTf1());
            row.createCell(2).setCellValue(td.getTf2());
            row.createCell(3).setCellValue(td.getTs());
            row.createCell(4).setCellValue(td.getTc());
        }
    }

    /**
     * 创建判定结论Sheet
     */
    private void createResultSheet(Sheet sheet, TestMaster test) {
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("判定项");
        headerRow.createCell(1).setCellValue("结果");
        headerRow.createCell(2).setCellValue("标准要求");
        headerRow.createCell(3).setCellValue("判定");

        // 温升判定
        Row row1 = sheet.createRow(1);
        row1.createCell(0).setCellValue("样品温升");
        row1.createCell(1).setCellValue(String.format("%.2f °C", test.getDeltats()));
        row1.createCell(2).setCellValue("≤ 50 °C");
        row1.createCell(3).setCellValue(test.getDeltats() <= 50 ? "通过" : "不通过");

        // 失重率判定
        Row row2 = sheet.createRow(2);
        row2.createCell(0).setCellValue("失重率");
        row2.createCell(1).setCellValue(String.format("%.2f%%", test.getLostweightPer()));
        row2.createCell(2).setCellValue("≤ 50%");
        row2.createCell(3).setCellValue(test.getLostweightPer() <= 50 ? "通过" : "不通过");

        // 火焰持续时间判定
        Row row3 = sheet.createRow(3);
        row3.createCell(0).setCellValue("火焰持续时间");
        row3.createCell(1).setCellValue(String.valueOf(test.getFlameduration()) + " 秒");
        row3.createCell(2).setCellValue("< 5 秒");
        row3.createCell(3).setCellValue(test.getFlameduration() < 5 ? "通过" : "不通过");

        // 综合判定
        boolean passed = test.getDeltats() <= 50 && test.getLostweightPer() <= 50 && test.getFlameduration() < 5;
        Row row4 = sheet.createRow(4);
        row4.createCell(0).setCellValue("综合判定");
        row4.createCell(1).setCellValue("");
        row4.createCell(2).setCellValue("全部通过");
        row4.createCell(3).setCellValue(passed ? "合格" : "不合格");
    }

    /**
     * 导出PDF文件 — 使用系统中文字体支持中文显示
     */
    public String exportPdf(String testId, TestMaster test, List<TemperatureData> data) {
        if (!config.getReport().isEnablePdfExport()) {
            return null;
        }

        String basePath = config.getReport().getOutputDirectory();
        String filePath = basePath + "/" + testId + "_报告.pdf";

        try {
            Files.createDirectories(Paths.get(basePath));

            PDDocument document = new PDDocument();
            PDPage page = new PDPage();
            document.addPage(page);

            // 加载中文字体
            ensureFonts(document);

            PDPageContentStream content = new PDPageContentStream(document, page);

            // 标题
            content.setFont(chineseFontBold, 16);
            content.beginText();
            content.newLineAtOffset(50, 750);
            content.showText("ISO 11820 建筑材料不燃性试验报告");
            content.endText();

            // 分隔线
            content.setLineWidth((float)1);
            content.moveTo(50, 740);
            content.lineTo(545, 740);
            content.stroke();

            // 基本信息
            content.setFont(chineseFont, 11);
            int y = 720;
            int lineHeight = 18;

            String[][] infoItems = {
                {"样品编号: ", test.getProductid()},
                {"试验编号: ", test.getTestid()},
                {"试验日期: ", test.getTestdate() != null ? test.getTestdate() : ""},
                {"操作员: ", test.getOperator()},
                {"环境温度: ", String.format("%.1f °C", test.getAmbtemp())},
                {"环境湿度: ", String.format("%.1f %%", test.getAmbhumi())},
                {"设备名称: ", test.getApparatusname() != null ? test.getApparatusname() : ""},
                {"试验前质量: ", String.format("%.2f g", test.getPreweight())},
                {"试验后质量: ", String.format("%.2f g", test.getPostweight())},
                {"失重量: ", String.format("%.2f g", test.getLostweight())},
                {"失重率: ", String.format("%.2f%%", test.getLostweightPer())},
                {"试验时长: ", test.getTotaltesttime() + " 秒"},
            };

            for (String[] item : infoItems) {
                content.beginText();
                content.newLineAtOffset(50, y);
                content.showText(item[0] + item[1]);
                content.endText();
                y -= lineHeight;
            }

            // 温升数据
            y -= 10;
            content.setFont(chineseFontBold, 12);
            content.beginText();
            content.newLineAtOffset(50, y);
            content.showText("温升数据:");
            content.endText();
            y -= lineHeight;

            content.setFont(chineseFont, 11);
            String[][] tempItems = {
                {"炉温1温升(ΔTf1): ", String.format("%.2f °C", test.getDeltatf1())},
                {"炉温2温升(ΔTf2): ", String.format("%.2f °C", test.getDeltatf2())},
                {"表面温升(ΔTs): ", String.format("%.2f °C", test.getDeltats())},
                {"中心温升(ΔTc): ", String.format("%.2f °C", test.getDeltatc())},
            };

            for (String[] item : tempItems) {
                content.beginText();
                content.newLineAtOffset(50, y);
                content.showText(item[0] + item[1]);
                content.endText();
                y -= lineHeight;
            }

            // 火焰信息
            y -= 10;
            content.setFont(chineseFontBold, 12);
            content.beginText();
            content.newLineAtOffset(50, y);
            content.showText("火焰信息:");
            content.endText();
            y -= lineHeight;

            content.setFont(chineseFont, 11);
            content.beginText();
            content.newLineAtOffset(50, y);
            content.showText("是否出现持续火焰: " + (test.getFlametime() > 0 ? "是" : "否"));
            content.endText();
            y -= lineHeight;

            if (test.getFlametime() > 0) {
                content.beginText();
                content.newLineAtOffset(50, y);
                content.showText("火焰发生时刻: " + test.getFlametime() + " 秒");
                content.endText();
                y -= lineHeight;

                content.beginText();
                content.newLineAtOffset(50, y);
                content.showText("火焰持续时间: " + test.getFlameduration() + " 秒");
                content.endText();
                y -= lineHeight;
            }

            // 判定结论
            y -= 20;
            content.setLineWidth((float)0.5);
            content.moveTo(50, y + 5);
            content.lineTo(545, y + 5);
            content.stroke();

            boolean tempPass = test.getDeltats() <= 50;
            boolean weightPass = test.getLostweightPer() <= 50;
            boolean flamePass = test.getFlameduration() < 5;
            boolean passed = tempPass && weightPass && flamePass;

            content.setFont(chineseFontBold, 14);
            content.beginText();
            content.newLineAtOffset(50, y);
            content.showText("判定结论: " + (passed ? "合格" : "不合格"));
            content.endText();
            y -= lineHeight;

            content.setFont(chineseFont, 11);
            content.beginText();
            content.newLineAtOffset(50, y);
            content.showText("  样品温升: " + String.format("%.2f °C", test.getDeltats()) + " → " + (tempPass ? "通过" : "不通过"));
            content.endText();
            y -= lineHeight;

            content.beginText();
            content.newLineAtOffset(50, y);
            content.showText("  失重率: " + String.format("%.2f%%", test.getLostweightPer()) + " → " + (weightPass ? "通过" : "不通过"));
            content.endText();
            y -= lineHeight;

            content.beginText();
            content.newLineAtOffset(50, y);
            content.showText("  火焰持续时间: " + test.getFlameduration() + " 秒 → " + (flamePass ? "通过" : "不通过"));
            content.endText();

            content.close();

            document.save(filePath);
            document.close();

            logger.info("PDF文件导出成功: {}", filePath);
            return filePath;
        } catch (IOException e) {
            logger.error("PDF导出失败", e);
            return null;
        }
    }

    /**
     * 导出查询结果为Excel — 用于记录查询页面的导出按钮
     */
    public String exportQueryExcel(List<Object[]> queryResults, String filePath) {
        try {
            Files.createDirectories(Paths.get(filePath).getParent());

            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("试验记录");

            // 表头
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("试验编号");
            headerRow.createCell(1).setCellValue("样品编号");
            headerRow.createCell(2).setCellValue("试验日期");
            headerRow.createCell(3).setCellValue("操作员");
            headerRow.createCell(4).setCellValue("试验时长(秒)");
            headerRow.createCell(5).setCellValue("状态");
            headerRow.createCell(6).setCellValue("试验前质量(g)");
            headerRow.createCell(7).setCellValue("试验后质量(g)");
            headerRow.createCell(8).setCellValue("失重率(%)");
            headerRow.createCell(9).setCellValue("温升(°C)");

            // 数据行
            for (int i = 0; i < queryResults.size(); i++) {
                Object[] row = queryResults.get(i);
                Row dataRow = sheet.createRow(i + 1);
                dataRow.createCell(0).setCellValue((String) row[0]);  // testid
                dataRow.createCell(1).setCellValue((String) row[1]);  // productid
                dataRow.createCell(2).setCellValue((String) row[2]);  // testdate
                dataRow.createCell(3).setCellValue((String) row[3]);  // operator
                dataRow.createCell(4).setCellValue((Integer) row[4]); // totaltesttime
                String status = "10000000".equals((String) row[5]) ? "已保存" : "未保存";
                dataRow.createCell(5).setCellValue(status);
                dataRow.createCell(6).setCellValue((Double) row[6]);  // preweight
                dataRow.createCell(7).setCellValue((Double) row[7]);  // postweight
                dataRow.createCell(8).setCellValue((Double) row[8]);  // lostweight_per
                dataRow.createCell(9).setCellValue((Double) row[9]);  // deltatf
            }

            FileOutputStream fos = new FileOutputStream(filePath);
            workbook.write(fos);
            fos.close();
            workbook.close();

            logger.info("查询结果Excel导出成功: {}", filePath);
            return filePath;
        } catch (IOException e) {
            logger.error("查询结果Excel导出失败", e);
            return null;
        }
    }
}
