package com.housemate.client.config;

public class ApiConfig {

    private static final String ENV = System.getProperty("app.env", "prod");

    public static final String BASE_URL = "https://api.housemateapp.stream/api";
}