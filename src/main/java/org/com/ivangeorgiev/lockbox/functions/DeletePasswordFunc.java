package org.com.ivangeorgiev.lockbox.functions;

import com.azure.cosmos.models.CosmosItemResponse;
import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import org.com.ivangeorgiev.lockbox.services.PasswordService;
import org.com.ivangeorgiev.lockbox.utils.HttpResponseMessageFactory;

import java.util.Optional;

public class DeletePasswordFunc {
    @FunctionName("DeletePasswordFunc")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req", methods = {HttpMethod.DELETE}, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        String id = request.getQueryParameters().get("id");
        if (id == null || id.isEmpty())
            HttpResponseMessageFactory.create(request, HttpStatus.BAD_REQUEST, false, "No password id provided", null);

        CosmosItemResponse<Object> itemResponse;
        try {
            PasswordService service = new PasswordService();
            itemResponse = service.delete(id);
        } catch (Exception ex) {
            return HttpResponseMessageFactory.create(request, HttpStatus.INTERNAL_SERVER_ERROR, false, ex.getMessage(), null);
        }

        if (itemResponse.getStatusCode() != HttpStatus.NO_CONTENT.value())
            return HttpResponseMessageFactory.create(request, HttpStatus.valueOf(itemResponse.getStatusCode()), false, "Password could not be created", null);

        return HttpResponseMessageFactory.create(request, HttpStatus.OK, true, "Password deleted successfully", null);
    }
}
