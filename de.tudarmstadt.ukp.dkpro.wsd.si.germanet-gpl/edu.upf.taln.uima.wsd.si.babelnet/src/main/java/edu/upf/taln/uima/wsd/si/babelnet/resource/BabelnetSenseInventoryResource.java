/*******************************************************************************
 * Copyright 2016
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

/**
 *
 */
package edu.upf.taln.uima.wsd.si.babelnet.resource;

import java.util.Map;

import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;

import de.tudarmstadt.ukp.dkpro.wsd.si.SenseInventoryException;
import de.tudarmstadt.ukp.dkpro.wsd.si.resource.SenseInventoryResourceBase;
import edu.upf.taln.uima.wsd.si.babelnet.BabelnetSenseInventory;
import it.uniroma1.lcl.babelnet.BabelNet;
import it.uniroma1.lcl.jlt.util.Language;


/**
 * A resource wrapping {@link BabelnetSenseInventory}
 *
 * @author <a href="mailto:miller@ukp.informatik.tu-darmstadt.de">Tristan Miller</a>
 *
 */
public class BabelnetSenseInventoryResource
    extends SenseInventoryResourceBase
{
    public static final String PARAM_BABELNET_CONFIGPATH = "babelNetPath";
    @ConfigurationParameter(name = PARAM_BABELNET_CONFIGPATH, description = "Path where the jlt.properties and babelnet.properties are located", mandatory = true)
    protected String babelNetPath;

    public static final String PARAM_BABELNET_LANG = "babelNetLang";
    @ConfigurationParameter(name = PARAM_BABELNET_LANG, description = "Language used to search in Babelnet", mandatory = true)
    protected Language babelNetLang;

    public static final String PARAM_BABELNET_DESCLANG = "babelNetDescLang";
    @ConfigurationParameter(name = PARAM_BABELNET_DESCLANG, description = "Language used to write the description in Babelnet", mandatory = true)
    protected Language babelNetDescLang;

    public static final String PARAM_babelNet_LEXICON = "babelNetLexicon";
    @ConfigurationParameter(name = PARAM_babelNet_LEXICON, description = "Lexicon to use with BabelNet; if null or none all available lexicons will be used", mandatory = false)
    protected String babelNetLexicon=null;


    /**
     * Returns the underlying {@link BabelNet} object.
     *
     * @return the underlying {@link BabelNet} object
     */
    public BabelNet getUnderlyingResource() {
        return ((BabelnetSenseInventory) inventory).getUnderlyingResource();
    }
    
    public BabelnetSenseInventory getInvenory() {
        return (BabelnetSenseInventory) inventory;
    }
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public boolean initialize(ResourceSpecifier aSpecifier,
            Map aAdditionalParams)
        throws ResourceInitializationException
    {
        if (!super.initialize(aSpecifier, aAdditionalParams)) {
            return false;
        }
        
        try {
            inventory = new BabelnetSenseInventory(babelNetPath,babelNetLang,babelNetDescLang);
            ((BabelnetSenseInventory) inventory).setLexicon(babelNetLexicon);
        }
        catch (SenseInventoryException e) {
            throw new ResourceInitializationException(e);
        }

        return true;
    }

    public String getLexiconSenseId(String senseId)
        throws SenseInventoryException
    {
        return ((BabelnetSenseInventory) inventory).getLexiconSenseId(senseId);
    }

    public String getLexiconSynsetId(String senseId)
        throws SenseInventoryException
    {
        return ((BabelnetSenseInventory) inventory).getLexiconSynsetId(senseId);
    }
}
