package musings;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import job.server.JobServer;
import job.server.factor.TrialDivisionManager;
import job.server.factor.TrialDivisionServer;

/**
 *
 * @author john
 */
public class GeneratePrimes {

    private TrialDivisionManager manager;
    private Thread managerThread;

    public GeneratePrimes() {
        manager = new TrialDivisionManager();
        managerThread = new Thread(manager);

    }

    public void init() throws RemoteException {
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }

        JobServer stub = (JobServer) UnicastRemoteObject.exportObject(manager, 0);
        Registry registry = LocateRegistry.getRegistry();
        registry.rebind("JobServer", stub);
        managerThread.start();
    }

    public boolean isPrime(BigInteger bi) {
        manager.setNumber(bi);
        BigDecimal percent = manager.currenNumberPercentComplete();
        while (!manager.getSolution().isComplete()) {

            BigDecimal next = manager.currenNumberPercentComplete();
            if (!percent.equals(next)) {
                percent = next;
                System.out.println("Working... " + manager.currenNumberPercentComplete() + "%");
                System.out.println("factors so far: " + manager.getSolution().getLeaves());
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException ex) {
                Logger.getLogger(TrialDivisionServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        Map<BigInteger, Integer> leaves = manager.getSolution().getLeaves();
        System.out.println("Factors for " + manager.getSolution().getNumber() + " are: " + leaves);
        if(leaves.size() == 1 && leaves.get(bi) != null && leaves.get(bi) > 0) {
            return true;
        }
        return false;
        
    }

    public List<BigInteger> getPrimes(BigInteger start, BigInteger end) {
        List<BigInteger> found = new ArrayList<BigInteger>();
        for(BigInteger i = BigInteger.valueOf(Long.MAX_VALUE); i.compareTo(end) < 0; i = i.add(BigInteger.ONE)) {
            boolean prime = isPrime(i);
            System.out.println(i + (prime ? " is prime." : " is not prime."));
            if(prime) {
                found.add(i);
            }
        }
        return found;
    }

    public static void main(String... args) throws RemoteException {
        GeneratePrimes server = new GeneratePrimes();
        server.init();
        List<BigInteger> primes = server.getPrimes(BigInteger.valueOf(Long.MAX_VALUE), BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.TEN.pow(2)));
        System.out.println(primes);
    }
}
