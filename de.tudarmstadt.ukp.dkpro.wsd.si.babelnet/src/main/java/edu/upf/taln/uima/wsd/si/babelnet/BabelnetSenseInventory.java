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
package edu.upf.taln.uima.wsd.si.babelnet;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections15.Transformer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import it.uniroma1.lcl.jlt.util.Language;
import de.tudarmstadt.ukp.dkpro.wsd.UnorderedPair;
import de.tudarmstadt.ukp.dkpro.wsd.si.POS;
import de.tudarmstadt.ukp.dkpro.wsd.si.SenseAlignment;
import de.tudarmstadt.ukp.dkpro.wsd.si.SenseDictionary;
import de.tudarmstadt.ukp.dkpro.wsd.si.SenseInventoryBase;
import de.tudarmstadt.ukp.dkpro.wsd.si.SenseInventoryException;
import de.tudarmstadt.ukp.dkpro.wsd.si.SenseTaxonomy;


import edu.uci.ics.jung.graph.UndirectedGraph;
import it.uniroma1.lcl.babelnet.*;
import it.uniroma1.lcl.babelnet.data.BabelExample;
import it.uniroma1.lcl.babelnet.data.BabelGloss;
import it.uniroma1.lcl.babelnet.data.BabelPOS;
import it.uniroma1.lcl.babelnet.data.BabelSenseSource;
import it.uniroma1.lcl.babelnet.resources.ResourceID;
import it.uniroma1.lcl.jlt.Configuration;


/**
 * A sense inevntory for UBY
 *
 * @author <a href="mailto:miller@ukp.informatik.tu-darmstadt.de">Tristan Miller</a>
 *
 */
public class BabelnetSenseInventory extends SenseInventoryBase
    implements SenseTaxonomy, SenseDictionary, SenseAlignment
{
    protected BabelNet babelNet;
    protected Set<BabelSenseSource> lexicons;  // the lexicon to be used it can be serval. Null empties the list
    protected BabelSenseSource [] lexiconsArr;  // the lexicon to be used it can be serval. Null empties the list
   // protected List<BabelSenseSource> langs;  // TODO Extra languages
     protected boolean allowMultiLingualAlignments = false;
    private final static SiPosToBabelNetPos siPosToBabelNetPos = new SiPosToBabelNetPos();
     private final Log logger = LogFactory.getLog(getClass());

    // Variables and cache for sense descriptions
    private String senseDescriptionFormat = "%w; %d";
    private final Map<String, BabelSynset> cachedSynset = new HashMap<String, BabelSynset>();
 	private Language language;
	

    public String getLanguage() {
		return language.toString();
	}

	public void setLanguage(String language) {
		this.language=Language.valueOf(language);
	}

	/**
     * Returns the underlying {@link BabelNet} object.
     *
     * @return the underlying {@link BabelNet} object
     */
    public BabelNet getUnderlyingResource() {
        return babelNet;
    }

  /**
     *
     * @param configPath
     *            Path where babelnet is configures. It should have the jlt.properties and babelnet.properties
     *            files. if not then duplicate this method with more paremeters 
     * @param lang
     * 			default language
     * @throws SenseInventoryException
     */
    public BabelnetSenseInventory(String configPath, Language lang)
        throws SenseInventoryException
    {
        File configPathF = new File("src/main/resources/config");
        Configuration jltConf = Configuration.getInstance();
        jltConf.setConfigurationFile(new File(configPathF, "jlt.properties"));
        BabelNetConfiguration bnConf = BabelNetConfiguration.getInstance();
        bnConf.setConfigurationFile(new File(configPathF, "babelnet.properties"));
        bnConf.setBasePath(configPathF.getAbsolutePath() + "/");
        babelNet = BabelNet.getInstance();
        /*System.out.println("intialized ");
        try {
			System.out.println("bablenet "+babelNet.getSenses(lang, "word").get(0));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        */
        this.lexicons=new HashSet<BabelSenseSource>();
        this.lexiconsArr=new BabelSenseSource[0];
        this.language=lang;
    }
   
    
    
    /*
    public BabelnetSenseInventory(BabelNetConfiguration bnConf)
        throws SenseInventoryException
    {
        try {
            babelNet = new BabelNet(bnConf);

            File configPath = new File("src/main/resources/config");
            Configuration jltConf = Configuration.getInstance();
            jltConf.setConfigurationFile(new File(configPath, "jlt.properties"));
            BabelNetConfiguration bnConf = BabelNetConfiguration.getInstance();
            bnConf.setConfigurationFile(new File(configPath, "babelnet.properties"));
            bnConf.setBasePath(configPath.getAbsolutePath() + "/");
            BabelNet babelnet = BabelNet.getInstance();


        }
        catch (IllegalArgumentException e) {
            throw new SenseInventoryException(e);
        }
    }
    */ 
    
    public void setSenseDescriptionFormat(String format)
    {
        if (format == null) {
            senseDescriptionFormat = "%d";
        }
        else {
            senseDescriptionFormat = format;
        }
    }

    /**
     * Filter all queries by the given lexicon
     *
     * @param lexiconName
     *            The name of the BabelSenseSource
     *            public enum BabelSenseSource available senses are 
		    BABELNET,    // BabelNet senses, not available as of version 3.0
		    WN,          // WordNet senses
		    OMWN,        // Open Multilingual WordNet
		    WONEF,       // WordNet du Francais
		    WIKI,        // Wikipedia page
		    WIKIDIS,     // Wikipedia disambiguation pages
		    WIKIDATA,    // Wikidata senses
		    OMWIKI,      // OmegaWiki senses
		    WIKICAT,     // Wikipedia category, not available as of version 3.0
		    WIKIRED,     // Wikipedia redirections
		    WIKT,        // Wiktionary senses
		    WIKIQU,      // Wikiquote page
		    WIKIQUREDI,  // Wikiquote redirections
		    WIKTLB,      // Wiktionary translation label
		    VERBNET,	 // VerbNet senses
		    FRAMENET,	 // FrameNet senses
		    MSTERM,      // Microsoft Terminology items
		    GEONM,       // GeoNames items
		    WNTR,        // Translations of WordNet senses
		    WIKITR       // Translations of Wikipedia links
     * @throws SenseInventoryException
     */
    public void setLexicon(String lexiconName)
        throws SenseInventoryException
    {
        if (lexiconName == null) {
            this.lexicons= new HashSet<BabelSenseSource>();
            this.lexiconsArr=new BabelSenseSource[0];
            cachedSynset.clear();        	
            return;
        }

        BabelSenseSource lexicon;
        try {
            lexicon = BabelSenseSource.valueOf(lexiconName);
        }
        catch (IllegalArgumentException e) {
            throw new SenseInventoryException(e);
        }
        cachedSynset.clear();        	
        lexicons.add( lexicon);
        lexiconsArr= new  BabelSenseSource[lexicons.size()];
        int i=0;
        for(BabelSenseSource res:lexicons){
        	lexiconsArr[i]=res;i++;
        }
        	
        //lexiconsArr=(BabelSenseSource[]) lexicons.toArray();
    }
 
    /**
     * @author Joan Codina
     * 
     * removes the lexicon from the list of lexicons
     * 
     * @param lexiconName the lexicon to remove (see setLexicon for list of available lexicons) 
     * @throws SenseInventoryException
     */
    
    public void removeLexicon(String lexiconName)
            throws SenseInventoryException
        {
           BabelSenseSource lexicon;
 	        try {
	            lexicon = BabelSenseSource.valueOf(lexiconName);
	        }
	        catch (IllegalArgumentException e) {
	            throw new SenseInventoryException(e);
	        }
           if (lexicons.contains(lexicon)) {
                lexicons.remove(lexicon);
                lexiconsArr= new  BabelSenseSource[lexicons.size()];
                lexiconsArr=(BabelSenseSource[]) lexicons.toArray();
            }
           cachedSynset.clear();        	
           return;
        }
    /**
     * Determines whether {@link #getSenseAlignments(String)} should also return
     * alignments to senses in other languages.
     *
     * @param allow
     */
    public void setAllowMultilingualAlignments(boolean allow)
    {
        this.allowMultiLingualAlignments = allow;
        flushSenseAlignmentCache();
    }

    private void flushSenseAlignmentCache()
    {
    	cachedSynset.clear();
    }

  
    @SuppressWarnings("unused")
    private BabelnetSenseInventory()
    {
    }

    //@Override
    public Map<String, List<String>> getSenseInventory()
        throws SenseInventoryException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<String> getSenses(String sod)
        throws SenseInventoryException
    {
        return getSenses(sod, null);
    }

    /**
     * Get a list of BabelNet lexical entries for a given lemma and part of speech.
     * Because our POS tags are more coarse-grained than the ones used by BabelNet,
     * we need to call BabelNet's getLexicalEntries() multiple times and merge the
     * results.
     *
     * @param lemma
     * @param pos
     * @return
     * @throws  
     */
/*    protected List<LexicalEntry> getLexicalEntriesByPOS(String lemma, POS pos)
    {
    	if (pos == null) {
            return babelNet.getLexicalEntries(lemma, null, lexicon);
        }
        List<LexicalEntry> entries = new ArrayList<LexicalEntry>();
        for (EPartOfSpeech babelNetPOS : siPosToBabelNetPos.transform(pos)) {
            entries.addAll(babelNet.getLexicalEntries(lemma, babelNetPOS, lexicon));
        }
        return entries;
    }
**/
    
    //@Override
    public List<String> getSenses(String sod, POS pos)
        throws SenseInventoryException, UnsupportedOperationException
    {
    	List<String> senses=new LinkedList<String>();
    	List<BabelSynset> synsetsList; 
 			try {
 		if (lexiconsArr.length==0){ 
      	if (pos==null)
      		synsetsList= babelNet.getSynsets( sod, language); 
 		else 
   		    synsetsList= babelNet.getSynsets(sod, language, siPosToBabelNetPos.transform(pos)[0]);
 		} else {
 	     	if (pos==null)
 	      		synsetsList= babelNet.getSynsets( sod, language,null, lexiconsArr); 
 	      	  // the null pos may give an error, if that happens then it the lexiconArr muyt be removed and filterend afterwards
 			else 
 	    	synsetsList= babelNet.getSynsets(sod, language, siPosToBabelNetPos.transform(pos)[0],lexiconsArr);
  		}
      	
       
         for (BabelSynset synset : synsetsList) {
        	 senses.add(synset.getId().getID());
        	 cachedSynset.put(synset.getId().getID(),synset);
/*
 *          BabelSynsetID	id = synset.getId( );
 *          String strId=id.getID();
         	     ResourceID rs= new BabelSynsetID(strId);
        	     babelNet.getSynset(rs);
*/       
        	 } 
     
        }catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace(); 
				throw new UnsupportedOperationException(e);
 			}
        return senses;
    }

    @Override
    public String getMostFrequentSense(String sod)
        throws SenseInventoryException, UnsupportedOperationException
    {
        return getMostFrequentSense(sod, null);
    }

    @Override
    public String getMostFrequentSense(String sod, POS pos)
        throws SenseInventoryException, UnsupportedOperationException
    {
		try {
    	List<BabelSynset> synsetsList; 
 		if (lexiconsArr.length==0){ 
 	      	if (pos==null)
 	      		synsetsList= babelNet.getSynsets( sod, language); 
 	 		else 
 	   		    synsetsList= babelNet.getSynsets(sod, language, siPosToBabelNetPos.transform(pos)[0]);
 	 		} else {
 	 	     	if (pos==null)
 	 	      		synsetsList= babelNet.getSynsets( sod, language,null, lexiconsArr); 
 	 	      	  // the null pos may give an error, if that happens then it the lexiconArr muyt be removed and filterend afterwards
 	 			else 
 	 	    	synsetsList= babelNet.getSynsets(sod, language, siPosToBabelNetPos.transform(pos)[0],lexiconsArr);
 	  		}
       if (synsetsList.isEmpty()) return null;    	
       Collections.sort(synsetsList, new BabelSynsetComparator(sod, Language.EN));
       BabelSynset mfs=synsetsList.get(0);
       cachedSynset.put(mfs.getId().getID(),mfs);

       return mfs.getId().getID();     
 	   }catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace(); 
			throw new UnsupportedOperationException(e);
		}
    }

    //@Override
    public String getSenseDescription(String synsetId)
        throws SenseInventoryException
    {
        try {
        BabelSynset synset = cachedSynsetGet(synsetId);
        BabelGloss glossB = synset.getMainGloss(language);
        String gloss="";
        if (glossB==null)
        {
        	System.out.println("no gloss on synset" + synsetId + " "+synset.getMainSense(language).getLemma());
        } else {
        	gloss= glossB.getGloss();
        }
        String description = senseDescriptionFormat.replace("%d",gloss);
			description = description.replace("%e", synset.getExamples(language).toString());
        List<BabelSense> senses = synset.getSenses(language );
        String ListSenses ="";
        String sep=" ";
        
        for (BabelSense sense:senses){
        	ListSenses+=sep + sense.getLemma();
        }
        description = description.replace("%w", ListSenses);
        return description;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new SenseInventoryException(e);
		}
    }

    //@Override
    public POS getPos(String senseId)
        throws SenseInventoryException
    {
        BabelSynset synset;
		try {
			synset = cachedSynsetGet(senseId);
		} catch (Exception e) {
			return null;
		}
        return siPosToBabelNetPos.deTransform(synset.getPOS());
    }

    //@Override
    public int getUseCount(String senseId)
        throws SenseInventoryException
    {
    	// TODO check that is not available in babelnet...
        throw new UnsupportedOperationException();
    }

    //@Override
    public String getSenseInventoryName()
    {
        if (lexicons.isEmpty()) {
            return "BabelNet";
        }
        else {
            return "BabelNet_" + lexicons.toString();
        }
    }

    //@Override
    public UndirectedGraph<String, UnorderedPair<String>> getUndirectedGraph()
        throws SenseInventoryException, UnsupportedOperationException
    {
    	// TODO GRAPH OPPERATIONS
        throw new UnsupportedOperationException();
    }

    //@Override
    public void setUndirectedGraph(
            UndirectedGraph<String, UnorderedPair<String>> graph)
        throws SenseInventoryException, UnsupportedOperationException
    {
    	// TODO GRAPH OPPERATIONS
        throw new UnsupportedOperationException();
    }

    //@Override
	public Set<String> getSenseNeighbours(String senseId)
			throws SenseInventoryException, UnsupportedOperationException {
		try {
			Set<String> neighbours = new HashSet<String>();
			BabelSynset synset = cachedSynsetGet(senseId);
			List<BabelSynsetIDRelation> edges = synset.getEdges();
			for (BabelSynsetIDRelation edge : edges) {
				BabelSynsetID target = edge.getBabelSynsetIDTarget();
				String targetName = target.getID();
				BabelSynset neighbour;
				neighbour = babelNet.getSynset(target);
				neighbours.add(targetName);
				cachedSynset.put(targetName, neighbour);
			}
			return neighbours;
		} catch (Exception e) {
			throw new SenseInventoryException(e);

		}
	}

    /**
     * Transforms a POS enum to a BabelNet POS
     *
     * @author Joan Codina
     *public enum BabelPOS
{
 NOUN, 
 ADJECTIVE,
 VERB,
 ADVERB,
 INTERJECTION,
 PREPOSITION,
 ARTICLE,
 DETERMINER,
 CONJUNCTION,
 PRONOUN;
}
     */
    public static class SiPosToBabelNetPos
        implements Transformer<POS, BabelPOS[]>
    {
        protected final BabelPOS BabelNetNounPOS[] = { BabelPOS.NOUN};
        protected final BabelPOS BabelNetVerbPOS[] = { BabelPOS.VERB};
        protected final BabelPOS BabelNetAdjectivePOS[] = { BabelPOS.ADJECTIVE };
        protected final BabelPOS BabelNetAdverbPOS[] = { BabelPOS.ADVERB };

        //@Override
        public BabelPOS[] transform(POS pos)
        {
            if (pos == null) {
                return null;
            }

            switch (pos) {
            case NOUN:
                return BabelNetNounPOS;
            case VERB:
                return BabelNetVerbPOS;
            case ADJ:
                return BabelNetAdjectivePOS;
            case ADV:
                return BabelNetAdverbPOS;
            }

            return null;
        }
        
        public POS deTransform(BabelPOS pos ){
        	
            if (pos == null) {
                return null;
            }

            switch (pos) {
            case NOUN:
                return POS.NOUN;
            case VERB:
                return POS.VERB;
            case ADJECTIVE:
                return  POS.ADJ;
            case ADVERB:
                return POS.ADV;
            }

            return null;
      	
        	
        }
    }

  

   // @Override
    public Set<String> getSenseExamples(String senseId)
        throws SenseInventoryException
    {
 		try {
    	Set<String> samples=new HashSet<String>();
        BabelSynset synset = cachedSynsetGet(senseId);
        List<BabelExample> examples = synset.getExamples(language);
		for (BabelExample example:examples){
    		samples.add(example.getExample());
    	}
 		return samples;
    	}catch(Exception e)
		{
			throw new SenseInventoryException(e);
		}
	
	}
 
	

   // @Override
    public Set<String> getSenseWords(String senseId)
        throws SenseInventoryException
    {
		try {
	    	Set<String> samples=new HashSet<String>();
	        BabelSynset synset = cachedSynsetGet(senseId);
				List<BabelSense> senses = synset.getMainSenses(language);
			for (BabelSense sense:senses){
	    		samples.add(sense.getLemma());
	    	}
	 		return samples;
	    	}catch(Exception e)
			{
				throw new SenseInventoryException(e);
			}

     }

    // @Override
    public String getSenseDefinition(String senseId)
        throws SenseInventoryException
    {
		try {
	        BabelSynset synset = cachedSynsetGet(senseId);
	        return synset.getMainGloss(language).getGloss();
	    	}catch(Exception e)
			{
				throw new SenseInventoryException(e);
			}
     }

    /**
     * Given a BabelNet sense ID, return the sense ID used by the underlying lexicon
     *
     * @param senseId
     * @return the sense ID used by the underlying lexicon
     * @throws SenseInventoryException
     */
    public String getLexiconSenseId(String senseId)
        throws SenseInventoryException
    { //TODO get lexiconSenseid is not that...
        try{
    	BabelSynset synset = cachedSynsetGet(senseId);
        return synset.getSynsetSource().name();
    	}catch(Exception e)
		{
			throw new SenseInventoryException(e);
		}
        
    }

    /**
     * Given a BabelNet sense ID, return the synset ID used by the underlying lexicon
     *
     * @param senseId
     * @return the synset ID used by the underlying lexicon
     * @throws SenseInventoryException
     */
    public String getLexiconSynsetId(String senseId)
        throws SenseInventoryException
    { 
      return senseId;
    }

    /**
     * Returns a set of alignments for the given sense
     *
     * @param senseId
     *            The ID of the sense whose alignments should be found
     * @return A (possibly empty) set of sense IDs for aligned senses
     *
     * @throws SenseInventoryException
     */
    // @Override
    public Set<String> getSenseAlignments(String senseId)
        throws SenseInventoryException
    { //TODO no sure that those are the alingments...
        try{
	    Set<String> samples=new HashSet<String>();	
    	BabelSynset synset = cachedSynsetGet(senseId);
    	BabelSynsetID id = synset.getId();
    	List<BabelSynsetIDRelation> related = id.getRelatedIDs();
        for (BabelSynsetIDRelation edge:related){
        	samples.add(edge.getBabelSynsetIDTarget().getID());
        }
        return samples;
    	}catch(Exception e) 
		{
			throw new SenseInventoryException(e);
		}
     }

 

   private BabelSynset cachedSynsetGet(String senseId) throws Exception {
	    if (cachedSynset.containsKey(senseId)) 
	       return cachedSynset.get(senseId);
	    BabelSynsetID id=new BabelSynsetID(senseId);	    
	    BabelSynset synset = babelNet.getSynset(id);	   
	    cachedSynset.put(senseId, synset);
	    return synset;
	}


}
