package edu.upf.taln.uima.wsd.candidateDetection;


import static org.apache.uima.fit.util.JCasUtil.select;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.wsd.si.POS;
import de.tudarmstadt.ukp.dkpro.wsd.si.SenseInventoryException;
import de.tudarmstadt.ukp.dkpro.wsd.si.uby.UbySenseInventory;
import de.tudarmstadt.ukp.dkpro.wsd.type.LexicalItemConstituent;
import de.tudarmstadt.ukp.dkpro.wsd.type.WSDItem;



public class UbyCandidateIdentification   extends JCasAnnotator_ImplBase {
	
   private static UbySenseInventory si;
   private static int maxWordsFragment = 7;
   private static Map<String, POS> POSMap;  
   private static final String ELEMENT_WORDFORM = "wf";
   public static final String DISAMBIGUATION_METHOD_NAME = "none";
   @Override
   public void initialize(UimaContext context)
       throws ResourceInitializationException
   {
       super.initialize(context);
       try {
		si = new UbySenseInventory(
		           "127.0.0.1:3307/uby_open_0_7_0",
		           // "com.mysql.jdbc.Driver", "mysql", "uby", "UbyTaln_17",
		           "com.mysql.jdbc.Driver", "mysql", "root", "ipat14",
		            false);
		
		    UbyCandidateIdentification.POSMap = new HashMap<String, POS>();
		    UbyCandidateIdentification.POSMap.put("JJ", POS.ADJ);
		    UbyCandidateIdentification.POSMap.put("JJR", POS.ADJ);
		    UbyCandidateIdentification.POSMap.put("JJS", POS.ADJ);
		    UbyCandidateIdentification.POSMap.put("NN", POS.NOUN);
		    UbyCandidateIdentification.POSMap.put("NNS", POS.NOUN);
		    UbyCandidateIdentification.POSMap.put("NNP", POS.NOUN);
		    UbyCandidateIdentification.POSMap.put("NNPS", POS.NOUN);
		    UbyCandidateIdentification.POSMap.put("RB", POS.ADV);
		    UbyCandidateIdentification.POSMap.put("RBR", POS.ADV);
		    UbyCandidateIdentification.POSMap.put("RBS", POS.ADV);
		    UbyCandidateIdentification.POSMap.put("VB", POS.VERB);
		    UbyCandidateIdentification.POSMap.put("VBD", POS.VERB);
		    UbyCandidateIdentification.POSMap.put("VBG", POS.VERB);
		    UbyCandidateIdentification.POSMap.put("VBN", POS.VERB);
		    UbyCandidateIdentification.POSMap.put("VBP", POS.VERB);
		    UbyCandidateIdentification.POSMap.put("VBZ", POS.VERB);
		    UbyCandidateIdentification.POSMap.put("WRB", POS.ADV);

		    UbyCandidateIdentification.POSMap.put("N", POS.NOUN);
		    UbyCandidateIdentification.POSMap.put("V", POS.VERB);
		    UbyCandidateIdentification.POSMap.put("J", POS.ADJ);
		    UbyCandidateIdentification.POSMap.put("R", POS.ADV);
		
		
	} catch (SenseInventoryException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}

	}

   
	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
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
				if (!token.getPos().getPosValue().equals("X") && UbyCandidateIdentification.POSMap.containsKey(token.getPos().getPosValue()) ) {
					// save the synsets for the single wf
					HashSet<String> senses= new HashSet<String>();
					senses.addAll(si.getSenses(lemma, UbyCandidateIdentification.POSMap.get(token.getPos().getPosValue())));
					senses.addAll(si.getSenses(token.getCoveredText(), UbyCandidateIdentification.POSMap.get(token.getPos().getPosValue())));
					if (senses.size() > 0) {
						
						wordFormCount++;
						String wordFormId = "uby" + ".s" + s + ".w" + i;

						LexicalItemConstituent c = newLexicalItemConstituent(aJCas, wordFormId, ELEMENT_WORDFORM,
								token.getBegin(), token.getEnd());
						String wordPos= this.POSMap.get(token.getPos().getPosValue()).toString();
						if (this.POSMap.get(token.getPos().getPosValue())==null){
							wordPos=POS.NOUN.toString();
							System.out.println("error in pos \t" + token.getPos().getPosValue() + "\t" + token.getCoveredText());
						}
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
					String wordFormId = "uby" + ".s" + s + ".mw" + wordFormCount;
					// POs
					POS pos = si.getPos(senses.get(0));
					if (pos !=null) {
					LexicalItemConstituent c = newLexicalItemConstituent(aJCas, wordFormId, ELEMENT_WORDFORM,
							nGram.begin, nGram.end);
					WSDItem w = newWsdItem(aJCas, wordFormId, nGram.begin, nGram.end, pos.toString(), ngramLemma);
					//TODO This should be an array with all the tokens ? 
					w.setConstituents(new FSArray(aJCas, 1));
					w.setConstituents(0, c);
					} else{
						System.out.println(ngram + "pos"+ pos );
					}

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
               
               for (int i = start; i < end; i++){
                   words.append((i > start ? " " : "") + tokens.get(i).getCoveredText());
                   lemmas.append((i > start ? " " : "") + tokens.get(i).getLemmaValue());
                 }
               NGram res= new NGram();
               res.begin=tokens.get(start).getBegin();
               res.end=tokens.get(end-1).getEnd();
               res.text=words.toString();
               res.lemmas=lemmas.toString();
            		   
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
