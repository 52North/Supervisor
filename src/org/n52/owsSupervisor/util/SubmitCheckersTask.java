/*******************************************************************************
Copyright (C) 2010
by 52 North Initiative for Geospatial Open Source Software GmbH

Contact: Andreas Wytzisk
52 North Initiative for Geospatial Open Source Software GmbH
Martin-Luther-King-Weg 24
48155 Muenster, Germany
info@52north.org

This program is free software; you can redistribute and/or modify it under 
the terms of the GNU General Public License serviceVersion 2 as published by the 
Free Software Foundation.

This program is distributed WITHOUT ANY WARRANTY; even without the implied
WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License along with
this program (see gnu-gpl v2.txt). If not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
visit the Free Software Foundation web page, http://www.fsf.org.

Author: Daniel Nüst
 
 ******************************************************************************/

package org.n52.owsSupervisor.util;

import java.util.Collection;
import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.n52.owsSupervisor.checks.IServiceChecker;
import org.n52.owsSupervisor.tasks.IJobScheduler;

/**
 * @author Daniel Nüst (daniel.nuest@uni-muenster.de)
 * 
 */
public class SubmitCheckersTask extends TimerTask {

    private static Logger log = Logger.getLogger(SubmitCheckersTask.class);

    private Collection<IServiceChecker> checkers;
    
    private IJobScheduler scheduler;

    /**
     * @param scheduler
     * @param checkers
     * @param i
     */
    public SubmitCheckersTask(IJobScheduler schedulerP, Collection<IServiceChecker> checkersP) {
        this.checkers = checkersP;
        this.scheduler = schedulerP;
        
        log.info("NEW " + this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.TimerTask#run()
     */
    @Override
    public void run() {
        log.info("Submitting checkers.");
        
        for (IServiceChecker c : this.checkers) {
            this.scheduler.submit(c);
        }
    }

}
