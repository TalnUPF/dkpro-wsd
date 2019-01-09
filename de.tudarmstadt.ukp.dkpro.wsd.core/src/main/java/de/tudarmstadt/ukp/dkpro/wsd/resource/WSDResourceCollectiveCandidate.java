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

import de.tudarmstadt.ukp.dkpro.wsd.algorithm.WSDAlgorithmCollectiveCandidate;
import de.tudarmstadt.ukp.dkpro.wsd.algorithm.WSDAlgorithmCollectivePOS;
import de.tudarmstadt.ukp.dkpro.wsd.si.SenseInventoryException;
import de.tudarmstadt.ukp.dkpro.wsd.type.WSDItem;
import edu.upf.taln.textplanning.core.corpora.CompactFrequencies;
import edu.upf.taln.textplanning.core.similarity.SimilarityFunction;
import edu.upf.taln.textplanning.core.similarity.vectors.SimilarityFunctionFactory;
import edu.upf.taln.textplanning.common.Serializer;
import edu.upf.taln.textplanning.core.similarity.vectors.SimilarityFunctionFactory;
import edu.upf.taln.textplanning.core.weighting.NoWeights;
import edu.upf.taln.textplanning.core.weighting.TFIDF;
import edu.upf.taln.textplanning.core.weighting.WeightingFunction;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.resource.ResourceInitializationException;

import java.io.File;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Map;


/**
 * A resource wrapping algorithms of type {@link WSDAlgorithmCollectivePOS}
 *
 * @author <a href="mailto:miller@ukp.informatik.tu-darmstadt.de">Tristan Miller</a>
 */
public class WSDResourceCollectiveCandidate
    extends WSDResourceBasic
    implements WSDAlgorithmCollectiveCandidate
{
	public final static String PARAM_FREQUENCIES_FILE = "frequenciesFile";
	@ConfigurationParameter(name = PARAM_FREQUENCIES_FILE, mandatory = true, description = "")
	private String frequenciesFile;
	public final static String PARAM_SIMILARITIES_FILE = "similaritiesFile";
	@ConfigurationParameter(name = PARAM_SIMILARITIES_FILE, mandatory = true, description = "")
	private String similaritiesFile;
	private WeightingFunction weightingFunction;
	private SimilarityFunction similarityFunction;

	@Override
	public void afterResourcesInitialized() throws ResourceInitializationException
	{
		super.afterResourcesInitialized();

		try
		{
			this.weightingFunction = new NoWeights();
			this.similarityFunction = SimilarityFunctionFactory.get(Paths.get(similaritiesFile), SimilarityFunctionFactory.Format.Binary_RandomAccess);
		}
		catch (Exception e)
		{
			throw new ResourceInitializationException(e);
		}
	}

	@Override
	public Map<String, Map<String, Double>> getDisambiguation(Collection<WSDItem> items, WeightingFunction weightingFunction, SimilarityFunction similarityFunction) throws SenseInventoryException {
		return ((WSDAlgorithmCollectiveCandidate) wsdAlgorithm)
                .getDisambiguation(items, this.weightingFunction, this.similarityFunction);
	}

}
