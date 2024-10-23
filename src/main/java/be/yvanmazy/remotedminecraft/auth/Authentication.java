package be.yvanmazy.remotedminecraft.auth;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public sealed interface Authentication permits EmptyAuthentication, TokenAuthentication, UsernameAuthentication {

    Authentication EMPTY = new EmptyAuthentication();

    @Contract("_ -> new")
    static @NotNull Authentication byToken(final @NotNull String accessToken) {
        return new TokenAuthentication(accessToken);
    }

    @Contract("_ -> new")
    static @NotNull Authentication byUsername(final @NotNull String username) {
        return new UsernameAuthentication(username);
    }

    @Contract(pure = true)
    @Nullable String username();

    @Contract(pure = true)
    @Nullable String accessToken();

}