package job.client;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.RemoteException;
import job.server.JobServer;

/**
 * Creates a JobClient and runs it.
 */
public class JobClientApplication {

    public static void main(String... args) throws RemoteException {
//        if (System.getSecurityManager() == null) {
//            System.setSecurityManager(new RMISecurityManager());
//        }

        // Set java VM property useCodebaseOnly=false
        System.setProperty("java.rmi.server.useCodebaseOnly", "false");
        
        //Set the codebase for the client
        String codebaseServer = JobClient.class.getProtectionDomain().getCodeSource().getLocation().toString();
        String codebaseShared = JobServer.class.getProtectionDomain().getCodeSource().getLocation().toString();
        System.out.println("\nClient URL : " + codebaseServer);
        System.out.println("\nShared URL : " + codebaseShared);
//        System.setProperty ("java.rmi.server.codebase", codebaseServer + " " + codebaseShared);

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
        
        JobClient client = new JobClient(7);
        client.run();
    }
}
