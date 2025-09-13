package org.com.ivangeorgiev.lockbox.factory;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.keys.cryptography.CryptographyClient;
import com.azure.security.keyvault.keys.cryptography.CryptographyClientBuilder;
import org.com.ivangeorgiev.lockbox.utils.KeyVaultSettings;

public class CryptographyClientFactory {

    public static CryptographyClient create(String keyId){
        return new CryptographyClientBuilder()
                .keyIdentifier(keyId)
                .credential(new DefaultAzureCredentialBuilder().build())
                .buildClient();
    }
}
