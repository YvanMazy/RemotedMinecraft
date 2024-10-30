package be.yvanmazy.remotedminecraft.auth;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;

record TokenAuth(String accessToken) implements Auth {

    public TokenAuth {
        Objects.requireNonNull(accessToken, "accessToken must not be null");
    }

    @Override
    public @Nullable String username() {
        return null;
    }

}