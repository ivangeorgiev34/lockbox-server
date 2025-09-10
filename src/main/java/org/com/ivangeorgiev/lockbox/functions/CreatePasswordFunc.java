package org.com.ivangeorgiev.lockbox.functions;

import com.azure.cosmos.models.CosmosItemResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import org.com.ivangeorgiev.lockbox.models.Password;
import org.com.ivangeorgiev.lockbox.services.PasswordService;
import org.com.ivangeorgiev.lockbox.utils.HttpResponseMessageFactory;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CreatePasswordFunc {

    @FunctionName("CreatePasswordFunc")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req", methods = {HttpMethod.POST}, authLevel = AuthorizationLevel.ANONYMOUS)
            HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        if (!request.getBody().isPresent())
            return HttpResponseMessageFactory.create(request, HttpStatus.BAD_REQUEST, false, "No body provided!", null);

        String body = request.getBody().get();
        ObjectMapper mapper = new ObjectMapper();

        Password password;
        try {
            password = mapper.readValue(body, Password.class);
        } catch (Exception ex) {
            return HttpResponseMessageFactory.create(request, HttpStatus.BAD_REQUEST, false, ex.getMessage(), null);
        }

        if ((password.getPassword() == null || password.getPassword().isEmpty())
                || (password.getEmail() == null || password.getEmail().isEmpty())
                || (password.getTitle() == null || password.getTitle().isEmpty()))
            return HttpResponseMessageFactory.create(request, HttpStatus.BAD_REQUEST, false, "Missing parameters!", null);

        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        Pattern pattern = Pattern.compile(emailRegex);
        Matcher matcher = pattern.matcher(password.getEmail());

        if (!matcher.matches())
            return HttpResponseMessageFactory.create(request, HttpStatus.BAD_REQUEST, false, "Invalid email format!", null);

        PasswordService service = new PasswordService();
        CosmosItemResponse<Password> itemResponse = service.create(password);

        if (itemResponse.getStatusCode() != HttpStatus.CREATED.value())
            return HttpResponseMessageFactory.create(request, HttpStatus.valueOf(itemResponse.getStatusCode()), false, "Password could not be created", null);

        return HttpResponseMessageFactory.create(request, HttpStatus.CREATED, true, "Password created successfully", itemResponse.getItem());
    }
}
