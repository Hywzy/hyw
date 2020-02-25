package com.offcn.exceptions;

import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;

import java.io.IOException;

public class ClientException implements ResponseErrorHandler {
    @Override
    public boolean hasError(ClientHttpResponse clientHttpResponse) throws IOException {
        return true;
    }

    @Override
    public void handleError(ClientHttpResponse clientHttpResponse) throws IOException {

    }
}
