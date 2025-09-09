package org.com.ivangeorgiev.lockbox.utils;

public class CosmosDbSettings {

    private static CosmosDbSettings instance;

    private final String endpoint;
    private final String key;
    private final String databaseName;
    private final String containerName;
    private final String partitionKey;

    private CosmosDbSettings() {
        this.endpoint = System.getenv(SettingConstants.COSMOS_DB_ENDPOINT);
        this.key = System.getenv(SettingConstants.COSMOS_DB_KEY);
        this.databaseName = System.getenv(SettingConstants.COSMOS_DB_NAME);
        this.containerName = System.getenv(SettingConstants.COSMOS_DB_CONTAINER_NAME);
        this.partitionKey = System.getenv(SettingConstants.COSMOS_DB_PARTITION_KEY);

        if (this.endpoint == null || this.endpoint.isEmpty()) {
            throw new IllegalStateException("Environment variable COSMOS_DB_ENDPOINT is not set.");
        }
        if (this.key == null || this.key.isEmpty()) {
            throw new IllegalStateException("Environment variable COSMOS_DB_KEY is not set.");
        }
        if (this.databaseName == null || this.databaseName.isEmpty()) {
            throw new IllegalStateException("Environment variable COSMOS_DB_NAME is not set.");
        }
        if (this.containerName == null || this.containerName.isEmpty()) {
            throw new IllegalStateException("Environment variable COSMOS_DB_CONTAINER_NAME is not set.");
        }
        if (this.partitionKey == null || this.partitionKey.isEmpty()) {
            throw new IllegalStateException("Environment variable COSMOS_DB_PARTITION_KEY is not set.");
        }
    }

    public static synchronized CosmosDbSettings getInstance() {
        if (instance == null) {
            instance = new CosmosDbSettings();
        }
        return instance;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public String getKey() {
        return key;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public String getContainerName() {
        return containerName;
    }

    public String getPartitionKey() {
        return partitionKey;
    }
}
