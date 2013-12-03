package job.server;

/**
 * Used by the JobServer to signal to a client that its session has ended and
 * the current request is invalid.
 */
public class SessionExpiredException extends Exception {
    public SessionExpiredException(String msg) {
        super(msg);
    }
    
    public SessionExpiredException() {
        
    }
}
