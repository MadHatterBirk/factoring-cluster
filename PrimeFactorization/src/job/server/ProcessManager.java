package job.server;

import job.Job;
import job.client.ClientCallback;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import job.client.ClientStatus;

/**
 * Partial implementation of a JobServer. ProcessManager implements session
 * management. When a session is ended or expired the jobs will be kept for
 * future requests to execute. Session management is in the run() method Any
 * object of this class or subclass should be executed in a thread.
 */
public abstract class ProcessManager implements JobServer, Runnable {

    protected Map<UUID, Job> expired;
    protected Map<UUID, Session> sessions;
    protected boolean run;

    /**
     * A session tracks every client on this server and the jobs that have been
     * issued to it. When a session is ended this object is used to stop the
     * jobs on the server and put them in the expired queue.
     */
    protected class Session {

        public UUID id;
        public ClientCallback client;
        public Map<UUID, Job> jobs;

        public Session(ClientCallback client) {
            this.client = client;
            id = UUID.randomUUID();
            jobs = new HashMap<UUID, Job>();
        }
    }

    /**
     * initializes the ProcessManager.
     */
    public ProcessManager() {
        expired = new HashMap<UUID, Job>();
        sessions = new HashMap<UUID, Session>();
    }

    /**
     * Creates a new session for this client and returns the session id.
     *
     * @param client
     * @return the session id.
     */
    @Override
    public synchronized UUID getSession(ClientCallback client) {
        Session session = new Session(client);
        sessions.put(session.id, session);
        Logger.getLogger(ProcessManager.class.getName()).info("created session " + session.id);
        return session.id;
    }

    /**
     * ends the session. All jobs sent to the client are put in the expired
     * queue.
     *
     * @param id
     * @throws SessionExpiredException
     */
    @Override
    public synchronized void endSession(UUID id) throws SessionExpiredException {
        Session session = sessions.get(id);
        if (session != null) {
            for (UUID i : session.jobs.keySet()) {
                expired.put(i, session.jobs.get(i));
            }
            sessions.remove(session.id);
            Logger.getLogger(ProcessManager.class.getName()).info("ended session " + session.id);
        } else {
            throw new SessionExpiredException();
        }
    }

    /**
     * adds a job to the session.
     *
     * @param session
     * @param job
     */
    protected synchronized void addJob(UUID session, Job job) {
        Session s = sessions.get(session);
        s.jobs.put(job.getId(), job);
    }

    /**
     * stops jobs on all clients.
     */
    protected synchronized void stopJobs() {
        for (UUID session : sessions.keySet()) {
            Session s = sessions.get(session);
            try {
                Logger.getLogger(ProcessManager.class.getName()).info("Stopping jobs for " + session);
                s.client.stopJobs();
                for (UUID jobid : s.jobs.keySet()) {
                    Job j = s.jobs.get(jobid);
                    expired.put(jobid, j);
                }
            } catch (RemoteException ex) {
                try {
                    endSession(session);
                } catch (SessionExpiredException ex1) {
                    Logger.getLogger(ProcessManager.class.getName()).log(Level.SEVERE, null, ex1);
                }
            }
        }
    }

    /**
     * causes run to return.
     */
    public synchronized void stop() {
        run = false;
    }

    /**
     * Checks sessions every 5 minutes and expires clients that are
     * unresponsive.
     */
    @Override
    public void run() {
        run = true;

        while (run) {
            synchronized (this) {
                List<UUID> endSessions = new ArrayList<UUID>();
                for (UUID id : sessions.keySet()) {
                    ClientCallback c = sessions.get(id).client;
                    try {
                        ClientStatus status = c.status();
                        Logger.getLogger(ProcessManager.class.getName()).info("got client status for " + id + ": " + status.getSessionID() + " -" + status.getJobStatus());
                        if(status.getSessionID() == null || !status.getSessionID().equals(id)) {
                            endSessions.add(id);
                        }
                    } catch (Exception ex) {
                        endSessions.add(id);
                        Logger.getLogger(ProcessManager.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                for (UUID id : endSessions) {
                    try {
                        endSession(id);
                    } catch (SessionExpiredException ex) {
                        Logger.getLogger(ProcessManager.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
            Logger.getLogger(ProcessManager.class.getName()).info("ProcessManager loop cycled. Waiting 5 minutes.");
            try {
                Thread.sleep(5 * 60 * 1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(ProcessManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        synchronized (this) {
            for (UUID id : sessions.keySet()) {
                try {
                    sessions.get(id).client.stopJobs();
                } catch (RemoteException ex) {
                    Logger.getLogger(ProcessManager.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
}
