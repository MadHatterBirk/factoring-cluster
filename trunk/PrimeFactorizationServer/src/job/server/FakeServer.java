package job.server;

import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import job.Job;
import job.client.ClientCallback;

/**
 * A simple JobServer that creates FakeJobs. This server is used to test the
 * client.
 */
public class FakeServer extends ProcessManager {

    private Map<UUID, ClientCallback> clients;
    private int jobs;
    Random rand;

    public FakeServer() {
        clients = new HashMap<UUID, ClientCallback>();
        rand = new Random();
    }

    @Override
    public Job getNextJob(UUID id) throws RemoteException {
        synchronized (this) {
            jobs++;
        }
        return new FakeJob(rand.nextInt(20) + 10);
    }

    @Override
    public void returnJob(UUID id, Job job) throws RemoteException {
        boolean stop = false;
        synchronized (this) {
            if (jobs >= 10) {
                jobs = 0;
                stop = true;
            }
        }
        if (stop) {
            stopJobs();
        }
    }

    public static void main(String... args) throws RemoteException {
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new RMISecurityManager());
        }
        FakeServer server = new FakeServer();
        JobServer stub = (JobServer) UnicastRemoteObject.exportObject(server, 0);
        Registry registry = LocateRegistry.getRegistry();
        registry.rebind("JobServer", stub);
        server.run();
    }
}
