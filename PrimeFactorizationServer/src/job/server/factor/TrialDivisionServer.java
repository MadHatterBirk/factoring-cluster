/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package job.server.factor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.text.NumberFormat;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import job.server.JobServer;

/**
 *
 * @author john
 */
public class TrialDivisionServer {

    private TrialDivisionManager manager;
    private Thread managerThread;

    /**
     * creates a trial division server.
     */
    public TrialDivisionServer() {
    }

    /**
     * adds the server to the rmi registry.
     *
     * @throws RemoteException
     */
    public void init() throws RemoteException {
        
        // Show the Server IP address
        String hostname = null;
        try {
            hostname = InetAddress.getLocalHost().getHostAddress();
            System.out.println("\nServer IP Address: " + hostname);
        } catch (UnknownHostException ex) {
            Logger.getLogger(TrialDivisionServer.class.getName()).log(Level.SEVERE, null, ex);
        }

        // Setup the java VM properties for codebase
        String codebaseServer = TrialDivisionManager.class.getProtectionDomain().getCodeSource().getLocation().toString();
        String codebaseShared = JobServer.class.getProtectionDomain().getCodeSource().getLocation().toString();
        System.out.println("\nServer URL : " + codebaseServer);
        System.out.println("\nShared URL : " + codebaseShared + "\n");
        System.setProperty("java.rmi.server.codebase", codebaseServer + " " + codebaseShared);
        
        // Set the hostname
        System.out.println("\nHostname: " + hostname);
        System.setProperty("java.rmi.server.hostname", hostname);
        
        // Set java VM property useCodebaseOnly=false
        System.setProperty("java.rmi.server.useCodebaseOnly", "false");


        // Create a simple security file in a known location, and use that
        final String POLICY_FILE_CONTENT = "grant {\n"
                + "permission java.security.AllPermission;\n" + "};\n";
        try {
            File tempFile = File.createTempFile("PrimeFactorization", ".policy");
            BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
            writer.write(POLICY_FILE_CONTENT);
            writer.close();
            tempFile.deleteOnExit();
            System.setProperty("java.security.policy", tempFile.getAbsolutePath());
            if (System.getSecurityManager() == null) {
                System.setSecurityManager(new SecurityManager());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        manager = new TrialDivisionManager();
        managerThread = new Thread(manager);
//        if (System.getSecurityManager() == null) {
//            System.setSecurityManager(new SecurityManager());
//        }

        JobServer stub = (JobServer) UnicastRemoteObject.exportObject(manager, 0);
        Registry registry = LocateRegistry.createRegistry(1099); // programatically start the rmi registry
        registry.rebind("JobServer", stub);
        managerThread.start();
    }

    /**
     * displays the menu to the user and continues to check the status of the
     * number as the server works on solving it.
     */
    public void menu() {
        while (true) {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            NumberFormat nf = NumberFormat.getInstance();
            while (true) {
                System.out.println("Please enter a positive number: ");
                try {
                    String s = br.readLine();
                    BigInteger number = new BigInteger(s);
                    manager.setNumber(number);
                } catch (IOException ex) {
                    Logger.getLogger(TrialDivisionServer.class.getName()).log(Level.SEVERE, null, ex);
                    continue;
                } catch (NumberFormatException ex) {
                    Logger.getLogger(TrialDivisionServer.class.getName()).log(Level.SEVERE, null, ex);
                    continue;
                }
                BigDecimal percent = manager.currenNumberPercentComplete();
                while (true) {
                    synchronized (manager) {
                        if (manager.getSolution().isComplete()) {
                            break;
                        }
                        BigDecimal next = manager.currenNumberPercentComplete();
                        if (!percent.equals(next)) {
                            percent = next;
                            System.out.println("Working on " + manager.getCurrentNumber() + "... " + manager.currenNumberPercentComplete() + "%");
                            System.out.println("factors so far: " + manager.getSolution().getLeaves());
                        }
                    }
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(TrialDivisionServer.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                synchronized (manager) {
                    FactorTree solution = manager.getSolution();
                    Map<BigInteger, Integer> leaves = solution.getLeaves();
                    System.out.println("Factors for " + solution.getNumber() + " are: " + leaves);
                    for (BigInteger bi : leaves.keySet()) {
                        if (solution.isPrime(bi)) {
                            System.out.println(bi + " is prime.");
                        }
                    }
                }
            }
        }
    }

    public static void main(String... args) {
        TrialDivisionServer server = new TrialDivisionServer();
        while (true) {
            try {
                server.init();
                server.menu();
            } catch (RemoteException ex) {
                Logger.getLogger(TrialDivisionServer.class.getName()).log(Level.SEVERE, null, ex);
            }
            Logger.getLogger(TrialDivisionServer.class.getName()).info("Attempting to connect to registry again in 10 seconds.");
            try {
                Thread.sleep(10 * 1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(TrialDivisionServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
