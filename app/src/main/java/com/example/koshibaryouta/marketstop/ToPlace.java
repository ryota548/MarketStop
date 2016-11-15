package com.example.koshibaryouta.marketstop;

public class ToPlace {

    String shopName;
    Double latitude;
    Double longitude;

    public ToPlace(String shopName, Double latitude, Double longitude){
        this.shopName = shopName;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public  String getShopName(){return this.shopName;}

    public Double getLatitude(){return this.latitude;}

    public Double getLongitude(){return  this.longitude;}
}
