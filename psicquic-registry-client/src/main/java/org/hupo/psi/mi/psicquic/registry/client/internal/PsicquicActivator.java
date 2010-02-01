/**
 * Copyright 2009 The European Bioinformatics Institute, and others.
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
package org.hupo.psi.mi.psicquic.registry.client.internal;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;


/**
 * Extension of the default OSGi bundle activator
 */
public final class PsicquicActivator implements BundleActivator {
    
    /**
     * Called whenever the OSGi framework starts our bundle
     */
    public void start(BundleContext bc) throws Exception {
        System.out.println("STARTING PSICQUIC Registry Client");
    }

    /**
     * Called whenever the OSGi framework stops our bundle
     */
    public void stop(BundleContext bc) throws Exception {
        System.out.println("STOPPING PSICQUIC Registry Client");

        // no need to unregister our service - the OSGi framework handles it for us
    }
}

