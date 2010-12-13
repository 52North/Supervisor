/**********************************************************************************
 Copyright (C) 2010
 by 52 North Initiative for Geospatial Open Source Software GmbH

 Contact: Andreas Wytzisk 
 52 North Initiative for Geospatial Open Source Software GmbH
 Martin-Luther-King-Weg 24
 48155 Muenster, Germany
 info@52north.org

 This program is free software; you can redistribute and/or modify it under the
 terms of the GNU General Public License version 2 as published by the Free
 Software Foundation.

 This program is distributed WITHOUT ANY WARRANTY; even without the implied
 WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 General Public License for more details.

 You should have received a copy of the GNU General Public License along with this 
 program (see gnu-gplv2.txt). If not, write to the Free Software Foundation, Inc., 
 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or visit the Free Software
 Foundation web page, http://www.fsf.org.
 
 Created on: 08.01.2010
 *********************************************************************************/

package org.n52.owsSupervisor.util;

import org.apache.log4j.Logger;

/**
 * 
 * Factory for creating instances of {@link IJobScheduler} that work with the timer given in the constructor.
 * 
 * @author Daniel Nüst (daniel.nuest@uni-muenster.de)
 * 
 */
public class JobSchedulerFactoryImpl implements IJobSchedulerFactory {

    private static Logger log = Logger.getLogger(JobSchedulerFactoryImpl.class);

    private TimerServlet timerServlet;

    /**
     * 
     * @param timer
     */
    public JobSchedulerFactoryImpl(TimerServlet timer) {
        this.timerServlet = timer;
        log.info("NEW " + this);
    }

    @Override
    public IJobScheduler getJobScheduler() {
        return new JobSchedulerImpl(this.timerServlet);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("JobSchedulerFactory [timerServlet=");
        sb.append(this.timerServlet);
        sb.append("]");
        return sb.toString();
    }
}
