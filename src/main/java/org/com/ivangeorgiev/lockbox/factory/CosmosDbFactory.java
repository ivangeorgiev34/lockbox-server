package org.com.ivangeorgiev.lockbox.factory;

import com.azure.cosmos.*;
import com.azure.cosmos.ConsistencyLevel;

public class CosmosDbFactory implements AutoCloseable {

    private final CosmosClient client;

    public CosmosDbFactory(String endpoint, String key) {
        client = new CosmosClientBuilder()
                .endpoint(endpoint)
                .key(key)
                .consistencyLevel(ConsistencyLevel.EVENTUAL)
                .buildClient();
    }

    public CosmosContainer getContainer(String databaseName, String containerName) {
        return client.getDatabase(databaseName).getContainer(containerName);
    }

    @Override
    public void close() {
        if (client != null) client.close();
    }
}
