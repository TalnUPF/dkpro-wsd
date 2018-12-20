/*******************************************************************************
 * Copyright 2017
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

package de.tudarmstadt.ukp.dkpro.wsd.algorithm;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.ExternalResourceFactory.createExternalResourceDescription;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.resource.ExternalResourceDescription;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.io.xmi.XmiReader;
import de.tudarmstadt.ukp.dkpro.core.io.xmi.XmiWriter;
import de.tudarmstadt.ukp.dkpro.wsd.annotator.WSDAnnotatorCollectiveCandidate;
import de.tudarmstadt.ukp.dkpro.wsd.resource.WSDResourceCollectiveCandidate;
//import edu.upf.taln.uima.wsd.si.babelnet.resource.BabelnetSenseInventoryResource;

public class TALNSenseBaselineTest {

    /*@Test
    public void test() throws Exception {

    	CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
    			XmiReader.class,
    			XmiReader.PARAM_SOURCE_LOCATION, "/home/ivan/misc/tensor/testSentences/toRun",
    			XmiReader.PARAM_LANGUAGE, "en",
    			XmiReader.PARAM_PATTERNS, new String[] { "[+]*-candidate.xmi" },
    			XmiReader.PARAM_TYPE_SYSTEM_FILE, "/home/ivan/misc/tensor/testSentences/xmi-candidates/TypeSystem.xml");
    	
    	ExternalResourceDescription BabelNet = createExternalResourceDescription(BabelnetSenseInventoryResource.class, 
				BabelnetSenseInventoryResource.PARAM_BABELNET_CONFIGPATH, "/home/ivan/misc/BabelNet-3.7/config", 
				BabelnetSenseInventoryResource.PARAM_BABELNET_LANG, "EN", 
				BabelnetSenseInventoryResource.PARAM_BABELNET_DESCLANG, "EN");
	    
	    ExternalResourceDescription mfsBaselineResourceBabelNet = createExternalResourceDescription(WSDResourceCollectiveCandidate.class,
	    		WSDResourceCollectiveCandidate.SENSE_INVENTORY_RESOURCE, BabelNet,
	    		WSDResourceCollectiveCandidate.DISAMBIGUATION_METHOD, TALNSenseBaseline.class.getName(),
	    		WSDResourceCollectiveCandidate.PARAM_FREQUENCIES_FILE, "/home/ivan/misc/tensor/resources/freqs.bin",
	    		WSDResourceCollectiveCandidate.PARAM_SIMILARITIES_FILE, "/home/ivan/misc/tensor/resources/sensembed-vectors-merged_bin");
		
		AnalysisEngineDescription mfsBaselineBabelNet = createEngineDescription(WSDAnnotatorCollectiveCandidate.class,
				WSDAnnotatorCollectiveCandidate.WSD_ALGORITHM_RESOURCE, mfsBaselineResourceBabelNet,
				WSDAnnotatorCollectiveCandidate.PARAM_BEST_ONLY, false);
		
		AnalysisEngineDescription writer = createEngineDescription(
    			XmiWriter.class,
    			XmiWriter.PARAM_TARGET_LOCATION, "/home/ivan/misc/tensor/testSentences/toRun",
    			XmiWriter.PARAM_OVERWRITE, true,
    			XmiWriter.PARAM_STRIP_EXTENSION, true,
    			XmiWriter.PARAM_FILENAME_EXTENSION, "-result.xmi");

		
		SimplePipeline.runPipeline(reader, mfsBaselineBabelNet, writer);
    }*/
}
