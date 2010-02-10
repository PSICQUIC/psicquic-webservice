/**
 * Copyright 2010 The European Bioinformatics Institute, and others.
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
package org.hupo.psi.mi.psicquic.ws.utils.rdf;

import com.hp.hpl.jena.rdf.model.Model;
import org.junit.Test;
import psidev.psi.mi.xml.PsimiXmlReader;
import psidev.psi.mi.xml.PsimiXmlVersion;
import psidev.psi.mi.xml.model.EntrySet;

import java.io.File;
import java.io.FileOutputStream;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class RdfBuilderTest {

    @Test
    public void testCreateModel() throws Exception {
        PsimiXmlReader reader = new PsimiXmlReader(PsimiXmlVersion.VERSION_254);
        EntrySet entrySet = reader.read(RdfBuilder.class.getResourceAsStream("/META-INF/10380924.xml"));

        RdfBuilder rdfBuilder = new RdfBuilder();
        Model model = rdfBuilder.createModel(entrySet);
        
        model.write(System.out, "N3");
        model.write(new FileOutputStream(new File("c:/test/test.rdf")));
    }
}
