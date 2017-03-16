package com.example.android.sample.myplaceapp.location;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

/**
 * 位置情報を取得するためのリポジトリ。
 */
public class PlaceRepository {

    /**
     * 1日をms換算した値。
     */
    public static final long DAY = 24L * 60L * 60L * 1000L;

    /**
     * コンストラクタ。
     */
    private PlaceRepository() {
        // インスタンス化禁止
    }

    /**
     * データを追加する。
     *
     * @param context
     * @param place
     * @return
     */
    public static Uri insert(Context context,Place place){
        ContentValues values = new ContentValues();
        values.put(PlaceDBHelper.COLUMN_LATITUDE,place.getLatitude());
        values.put(PlaceDBHelper.COLUMN_LONGITUDE,place.getLongitude());
        values.put(PlaceDBHelper.COLUMN_TIME,place.getTime());

        return context.getContentResolver().insert(PlaceProvider.CONTENT_URI,values);
    }

    /**
     *
     * @param context
     * @param day
     * @return
     */
    public static Place getLastestPlaceInDay(Context context,long day){
        // URIにlimit=1を追加する
        Uri uri = PlaceProvider.CONTENT_URI.buildUpon()
                .appendQueryParameter("limite","1")
                .build();

        // 指定した日の0:00
        long dayStart = (day/DAY) * DAY;
        // 指定した日の23:59:59
        long dayEnd = dayStart + DAY - 1L;

        // 指定した日に記録された位置情報を返す
        Cursor cursor = context.getContentResolver().query(uri,
                null,
                PlaceDBHelper.COLUMN_TIME + " BETWEEN ? AND ?",
                new String[]{String.valueOf(dayStart),String.valueOf(dayEnd)},
                PlaceDBHelper.COLUMN_REGISTER_TIME + " DESC");

        Place place = null;
        if(cursor != null && cursor.moveToNext()){
            place = cursorToPlace(cursor);
            cursor.close();
        }

        return place;
    }

    /**
     * 記録がつけられた日付を全て返す。
     *
     * @param context
     * @return
     */
    public static List<String> getAllDateString(Context context){

        // distinctをつける
        Uri uri = PlaceProvider.CONTENT_URI.buildUpon()
                .appendQueryParameter("distinct","true")
                .build();

        // 「記録された日」をyyyy-mm-ddに変換する
        String timeToDate = "strftime('%Y-%m-%d', " + PlaceDBHelper.COLUMN_TIME  + "/ 1000, 'unixepoch') AS DATE";

        Cursor cursor = context.getContentResolver().query(uri,
                new String[]{timeToDate},
                null, null,
                PlaceDBHelper.COLUMN_REGISTER_TIME + " DESC");

        List<String> dateStrings = new ArrayList<String>();

        if(cursor != null){
            while (cursor.moveToNext()){
                dateStrings.add(cursor.getString(cursor.getColumnIndex("DATE")));
            }
            cursor.close();
        }

        return dateStrings;
    }

    /**
     * カーソルからデータを取り出して、Placeオブジェクトに変換する。
     *
     * @param cursor
     * @return
     */
    public static Place cursorToPlace(Cursor cursor) {
        Place place = new Place();

        place.setId(cursor.getLong(cursor.getColumnIndex(PlaceDBHelper.COLUMN_ID)));
        place.setLatitude(cursor.getDouble(cursor.getColumnIndex(PlaceDBHelper.COLUMN_LATITUDE)));
        place.setLongitude(cursor.getDouble(cursor.getColumnIndex(PlaceDBHelper.COLUMN_LONGITUDE)));
        place.setTime(cursor.getLong(cursor.getColumnIndex(PlaceDBHelper.COLUMN_TIME)));

        return place;
    }

}
