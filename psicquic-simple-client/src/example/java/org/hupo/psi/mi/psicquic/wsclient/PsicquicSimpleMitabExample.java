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
package org.hupo.psi.mi.psicquic.wsclient;

import org.hupo.psi.mi.psicquic.wsclient.PsicquicSimpleClient;
import psidev.psi.mi.jami.binary.BinaryInteraction;
import psidev.psi.mi.jami.tab.io.parser.BinaryLineParser;
import psidev.psi.mi.jami.tab.io.parser.ParseException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Example showing the use of the psimitab library to create an object model from the results.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class PsicquicSimpleMitabExample {

    public static void main(String[] args) throws Exception {
        // get a REST URl from the registry http://www.ebi.ac.uk/Tools/webservices/psicquic/registry/registry?action=STATUS
        
        PsicquicSimpleClient client = new PsicquicSimpleClient("http://www.ebi.ac.uk/Tools/webservices/psicquic/intact/webservices/current/search/");

        try {
            InputStream result = client.getByQuery("brca2");

            BinaryLineParser parser = new BinaryLineParser(result);

            Collection<BinaryInteraction> binaryInteractions = new ArrayList<BinaryInteraction>();

            while(!parser.hasFinished()){
                binaryInteractions.add(parser.MitabLine());
            }

            System.out.println("Interactions found: "+binaryInteractions.size());

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e){
            e .printStackTrace();
        }
    }

}
