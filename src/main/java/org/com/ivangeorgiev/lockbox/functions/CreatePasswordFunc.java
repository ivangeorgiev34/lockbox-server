package org.com.ivangeorgiev.lockbox.functions;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.security.keyvault.keys.cryptography.CryptographyClient;
import com.azure.security.keyvault.keys.cryptography.models.EncryptResult;
import com.azure.security.keyvault.keys.cryptography.models.EncryptionAlgorithm;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import org.com.ivangeorgiev.lockbox.factory.CosmosDbFactory;
import org.com.ivangeorgiev.lockbox.factory.CryptographyClientFactory;
import org.com.ivangeorgiev.lockbox.models.Password;
import org.com.ivangeorgiev.lockbox.utils.KeyVaultSettings;
import org.com.ivangeorgiev.lockbox.utils.SettingConstants;

import java.util.Base64;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CreatePasswordFunc {

    private final String cosmosDbEndpoint;
    private final String cosmosDbKey;
    private final String cosmosDbName;
    private final String cosmosDbContainerName;
    private final String cosmosDbPartitionKey;

    public CreatePasswordFunc() {
        cosmosDbEndpoint = System.getenv(SettingConstants.COSMOS_DB_ENDPOINT);
        cosmosDbKey = System.getenv(SettingConstants.COSMOS_DB_KEY);
        cosmosDbName = System.getenv(SettingConstants.COSMOS_DB_NAME);
        cosmosDbContainerName = System.getenv(SettingConstants.COSMOS_DB_CONTAINER_NAME);
        cosmosDbPartitionKey = System.getenv(SettingConstants.COSMOS_DB_PARTITION_KEY);
    }

    @FunctionName("CreatePasswordFunc")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req", methods = {HttpMethod.POST}, authLevel = AuthorizationLevel.ANONYMOUS)
            HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        if (!request.getBody().isPresent()) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("No body provided!").build();
        }

        String body = request.getBody().get();
        ObjectMapper mapper = new ObjectMapper();

        Password password;
        try {
            password = mapper.readValue(body, Password.class);
        } catch (Exception ex) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body(ex.getMessage()).build();
        }

        if ((password.getPassword() == null || password.getPassword().isEmpty())
                || (password.getEmail() == null || password.getEmail().isEmpty())
                || (password.getTitle() == null || password.getTitle().isEmpty())) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Missing fields!").build();
        }

        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        Pattern pattern = Pattern.compile(emailRegex);
        Matcher matcher = pattern.matcher(password.getEmail());

        if (!matcher.matches())
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Invalid email format!").build();

        CryptographyClient cryptoClient = CryptographyClientFactory.create(KeyVaultSettings.getInstance().getKeyId());

        EncryptResult res = cryptoClient.encrypt(EncryptionAlgorithm.RSA_OAEP, password.getPassword().getBytes());

        password.setId(UUID.randomUUID().toString());
        password.setPassword(Base64.getEncoder().encodeToString(res.getCipherText()));

        CosmosItemResponse<Password> itemResponse;
        try (CosmosDbFactory factory = new CosmosDbFactory(cosmosDbEndpoint, cosmosDbKey)
        ) {
            CosmosContainer container = factory.getContainer(cosmosDbName, cosmosDbContainerName);

            PartitionKey partitionKey = new PartitionKey(cosmosDbPartitionKey);
            CosmosItemRequestOptions opt = new CosmosItemRequestOptions()
                    .setConsistencyLevel(ConsistencyLevel.EVENTUAL);
            itemResponse = container.createItem(password, partitionKey, opt);
        }

        if (itemResponse.getStatusCode() != HttpStatus.CREATED.value())
            return request.createResponseBuilder(HttpStatus.valueOf(itemResponse.getStatusCode())).body("Password could not be created").build();

        return request.createResponseBuilder(HttpStatus.CREATED).body(password).build();
    }
}
