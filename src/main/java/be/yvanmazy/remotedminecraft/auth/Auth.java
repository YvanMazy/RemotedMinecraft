package be.yvanmazy.remotedminecraft.auth;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public sealed interface Auth permits EmptyAuth, TokenAuth, UsernameAuth {

    Auth EMPTY = new EmptyAuth();

    @Contract("_ -> new")
    static @NotNull Auth byToken(final @NotNull String accessToken) {
        return new TokenAuth(accessToken);
    }

    @Contract("_ -> new")
    static @NotNull Auth byUsername(final @NotNull String username) {
        return new UsernameAuth(username);
    }

    @Contract(pure = true)
    @Nullable String username();

    @Contract(pure = true)
    @Nullable String accessToken();

}