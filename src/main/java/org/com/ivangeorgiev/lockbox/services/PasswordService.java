package org.com.ivangeorgiev.lockbox.services;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.models.*;
import com.azure.security.keyvault.keys.cryptography.CryptographyClient;
import com.azure.security.keyvault.keys.cryptography.models.DecryptResult;
import com.azure.security.keyvault.keys.cryptography.models.EncryptResult;
import org.com.ivangeorgiev.lockbox.factory.CosmosDbFactory;
import org.com.ivangeorgiev.lockbox.factory.CryptographyClientFactory;
import org.com.ivangeorgiev.lockbox.models.Password;
import org.com.ivangeorgiev.lockbox.models.PasswordDto;
import org.com.ivangeorgiev.lockbox.models.UpdatePasswordDto;
import org.com.ivangeorgiev.lockbox.utils.CosmosDbSettings;
import org.com.ivangeorgiev.lockbox.utils.KeyVaultSettings;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class PasswordService {

    public CosmosItemResponse<Password> create(Password password) {
        CryptographyClient cryptoClient = CryptographyClientFactory.create(KeyVaultSettings.getInstance().getKeyId());

        EncryptResult res = cryptoClient.encrypt(KeyVaultSettings.getInstance().getEncryptionAlgorithm(), password.getPassword().getBytes());

        password.setId(UUID.randomUUID().toString());
        password.setPassword(Base64.getEncoder().encodeToString(res.getCipherText()));

        CosmosItemResponse<Password> itemResponse;
        try (CosmosDbFactory factory = new CosmosDbFactory(CosmosDbSettings.getInstance().getEndpoint(), CosmosDbSettings.getInstance().getKey())
        ) {
            CosmosContainer container = factory.getContainer(CosmosDbSettings.getInstance().getDatabaseName(), CosmosDbSettings.getInstance().getContainerName());

            PartitionKey partitionKey = new PartitionKey(CosmosDbSettings.getInstance().getPartitionKey());
            CosmosItemRequestOptions opt = new CosmosItemRequestOptions()
                    .setConsistencyLevel(ConsistencyLevel.EVENTUAL)
                    .setContentResponseOnWriteEnabled(true);
            itemResponse = container.createItem(password, partitionKey, opt);
        }

        return itemResponse;
    }

    public List<PasswordDto> getAll() {
        List<PasswordDto> passwords;

        try (CosmosDbFactory factory = new CosmosDbFactory(CosmosDbSettings.getInstance().getEndpoint(), CosmosDbSettings.getInstance().getKey())) {

            PartitionKey key = new PartitionKey(CosmosDbSettings.getInstance().getPartitionKey());
            CosmosQueryRequestOptions options = new CosmosQueryRequestOptions()
                    .setConsistencyLevel(ConsistencyLevel.EVENTUAL)
                    .setPartitionKey(key);

            CosmosContainer container = factory.getContainer(CosmosDbSettings.getInstance().getDatabaseName(), CosmosDbSettings.getInstance().getContainerName());

            CryptographyClient cryptoClient = CryptographyClientFactory.create(KeyVaultSettings.getInstance().getKeyId());

            passwords = container
                    .readAllItems(key, options, PasswordDto.class)
                    .stream()
                    .peek((pass) -> {

                        DecryptResult res = cryptoClient.decrypt(KeyVaultSettings.getInstance().getEncryptionAlgorithm(), Base64.getDecoder().decode(pass.getPassword()));

                        pass.setPassword(new String(res.getPlainText(), StandardCharsets.UTF_8));
                    })
                    .collect(Collectors.toList());
        }

        return passwords;
    }

    public CosmosItemResponse<Object> delete(String id) {

        CosmosItemResponse<Object> itemResponse;
        try (CosmosDbFactory factory = new CosmosDbFactory(CosmosDbSettings.getInstance().getEndpoint(), CosmosDbSettings.getInstance().getKey())) {

            CosmosContainer container = factory.getContainer(CosmosDbSettings.getInstance().getDatabaseName(), CosmosDbSettings.getInstance().getContainerName());

            CosmosItemRequestOptions opt = new CosmosItemRequestOptions()
                    .setConsistencyLevel(ConsistencyLevel.EVENTUAL);

            itemResponse = container.deleteItem(id, new PartitionKey(CosmosDbSettings.getInstance().getPartitionKey()), opt);
        }

        return itemResponse;
    }

    public CosmosItemResponse<Password> update(String id, UpdatePasswordDto password) {

        CosmosItemResponse<Password> updatedPassword;
        try (CosmosDbFactory factory = new CosmosDbFactory(CosmosDbSettings.getInstance().getEndpoint(), CosmosDbSettings.getInstance().getKey())) {

            CosmosPatchOperations operations = updateItemProps(password);

            PartitionKey partitionKey = new PartitionKey(CosmosDbSettings.getInstance().getPartitionKey());

            CosmosPatchItemRequestOptions opt = new CosmosPatchItemRequestOptions();
            opt.setContentResponseOnWriteEnabled(true);
            opt.setConsistencyLevel(ConsistencyLevel.EVENTUAL);

            CosmosContainer container = factory.getContainer(CosmosDbSettings.getInstance().getDatabaseName(), CosmosDbSettings.getInstance().getContainerName());

            updatedPassword = container.patchItem(id, partitionKey, operations, opt, Password.class);
        }

        return updatedPassword;
    }

    private CosmosPatchOperations updateItemProps(UpdatePasswordDto password) {
        CosmosPatchOperations operations = CosmosPatchOperations.create();

        if (password.getTitle() != null) operations.set("/title", password.getTitle());
        if (password.getUsername() != null) operations.set("/username", password.getTitle());
        if (password.getEmail() != null) operations.set("/email", password.getEmail());
        if (password.getPassword() != null) {

            CryptographyClient cryptoClient = CryptographyClientFactory.create(KeyVaultSettings.getInstance().getKeyId());

            EncryptResult res = cryptoClient.encrypt(KeyVaultSettings.getInstance().getEncryptionAlgorithm(), password.getPassword().getBytes());
            String str = Base64.getEncoder().encodeToString(res.getCipherText());

            operations.set("/password", str);
        }

        return operations;
    }
}

