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
import org.com.ivangeorgiev.lockbox.utils.CosmosDbSettings;
import org.com.ivangeorgiev.lockbox.utils.HttpResponseMessageFactory;
import org.com.ivangeorgiev.lockbox.utils.KeyVaultSettings;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;

public class GetPasswordFunc {

    @FunctionName("GetPasswordFunc")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req", methods = {HttpMethod.GET}, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        String passwordId = request.getQueryParameters().get("id");

        if (passwordId == null || passwordId.isEmpty())
            return HttpResponseMessageFactory.create(request, HttpStatus.BAD_REQUEST, false, "Please provide password id", null);

        CosmosItemResponse<PasswordDto> itemResponse;
        try (CosmosDbFactory factory = new CosmosDbFactory(CosmosDbSettings.getInstance().getEndpoint(), CosmosDbSettings.getInstance().getKey())) {

            CosmosContainer container = factory.getContainer(CosmosDbSettings.getInstance().getDatabaseName(), CosmosDbSettings.getInstance().getContainerName());

            try {
                itemResponse = container.readItem(passwordId, new PartitionKey(CosmosDbSettings.getInstance().getPartitionKey()), PasswordDto.class);
            } catch (NotFoundException ex) {
                return HttpResponseMessageFactory.create(request, HttpStatus.NOT_FOUND, false, "Item with such id could not be found", null);
            }
        }

        if (itemResponse.getStatusCode() != HttpStatus.OK.value())
            return HttpResponseMessageFactory.create(request, HttpStatus.valueOf(itemResponse.getStatusCode()), false, "Item with such id could not be found", null);

        CryptographyClient cryptoClient = CryptographyClientFactory.create(KeyVaultSettings.getInstance().getKeyId());

        PasswordDto password = itemResponse.getItem();

        DecryptResult decryptRes = cryptoClient.decrypt(KeyVaultSettings.getInstance().getEncryptionAlgorithm(), Base64.getDecoder().decode(password.getPassword()));
        password.setPassword(new String(decryptRes.getPlainText(), StandardCharsets.UTF_8));

        return HttpResponseMessageFactory.create(request, HttpStatus.OK, true, "Password retrieved", password);
    }
}
