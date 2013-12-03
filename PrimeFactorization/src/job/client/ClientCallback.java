package job.client;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Remote interface for a ClientCallback. This is used by the JobServer to stop
 * a client from executing jobs and to check the status of the client.
 */
public interface ClientCallback extends Remote {
    /**
     * Stop current jobs being run on the client.
     * @throws RemoteException 
     */
    void stopJobs() throws RemoteException;
    
    /**
     * Get the status of the client.
     * @return the ClientStatus
     * @throws RemoteException 
     */
    ClientStatus status() throws RemoteException;
}
