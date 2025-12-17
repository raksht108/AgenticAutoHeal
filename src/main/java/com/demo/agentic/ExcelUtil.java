package com.demo.agentic;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.*;

public class ExcelUtil {

    public static List<XPathRecord> read(String path) throws Exception {
        System.out.println("Reading Excel: " + path);

        Workbook wb = new XSSFWorkbook(new FileInputStream(path));
        Sheet sheet = wb.getSheetAt(0);
        List<XPathRecord> list = new ArrayList<>();

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row r = sheet.getRow(i);
            if (r == null) continue;

            list.add(new XPathRecord(
                    i,
                    get(r, 0),
                    get(r, 1),
                    get(r, 2),
                    get(r, 3)
            ));
        }
        wb.close();
        return list;
    }

    public static void write(String path, List<XPathRecord> records)
            throws Exception {

        Workbook wb = new XSSFWorkbook(new FileInputStream(path));
        Sheet sheet = wb.getSheetAt(0);

        for (XPathRecord r : records) {
            Row row = sheet.getRow(r.row());
            if (row == null) row = sheet.createRow(r.row());

            set(row, 1, r.xpath());
            set(row, 2, r.status());
            set(row, 3, r.lastUpdated());
        }

        FileOutputStream fos = new FileOutputStream(path);
        wb.write(fos);
        wb.close();

        System.out.println(" Excel updated successfully");
    }

    private static String get(Row r, int i) {
        Cell c = r.getCell(i);
        return c == null ? "" : c.getStringCellValue();
    }

    private static void set(Row r, int i, String v) {
        Cell c = r.getCell(i);
        if (c == null) c = r.createCell(i);
        c.setCellValue(v == null ? "" : v);
    }
}
