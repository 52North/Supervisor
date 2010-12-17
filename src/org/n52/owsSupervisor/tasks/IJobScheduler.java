/**********************************************************************************
 Copyright (C) 2009
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
 
 Created on: 13.7.2009
 *********************************************************************************/

package org.n52.owsSupervisor.tasks;

import org.n52.owsSupervisor.checks.IServiceChecker;

/**
 * 
 * @author Daniel Nüst (daniel.nuest@uni-muenster.de)
 * 
 */
public interface IJobScheduler {

    /**
     * Cancels the task with the given identifier.
     * 
     * See {@link java.util.TimerTask#cancel()} for details.
     * 
     * @param identifier
     */
    public abstract void cancel(String identifier);

    /**
     * 
     * @param checker
     * @return the id
     */
    public abstract String submit(IServiceChecker checker);

}