package be.yvanmazy.remotedminecraft.auth;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;

record UsernameAuth(String username) implements Auth {

    public UsernameAuth {
        Objects.requireNonNull(username, "username must not be null");
    }

    @Override
    public @Nullable String accessToken() {
        return null;
    }

}