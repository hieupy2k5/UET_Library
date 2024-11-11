package org.example.uet_library;

import java.io.IOException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;

public class BookAPI {

    private static final String ApiKey = "AIzaSyC8Wq4sinCA-uJQp-QP4hrLB06K--OwYP0";
    private static String Baseurl = "https://www.googleapis.com/books/v1/volumes?";
    private final OkHttpClient client = new OkHttpClient();

    public JSONArray fetchBooks(String query, String filter) {
        switch (filter) {
            case "Title" -> Baseurl = "https://www.googleapis.com/books/v1/volumes?q=";
            case "Author" -> Baseurl = "https://www.googleapis.com/books/v1/volumes?q=author";
            case "ISBN" -> Baseurl = "https://www.googleapis.com/books/v1/volumes?q=isbn";
        }

        String url = Baseurl + query + "&key=" + ApiKey + "&maxResults=3";

        Request request = new Request.Builder()
            .url(url)
            .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }

            String json = response.body().string();
            JSONObject jsonObject = new JSONObject(json);

            return jsonObject.getJSONArray("items");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
