/*
 * Copyright 2007 Guy Van den Broeck
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
package org.outerj.daisy.diff;

import org.xml.sax.ContentHandler;

import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;

public class XslFilter {

    public ContentHandler xsl(ContentHandler consumer, String xslPath)
            throws IOException {

        try {
            // Create transformer factory
            TransformerFactory factory = TransformerFactory.newInstance();

            // Use the factory to create a template containing the xsl file
            System.out.println(xslPath);

            StreamSource ss = new StreamSource(
                    getClass().getClassLoader().getResourceAsStream(xslPath));

            Templates template = factory.newTemplates(ss);

            // Use the template to create a transformer
            TransformerFactory transFact = TransformerFactory.newInstance();
            SAXTransformerFactory saxTransFact = (SAXTransformerFactory) transFact;
            // create a ContentHandler
            TransformerHandler transHand = saxTransFact
                    .newTransformerHandler(template);

            transHand.setResult(new SAXResult(consumer));

            return transHand;

        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (TransformerFactoryConfigurationError e) {
            e.printStackTrace();
        }
        throw new IllegalStateException("Can't transform xml.");

    }

}
