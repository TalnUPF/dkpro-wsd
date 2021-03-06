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

package de.tudarmstadt.ukp.dkpro.wsd.annotator;

import de.tudarmstadt.ukp.dkpro.wsd.algorithm.WSDAlgorithmCollectiveBasic;
import de.tudarmstadt.ukp.dkpro.wsd.algorithm.WSDAlgorithmCollectiveCandidate;
import de.tudarmstadt.ukp.dkpro.wsd.si.SenseInventoryException;
import de.tudarmstadt.ukp.dkpro.wsd.type.WSDItem;
import org.apache.uima.UimaContext;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.resource.ResourceInitializationException;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * An annotator which calls a {@link WSDAlgorithmCollectiveBasic} disambiguation
 * algorithm on a collection of {@link WSDItem}s.
 *
 * @author <a href="mailto:miller@ukp.informatik.tu-darmstadt.de">Tristan Miller</a>
 */
public class WSDAnnotatorCollectiveCandidate extends WSDAnnotatorBaseCollective
{
	public final static String WSD_ALGORITHM_RESOURCE = "WSDAlgorithmResource";
	@ExternalResource(key = WSD_ALGORITHM_RESOURCE)
	protected WSDAlgorithmCollectiveCandidate wsdMethod;

	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException
	{
		super.initialize(context);
		inventory = wsdMethod.getSenseInventory();
		normalizeConfidence = false;
	}

	@Override
	protected Map<WSDItem, Map<String, Double>> getDisambiguation(Collection<WSDItem> wsdItems) throws SenseInventoryException
	{
		Map<String, Map<String, Double>> resultsByToken = wsdMethod.getDisambiguation(wsdItems, null, null);
		Map<WSDItem, Map<String, Double>> resultsByWSDItem = new HashMap<>();
		for (WSDItem wsdItem : wsdItems)
		{
			Map<String, Double> senseMap = resultsByToken.get(wsdItem.getCoveredText());
			if (senseMap != null)
				resultsByWSDItem.put(wsdItem, senseMap);
		}

		return resultsByWSDItem;
	}

	@Override
	protected String getDisambiguationMethod()
	{
		if (disambiguationMethodName != null)
			return disambiguationMethodName;
		else
			return wsdMethod.getDisambiguationMethod();
	}
}
