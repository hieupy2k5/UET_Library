package org.example.uet_library;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class QRGenerateAPI {
    public static byte[] generateQRCode(String text) throws IOException {
        text = URLEncoder.encode(text, StandardCharsets.UTF_8.toString());
        String url = "https://api.qrserver.com/v1/create-qr-code/?size=100x100&data=" + text;
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();
        Response response = client.newCall(request).execute();
        return response.body().bytes();
    }
}
