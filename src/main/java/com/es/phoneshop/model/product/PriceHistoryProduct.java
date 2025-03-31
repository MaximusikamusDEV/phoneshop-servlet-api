package com.es.phoneshop.model.product;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PriceHistoryProduct {

    private LocalDate date;
    private BigDecimal price;

    public PriceHistoryProduct(){
    }

    public PriceHistoryProduct(LocalDate date, BigDecimal price) {
        this.date = date;
        this.price = price;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public LocalDate getDate() {
        return date;
    }

    public BigDecimal getPrice() {
        return price;
    }
}
