/**
 * ﻿Copyright (C) 2013 - 2014 52°North Initiative for Geospatial Open Source Software GmbH
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
package org.n52.supervisor.checks;

import org.n52.supervisor.api.Check;
import org.n52.supervisor.api.CheckRunner;
import org.n52.supervisor.checks.util.HeapCheck;
import org.n52.supervisor.checks.util.HeapCheckRunner;
import org.n52.supervisor.checks.util.SelfCheck;
import org.n52.supervisor.checks.util.SelfCheckRunner;

public class BasicRunnerFactory implements RunnerFactory {

	@Override
	public CheckRunner resolveRunner(Check check) {
		CheckRunner r = null;
        
		if (check instanceof HeapCheck) {
            final HeapCheck hc = (HeapCheck) check;
            r = new HeapCheckRunner(hc);
        }
        else if (check instanceof SelfCheck) {
            final SelfCheck sc = (SelfCheck) check;
            r = new SelfCheckRunner(sc);
        }
        
        return r;
	}

}
