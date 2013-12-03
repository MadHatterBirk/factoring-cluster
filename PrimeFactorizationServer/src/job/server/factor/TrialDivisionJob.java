package job.server.factor;

import java.math.BigInteger;
import job.JobStatus;

/**
 * Performs trial division of a number for a specific range. Once the number is
 * factored run returns with leftFactor and rightFactor set. If a factor is not
 * found run returns without setting leftFactor or rightFactor.
 */
public class TrialDivisionJob extends FactorizationJob {

    private BigInteger start;
    private BigInteger end;
    private BigInteger leftFactor;
    private BigInteger rightFactor;

    public TrialDivisionJob(BigInteger number, BigInteger start, BigInteger end) {
        super(number);
        this.start = start;
        this.end = end;
    }

    private synchronized boolean divide(BigInteger i) {
        boolean r = false;
        BigInteger[] divMod = number.divideAndRemainder(i);
        if (divMod[1].equals(BigInteger.ZERO)) {
            leftFactor = i;
            rightFactor = divMod[0];
            r = true;
        }
        //System.out.println(i + " divides " + number + " = " + r);
        return r;
    }

    /**
     * @return the number
     */
    public synchronized BigInteger getNumber() {
        return number;
    }

    /**
     * @return the start
     */
    public synchronized BigInteger getStart() {
        return start;
    }

    /**
     * @return the end
     */
    public synchronized BigInteger getEnd() {
        return end;
    }

    /**
     * @return the leftFactor
     */
    public synchronized BigInteger getLeftFactor() {
        return leftFactor;
    }

    /**
     * @return the rightFactor
     */
    public synchronized BigInteger getRightFactor() {
        return rightFactor;
    }

    @Override
    public void run() {
        System.out.println("Starting " + number + " from " + start + " to " + end + ".");
        synchronized (this) {
            if (status != JobStatus.NEW) {
                return;
            }
            status = JobStatus.RUNNING;
            watch.start();
        }

        //check start is odd
        synchronized (this) {
            if (start.equals(BigInteger.valueOf(2)) && divide(start)) {
                return;
            }
            if (start.mod(BigInteger.valueOf(2)).equals(BigInteger.ZERO)) {
                start = start.add(BigInteger.ONE);
            }
        }

        //check all odd
        BigInteger i = null;
        synchronized (this) {
            i = start;
        }
        for (;; i = i.add(BigInteger.valueOf(2))) {
            synchronized (this) {
                if (status != JobStatus.RUNNING) {
                    watch.stop();
                    return;
                }

                if (i.compareTo(end) >= 0) {
                    status = JobStatus.COMPLETE;
                    watch.stop();
                    return;
                }
                if (divide(i)) {
                    status = JobStatus.COMPLETE;
                    watch.stop();
                    return;
                }
            }
        }
    }

    @Override
    public synchronized String toString() {
        return "TrialDivisionJob " + status + " from " + start + " to " + end + " number:" + number + " = " + leftFactor + " * " + rightFactor;
    }
}
