package io.github.lightman314.lightmanscurrency.api.config;

import org.jetbrains.annotations.NotNull;

public abstract class ClientConfigFile extends ConfigFile {
    protected ClientConfigFile(@NotNull String fileName) { super(fileName); }
    @Override
    protected boolean isClientOnly() { return true; }
}
