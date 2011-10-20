/**********************************************************************************
 Copyright (C) 2009
 by 52 North Initiative for Geospatial Open Source Software GmbH

 Contact: Andreas Wytzisk 
 52 North Initiative for Geospatial Open Source Software GmbH
 Martin-Luther-King-Weg 24
 48155 Muenster, Germany
 info@52north.org

 This program is free software; you can redistribute and/or modify it under the
 terms of the GNU General Public License serviceVersion 2 as published by the Free
 Software Foundation.

 This program is distributed WITHOUT ANY WARRANTY; even without the implied
 WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 General Public License for more details.

 You should have received a copy of the GNU General Public License along with this 
 program (see gnu-gplv2.txt). If not, write to the Free Software Foundation, Inc., 
 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or visit the Free Software
 Foundation web page, http://www.fsf.org.
 
 Created on: 13.7.2009
 *********************************************************************************/

package org.n52.owsSupervisor.tasks;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.servlet.GenericServlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.UnavailableException;

import org.apache.log4j.Logger;

/**
 * 
 * This class can be used to execute {@link TimerTask} instances. It runs as a servlet and can be accessed by
 * other servlets for task scheduling and cancelling. The actual service method for GET and POST requests are
 * not implemented. It also provides methods to access the appropriate instances of
 * {@link ICatalogStatusHandler} and {@link ICatalogFactory} for tasks that run within this servlet.
 * 
 * @author Daniel Nüst (daniel.nuest@uni-muenster.de)
 * 
 */
public class TaskServlet extends GenericServlet {

    /**
     * Inner class to handle storage and cancelling of tasks at runtime.
     */
    private class TaskElement {
        protected Date date;
        protected long delay;
        protected String id;
        protected long period;
        protected TimerTask task;

        /**
         * 
         * @param identifier
         * @param task
         * @param delay
         * @param period
         */
        protected TaskElement(String identifier, TimerTask taskP, long delayP, long periodP) {
            this.id = identifier;
            this.task = taskP;
            this.delay = delayP;
            this.period = periodP;
            this.date = new Date(0l);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("TaskElement [");
            sb.append(this.task);
            sb.append(", delay=");
            sb.append(this.delay);
            sb.append(", period=");
            sb.append(this.period);
            sb.append(", date=");
            sb.append(this.date);
            sb.append("]");
            return sb.toString();
        }
    }

    private static final String CHECK_THREAD_COUNT = "CHECK_THREAD_COUNT";

    /**
	 * 
	 */
    private static ScheduledThreadPoolExecutor executor;

    /**
     * The init parameter of the configFile
     */
    private static final String INIT_PARAM_CONFIG_FILE = "configFile";

    private static Logger log = Logger.getLogger(TaskServlet.class);

    /**
     * The identifier that can be used to access the instance of this servlet an run-time.
     */
    public static final String NAME_IN_CONTEXT = "TimerServlet";

    /**
	 * 
	 */
    private static final long serialVersionUID = -7342914044857243635L;

    private Properties props;

    /**
     * List that holds all repeated task during run-time.
     */
    private ArrayList<TaskElement> tasks;

    /**
     * Default constructor.
     */
    public TaskServlet() {
        super();
    }

    /**
     * 
     * @param identifier
     */
    public void cancel(String identifier) {
        for (TaskElement te : this.tasks) {
            if (te.id.equals(identifier)) {
                te.task.cancel();
                log.info("CANCELED " + te);
            }

        }
    }

    @Override
    public void destroy() {
        super.destroy();
        log.info("Destroy " + this.toString());
        executor.shutdownNow();
        executor = null;
        this.tasks.clear();
        this.tasks = null;
    }

    /**
     * @return the tasks
     */
    public ArrayList<TaskElement> getTasks() {
        return this.tasks;
    }

    @Override
    public void init() throws ServletException {
        super.init();
        log.info(" * Initializing Timer ... ");

        ServletContext context = getServletContext();
        context.setAttribute(NAME_IN_CONTEXT, this);

        // get configFile as Inputstream
        InputStream configStream = context.getResourceAsStream(getInitParameter(INIT_PARAM_CONFIG_FILE));
        if (configStream == null) {
            log.fatal("Could not opoen the config file!");
            throw new UnavailableException("Could not open the config file.");
        }

        // load properties file
        try {
            this.props = loadProperties(configStream);
        }
        catch (IOException e) {
            log.fatal("Could not load properties file!");
            throw new UnavailableException("Could not load properties file!");
        }

        // init executor
        int threadCount = Integer.parseInt(this.props.getProperty(CHECK_THREAD_COUNT));
        executor = new ScheduledThreadPoolExecutor(threadCount);

        this.tasks = new ArrayList<TaskServlet.TaskElement>();

        log.info(" ***** Timer initiated successfully! ***** ");
    }

    /**
     * method loads the config file
     * 
     * @param is
     *        InputStream containing the config file
     * @return Returns properties of the given config file
     * @throws IOException
     */
    private Properties loadProperties(InputStream is) throws IOException {
        Properties properties = new Properties();
        properties.load(is);

        return properties;
    }

    @Override
    public void service(ServletRequest req, ServletResponse res) {
        throw new UnsupportedOperationException("Not supperted by TimerServlet!");
    }

    /**
     * 
     * @param identifier
     * @param task
     * @param delay
     */
    public void submit(String identifier, TimerTask task, long delay) {
        executor.schedule(task, delay, TimeUnit.MILLISECONDS);
        if (log.isDebugEnabled()) {
            log.debug("Submitted: " + task + " with delay = " + delay);
        }
        this.tasks.add(new TaskElement(identifier, task, delay, 0l));
    }

    /**
     * 
     * " Finally, fixed-rate execution is appropriate for scheduling multiple repeating timer tasks that must
     * remain synchronized with respect to one another." See
     * {@link Timer#scheduleAtFixedRate(TimerTask, long, long)} for details.
     * 
     * @param task
     * @param delay
     * @param period
     */
    public void submit(String identifier, TimerTask task, long delay, long period) {
        executor.scheduleAtFixedRate(task, delay, period, TimeUnit.MILLISECONDS);
        if (log.isDebugEnabled()) {
            log.debug("Submitted: " + task + " with period = " + period + ", delay = " + delay);
        }
        this.tasks.add(new TaskElement(identifier, task, delay, period));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("TimerServlet [executor: ");
        sb.append(executor.toString());
        sb.append(" -- ");
        sb.append("tasks: ");
        for (TaskElement te : this.tasks) {
            sb.append(te);
            sb.append(", ");
        }
        sb.delete(sb.length() - 1, sb.length() - 1);
        sb.append("]");
        return sb.toString();
    }
}
