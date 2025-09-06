package org.com.ivangeorgiev.lockbox.functions;

import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.implementation.NotFoundException;
import com.azure.cosmos.models.CosmosItemResponse;
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
import java.util.Optional;

public class GetPasswordFunc {
    private final String cosmosDbEndpoint;
    private final String cosmosDbKey;
    private final String cosmosDbName;
    private final String cosmosDbContainerName;
    private final String cosmosDbPartitionKey;

    public GetPasswordFunc() {
        cosmosDbEndpoint = System.getenv(SettingConstants.COSMOS_DB_ENDPOINT);
        cosmosDbKey = System.getenv(SettingConstants.COSMOS_DB_KEY);
        cosmosDbName = System.getenv(SettingConstants.COSMOS_DB_NAME);
        cosmosDbContainerName = System.getenv(SettingConstants.COSMOS_DB_CONTAINER_NAME);
        cosmosDbPartitionKey = System.getenv(SettingConstants.COSMOS_DB_PARTITION_KEY);
    }

    @FunctionName("GetPasswordFunc")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req", methods = {HttpMethod.GET}, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        String passwordId = request.getQueryParameters()
                .get("id");

        if (passwordId == null || passwordId.isEmpty())
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Please provide password id").build();

        CosmosItemResponse<PasswordDto> itemResponse;
        try (CosmosDbFactory factory = new CosmosDbFactory(cosmosDbEndpoint, cosmosDbKey)) {

            CosmosContainer container = factory.getContainer(cosmosDbName, cosmosDbContainerName);

            try {
                itemResponse = container.readItem(passwordId, new PartitionKey(cosmosDbPartitionKey), PasswordDto.class);
            } catch (NotFoundException ex) {
                return request.createResponseBuilder(HttpStatus.valueOf(HttpStatus.NOT_FOUND.value())).body("Item with such id could not be found").build();
            }
        }

        if (itemResponse.getStatusCode() != HttpStatus.OK.value())
            return request.createResponseBuilder(HttpStatus.valueOf(itemResponse.getStatusCode())).body("Item with such id could not be found").build();

        CryptographyClient cryptoClient = CryptographyClientFactory.create(KeyVaultSettings.getInstance().getKeyId());

        PasswordDto password = itemResponse.getItem();

        DecryptResult decryptRes = cryptoClient.decrypt(KeyVaultSettings.getInstance().getEncryptionAlgorithm(), Base64.getDecoder().decode(password.getPassword()));
        password.setPassword(new String(decryptRes.getPlainText(), StandardCharsets.UTF_8));

        return request.createResponseBuilder(HttpStatus.OK).body(password).build();
    }
}
