package com.example.android.sample.myplaceapp.camera;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

/**
 * 画像をキャッシュするクラス。
 */
public class ImageCache {

    /**
     * キャッシュ。
     */
    private LruCache<String, Bitmap> mCache;

    /**
     * 画像をキャッシュする。
     */
    public ImageCache() {
        long maxMemory = Runtime.getRuntime().maxMemory() / 1024;
        int cacheSize = (int)(maxMemory / 8);

        mCache = new LruCache<String, Bitmap>(cacheSize) {
            protected int sizeOf(String key, Bitmap value) {
                return value.getAllocationByteCount() / 1024;
            }
        };
    }

    /**
     * 画像をキャッシュに保存する。
     *
     * @param key
     * @param image
     */
    public void put(String key, Bitmap image) {
        mCache.put(key, image);
    }

    /**
     * 画像をキャッシュから取り出す。
     *
     * @param key
     * @return
     */
    public Bitmap get(String key) {
        return mCache.get(key);
    }

    /**
     * キャッシュを削除する。
     */
    public void clear() {
        mCache.evictAll();
    }

}
