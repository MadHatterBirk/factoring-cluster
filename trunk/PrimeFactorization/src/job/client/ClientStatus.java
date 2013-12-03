package job.client;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import job.Job;
import job.JobStatus;

/**
 * Represents the status of a client.
 */
public class ClientStatus implements Serializable {
    private UUID session;
    private Map<UUID, JobStatus> jobStatus;
    
    /**
     * Creates a ClientStatus.
     * @param jobs
     * @param session 
     */
    public ClientStatus(UUID session, Map<UUID, Job> jobs) {
        this.session = session;
        jobStatus = new HashMap<UUID, JobStatus>();
        for(UUID id : jobs.keySet()) {
            jobStatus.put(id, jobs.get(id).getStatus());
        }
        jobStatus = Collections.unmodifiableMap(jobStatus);
    }
    
    /**
     * returns the session id for this client.
     * @return 
     */
    public UUID getSessionID() {
        return session;
    }
    
    /**
     * returns the status of all the jobs running on the client.
     * @return Map<UUID, JobStatus> is an unmodifiable map.
     */
    public Map<UUID, JobStatus> getJobStatus() {
        return jobStatus;
    }
}
