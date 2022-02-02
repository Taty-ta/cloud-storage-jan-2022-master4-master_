package com.geekbrains.cloud.jan.client;

import java.io.IOException;

import com.geekbrains.cloud.jan.model.AbstractMessage;

public interface Callback {

    void onMessageReceived(AbstractMessage message) throws IOException;

}
