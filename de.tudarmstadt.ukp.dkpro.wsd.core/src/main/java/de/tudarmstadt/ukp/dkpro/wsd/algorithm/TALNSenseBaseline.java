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

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import com.google.common.base.Stopwatch;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import edu.upf.taln.textplanning.core.TextPlanner;
import edu.upf.taln.textplanning.core.similarity.SimilarityFunction;
import edu.upf.taln.textplanning.core.structures.Candidate;
import edu.upf.taln.textplanning.core.structures.Meaning;
import edu.upf.taln.textplanning.core.structures.Mention;
import edu.upf.taln.textplanning.core.weighting.WeightingFunction;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.uima.fit.util.JCasUtil;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.wsd.si.SenseInventory;
import de.tudarmstadt.ukp.dkpro.wsd.si.SenseInventoryException;
import de.tudarmstadt.ukp.dkpro.wsd.type.WSDItem;

/**
 * A word sense disambiguation algorithm which, given a subject of
 * disambiguation, looks up all the candidate senses in the sense inventory and
 * chooses one marked as most frequently used.
 *
 * @author <a href="mailto:miller@ukp.informatik.tu-darmstadt.de">Tristan Miller</a>
 */
public class TALNSenseBaseline extends AbstractWSDAlgorithm	implements WSDAlgorithmCollectiveCandidate
{
	private final static Logger log = LogManager.getLogger();

	public TALNSenseBaseline(SenseInventory inventory)
	{
		super(inventory);

	}

	@Override
	public Map<String, Map<String, Double>> getDisambiguation(Collection<WSDItem> items, WeightingFunction weightingFunction,
	                                                          SimilarityFunction similarityFunction) throws SenseInventoryException
	{

		log.info("Looking up Babelnet synsets for " + items.size() + " items");
		int counter = 0;
		final Stopwatch timer = Stopwatch.createStarted();
		List<Candidate> candidates = new ArrayList<>();


		for (WSDItem item : items)
		{
			final Sentence sentence = JCasUtil.selectCovering(Sentence.class, item).get(0);
			final String pos = item.getPos();
			String lemma = JCasUtil.selectCovered(Lemma.class, item).stream()
					.map(Lemma::getValue)
					.collect(Collectors.joining(" "));
			// TODO NE and type info for mention should come from NER
			Mention mention = Mention.get(String.valueOf(sentence.getBegin()), Pair.of(item.getBegin(), item.getEnd()), item.getCoveredText(),
					lemma, pos, false, "");

			String sod = item.getSubjectOfDisambiguation();
			de.tudarmstadt.ukp.dkpro.wsd.si.POS wsdPos = de.tudarmstadt.ukp.dkpro.wsd.si.POS.valueOf(item.getPos());

			for (String sense : inventory.getSenses(sod, wsdPos))
			{
//					BabelnetSenseInventoryResource bnir = (BabelnetSenseInventoryResource) inventory;
//					BabelnetSenseInventory bni = bnir.getInventory();
//					BabelSynset synset = bni.getUnderlyingResource().getSynset(new BabelSynsetID(sense));
//					String label = synset.getSenses(Language.EN).iterator().next().toString();
//					boolean isNameEntiry = synset.getSynsetType() == BabelSynsetType.NAMED_ENTITY;
//					meaning = Meaning.get(sense, label, isNameEntiry);

				Meaning meaning = Meaning.get(sense, inventory.getSenseDescription(sense), false);
				Candidate candidate = new Candidate(meaning, mention);
				candidates.add(candidate);
			}

			if (++counter % 1000 == 0)
				log.info(counter + " items looked up out of " + items.size());
		}
		log.info(candidates.size() + " candidate meanings collected in " + timer.stop());

		TextPlanner.rankMeanings(candidates, weightingFunction, similarityFunction, new TextPlanner.Options());

//		final Map<String, List<Candidate>> candidates_by_mention = candidates.stream()
//				.collect(Collectors.groupingBy(c -> c.getMention().getSurface_form()));
//		candidates_by_mention.keySet()
//				.forEach(mention ->
//				{
//					log.info("Meanings for mention " + mention);
//					candidates_by_mention.get(mention).stream()
//						.map(Candidate::getMeaning)
//						.sorted(Comparator.comparingDouble(Meaning::getWeight).reversed())
//						.forEach(meaning -> log.debug("\t" + meaning + " " + DebugUtils.printDouble(meaning.getWeight()) + " " + similarityFunction.isDefinedFor(meaning.getReference())));
//				});

		Map<String, Map<String, Double>> result = new HashMap<>();
		for (Candidate candidate : candidates)
		{

			Map<String, Double> senses = new HashMap<>();
			if (result.containsKey(candidate.getMention().getSurface_form()))
			{
				senses = result.get(candidate.getMention().getSurface_form());
				senses.put(candidate.getMeaning().getReference(), candidate.getMeaning().getWeight());
			}
			else
				senses.put(candidate.getMeaning().getReference(), candidate.getMeaning().getWeight());
			result.put(candidate.getMention().getSurface_form(), senses);
		}

		return result;
	}

}
