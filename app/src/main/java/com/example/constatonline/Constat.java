package com.example.constatonline;

import java.util.ArrayList;

public class Constat {
    private int id;
    private String date, vehicule1, vehicule2, place;
    private ArrayList<byte[]> photos;
    private double latitude, longitude;

    public Constat(int id, String date, String v1, String v2, ArrayList<byte[]> photos,
                   double lat, double lon, String place) {
        this.id = id;
        this.date = date;
        this.vehicule1 = v1;
        this.vehicule2 = v2;
        this.photos = photos;
        this.latitude = lat;
        this.longitude = lon;
        this.place = place;
    }

    public int getId() { return id; }
    public String getDate() { return date; }
    public String getVehicule1() { return vehicule1; }
    public String getVehicule2() { return vehicule2; }
    public ArrayList<byte[]> getPhotos() { return photos; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public String getPlace() { return place; }

    @Override
    public String toString() {
        return date + " - " + vehicule1 + " vs " + vehicule2 + " (" + place + ")";
    }
}
