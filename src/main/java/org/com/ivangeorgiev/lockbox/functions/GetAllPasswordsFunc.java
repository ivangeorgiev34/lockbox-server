package org.com.ivangeorgiev.lockbox.functions;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import org.com.ivangeorgiev.lockbox.models.PasswordDto;
import org.com.ivangeorgiev.lockbox.services.CacheService;
import org.com.ivangeorgiev.lockbox.services.PasswordService;
import org.com.ivangeorgiev.lockbox.utils.GlobalConstants;
import org.com.ivangeorgiev.lockbox.utils.HttpResponseMessageFactory;

import java.util.List;
import java.util.Optional;

public class GetAllPasswordsFunc {

    @FunctionName("GetAllPasswordsFunc")
    public HttpResponseMessage run(@HttpTrigger(name = "req", methods = {HttpMethod.GET}, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request, final ExecutionContext context) {

        List<PasswordDto> passwords = CacheService.get(GlobalConstants.CACHE_PASSWORDS_KEY);

        if (passwords == null) {
            PasswordService service = new PasswordService();
            passwords = service.getAll();

            CacheService.put(GlobalConstants.CACHE_PASSWORDS_KEY, passwords);
        }

        return HttpResponseMessageFactory.create(request, HttpStatus.OK, true, "Passwords retrieved", passwords);
    }
}
