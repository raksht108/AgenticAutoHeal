package com.demo.agentic;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

public class AgenticRunner {

    public static void main(String[] args) throws Exception {

        WebDriver driver = new ChromeDriver();
        AgenticXPathAuditor.run(
                driver,
                "src/main/resources/xpaths.xlsx");
        driver.quit();
    }
}
