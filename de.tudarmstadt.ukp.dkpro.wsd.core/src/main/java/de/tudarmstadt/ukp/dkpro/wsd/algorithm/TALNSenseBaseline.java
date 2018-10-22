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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.apache.uima.fit.util.JCasUtil;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.wsd.si.SenseInventory;
import de.tudarmstadt.ukp.dkpro.wsd.si.SenseInventoryException;
import de.tudarmstadt.ukp.dkpro.wsd.type.WSDItem;
import edu.upf.taln.textplanning.TextPlanner;
import edu.upf.taln.textplanning.input.amr.Candidate;
import edu.upf.taln.textplanning.input.amr.Candidate.Type;
import edu.upf.taln.textplanning.similarity.SimilarityFunction;
import edu.upf.taln.textplanning.structures.Meaning;
import edu.upf.taln.textplanning.structures.Mention;
import edu.upf.taln.textplanning.weighting.WeightingFunction;
import edu.upf.taln.uima.wsd.si.babelnet.BabelnetSenseInventory;
import edu.upf.taln.uima.wsd.si.babelnet.resource.BabelnetSenseInventoryResource;
import it.uniroma1.lcl.babelnet.BabelSynset;
import it.uniroma1.lcl.babelnet.BabelSynsetID;
import it.uniroma1.lcl.babelnet.BabelSynsetType;
import it.uniroma1.lcl.babelnet.InvalidBabelSynsetIDException;
import it.uniroma1.lcl.jlt.util.Language;

/**
 * A word sense disambiguation algorithm which, given a subject of
 * disambiguation, looks up all the candidate senses in the sense inventory and
 * chooses one marked as most frequently used.
 *
 * @author <a href="mailto:miller@ukp.informatik.tu-darmstadt.de">Tristan Miller</a>
 *
 */
public class TALNSenseBaseline
    extends AbstractWSDAlgorithm
    implements WSDAlgorithmCollectiveCandidate
{
    private final Logger logger = Logger.getLogger(getClass());

    public TALNSenseBaseline(SenseInventory inventory)
    {
        super(inventory);
    }
    

	@Override
	public Map<String, Map<String, Double>> getDisambiguation(Collection<WSDItem> items, WeightingFunction weightingFunction, SimilarityFunction similarityFunction) throws SenseInventoryException {
		
		List<Candidate> candidates = new ArrayList<>();
		
		for (WSDItem item : items) {
			String sod = item.getSubjectOfDisambiguation();
			de.tudarmstadt.ukp.dkpro.wsd.si.POS wsdPos = de.tudarmstadt.ukp.dkpro.wsd.si.POS.valueOf(item.getPos());
			
			String lemma = JCasUtil.selectCovered(Lemma.class, item).get(0).getValue();
			String pos = JCasUtil.selectCovered(POS.class, item).get(0).getPosValue();
			
			List<String> senses = inventory.getSenses(sod, wsdPos);
			//String itemId = String.valueOf(item.hashCode());
			
			Type type = Type.Other; //TODO: get entity type
			Mention mention = new Mention("0", Pair.of(item.getBegin(), item.getEnd()), item.getCoveredText(), lemma, pos, type);
			
			for (String sense : senses) {
				Meaning meaning;
				try {
					BabelnetSenseInventoryResource bnir = (BabelnetSenseInventoryResource)inventory;
					BabelnetSenseInventory bni = bnir.getInventory();
					BabelSynset synset = bni.getUnderlyingResource().getSynset(new BabelSynsetID(sense));
					String label = synset.getSenses(Language.EN).iterator().next().toString();
					boolean isNameEntiry = synset.getSynsetType() == BabelSynsetType.NAMED_ENTITY;
					
					meaning = Meaning.get(sense, label, isNameEntiry); 
				} catch (IOException | InvalidBabelSynsetIDException | ClassCastException e) {
					e.printStackTrace();
					meaning = Meaning.get(sense, sense, false); 
				} 
				
				Candidate candidate = new Candidate(meaning, mention);
				candidates.add(candidate);
			}
		}
		
		TextPlanner.rankMeanings(candidates, weightingFunction, similarityFunction, new TextPlanner.Options());
		
		//TODO test
		Map<String, Map<String, Double>> result = new HashMap<>();
		for (Candidate candidate : candidates) {
			
			Map<String, Double> senses = new HashMap<>();
			if(result.containsKey(candidate.getMention().getSurface_form())){
				senses = result.get(candidate.getMention().getSurface_form());
				senses.put(candidate.getMeaning().getReference(), candidate.getMeaning().getWeight());
			} else {
				senses.put(candidate.getMeaning().getReference(), candidate.getMeaning().getWeight());
			}
			result.put(candidate.getMention().getSurface_form(), senses);
		}
		
		return result;
	}

}