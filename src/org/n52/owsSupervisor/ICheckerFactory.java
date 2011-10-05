/*******************************************************************************
Copyright (C) 2011
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

package org.n52.owsSupervisor;

import java.util.Collection;

import org.n52.owsSupervisor.checks.IServiceChecker;


/**
 * 
 * @author Daniel Nüst (d.nuest@52north.org)
 * 
 */
public interface ICheckerFactory {

    public static final long EVERY_12_HOURS = 1000 * 60 * 60 * 12;

    public static final long EVERY_24_HOURS = 1000 * 60 * 60 * 24;

    public static final long EVERY_HALF_HOUR = 1000 * 60 * 30;

    public static final long EVERY_HOUR = 1000 * 60 * 60;

    public static final long EVERY_WEEK = 1000 * 60 * 60 * 24 * 7;
    
    /**
     * 
     * @return
     */
    public abstract Collection<IServiceChecker> getCheckers();

}
