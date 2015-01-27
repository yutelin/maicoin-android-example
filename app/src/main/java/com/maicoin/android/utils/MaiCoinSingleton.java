package com.maicoin.android.utils;

import com.maicoin.api.MaiCoin;
import com.maicoin.api.MaiCoinBuilder;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by yutelin on 1/19/15.
 */
public class MaiCoinSingleton {
    private static final String API_KEY = "YOU_API_KEY";
    private static final String API_SECRET = "YOU_API_SECRET";
    private static final String API_BASE_URI = "https://api.maicoin.com/v1/";

    private static MaiCoin instance;
    public static MaiCoin getInstance() {
        if(instance == null) {
            synchronized(MaiCoinSingleton.class) {
                if(instance == null) {
                    try {
                        instance = new MaiCoinBuilder().setApiKey(API_KEY, API_SECRET).setBaseUrl(new URL(API_BASE_URI)).build();
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return instance;
    }
}
