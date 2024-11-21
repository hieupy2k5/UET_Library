package org.example.uet_library;

import java.io.IOException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;

public class BookAPI {

    private static final String ApiKey = "AIzaSyC8Wq4sinCA-uJQp-QP4hrLB06K--OwYP0";
    private final OkHttpClient client = new OkHttpClient();

    public JSONArray fetchBooks(String query, String filter) {
        String baseUrl = "https://www.googleapis.com/books/v1/volumes?q=";

        // Construct the URL based on the filter
        switch (filter) {
            case "Title" -> baseUrl += "intitle:" + query;
            case "Author" -> baseUrl += "inauthor:" + query;
            case "ISBN" -> baseUrl += "isbn:" + query;
            default -> baseUrl += query; // Default case for generic search
        }

        String url = baseUrl + "&key=" + ApiKey + "&maxResults=1";


        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected response code: " + response);
            }

            String json = response.body().string();
            JSONObject jsonObject = new JSONObject(json);

            return jsonObject.optJSONArray("items") != null ? jsonObject.getJSONArray("items") : new JSONArray();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new JSONArray();
    }
}
