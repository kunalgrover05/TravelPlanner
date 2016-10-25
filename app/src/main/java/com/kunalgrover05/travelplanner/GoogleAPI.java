package com.kunalgrover05.travelplanner;

import com.kunalgrover05.travelplanner.JavaGen.Schema;

import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by grokunal on 22/10/16.
 */
public interface GoogleAPI {
    /**
     * Created by grokunal on 16/09/16.
     */
    @GET("/maps/api/place/nearbysearch/json?radius=500")
    Observable<Schema> nearbyLocations(@Query("key") String key, @Query("location") String location);

}
