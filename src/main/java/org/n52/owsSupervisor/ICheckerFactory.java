/**
 * ﻿Copyright (C) 2013 52°North Initiative for Geospatial Open Source Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.n52.owsSupervisor;

import java.util.Collection;



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
