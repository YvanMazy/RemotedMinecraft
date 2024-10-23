package be.yvanmazy.remotedminecraft.auth;

import org.jetbrains.annotations.Nullable;

final class EmptyAuthentication implements Authentication {

    @Override
    public @Nullable String username() {
        return null;
    }

    @Override
    public @Nullable String accessToken() {
        return null;
    }

}