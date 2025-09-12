package org.com.ivangeorgiev.lockbox.functions;

import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.security.keyvault.keys.cryptography.CryptographyClient;
import com.azure.security.keyvault.keys.cryptography.models.DecryptResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import org.com.ivangeorgiev.lockbox.factory.CryptographyClientFactory;
import org.com.ivangeorgiev.lockbox.models.Password;
import org.com.ivangeorgiev.lockbox.models.PasswordDto;
import org.com.ivangeorgiev.lockbox.models.UpdatePasswordDto;
import org.com.ivangeorgiev.lockbox.services.PasswordService;
import org.com.ivangeorgiev.lockbox.utils.HttpResponseMessageFactory;
import org.com.ivangeorgiev.lockbox.utils.KeyVaultSettings;
import org.com.ivangeorgiev.lockbox.utils.PasswordValidator;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;

public class UpdatePasswordFunc {
    @FunctionName("UpdatePasswordFunc")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req", methods = {HttpMethod.PATCH}, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        String id = request.getQueryParameters().get("id");

        if (id == null || id.isEmpty())
            return HttpResponseMessageFactory.create(request, HttpStatus.BAD_REQUEST, false, "No password id provided", null);

        if (!request.getBody().isPresent())
            return HttpResponseMessageFactory.create(request, HttpStatus.BAD_REQUEST, false, "No body provided", null);

        ObjectMapper mapper = new ObjectMapper();

        UpdatePasswordDto reqBody;
        try {
            reqBody = mapper.readValue(request.getBody().get(), UpdatePasswordDto.class);
        } catch (Exception ex) {
            return HttpResponseMessageFactory.create(request, HttpStatus.BAD_REQUEST, false, ex.getMessage(), null);
        }

        if (reqBody.getEmail() != null && !PasswordValidator.validateEmail(reqBody.getEmail()))
            return HttpResponseMessageFactory.create(request, HttpStatus.BAD_REQUEST, false, "Invalid email format", null);

        PasswordService service = new PasswordService();
        CosmosItemResponse<Password> itemResponse = service.update(id, reqBody);

        if (itemResponse.getStatusCode() != HttpStatus.OK.value())
            return HttpResponseMessageFactory.create(request, HttpStatus.INTERNAL_SERVER_ERROR, false, "Password couldn't be updated", null);

        Password passwordItemResponse = itemResponse.getItem();

        CryptographyClient cryptoClient = CryptographyClientFactory.create(KeyVaultSettings.getInstance().getKeyId());
        DecryptResult res = cryptoClient.decrypt(KeyVaultSettings.getInstance().getEncryptionAlgorithm(), Base64.getDecoder().decode(passwordItemResponse.getPassword()));
        passwordItemResponse.setPassword(new String(res.getPlainText(), StandardCharsets.UTF_8));

        PasswordDto dto = mapper.convertValue(passwordItemResponse, PasswordDto.class);

        return HttpResponseMessageFactory.create(request, HttpStatus.valueOf(itemResponse.getStatusCode()), true, "Password updated successfully", dto);
    }
}
