package org.com.ivangeorgiev.lockbox.utils;

import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import org.com.ivangeorgiev.lockbox.models.ApiResponseMessage;

import java.util.Optional;

public class HttpResponseMessageFactory {

    public static <T> HttpResponseMessage create(HttpRequestMessage<Optional<String>> request, HttpStatus status, Boolean success, String message, T content) {
        ApiResponseMessage<T> res = new ApiResponseMessage<T>(success, message, content);

        return request.createResponseBuilder(status).body(res).build();
    }
}
