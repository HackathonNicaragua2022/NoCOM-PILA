package com.steward.nowpaid.model;

public class ProductItem
{
    public String name;
    public float price;
    public int imageRes;

    public ProductItem(String name,float price,int imageRes){
        this.name = name;
        this.price = price;
        this.imageRes = imageRes;
    }
}