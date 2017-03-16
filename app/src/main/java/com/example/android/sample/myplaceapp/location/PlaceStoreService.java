package com.example.android.sample.myplaceapp.location;

import android.app.IntentService;
import android.content.Intent;
import android.location.Location;
import android.support.annotation.Nullable;

import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationResult;

import java.util.Date;
import java.util.List;

/**
 * 位置情報を発行するIntentService。
 */
public class PlaceStoreService extends IntentService {

    private static final String TAG = "PlaceStoreService";

    /**
     * コンストラクタ。
     */
    public PlaceStoreService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        // 位置情報の取得可否に関するデータを含んでいる場合
        if (LocationAvailability.hasLocationAvailability(intent)){
            // 位置情報の取得可否情報を得る
            LocationAvailability availability =  LocationAvailability.extractLocationAvailability(intent);

            if(!availability.isLocationAvailable()){
                // 位置情報が取得不能になっている場合何もしない
                return;
            }
        }

        // 位置情報に関するデータを含んでいる場合
        if (LocationResult.hasResult(intent)){
            // 位置情報の取得結果を得る
            LocationResult locationResult = LocationResult.extractResult(intent);

            List<Location> locations = locationResult.getLocations();

            // 位置情報を保存する
            storeLocationData(locations);
        }
    }

    /**
     * 位置情報を保存する。
     *
     * @param locations
     */
    private void storeLocationData(List<Location> locations) {
        // 本日の、直近の位置情報を取得する
        Place latestPlace =PlaceRepository.getLastestPlaceInDay(this,new Date().getTime());

        for (Location location : locations){
            if(location.isFromMockProvider()){
                // モックから送られた情報は無視する
                continue;
            }

            if (latestPlace != null){
                float[] results = new float[1];
                // 直近の位置情報からの距離を計算する
                Location.distanceBetween(
                        latestPlace.getLatitude(),
                        latestPlace.getLongitude(),
                        location.getLatitude(),
                        location.getLongitude(),
                        results);

                // 100m以上離れていない場合には、無視する
                if (results[0] < 100f){
                    continue;
                }
            }

            // DBに保存する
            Place place = new Place();
            place.setLatitude(location.getLatitude());
            place.setLongitude(location.getLongitude());
            place.setTime(location.getTime());

            // DBに挿入する
            PlaceRepository.insert(this,place);

            // 直近の位置情報を、この位置情報にする
            latestPlace = place;
        }

    }
}
