package job.server.factor;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import job.Job;
import java.util.UUID;
import java.util.logging.Logger;
import job.server.SessionExpiredException;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author john
 */
public class TrialDivisionManager extends FactorizationManager {

    private BigInteger currentNumber;
    private BigInteger nextStart;
    private BigInteger currentMax;

    /**
     * @param currentNumber the currentNumber to test with TrialDivisionJobs
     */
    private synchronized void setCurrentNumber(BigInteger currentNumber) {
        this.currentNumber = currentNumber;
        nextStart = BigInteger.valueOf(2);
        currentMax = sqrt(currentNumber).add(BigInteger.ONE);
        Logger.getLogger(TrialDivisionManager.class.getName()).info("set currentNumber to " + currentNumber + ", nextStart to " + nextStart + ", currentMax to " + currentMax);

    }
    
    public synchronized BigInteger getCurrentNumber() {
        return currentNumber;
    }

    /**
     * return the percentage complete for the current number.
     * @return 
     */
    public synchronized BigDecimal currenNumberPercentComplete() {
        BigInteger value = nextStart;
        for (UUID session : sessions.keySet()) {
            Session s = sessions.get(session);
            for (UUID job : s.jobs.keySet()) {
                TrialDivisionJob j = (TrialDivisionJob) s.jobs.get(job);
                if (j.getStart().compareTo(value) < 0) {
                    value = j.getStart();
                }
            }
        }
        for (UUID job : expired.keySet()) {
            TrialDivisionJob j = (TrialDivisionJob) expired.get(job);
            if (j.getStart().compareTo(value) < 0) {
                value = j.getStart();
            }
        }
        return new BigDecimal(value).divide(new BigDecimal(currentMax), MathContext.DECIMAL32).multiply(BigDecimal.valueOf(100));

    }

    /**
     * return the square root of BigInteger x.
     * @param x
     * @return 
     */
    //from http://stackoverflow.com/a/11962756
    private static BigInteger sqrt(BigInteger x) {

        if (x.compareTo(BigInteger.ZERO) < 0) {
            throw new IllegalArgumentException("Negative argument.");
        }
        // square roots of 0 and 1 are trivial and
        // y == 0 will cause a divide-by-zero exception
        if (x.equals(BigInteger.ZERO) || x.equals(BigInteger.ONE)) {
            return x;
        } // end if
        BigInteger y;
        // starting with y = x / 2 avoids magnitude issues with x squared
        for (y = x.shiftRight(1);
                y.compareTo(x.divide(y)) > 0;
                y = ((x.divide(y)).add(y)).shiftRight(1));
        return y;
    }

    /**
     * Sets the current problem the server should work on.
     * @param number 
     */
    public synchronized void setNumber(BigInteger number) {
        super.setNumber(number);
        setCurrentNumber(number);
    }

    /**
     * returns the next job to be worked on to a JobClient.
     * @param id
     * @return 
     */
    @Override
    public synchronized Job getNextJob(UUID id) throws SessionExpiredException {
        
        Session s = sessions.get(id);
        if(s == null) {
            Logger.getLogger(TrialDivisionManager.class.getName()).info("session does not exist " + id);
            throw new SessionExpiredException("session does not exist " + id);
        }

        //solution found. waiting for next problem.
        if (solution == null || solution.isComplete()) {
            Logger.getLogger(TrialDivisionManager.class.getName()).info("waiting for next problem.");
            return null;
        }

        //work on expired jobs
        if (expired.size() > 0) {
            Job j = expired.get(expired.keySet().iterator().next());
            expired.remove(j.getId());
            addJob(id, j);
            Logger.getLogger(TrialDivisionManager.class.getName()).info("Using expired job. " + j);
            return j;
        }

        //all jobs have been issued.
        if (nextStart.compareTo(currentMax) >= 0) {
            Logger.getLogger(TrialDivisionManager.class.getName()).info("All Jobs have been issued.");
            return null;
        }

        //create new job with range from nextStart up to currentMax.
        BigInteger start = nextStart;
        BigInteger end = nextStart.add(BigInteger.valueOf(500000000));
        if (end.compareTo(currentMax) > 0) {
            end = currentMax;
        }
        Job j = new TrialDivisionJob(currentNumber, start, end);

        //set next start for next job created.
        nextStart = end;
        Logger.getLogger(TrialDivisionManager.class.getName()).info("created " + j);
        return j;
    }

    /**
     * sets up the next next number to be factored. This is called when a solution
     * is found in returnJob.
     */
    private synchronized void setupNextNumber() {
        BigInteger nextNumber = solution.getNextUnsolvedNumber();
        while (nextNumber != null && (nextNumber.equals(BigInteger.valueOf(2)) || nextNumber.equals(BigInteger.valueOf(3)))) {
            solution.setPrime(nextNumber, true);
            Logger.getLogger(TrialDivisionManager.class.getName()).info("found " + nextNumber + " is prime.");
            nextNumber = solution.getNextUnsolvedNumber();
        }
        if (nextNumber != null) {
            setCurrentNumber(nextNumber);
        }

        List<UUID> remove = new ArrayList<UUID>();
        for (UUID jid : expired.keySet()) {
            TrialDivisionJob exJob = (TrialDivisionJob) expired.get(jid);
            if (!exJob.getNumber().equals(currentNumber)) {
                remove.add(jid);
            }
        }
        for (UUID i : remove) {
            expired.remove(i);
        }
        remove.clear();
        for (UUID i : sessions.keySet()) {
            Session s2 = sessions.get(i);
            for (UUID j : s2.jobs.keySet()) {
                TrialDivisionJob k = (TrialDivisionJob) s2.jobs.get(j);
                if (!k.getNumber().equals(currentNumber)) {
                    remove.add(j);
                }
            }
            for (UUID r : remove) {
                s2.jobs.remove(r);
            }
            remove.clear();
        }
    }

    /**
     * called by a client when a job is being returned.
     * @param id
     * @param job 
     */
    @Override
    public synchronized void returnJob(UUID id, Job job) throws SessionExpiredException {
        TrialDivisionJob complete = (TrialDivisionJob) job;

        //remove job from session.
        Session s = sessions.get(id);
        if(s == null) {
            Logger.getLogger(TrialDivisionManager.class.getName()).info("session does not exist " + id);
            throw new SessionExpiredException("session does not exist " + id);
        }
        s.jobs.remove(job.getId());

        //ignore returning old jobs.
        if (!complete.getNumber().equals(currentNumber)) {
            Logger.getLogger(TrialDivisionManager.class.getName()).info("returned old job " + complete);
            return;
        }

        //figure out how many jobs are out for currentNumber.
        int issuedJobs = 0;
        for (UUID i : expired.keySet()) {
            TrialDivisionJob j = (TrialDivisionJob) expired.get(i);
            if (currentNumber.equals(j.getNumber())) {
                issuedJobs++;
            }
        }
        for (UUID i : sessions.keySet()) {
            Session s2 = sessions.get(i);
            for (UUID j : s2.jobs.keySet()) {
                TrialDivisionJob k = (TrialDivisionJob) s2.jobs.get(j);
                if (currentNumber.equals(k.getNumber())) {
                    issuedJobs++;
                }
            }
        }
        Logger.getLogger(TrialDivisionManager.class.getName()).info("there are " + issuedJobs + " jobs issued for " + currentNumber);

        //if a solution has been found add it and start next number or if the
        //entire range has been checked and no issued jobs remain. The number
        //must be prime.
        if (complete.getLeftFactor() != null && complete.getRightFactor() != null) {
            solution.setFactors(complete.getNumber(), complete.getLeftFactor(), complete.getRightFactor());
            Logger.getLogger(TrialDivisionManager.class.getName()).info("returned solution " + complete);
            stopJobs();
            setupNextNumber();
        } else if (nextStart.equals(currentMax) && issuedJobs == 0) {
            solution.setPrime(currentNumber, true);
            Logger.getLogger(TrialDivisionManager.class.getName()).info("returned last job found " + currentNumber + " is prime. " + complete);
            stopJobs();
            setupNextNumber();
        } else {
            Logger.getLogger(TrialDivisionManager.class.getName()).info("returned job with no solution. " + complete);
        }

    }
}
