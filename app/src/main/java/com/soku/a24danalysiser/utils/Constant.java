package com.soku.a24danalysiser.utils;

public class Constant {
    private final static String BASE_URL = "10.151.142.42:8080";
    private static int ID = 0;

    public static String URL(String url) {
        return String.format("http://%s%s", BASE_URL, url);
    }

    public static int getId() {
        return ID;
    }

    public static void setId(int ID) {
        Constant.ID = ID;
    }
}
