/**
 * 
 */

package org.n52.owsSupervisor.checks;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;

import org.apache.log4j.Logger;
import org.n52.owsSupervisor.ICheckResult;
import org.n52.owsSupervisor.ICheckResult.ResultType;
import org.n52.owsSupervisor.Supervisor;
import org.n52.owsSupervisor.ui.EmailNotification;
import org.n52.owsSupervisor.ui.INotification;

/**
 * 
 * this check collects information about currently running checks and the state of the service.
 * 
 * @author Daniel Nüst (d.nuest@52north.org)
 * 
 */
public class SelfCheck extends AbstractServiceCheck {

    private static final long L1024_2 = 1024 * 1024;

    private static Logger log = Logger.getLogger(SelfCheck.class);

    private String message;

    /**
     * 
     * @param serviceURL
     * @param notifyEmail
     * @param checkInterval
     * @throws MalformedURLException
     */
    public SelfCheck(String serviceURL, String notifyEmail, String checkInterval) throws MalformedURLException {
        this(notifyEmail, new URL(serviceURL), Long.valueOf(checkInterval).longValue());
    }

    /**
     * 
     * @param notifyEmail
     * @param serviceURL
     * @param checkInterval
     */
    public SelfCheck(String notifyEmail, URL serviceURL, long checkInterval) {
        super(notifyEmail, serviceURL, checkInterval);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.n52.owsSupervisor.checks.IServiceChecker#check()
     */
    @Override
    public boolean check() {
        // check if everything is running fine...
        long heapSize = Runtime.getRuntime().totalMemory();
        long heapMaxSize = Runtime.getRuntime().maxMemory();
        long heapFreeSize = Runtime.getRuntime().freeMemory();

        StringBuilder sb = new StringBuilder();

        sb.append("Self check ran succesfully, service is most probably up and running.\n Go to <a href='");
        sb.append(getServiceURL().toString());
        sb.append("' title='OwsSupervisor HTML Interface'>");
        sb.append(getServiceURL().toString());
        sb.append("</a>");
        sb.append(" for the current check status.");

        sb.append(" *** Heap Info: Size (Mb) is ");
        sb.append(heapSize / L1024_2);
        sb.append(" of ");
        sb.append(heapMaxSize / L1024_2);
        sb.append(" leaving ");
        sb.append(heapFreeSize / L1024_2);
        sb.append(".");

        this.message = sb.toString();

        // TODO add currently running tasks and their last message

        addResult(new ServiceCheckResult(getService(), this.message, ResultType.POSITIVE));

        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.n52.owsSupervisor.checks.AbstractServiceCheck#notifyFailure()
     */
    @Override
    public void notifyFailure() {
        log.fatal("SelfChecker cannot fail!");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.n52.owsSupervisor.checks.AbstractServiceCheck#notifySuccess()
     */
    @Override
    public void notifySuccess() {
        // send email
        if (log.isDebugEnabled())
            log.debug("Check SUCCESSFUL: " + this);

        if (getEmail() == null) {
            log.error("Can not notify via email, is null!");
            return;
        }

        Collection<ICheckResult> results = getResults();

        INotification noti = new EmailNotification(getServiceURL().toString(), getEmail(), results);
        // append for email notification to queue
        Supervisor.appendNotification(noti);

        if (log.isDebugEnabled())
            log.debug("Submitted email with " + results.size() + " successes.");
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "SelfCheck [" + getServiceURL() + ", email=" + getEmail() + ", check interval=" + getCheckIntervalMillis();
    }

}
