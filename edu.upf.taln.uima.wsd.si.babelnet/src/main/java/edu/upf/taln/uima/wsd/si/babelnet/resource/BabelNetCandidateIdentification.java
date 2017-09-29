package edu.upf.taln.uima.wsd.si.babelnet.resource;


import static org.apache.uima.fit.util.JCasUtil.select;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.uima.UimaContext;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.wsd.si.POS;
import de.tudarmstadt.ukp.dkpro.wsd.si.SenseInventory;
import de.tudarmstadt.ukp.dkpro.wsd.si.SenseInventoryException;
import edu.upf.taln.uima.wsd.si.babelnet.BabelnetSenseInventory;
import edu.upf.taln.uima.wsd.si.babelnet.resource.BabelnetSenseInventoryResource;
import de.tudarmstadt.ukp.dkpro.wsd.type.LexicalItemConstituent;
import de.tudarmstadt.ukp.dkpro.wsd.type.Sense;
import de.tudarmstadt.ukp.dkpro.wsd.type.WSDItem;
import de.tudarmstadt.ukp.dkpro.wsd.type.WSDResult;
import it.uniroma1.lcl.jlt.util.Language;


public class BabelNetCandidateIdentification   extends JCasAnnotator_ImplBase {


    public final static String PARAM_BABELNET = "babelnetInventory";
    @ExternalResource(key =  PARAM_BABELNET)
    protected BabelnetSenseInventoryResource babelnetInventory;


    public static final String PARAM_babelNet_LEXICON = "babelNetLexicon";
    @ConfigurationParameter(name = PARAM_babelNet_LEXICON, description = "Lexicon to use with BabelNet; if null or none all available lexicons will be used", mandatory = false)
    protected String babelNetLexicon=null;
	
   //private Map<String, POS> POSMap;
   private static BabelnetSenseInventory si;
   private static List<String> nounTags;
   private static int maxWordsFragment = 7;
   private  Map<String, POS> POSMap;  
   private static final String ELEMENT_WORDFORM = "wf";
   public static final String DISAMBIGUATION_METHOD_NAME = "none";
   @Override
   public void initialize(UimaContext context)
       throws ResourceInitializationException
   {
       super.initialize(context);
   	    si=babelnetInventory.getInvenory();
	    this.POSMap = new HashMap<String, POS>();
	    this.POSMap.put("JJ", POS.ADJ);
	    this.POSMap.put("JJR", POS.ADJ);
	    this.POSMap.put("JJS", POS.ADJ);
	    this.POSMap.put("NN", POS.NOUN);
	    this.POSMap.put("NNS", POS.NOUN);
	    this.POSMap.put("NNP", POS.NOUN);
	    this.POSMap.put("NNPS", POS.NOUN);
	    this.POSMap.put("RB", POS.ADV);
	    this.POSMap.put("RBR", POS.ADV);
	    this.POSMap.put("RBS", POS.ADV);
	    this.POSMap.put("VB", POS.VERB);
	    this.POSMap.put("VBD", POS.VERB);
	    this.POSMap.put("VBG", POS.VERB);
	    this.POSMap.put("VBN", POS.VERB);
	    this.POSMap.put("VBP", POS.VERB);
	    this.POSMap.put("VBZ", POS.VERB);
	    this.POSMap.put("WRB", POS.ADV);

	    this.POSMap.put("N", POS.NOUN);
	    this.POSMap.put("V", POS.VERB);
	    this.POSMap.put("J", POS.ADJ);
	    this.POSMap.put("R", POS.ADV);
	
	    nounTags = Arrays.asList("N", "NN", "NNP", "NNS");
	}

   
	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		// it...
		int s = 0;
		List<Sentence> sentences = new ArrayList<>(select(aJCas, Sentence.class));
		for (Sentence sentence : sentences) {
			s++;
			int wordFormCount = 0;
			List<Token> tokens = JCasUtil.selectCovered(aJCas, Token.class, sentence);
			for (int i = 0; i < tokens.size(); i++) {
				Token token = tokens.get(i);
				String lemma = token.getLemmaValue();
				// save synsets for pos!=X
				try {
				if (!token.getPos().getPosValue().equals("X") && this.POSMap.containsKey(token.getPos().getPosValue()) ) {
					// save the synsets for the single wf
					HashSet<String> senses= new HashSet<String>();
					POS pos = this.POSMap.get(token.getPos().getPosValue());
					if (lemma!=null) senses.addAll(si.getSenses(lemma, pos));
					senses.addAll(si.getSenses(token.getCoveredText(), pos));
					if (senses.size() > 0) {
						wordFormCount++;
						String wordFormId = "babelNet" + ".s" + s + ".w" + i;

						LexicalItemConstituent c = newLexicalItemConstituent(aJCas, wordFormId, ELEMENT_WORDFORM,
								token.getBegin(), token.getEnd());
						String wordPos= this.POSMap.get(token.getPos().getPosValue()).toString();
						if (this.POSMap.get(token.getPos().getPosValue())==null){
							wordPos=POS.NOUN.toString();
							System.out.println("error in pos \t" + token.getPos().getPosValue() + "\t" + token.getCoveredText());
						}
						if (lemma==null) lemma=token.getCoveredText();
						WSDItem w = newWsdItem(aJCas, wordFormId, token.getBegin(), token.getEnd(),
								wordPos, lemma);
						w.setConstituents(new FSArray(aJCas, 1));
						w.setConstituents(0, c);
						// Get an array of sense tags. Sense tags are found
						// in the lexsn attribute and are separated with
						// semicolons. Sometimes the head_word field contains
						// a superfluous character in parentheses which must
						// be removed. (These quirks are not documented in
						// the SemCor file format specification.)
						/*
						FSArray senseArray = new FSArray(aJCas, senses.size());
						int validWfCount = 0;
						for (String senseIt : senses) {
							Sense sense = new Sense(aJCas);
							sense.setId(lemma + "%" + senseIt);
							sense.setConfidence(1.0);
							sense.addToIndexes();
							senseArray.set(validWfCount++, sense);
						}
						/* WSDResult wsdResult = new WSDResult(aJCas, token.getBegin(), token.getEnd());
						a .setWsdItem(w);
						wsdResult.setSenses(senseArray);
						wsdResult.setSenseInventory(si.getSenseInventoryName());
						wsdResult.setDisambiguationMethod(DISAMBIGUATION_METHOD_NAME);
						wsdResult.addToIndexes();
                        */
					}
				} else {
					// System.out.println("not processed \t" + token.getPos().getPosValue() + "\t" + token.getCoveredText());
				}
					} catch (Exception e) {
						System.out.println(lemma +" \t " + token.getCoveredText() + "\t" +token.getPos().getPosValue());
						System.out.println( this.POSMap.get(token.getPos().getPosValue()));
							e.printStackTrace();
					}	
			}
			List<NGram> ngrams = all_ngrams(maxWordsFragment, tokens);
			for (NGram nGram : ngrams) {
				String ngram = nGram.text;
				String ngramLemma = nGram.lemmas;
				try {
					//HashSet<String> senses= new HashSet<String>();
					List <String> senses;
					senses=si.getSenses(ngramLemma);
					senses.addAll(si.getSenses(ngram));
				
				if (senses.size() > 0) {
					wordFormCount++;
					String wordFormId = "babelNet" + ".s" + s + ".mw" + wordFormCount;
					// POs
					POS pos = si.getPos(senses.get(0));
					if (pos !=null) {
					LexicalItemConstituent c = newLexicalItemConstituent(aJCas, wordFormId, ELEMENT_WORDFORM,
							nGram.begin, nGram.end);
					if (ngramLemma.trim().isEmpty()) ngramLemma=ngram;
					WSDItem w = newWsdItem(aJCas, wordFormId, nGram.begin, nGram.end, pos.toString(), ngramLemma);
					//TODO This should be an array with all the tokens ? 
					w.setConstituents(new FSArray(aJCas, 1));
					w.setConstituents(0, c);
					} else{
						System.out.println(ngram + "pos"+ pos );
					}
					// Get an array of sense tags. Sense tags are found
					// in the lexsn attribute and are separated with
					// semicolons. Sometimes the head_word field contains
					// a superfluous character in parentheses which must
					// be removed. (These quirks are not documented in
					// the SemCor file format specification.)
					/*FSArray senseArray = new FSArray(aJCas, senses.size());
					int validWfCount = 0;
					for (String senseIt : senses) {
						Sense sense = new Sense(aJCas);
						sense.setId(ngramLemma + "%" + senseIt);
						sense.setConfidence(1.0);
						sense.addToIndexes();
						senseArray.set(validWfCount++, sense);
					}
					/*
					WSDResult wsdResult = new WSDResult(aJCas, nGram.begin, nGram.end);
					wsdResult.setWsdItem(w);
					wsdResult.setSenses(senseArray);
					wsdResult.setSenseInventory(si.getSenseInventoryName());
					wsdResult.setDisambiguationMethod(DISAMBIGUATION_METHOD_NAME);
					wsdResult.addToIndexes();
					*/
				}
				} catch (Exception e) {
					e.printStackTrace();
		    	}	

			}
		}
	}

           public static class NGram {
        	   public String text;
        	   public String lemmas;
        	   public int begin;
        	   public int end;
           }
       
           public static List<NGram> ngrams(int n, List<Token> tokens) {
               List<NGram> ngrams = new ArrayList<NGram>();
               for (int i = 0; i < tokens.size() - n + 1; i++){
                   ngrams.add(concat(tokens, i, i+n));
               }
               return ngrams;
           }

           public static NGram concat(List<Token> tokens, int start, int end) {
               StringBuilder words = new StringBuilder();             
               StringBuilder lemmas= new StringBuilder();
               String rev="\u202C";
               if (si.isRightToLeft()) rev="\u202B";             
               for (int i = start; i < end; i++){
                   words.append((i > start ? " " : "")+ rev + tokens.get(i).getCoveredText());
                   if (tokens.get(i).getLemma()!=null) lemmas.append((i > start ? " " : "")+ rev + tokens.get(i).getLemmaValue());
                 }
               NGram res= new NGram();
               res.begin=tokens.get(start).getBegin();
               res.end=tokens.get(end-1).getEnd();
               res.text=words.toString().replaceAll(rev, "");
               res.lemmas=lemmas.toString().replaceAll(rev, "");
            		   
               return res;
           }
  
           public static List<NGram> all_ngrams(int size, List<Token> tokens) {
               List<NGram> ngrams = new ArrayList<NGram>();

           	for (int n = 2; n <= size; n++) {
           		ngrams.addAll(ngrams(n,tokens));
               }
           	return ngrams;
           }   
           
           
    

   /**
    * Creates a new LexicalItemConstituent annotation and adds it to the
    * annotation index.
    *
    * @param jCas
    *            The CAS in which to create the annotation.
    * @param id
    *            An identifier for the annotation.
    * @param constituentType
    *            The constituent type (e.g., "head", "satellite").
    * @param begin
    *            The index of the first character of the annotation in the
    *            document.
    * @param end
    *            the index of the last character.
    * @return The new annotation.
    */	
protected LexicalItemConstituent newLexicalItemConstituent(JCas jCas, String id, String constituentType, int begin, int end)
{
    LexicalItemConstituent c = new LexicalItemConstituent(jCas);
    c.setBegin(begin);
    c.setEnd(end);
    c.setConstituentType(constituentType);
    c.setId(id);
    c.addToIndexes();
    return c;
}




/**
 * Creates a new WSDItem annotation and adds it to the annotation index.
 *
 * @param jCas
 *            The CAS in which to create the annotation.
 * @param id
 *            An identifier for the annotation.
 * @param begin
 *            The index of the first character of the annotation in the
 *            document.
 * @param end
 *            The index of the last character (plus 1)  of the annotation in the
 *            document.
 * @param pos
 *            The part of speech, if known, otherwise null.
 * @param lemma
 *            The lemmatized form, if known, otherwise null.
 * @return The new annotation.
 */
protected WSDItem newWsdItem(JCas jCas, String id, int begin, int end,
        String pos, String lemma)
{
    WSDItem w = new WSDItem(jCas);
    w.setBegin(begin);
    w.setEnd(end);
    w.setId(id);
    if (pos == null) {
        w.setPos(null);
    }
    else {
        w.setPos(pos);
    }
    w.setSubjectOfDisambiguation(lemma);
    w.addToIndexes();
    return w;
}



}
