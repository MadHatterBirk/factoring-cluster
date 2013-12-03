package job.server.factor;

import java.math.BigInteger;
import job.server.ProcessManager;

/**
 * Abstract class for FactorizationManager. This is a ProcessManager for factoring
 * a number.
 */
public abstract class FactorizationManager extends ProcessManager {
    private BigInteger number;
    protected FactorTree solution;
    
    public synchronized void setNumber(BigInteger number) {
        this.number = number;
        solution = new FactorTree(number);
    }
    
    public synchronized FactorTree getSolution() {
        return solution;
    }
}
