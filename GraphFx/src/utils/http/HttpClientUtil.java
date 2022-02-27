package utils.http;

import okhttp3.*;
import types.Tuple;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class HttpClientUtil {

    private final static SimpleCookieManager simpleCookieManager = new SimpleCookieManager();
    private final static OkHttpClient HTTP_CLIENT =
            new OkHttpClient.Builder()
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .cookieJar(simpleCookieManager)
                    .followRedirects(false)
                    .build();

    public static void setCookieManagerLoggingFacility(Consumer<String> logConsumer) {
        simpleCookieManager.setLogData(logConsumer);
    }

    public static void removeCookiesOf(String domain) {
        simpleCookieManager.removeCookiesOf(domain);
    }

    public static void runAsync(String finalUrl, Callback callback) {
        Request request = new Request.Builder()
                .url(finalUrl)
                .build();

        Call call = HttpClientUtil.HTTP_CLIENT.newCall(request);

        call.enqueue(callback);
    }

    public static Response runSync(String finalUrl) {
        Request request = new Request.Builder()
                .url(finalUrl)
                .build();

        Response response;
        try {
            response = HttpClientUtil.HTTP_CLIENT.newCall(request).execute();
        } catch (IOException e) {
            return new Response.Builder().code(500).build();
        }
        return response;
    }

    @SafeVarargs
    public static String createUrl(String url, Tuple<String, String>... tuples) {
        String createdUrl = "";
        try {
            HttpUrl.Builder builder = Objects.requireNonNull(HttpUrl
                            .parse(url))
                    .newBuilder();

            for (Tuple<String, String> tuple : tuples) {
                builder.addQueryParameter(tuple.x, tuple.y);
            }
            createdUrl = builder.build().toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return createdUrl;
    }

    public static void runAsyncBody(String finalUrl, RequestBody body, Callback callback) {

        Request request = new Request.Builder()
                .url(finalUrl)
                .post(body)
                .build();

        Call call = HttpClientUtil.HTTP_CLIENT.newCall(request);

        call.enqueue(callback);
    }


    public static void runAsyncBody(String finalUrl, String body, Callback callback) {

        RequestBody bodyReq = RequestBody.create(
                MediaType.parse("application/json"), body);

        Request request = new Request.Builder()
                .url(finalUrl)
                .post(bodyReq)
                .build();

        Call call = HttpClientUtil.HTTP_CLIENT.newCall(request);

        call.enqueue(callback);
    }


    public static void shutdown() {
        System.out.println("Shutting down HTTP CLIENT");
        HTTP_CLIENT.dispatcher().executorService().shutdown();
        HTTP_CLIENT.connectionPool().evictAll();
    }
}
