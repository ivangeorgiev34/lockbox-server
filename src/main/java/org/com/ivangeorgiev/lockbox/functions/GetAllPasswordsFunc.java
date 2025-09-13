package org.com.ivangeorgiev.lockbox.functions;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import org.com.ivangeorgiev.lockbox.models.PasswordDto;
import org.com.ivangeorgiev.lockbox.services.PasswordService;
import org.com.ivangeorgiev.lockbox.utils.HttpResponseMessageFactory;

import java.util.List;
import java.util.Optional;

public class GetAllPasswordsFunc {

    @FunctionName("GetAllPasswordsFunc")
    public HttpResponseMessage run(@HttpTrigger(name = "req", methods = {HttpMethod.GET}, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request, final ExecutionContext context) {

        PasswordService service = new PasswordService();
        List<PasswordDto> passwords = service.getAll();

        return HttpResponseMessageFactory.create(request, HttpStatus.OK, true, "Passwords retrieved", passwords);
    }
}
