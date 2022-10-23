package com.steward.nowpaid.model;

public class RatingItem
{
    public String name;
    public String review;
    public int rating;

    public RatingItem(String n,String r,int ra){
        this.name = n;
        this.review = r;
        this.rating = ra;
    }
}