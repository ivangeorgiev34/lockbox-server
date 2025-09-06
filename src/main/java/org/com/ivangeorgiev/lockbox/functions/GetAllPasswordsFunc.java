package org.com.ivangeorgiev.lockbox.functions;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.PartitionKey;
import com.azure.security.keyvault.keys.cryptography.CryptographyClient;
import com.azure.security.keyvault.keys.cryptography.models.DecryptResult;
import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import org.com.ivangeorgiev.lockbox.factory.CosmosDbFactory;
import org.com.ivangeorgiev.lockbox.factory.CryptographyClientFactory;
import org.com.ivangeorgiev.lockbox.models.PasswordDto;
import org.com.ivangeorgiev.lockbox.utils.KeyVaultSettings;
import org.com.ivangeorgiev.lockbox.utils.SettingConstants;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class GetAllPasswordsFunc {
    private final String cosmosDbEndpoint;
    private final String cosmosDbKey;
    private final String cosmosDbName;
    private final String cosmosDbContainerName;
    private final String cosmosDbPartitionKey;

    public GetAllPasswordsFunc() {
        cosmosDbEndpoint = System.getenv(SettingConstants.COSMOS_DB_ENDPOINT);
        cosmosDbKey = System.getenv(SettingConstants.COSMOS_DB_KEY);
        cosmosDbName = System.getenv(SettingConstants.COSMOS_DB_NAME);
        cosmosDbContainerName = System.getenv(SettingConstants.COSMOS_DB_CONTAINER_NAME);
        cosmosDbPartitionKey = System.getenv(SettingConstants.COSMOS_DB_PARTITION_KEY);
    }

    @FunctionName("GetAllPasswordsFunc")
    public HttpResponseMessage run(@HttpTrigger(name = "req", methods = {HttpMethod.GET}, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request, final ExecutionContext context) {

        List<PasswordDto> passwords;

        try (CosmosDbFactory factory = new CosmosDbFactory(cosmosDbEndpoint, cosmosDbKey)) {

            PartitionKey key = new PartitionKey(cosmosDbPartitionKey);
            CosmosQueryRequestOptions options = new CosmosQueryRequestOptions()
                    .setConsistencyLevel(ConsistencyLevel.EVENTUAL)
                    .setPartitionKey(key);

            CosmosContainer container = factory.getContainer(cosmosDbName, cosmosDbContainerName);

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

        return request.createResponseBuilder(HttpStatus.OK).body(passwords).build();
    }
}
