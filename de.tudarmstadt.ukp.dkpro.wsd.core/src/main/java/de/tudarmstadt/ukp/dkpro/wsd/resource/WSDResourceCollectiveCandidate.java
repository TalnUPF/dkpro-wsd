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

package de.tudarmstadt.ukp.dkpro.wsd.resource;

import java.io.File;
import java.util.Collection;
import java.util.Map;

import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.wsd.algorithm.WSDAlgorithmCollectiveCandidate;
import de.tudarmstadt.ukp.dkpro.wsd.algorithm.WSDAlgorithmCollectivePOS;
import de.tudarmstadt.ukp.dkpro.wsd.si.SenseInventoryException;
import de.tudarmstadt.ukp.dkpro.wsd.type.WSDItem;
import edu.upf.taln.textplanning.corpora.CompactFrequencies;
import edu.upf.taln.textplanning.similarity.RandomAccessVectorsSimilarity;
import edu.upf.taln.textplanning.similarity.SimilarityFunction;
import edu.upf.taln.textplanning.utils.Serializer;
import edu.upf.taln.textplanning.weighting.TFIDF;
import edu.upf.taln.textplanning.weighting.WeightingFunction;

/**
 * A resource wrapping algorithms of type {@link WSDAlgorithmCollectivePOS}
 *
 * @author <a href="mailto:miller@ukp.informatik.tu-darmstadt.de">Tristan Miller</a>
 *
 */
public class WSDResourceCollectiveCandidate
    extends WSDResourceBasic
    implements WSDAlgorithmCollectiveCandidate
{

	public final static String PARAM_FREQUENCIES_FILE = "frequenciesFile";
    @ConfigurationParameter(name = PARAM_FREQUENCIES_FILE, mandatory = true, description = "")
    protected String frequenciesFile;
    
    public final static String PARAM_SIMILARITIES_FILE = "similaritiesFile";
    @ConfigurationParameter(name = PARAM_SIMILARITIES_FILE, mandatory = true, description = "")
    protected String similaritiesFile;
    
    WeightingFunction weightingFunction;
	SimilarityFunction similarityFunction;
    
	public void loadFrequencyAndSimilarity() throws ResourceInitializationException {

		try {
	        //CompactFrequencies corpus = (CompactFrequencies) Serializer.deserialize(new File(frequenciesFile).toPath());
	        this.weightingFunction = new NoWeights(); //TFIDF(corpus, i -> true);
	        this.similarityFunction = new RandomAccessVectorsSimilarity(new File(similaritiesFile).toPath());
		} catch (Exception e) {
			e.printStackTrace();
			throw new ResourceInitializationException(e);
		}
    }
	
	@Override
	public void afterResourcesInitialized() throws ResourceInitializationException{
		super.afterResourcesInitialized();
		loadFrequencyAndSimilarity();
	}

	@Override
	public Map<String, Map<String, Double>> getDisambiguation(Collection<WSDItem> items, WeightingFunction weightingFunction, SimilarityFunction similarityFunction) throws SenseInventoryException {
		return ((WSDAlgorithmCollectiveCandidate) wsdAlgorithm)
                .getDisambiguation(items, this.weightingFunction, this.similarityFunction);
	}

}
