package org.com.ivangeorgiev.lockbox.models;

import org.com.ivangeorgiev.lockbox.utils.SettingConstants;

public class Password {
    private String id;
    private String title;
    private String username;
    private String email;
    private String password;
    private String partitionKey;

    public Password() {
        partitionKey = System.getenv(SettingConstants.COSMOS_DB_PARTITION_KEY);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPartitionKey() {
        return this.partitionKey;
    }

    public void setPartitionKey(String partitionKey) {
        this.partitionKey = partitionKey;
    }
}
