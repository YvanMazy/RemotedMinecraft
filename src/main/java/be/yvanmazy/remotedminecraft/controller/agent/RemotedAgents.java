package be.yvanmazy.remotedminecraft.controller.agent;

import org.jetbrains.annotations.NotNull;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public final class RemotedAgents {

    public static <T extends RemotedAgent> void init(final @NotNull String option,
                                                     final @NotNull String id,
                                                     final @NotNull RemoteSupplier<T> agentSupplier) throws RemoteException {
        init(Integer.parseInt(option), id, agentSupplier);
    }

    public static <T extends RemotedAgent> void init(final int port,
                                                     final @NotNull String id,
                                                     final @NotNull RemoteSupplier<T> agentSupplier) throws RemoteException {
        final Registry registry = LocateRegistry.createRegistry(port);
        registry.rebind(id, agentSupplier.get());
    }

    private RemotedAgents() throws IllegalAccessException {
        throw new IllegalAccessException("You cannot instantiate this class");
    }

    @FunctionalInterface
    public interface RemoteSupplier<T> {

        T get() throws RemoteException;

    }

}