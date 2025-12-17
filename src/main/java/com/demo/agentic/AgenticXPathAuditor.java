package com.demo.agentic;

import org.openqa.selenium.*;
import java.time.LocalDateTime;
import java.util.List;

public class AgenticXPathAuditor {

    public static void run(
            WebDriver driver,
            String excelPath) throws Exception {

        List<XPathRecord> records =
                ExcelUtil.read(excelPath);

        String currentUrl = "";

        for (XPathRecord r : records) {

            System.out.println("\n Checking XPath: " + r.xpath());
            System.out.println("URL: " + r.url());

            try {
                if (!r.url().equals(currentUrl)) {
                    currentUrl = r.url();
                    driver.get(currentUrl);
                }
            } catch (Exception e) {
                System.out.println("URL not reachable");
                r.setStatus("URL_NOT_REACHABLE");
                r.setLastUpdated(now());
                continue;
            }

            try {
                driver.findElement(By.xpath(r.xpath()));
                r.setStatus("VALID");
                r.setLastUpdated(now());
                continue;
            } catch (Exception e) {
                System.out.println("XPath broken – invoking ChatGPT");
            }

            String parentHtml =
                    extractParentHtml(driver, r.xpath());

            List<String> candidates =
                    ChatGPTFeatureStep.getCandidateXPaths(
                            r.xpath(), parentHtml, 3);

            boolean healed = false;

            for (String xp : candidates) {
                try {
                    driver.findElement(By.xpath(xp));
                    r.setXPath(xp);
                    r.setStatus("HEALED");
                    r.setLastUpdated(now());
                    healed = true;
                    System.out.println("Healed XPath: " + xp);
                    break;
                } catch (Exception ignored) {}
            }

            if (!healed) {
                System.out.println("Healing failed");
                r.setStatus("FAILED_TO_HEAL");
                r.setLastUpdated(now());
            }
        }

        ExcelUtil.write(excelPath, records);
    }

    private static String extractParentHtml(
            WebDriver driver,
            String failedXPath) {

        try {
            WebElement el = driver.findElement(
                    By.xpath("//*[contains(normalize-space(.),'" +
                            extractText(failedXPath) + "')]"));
            return el.findElement(By.xpath("./ancestor::*[1]"))
                     .getAttribute("outerHTML");
        } catch (Exception e) {
            System.out.println("Using fallback DOM snippet");
            return driver.getPageSource()
                    .substring(0, 3000);
        }
    }

    private static String extractText(String xpath) {
        return xpath.replaceAll(
                ".*text\\(\\)\\s*,?\\s*'([^']+)'.*", "$1");
    }

    private static String now() {
        return LocalDateTime.now().toString();
    }
}
