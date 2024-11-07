package be.yvanmazy.remotedminecraft.example.agent;

import be.yvanmazy.remotedminecraft.controller.agent.RemotedAgent;

import java.rmi.RemoteException;

public interface MyCustomAgent extends RemotedAgent {

    String ID = "myAgent";

    int getFps() throws RemoteException;

}