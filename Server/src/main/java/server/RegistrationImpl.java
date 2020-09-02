package server;

import shared.Registration;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * server.WordQuizzleServer
 */
public class RegistrationImpl extends UnicastRemoteObject implements Registration {
    public RegistrationImpl() throws RemoteException {
        super();

    }

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Override
    public String register(String name, String password) throws RemoteException {
        Database database = Database.getInstance();
        if (password.isBlank()) {
            return "server.Password non valida";
        }
        if (!database.isRegistered(name)) {// un verifica approssimativa(no lock) per evitare computazione inutile per
            // l'hashing della password
            User user = new User(name, password);
            if (database.register(user))
                return "OK";
        }
        return "Nome utente già in uso";
    }

}