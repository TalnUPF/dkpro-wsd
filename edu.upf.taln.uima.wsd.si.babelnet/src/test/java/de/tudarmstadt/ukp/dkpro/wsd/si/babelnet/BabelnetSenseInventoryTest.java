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
package de.tudarmstadt.ukp.dkpro.wsd.si.babelnet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.wsd.si.POS;
import de.tudarmstadt.ukp.dkpro.wsd.si.SenseInventoryException;
import edu.upf.taln.uima.wsd.si.babelnet.BabelnetSenseInventory;
import it.uniroma1.lcl.jlt.util.Language;

/**
 * @author Joan Codina
 * lexicon names
 * FrameNet
OmegaWiki_deu
OmegaWiki_eng
OntoWiktionaryDE
OntoWiktionaryEN
OpenThesaurus
VerbNet
Wikipedia_deu
Wikipedia_eng
WiktionaryDE
WiktionaryEN
WordNet

 *
 */

public class BabelnetSenseInventoryTest
{
    private static BabelnetSenseInventory si;

    public static void main( String[] args )
    {
    	try {
    		setUpBeforeClass();
    		
        	BabelnetSenseInventoryTest babelNet = new BabelnetSenseInventoryTest();
    		babelNet.babelNetSenseInventoryTest();
    		babelNet.frequencyTest();
    		babelNet.alignmentTest();
    		babelNet.checkSentence("The black bears were sitting the black bear sat in front of the main door of the White House, the house of the President of the United States of America."
    							,7);
    		
    	} catch (Exception e){
    		e.printStackTrace();
    	}
    	
    }
    public static List<String> ngrams(int n, String [] words) {
        List<String> ngrams = new ArrayList<String>();
        for (int i = 0; i < words.length - n + 1; i++)
            ngrams.add(concat(words, i, i+n));
        return ngrams;
    }

    public static String concat(String[] words, int start, int end) {
        StringBuilder sb = new StringBuilder();
        for (int i = start; i < end; i++)
            sb.append((i > start ? " " : "") + words[i]);
        return sb.toString();
    }

    public static List<String> all_ngrams(int size, String [] words) {
        List<String> ngrams = new ArrayList<String>();

    	for (int n = 1; n <= size; n++) {
    		ngrams.addAll(ngrams(n,words));
        }
    	return ngrams;
    }   
    private void checkSentence(String sentence, int ngramsSize) {
    	 try {
			si.setLexicon(null);;
	    	//si.setLexicon("WiktionaryEN");
  	     String[] words = sentence.split("[ ,.:';]+");
  	     List<String> ngrams = all_ngrams(ngramsSize, words);
         for (String word :ngrams) {
        	 //check if present in babelNet
        	 List<String> senses2 =si.getSenses(word);
        	 List<String> senses =si.getSenses(word, POS.NOUN);
       	 
        	 if(senses2.size()>0) { System.out.println( senses2.size()+"\t"+senses.size()+"\t"+word);
        	 	String sense=si.getMostFrequentSense(word,POS.NOUN);
        	 	if(sense!=null) printSenseInformation( sense); 
        	 }
         }
         
   	 
		} catch (SenseInventoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
  	}

	public static void setUpBeforeClass()
        throws Exception
    {
        si = new BabelnetSenseInventory("src/main/resources/config",Language.EN);
    }

  
    public void babelNetSenseInventoryTest()
        throws SenseInventoryException
    {

    //    si.setLexicon(null);
    	 si.setLexicon("WN");

      //  List<String> senses = si.getSenses("set", POS.NOUN);
        List<String> senses = si.getSenses("black bear", POS.NOUN);


        int i = 0;
        for (String sense : senses) {
            System.out.println("Sense " + i++ + " of " + senses.size());
            printSenseInformation(sense);
        }
    }

    public void frequencyTest()
        throws SenseInventoryException
    {
    	System.out.println("============== Frequency Test ======================== ");
        si.setLexicon(null);
        System.out.println(si.getSenseInventoryName());
        si.getMostFrequentSense("set");
        System.out.println(si.getMostFrequentSense("set"));
  
        si.setLexicon("WN");
        System.out.println(si.getSenseInventoryName());
        System.out.println(si.getMostFrequentSense("set"));
       }

    public void alignmentTest()
        throws SenseInventoryException
    {
    	System.out.println("============== Aignment TEST ======================== ");
        // final String id = "WN_Sense_40443";
        final String id = "bn:00103024a";
        // si.setLexicon("WordNet");
        printSenseInformation(id);
        si.setAllowMultilingualAlignments(false);
        
        Set<String> alignments = si.getSenseAlignments(id); 
        System.out.println(alignments.size());
        // assertEquals(1, alignments.size());
        System.out.println("Monolingual alignments of " + id + ": " + si.getSenseAlignments(id));
        for (String sense : alignments) {
            printSenseInformation(sense);
        }

        si.setAllowMultilingualAlignments(true);
        alignments = si.getSenseAlignments(id);
        // The following only works if multilingual links have been imported
        // assertEquals(12, alignments.size());
        System.out.println("All alignments of " + id + ": " + si.getSenseAlignments(id));
        for (String sense : alignments) {
            printSenseInformation(sense);
        }
    }

    private void printSenseInformation(String sense) throws SenseInventoryException {
        System.out.println("babelNet sense ID: " + sense);
        System.out.println("Lexicon sense ID:" + si.getLexiconSenseId(sense));
        System.out.println("Lexicon synset ID:" + si.getLexiconSynsetId(sense));
        System.out.println("Description: " + si.getSenseDescription(sense));
        System.out.println("Definition: " + si.getSenseDefinition(sense));
        System.out.println("Examples: " + si.getSenseExamples(sense));
        System.out.println("Words: " + si.getSenseWords(sense));
        System.out.println("Neighbours: " + si.getSenseNeighbours(sense));
        try { System.out.println("Alignments: " + si.getSenseAlignments(sense)); } catch (Exception e) {System.out.println("error in printing alignments");}
        System.out.println();
    }
}