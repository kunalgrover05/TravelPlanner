package com.kunalgrover05.travelplanner;

/**
 * Created by grokunal on 22/10/16.
 */
public class MyPlace {
    private String placeId;
    private String name;
    private String adder;

    public MyPlace() {

    }

    public String getPlaceId() {
        return placeId;
    }

    public String getName() {
        return name;
    }

    public String getAdder() {
        return adder;
    }

    public MyPlace(String placeId, String name, String adder) {
        this.adder = adder;
        this.placeId = placeId;
        this.name = name;
    }
}
