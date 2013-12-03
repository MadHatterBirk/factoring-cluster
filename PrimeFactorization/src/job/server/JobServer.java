package job.server;


import job.Job;
import job.client.ClientCallback;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.UUID;

/**
 * Remote interface for a JobServer.
 * 
 * When implementing a new JobServer do not use this interface. Instead extend
 * ProcessManager as it has session management already implemented.
 */
public interface JobServer extends Remote {
    /**
     * obtains a session for the client. Use the UUID when sending any other
     * request to the server.
     * @param client
     * @return UUID. The session id for this client.
     * @throws RemoteException 
     */
    UUID getSession(ClientCallback client) throws RemoteException;
    
    /**
     * ends a client session on the server.
     * @param id
     * @throws RemoteException
     * @throws SessionExpiredException if the session is already expired and 
     * this request is invalid.
     */
    void endSession(UUID id) throws RemoteException, SessionExpiredException;
    
    /**
     * gets the next job for the client.
     * @param id
     * @return
     * @throws RemoteException
     * @throws SessionExpiredException if the session is already expired and 
     * this request is invalid.
     */
    Job getNextJob(UUID id) throws RemoteException, SessionExpiredException;
    
    /**
     * returns a job to the server.
     * @param id
     * @param job
     * @throws RemoteException
     * @throws SessionExpiredException if the session is already expired and 
     * this request is invalid.
     */
    void returnJob(UUID id, Job job) throws RemoteException, SessionExpiredException;
}
