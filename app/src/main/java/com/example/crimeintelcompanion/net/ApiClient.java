package com.example.crimeintelcompanion.net;

import okhttp3.*;

public class ApiClient {
    private static final String BASE_URL =
            "https://e8xqen0dcb.execute-api.us-east-1.amazonaws.com/prod/";
    private static final OkHttpClient client = new OkHttpClient();

    public static void postJson(String endpoint, String jsonBody, Callback callback) {
        System.out.println("🔵 API CALL → " + BASE_URL + endpoint);
        System.out.println("📤 BODY → " + jsonBody);

        RequestBody body = RequestBody.create(
                jsonBody, MediaType.parse("application/json; charset=utf-8"));

        Request request = new Request.Builder()
                .url(BASE_URL + endpoint)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(callback);
    }
}
