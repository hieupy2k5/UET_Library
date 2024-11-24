package org.example.uet_library.apis;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class QRGenerateAPI {

    private QRGenerateAPI() {
    }

    private static final QRGenerateAPI instance = new QRGenerateAPI();

    public static QRGenerateAPI getInstance() {
        return instance;
    }

    public byte[] generateQRCode(String text) throws IOException {
        text = URLEncoder.encode(text, StandardCharsets.UTF_8.toString());
        String url = "https://api.qrserver.com/v1/create-qr-code/?size=100x100&data=" + text;
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();
        Response response = client.newCall(request).execute();
        return response.body().bytes();
    }
}
