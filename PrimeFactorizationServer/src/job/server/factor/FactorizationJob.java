package job.server.factor;


import java.math.BigInteger;
import job.Job;

/**
 * Abstract job for factoring a number.
 */
public abstract class FactorizationJob extends Job {
    protected BigInteger number;
    
    public synchronized BigInteger getNumber() {
        return number;
    }
    
    public FactorizationJob(BigInteger number) {
        this.number = number;
    }
}
