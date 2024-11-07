package be.yvanmazy.remotedminecraft.controller.agent;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemotedAgent extends Remote {

    boolean isLoaded() throws RemoteException;

    boolean isReady() throws RemoteException;

}