package org.com.ivangeorgiev.lockbox.models;

public class ApiResponseMessage<T> {
    private T content;
    private Boolean success;
    private String message;

    public ApiResponseMessage(Boolean success, String message, T content) {
        this.success = success;
        this.message = message;
        this.content = content;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getContent() {
        return content;
    }

    public void setContent(T content) {
        this.content = content;
    }
}
