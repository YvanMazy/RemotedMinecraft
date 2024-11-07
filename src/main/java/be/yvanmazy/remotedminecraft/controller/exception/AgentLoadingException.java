package be.yvanmazy.remotedminecraft.controller.exception;

public final class AgentLoadingException extends Exception {

    public AgentLoadingException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public AgentLoadingException(final String message) {
        super(message);
    }

}