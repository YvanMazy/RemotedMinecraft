package be.yvanmazy.remotedminecraft.example.agent;

import be.yvanmazy.remotedminecraft.controller.agent.RemotedAgents;

import java.lang.instrument.Instrumentation;
import java.rmi.RemoteException;

public class AgentMain {

    public static void premain(final String option, final Instrumentation inst) throws RemoteException {
        agentmain(option);
    }

    public static void agentmain(final String option) throws RemoteException {
        RemotedAgents.init(option, MyCustomAgent.ID, MyCustomAgentImpl::new);
    }

    private AgentMain() {
    }

}