package shared;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Registration
 */
public interface Registration extends Remote {
    public final String SERVICE_NAME = "WorldQuizzleRegistration";

    public String register(String name, String password) throws RemoteException;
}