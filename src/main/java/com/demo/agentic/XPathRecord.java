package com.demo.agentic;

public class XPathRecord {

    private final int rowIndex;
    private String url;
    private String xpath;
    private String status;
    private String lastUpdated;

    public XPathRecord(int rowIndex, String url, String xpath,
                       String status, String lastUpdated) {
        this.rowIndex = rowIndex;
        this.url = url;
        this.xpath = xpath;
        this.status = status;
        this.lastUpdated = lastUpdated;
    }

    public int row() { return rowIndex; }
    public String url() { return url; }
    public String xpath() { return xpath; }
    public String status() { return status; }
    public String lastUpdated() { return lastUpdated; }

    public void setXPath(String x) { this.xpath = x; }
    public void setStatus(String s) { this.status = s; }
    public void setLastUpdated(String t) { this.lastUpdated = t; }
}
