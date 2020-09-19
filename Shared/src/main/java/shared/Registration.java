package shared;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Registration
 */
public interface Registration extends Remote {
    String SERVICE_NAME = "WorldQuizzleRegistration";

    String register(String name, String password) throws RemoteException;
}