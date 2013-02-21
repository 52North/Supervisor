/**
 * ﻿Copyright (C) 2013
 * by 52 North Initiative for Geospatial Open Source Software GmbH
 *
 * Contact: Andreas Wytzisk
 * 52 North Initiative for Geospatial Open Source Software GmbH
 * Martin-Luther-King-Weg 24
 * 48155 Muenster, Germany
 * info@52north.org
 *
 * This program is free software; you can redistribute and/or modify it under
 * the terms of the GNU General Public License version 2 as published by the
 * Free Software Foundation.
 *
 * This program is distributed WITHOUT ANY WARRANTY; even without the implied
 * WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program (see gnu-gpl v2.txt). If not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
 * visit the Free Software Foundation web page, http://www.fsf.org.
 */
package org.n52.owsSupervisor.checks;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.Logger;
import org.n52.owsSupervisor.ICheckResult;
import org.n52.owsSupervisor.IServiceChecker;

/**
 * @author Daniel Nüst
 * 
 */
public class CheckerCollection implements IServiceChecker {

    private static Logger log = Logger.getLogger(CheckerCollection.class);

    private Collection<IServiceChecker> checkers = new ArrayList<IServiceChecker>();

    /**
     * 
     * @param checkers
     */
    public CheckerCollection(Collection<IServiceChecker> checkersP) {
        this.checkers = checkersP;
    }

    /*
     * (non-Javadoc)
     * @see org.n52.owsSupervisor.checks.IServiceChecker#addResult(org.n52.owsSupervisor.checks.ICheckResult)
     */
    @Override
    public void addResult(ICheckResult r) {
        throw new UnsupportedOperationException("Collection of checkers, which can contain different intervals!");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.n52.owsSupervisor.IServiceChecker#check()
     */
    @Override
    public boolean check() {
        log.debug("Checking collection of " + this.checkers.size());

        boolean b = true;
        int success = 0;
        int failure = 0;
        for (IServiceChecker c : this.checkers) {
            if ( !c.check()) {
                b = false;
                failure++;
            }
            else {
                success++;
            }
        }

        log.debug("Checked collection: " + success + " successful and " + failure + " failed checks.");

        return b;
    }

    /**
     * 
     * @return
     */
    public Collection<IServiceChecker> getCheckers() {
        return this.checkers;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.n52.owsSupervisor.IServiceChecker#getCheckIntervalMillis()
     */
    @Override
    public long getCheckIntervalMillis() {
        throw new UnsupportedOperationException("Collection of checkers, which can contain different intervals!");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.n52.owsSupervisor.IServiceChecker#getResults()
     */
    @Override
    public Collection<ICheckResult> getResults() {
        ArrayList<ICheckResult> results = new ArrayList<ICheckResult>();
        for (IServiceChecker c : this.checkers) {
            results.addAll(c.getResults());
        }
        return results;
    }

    /*
     * (non-Javadoc)
     * @see org.n52.owsSupervisor.checks.IServiceChecker#getService()
     */
    @Override
    public String getService() {
        throw new UnsupportedOperationException("Collection of checkers, which can multiple services!");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.n52.owsSupervisor.IServiceChecker#notifyFailure()
     */
    @Override
    public void notifyFailure() {
        for (IServiceChecker c : this.checkers) {
            c.notifyFailure();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.n52.owsSupervisor.checks.IServiceChecker#notifySuccess()
     */
    @Override
    public void notifySuccess() {
        for (IServiceChecker c : this.checkers) {
            c.notifySuccess();
        }
    }

}
