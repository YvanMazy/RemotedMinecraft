package be.yvanmazy.remotedminecraft.auth;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;

record TokenAuthentication(String accessToken) implements Authentication {

    public TokenAuthentication {
        Objects.requireNonNull(accessToken, "accessToken must not be null");
    }

    @Override
    public @Nullable String username() {
        return null;
    }

}