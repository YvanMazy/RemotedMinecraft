package be.yvanmazy.remotedminecraft.example.agent;

import net.minecraft.client.Minecraft;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

final class MyCustomAgentImpl extends UnicastRemoteObject implements MyCustomAgent {

    MyCustomAgentImpl() throws RemoteException {
        super();
    }

    @Override
    public int getFps() throws RemoteException {
        return Minecraft.getInstance().getFps();
    }

    @Override
    @SuppressWarnings("ConstantValue")
    public boolean isReady() throws RemoteException {
        final Minecraft minecraft = Minecraft.getInstance();
        return minecraft != null && minecraft.isGameLoadFinished();
    }

    @Override
    public boolean isLoaded() throws RemoteException {
        return true;
    }

}