package job;


import java.io.Serializable;
import java.util.UUID;

/**
 * Top level Job class. Provides basic attributes and methods that every job 
 * should have.
 * 
 * Implementing a Job should follow these rules:
 * Implement the StopWatch to generate the total running time for the Job.
 *      - call start at beginning of run and stop at end.
 * Make sure the JobStatus represents the current state of the Job.
 *      - set to RUNNING when run starts.
 *      - set to COMPLETE when run is finished.
 * Check JobStatus is always RUNNING in all loops and long running processes and return from run if set to STOPPED.
 */
public abstract class Job implements Runnable, Serializable {
    private UUID id;
    protected StopWatch watch;
    protected JobStatus status;
    
    /**
     * Initialize data members. id will become a randomUUID, status is NEW.
     */
    public Job() {
        id = UUID.randomUUID();
        status = JobStatus.NEW;
        watch = new StopWatch();
    }
    
    /**
     * return the id.
     * @return id for Job.
     */
    public synchronized UUID getId() {
        return id;
    }
    
    /**
     * return the time spent in nano seconds.
     * @return 
     */
    public synchronized long getTime() {
        return watch.getTime();
    }
    
    /**
     * return the time string for this job.
     */
    public synchronized String getTimeString() {
        return watch.toString();
    }
    
    public synchronized JobStatus getStatus() {
        return status;
    }
    
    /**
     * stops this job. Should cause run to return immediately.
     */
    public synchronized void stop() {
        status = JobStatus.STOPPED;
    }
}
