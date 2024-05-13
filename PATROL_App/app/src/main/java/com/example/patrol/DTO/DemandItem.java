package com.example.patrol.DTO;

public class DemandItem {
    private String productName;
    private int demandAmount;

    public DemandItem(String productName, int demandAmount) {
        this.productName = productName;
        this.demandAmount = demandAmount;
    }

    public String getProductName() {
        return productName;
    }

    public int getDemandAmount() {
        return demandAmount;
    }

    @Override
    public String toString() {
        return "DemandItem{" +
                "productName='" + productName + '\'' +
                ", demandAmount=" + demandAmount +
                '}';
    }
}
