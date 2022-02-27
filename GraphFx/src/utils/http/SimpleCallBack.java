package utils.http;

import javafx.application.Platform;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class SimpleCallBack implements Callback {

    Consumer<String> onFail;
    Consumer<String> onSuccess;
    BiConsumer<String, Integer> onSuccessResponseCode;
    public CountDownLatch countDownLatch = new CountDownLatch(1);

    public SimpleCallBack() {
    }

    public SimpleCallBack(Consumer<String> onSuccess) {
        this.onSuccess = onSuccess;
    }

    public SimpleCallBack(BiConsumer<String, Integer> onSuccess) {
        this.onSuccessResponseCode = onSuccess;
    }

    public SimpleCallBack(Consumer<String> onSuccess, Consumer<String> onFail) {
        this.onSuccess = onSuccess;
        this.onFail = onFail;
    }

    @Override

    public void onFailure(Call call, IOException e) {
        countDownLatch.countDown();
        Platform.runLater(() ->
                {
                    countDownLatch.countDown();
                    System.out.println("Something went wrong: " + e.getMessage());
                    if (onFail != null) onFail.accept(e.getMessage());
                }
        );
    }

    @Override
    public void onResponse(Call call, Response response) throws IOException {
        countDownLatch.countDown();
        String responseBody = response.body().string();
        {
            if (onSuccessResponseCode != null)
                onSuccessResponseCode.accept(responseBody, response.code());
            else {
                if (response.code() != 200) {
                    Platform.runLater(() ->
                            System.out.println("Something went wrong: " + responseBody)
                    );
                } else {
                    Platform.runLater(() -> {
                        System.out.println("OK " + responseBody);
                        if (onSuccess != null) onSuccess.accept(responseBody);
                    });
                }
            }
        }
    }
}
