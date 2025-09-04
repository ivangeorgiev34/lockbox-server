package org.com.ivangeorgiev.lockbox.utils;

public class KeyVaultSettings {

    private static KeyVaultSettings instance;

    private final String keyVaultUrl;
    private final String keyName;
    private final String keyVersion;

    private KeyVaultSettings() {
        this.keyVaultUrl = System.getenv(SettingConstants.KEY_VAULT_URL);
        this.keyName = System.getenv(SettingConstants.KEY_VAULT_KEY_NAME);
        this.keyVersion = System.getenv(SettingConstants.KEY_VAULT_KEY_VERSION);

        if (this.keyVaultUrl == null || this.keyVaultUrl.isEmpty()) {
            throw new IllegalStateException("Environment variable KEY_VAULT_URL is not set.");
        }
        if (this.keyName == null || this.keyName.isEmpty()) {
            throw new IllegalStateException("Environment variable KEY_VAULT_KEY_NAME is not set.");
        }

        if (this.keyVersion == null || this.keyVersion.isEmpty()) {
            throw new IllegalStateException("Environment variable KEY_VAULT_KEY_VERSION is not set.");
        }
    }

    public static synchronized KeyVaultSettings getInstance() {
        if (instance == null) {
            instance = new KeyVaultSettings();
        }
        return instance;
    }

    public String getKeyVaultUrl() {
        return keyVaultUrl;
    }

    public String getKeyName() {
        return keyName;
    }

    public String getKeyVersion() {
        return keyVersion;
    }

    public String getKeyId() {
        return String.join("/", keyVaultUrl, "keys", keyName, keyVersion);
    }
}
