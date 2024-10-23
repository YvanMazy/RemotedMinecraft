package be.yvanmazy.remotedminecraft.auth;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;

record UsernameAuthentication(String username) implements Authentication {

    public UsernameAuthentication {
        Objects.requireNonNull(username, "username must not be null");
    }

    @Override
    public @Nullable String accessToken() {
        return null;
    }

}