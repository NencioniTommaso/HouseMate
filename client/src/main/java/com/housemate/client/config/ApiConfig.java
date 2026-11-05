package com.housemate.client.config;

public class ApiConfig {

    private static final String ENV = System.getProperty("app.env", "prod");

    public static final String BASE_URL = ENV.equals("local")
            ? "http://localhost:8080"
            : "https://housemate-backend-urlu.onrender.com";
}