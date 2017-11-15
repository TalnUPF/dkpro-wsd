package edu.upf.taln.uima.wsd.candidateDetection;


import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.apache.uima.fit.factory.ExternalResourceFactory.createExternalResourceDescription;
import static org.junit.Assert.*;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.logging.Logger;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.pipeline.JCasIterable;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.resource.ExternalResourceDescription;
import org.apache.uima.resource.ResourceInitializationException;


import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;
import de.tudarmstadt.ukp.dkpro.core.io.xmi.XmiWriter;
//import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpLemmatizer;
import de.tudarmstadt.ukp.dkpro.core.matetools.MateLemmatizer;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpSegmenter;
import de.tudarmstadt.ukp.dkpro.wsd.algorithm.FirstSenseBaseline;
import de.tudarmstadt.ukp.dkpro.wsd.algorithm.MostFrequentSenseBaseline;
import de.tudarmstadt.ukp.dkpro.wsd.algorithm.WSDAlgorithmIndividualBasic;
import de.tudarmstadt.ukp.dkpro.wsd.annotator.WSDAnnotatorContextPOS;
import de.tudarmstadt.ukp.dkpro.wsd.annotator.WSDAnnotatorIndividualBasic;
import de.tudarmstadt.ukp.dkpro.wsd.annotator.WSDAnnotatorIndividualPOS;
import de.tudarmstadt.ukp.dkpro.wsd.candidates.WSDItemAnnotator;
import de.tudarmstadt.ukp.dkpro.wsd.lesk.algorithm.SimplifiedLesk;
import de.tudarmstadt.ukp.dkpro.wsd.lesk.resource.WSDResourceSimplifiedLesk;
import de.tudarmstadt.ukp.dkpro.wsd.lesk.util.normalization.NoNormalization;
import de.tudarmstadt.ukp.dkpro.wsd.lesk.util.overlap.PairedOverlap;
import de.tudarmstadt.ukp.dkpro.wsd.resource.WSDResourceIndividualBasic;
import de.tudarmstadt.ukp.dkpro.wsd.resource.WSDResourceIndividualPOS;
import de.tudarmstadt.ukp.dkpro.wsd.si.wordnet.WordNetSynsetSenseInventory;
import de.tudarmstadt.ukp.dkpro.wsd.si.wordnet.resource.WordNetSenseKeySenseInventoryResource;
import de.tudarmstadt.ukp.dkpro.wsd.type.WSDItem;
import de.tudarmstadt.ukp.dkpro.wsd.type.WSDResult;
import de.tudarmstadt.ukp.dkpro.wsd.si.POS;
import de.tudarmstadt.ukp.dkpro.wsd.si.uby.resource.UbySenseInventoryResource;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
/**
 * In this pipeline, we try 
 */
public class UbyCandidateDetectionTest {
    
	static private HashMap<String,String> FirstSenses;
	static private HashMap<String,String> MostFreqSenses;
	static private HashMap<String,String>LeskSenses;
	static private HashMap<String,Double> LeskConf;
	static private  JCasIterable pipelineFirst;
	static private  JCasIterable pipelineMost;
	static private  JCasIterable pipelineLesk;
	static private  Logger logger;
	 
	 @Before
	public  void setUp() throws ResourceInitializationException{
		FirstSenses=new HashMap<>();
		MostFreqSenses=new HashMap<>();
		LeskSenses=new HashMap<>();
		LeskConf=new HashMap<>();
		
		
		FirstSenses.put("people","WN_Sense_108540");
		FirstSenses.put("were","WN_Sense_147694");
		FirstSenses.put("dead","WN_Sense_178794");
		FirstSenses.put("gunman","WN_Sense_82958");
		FirstSenses.put("opened fire","WN_Sense_160859");
		FirstSenses.put("opened","WN_Sense_160848");
		FirstSenses.put("fire","WN_Sense_72671");
		FirstSenses.put("Amish","WktEN_sense_85543");
		FirstSenses.put("schoolhouse","WN_Sense_122324");
		FirstSenses.put("Monday","WN_Sense_22504");
		FirstSenses.put("Pennsylvania","WN_Sense_25741");
		FirstSenses.put("bucolic","WN_Sense_176119");
		FirstSenses.put("Lancaster County","WikiEN_sense_215687");
		FirstSenses.put("Lancaster","WN_Sense_19088");
		FirstSenses.put("County","WN_Sense_58928");
		FirstSenses.put("county","WN_Sense_58928");
		FirstSenses.put("coroner","WN_Sense_58427");
		FirstSenses.put("said","WN_Sense_165251");
		
		
		MostFreqSenses.put("people","people%1:14:00::");
		MostFreqSenses.put("were","be%2:42:03::");
		MostFreqSenses.put("dead","dead%3:00:01::");
		MostFreqSenses.put("gunman","gunman%1:18:00::");
		MostFreqSenses.put("opened fire","open_fire%2:33:00::");
		MostFreqSenses.put("opened","open%2:35:00::");
		MostFreqSenses.put("fire","fire%1:11:00::");
		MostFreqSenses.put("schoolhouse","schoolhouse%1:06:00::");
		MostFreqSenses.put("Monday","monday%1:28:00::");
		MostFreqSenses.put("Pennsylvania","pennsylvania%1:15:00::");
		MostFreqSenses.put("bucolic","bucolic%5:00:01:rural:00");
		MostFreqSenses.put("Lancaster","lancaster%1:15:00::");
		MostFreqSenses.put("County","county%1:15:00::");
		MostFreqSenses.put("county","county%1:15:00::");
		MostFreqSenses.put("coroner","coroner%1:18:00::");
		MostFreqSenses.put("said","say%2:32:00::");

		LeskSenses.put("people","FN_Sense_3143");
		LeskConf.put("people",1.0);
		LeskSenses.put("dead","FN_Sense_9910");
		LeskConf.put("dead",1.0);
		LeskSenses.put("gunman","FN_Sense_2021");
		LeskConf.put("gunman",1.0);
		LeskSenses.put("opened fire","OntoWktEN_sense_192761");
		LeskConf.put("opened fire",1.0);
		LeskSenses.put("opened","FN_Sense_7472");
		LeskConf.put("opened",1.0);
		LeskSenses.put("fire","OW_eng_Sense_11720");
		LeskConf.put("fire",1.0);
		LeskSenses.put("Amish","OntoWktEN_sense_95218");
		LeskConf.put("Amish",1.0);
		LeskSenses.put("schoolhouse","OntoWktEN_sense_213889");
		LeskConf.put("schoolhouse",0.75);
		LeskSenses.put("Monday","FN_Sense_37");
		LeskConf.put("Monday",1.0);
		LeskSenses.put("bucolic","OntoWktEN_sense_44382");
		LeskConf.put("bucolic",1.0);
		LeskSenses.put("Lancaster County","WikiDE_sense_146998");
		LeskConf.put("Lancaster County",1.0);
		LeskSenses.put("Lancaster","OntoWktEN_sense_160840");
		LeskConf.put("Lancaster",0.71875);
		LeskSenses.put("County","FN_Sense_1082");
		LeskConf.put("County",0.6818181818181819);
		LeskSenses.put("county","FN_Sense_1082");
		LeskConf.put("county",0.6818181818181819);
		LeskSenses.put("coroner","OntoWktEN_sense_118923");
		LeskConf.put("coroner",0.9166666666666666);

		
				logger = Logger.getLogger(UbyCandidateDetectionTest.class.toString());

				// setup components
				
				// READER 
				CollectionReaderDescription reader = createReaderDescription(TextReader.class,
						TextReader.PARAM_LANGUAGE, "en",
						TextReader.PARAM_SOURCE_LOCATION, "src/test/input/",
						TextReader.PARAM_PATTERNS, "*.txt"
						);
		        // TEXT ANALYSIS
				AnalysisEngineDescription segmenter = createEngineDescription(OpenNlpSegmenter.class);
				AnalysisEngineDescription pos = createEngineDescription(OpenNlpPosTagger.class);
				AnalysisEngineDescription lemma = createEngineDescription(MateLemmatizer.class,
						MateLemmatizer.PARAM_MODEL_LOCATION, new File("/home/joan/Desktop/TALN/UIMA/", "CoNLL2009-ST-English-ALL.anna-3.3.lemmatizer.model"));
				AnalysisEngineDescription candidates = createEngineDescription(UbyCandidateIdentification.class);
		         
		 
		 		/* The same with wordnet */
		 		
		        final String wordnetInventoryName = "WordNet_3.0_sensekey";
		        ExternalResourceDescription wordnet30 = createExternalResourceDescription(
		                WordNetSenseKeySenseInventoryResource.class,
		                WordNetSenseKeySenseInventoryResource.PARAM_WORDNET_PROPERTIES_URL,
		                "classpath:/extjwnl_properties.xml",
		                WordNetSenseKeySenseInventoryResource.PARAM_SENSE_INVENTORY_NAME,
		                wordnetInventoryName);
				// Here's a resource encapsulating the most frequent sense baseline
				// algorithm, which we bind to the JLSR sense inventory.
				ExternalResourceDescription mfsBaselineResource = createExternalResourceDescription(
						WSDResourceIndividualPOS.class,
						WSDResourceIndividualPOS.SENSE_INVENTORY_RESOURCE, wordnet30,
						WSDResourceIndividualPOS.DISAMBIGUATION_METHOD,	MostFrequentSenseBaseline.class.getName());
				// And here we create an analysis engine, and bind to it the
				// most frequent sense baseline resource.
				AnalysisEngineDescription mfsBaseline = createEngineDescription(
						WSDAnnotatorIndividualPOS.class,
						WSDAnnotatorIndividualPOS.WSD_ALGORITHM_RESOURCE,mfsBaselineResource,
						WSDAnnotatorIndividualPOS.PARAM_MAXIMUM_ITEMS_TO_ATTEMPT,50);	  // it must create the inventory as a resource
		 		// the desambiguator resource WSDResourceIndividualPOS (that uses the inventory)
		 		// and finally the annotator (that uses the desambiguator)
		 		
		        final String UbyInventoryName = "Uby_sensekey";
		        ExternalResourceDescription uby = createExternalResourceDescription(
		        		UbySenseInventoryResource.class,
		                UbySenseInventoryResource.PARAM_UBY_DATABASE_URL,"127.0.0.1:3307/uby_open_0_7_0",
		                UbySenseInventoryResource.PARAM_UBY_DB_VENDOR, "mysql",
		                UbySenseInventoryResource.PARAM_UBY_JDBC_DRIVER_CLASS,"com.mysql.jdbc.Driver",
		                UbySenseInventoryResource.PARAM_UBY_PASSWORD, "ipat14",
		                UbySenseInventoryResource.PARAM_UBY_USER, "root");
		         // connect to database ... 
		        //  ssh -2 -N -C -L 3307:localhost:3306 jcodina@ipatdoc.taln.upf.edu
		        // installed from uby_open_0_7

				// Here's a resource encapsulating the most frequent sense baseline
				// algorithm, but needs the frequency which uby does not provide for all senses, producing errors.
				ExternalResourceDescription mfsBaselineResourceUby = createExternalResourceDescription(
						WSDResourceIndividualBasic.class,
						WSDResourceIndividualBasic.SENSE_INVENTORY_RESOURCE, uby,
						WSDResourceIndividualBasic.DISAMBIGUATION_METHOD,	MostFrequentSenseBaseline.class.getName());
				// And here we create an analysis engine, and bind to it the
				// most frequent sense baseline resource.
				AnalysisEngineDescription mfsBaselineUby = createEngineDescription(
						WSDAnnotatorIndividualBasic.class,
						WSDAnnotatorIndividualBasic.WSD_ALGORITHM_RESOURCE,mfsBaselineResourceUby,
						WSDAnnotatorIndividualBasic.PARAM_MAXIMUM_ITEMS_TO_ATTEMPT,50);		// Here's a resource encapsulating the most frequent sense baseline
			    // Create a resource for the simplified Lesk algorithm.  We bind our
		        // WordNet sense inventory to it, and we specify some parameters such
		        // as what strategy the algorithm uses for tokenizing the text and
		        // how it computes the word overlaps.
		        ExternalResourceDescription simplifiedLeskResource = createExternalResourceDescription(
		                WSDResourceSimplifiedLesk.class,
		                WSDResourceSimplifiedLesk.SENSE_INVENTORY_RESOURCE, uby,
		                WSDResourceSimplifiedLesk.PARAM_NORMALIZATION_STRATEGY,NoNormalization.class.getName(),
		                WSDResourceSimplifiedLesk.PARAM_OVERLAP_STRATEGY, PairedOverlap.class.getName(),
		                WSDResourceSimplifiedLesk.PARAM_TOKENIZATION_STRATEGY,EnglishStopLemmatizer.class.getName()
		                );

		        // Create an annotator for the WSD algorithm.  We bind the simplified
		        // Lesk resource to it, and specify some further parameters, such as
		        // how much text to pass it (in this case, a sentence) and how to
		        // post-process the disambiguation confidence scores
		        AnalysisEngineDescription simplifiedLesk = createEngineDescription(
		                WSDAnnotatorContextPOS.class,
		                WSDAnnotatorContextPOS.WSD_METHOD_CONTEXT, simplifiedLeskResource,
		                WSDAnnotatorContextPOS.PARAM_CONTEXT_ANNOTATION, Sentence.class.getName(),
		                WSDAnnotatorContextPOS.PARAM_NORMALIZE_CONFIDENCE, true,
		                WSDAnnotatorContextPOS.PARAM_BEST_ONLY, false, 
		                // WSDAnnotatorContextPOS.PARAM_DISAMBIGUATION_METHOD_NAME,""
		                WSDAnnotatorContextPOS.PARAM_MAXIMUM_ITEMS_TO_ATTEMPT, 50);
		        
		        
		        
		        // Create a resource for the first sense baseline
		        ExternalResourceDescription firstBaselineResource = createExternalResourceDescription(
		                WSDResourceIndividualPOS.class,
		                WSDResourceIndividualPOS.SENSE_INVENTORY_RESOURCE, uby,
		                WSDResourceIndividualPOS.DISAMBIGUATION_METHOD,
		                FirstSenseBaseline.class.getName());

		        // Create an annotator for the first sense baseline
		        AnalysisEngineDescription firstBaseline = createEngineDescription(
		                WSDAnnotatorIndividualPOS.class,
		                WSDAnnotatorIndividualPOS.WSD_ALGORITHM_RESOURCE,
		                firstBaselineResource      );
		        
		 
				AnalysisEngineDescription xmiWriter = createEngineDescription(XmiWriter.class,
						XmiWriter.PARAM_TARGET_LOCATION, "output/",
						XmiWriter.PARAM_OVERWRITE, true,
						XmiWriter.PARAM_TYPE_SYSTEM_FILE, "TypeSystem.xml");
				
				// configure pipeline
				 pipelineFirst = new JCasIterable(reader, segmenter, pos,lemma, candidates,firstBaseline /*itemAnno, simplifiedLesk*/);
				 pipelineMost = new JCasIterable(reader, segmenter, pos,lemma, candidates,mfsBaseline/*itemAnno, simplifiedLesk*/ );
				 pipelineLesk = new JCasIterable(reader, segmenter, pos,lemma, candidates,simplifiedLesk, xmiWriter);
			    //JCasIterable pipeline = new JCasIterable(reader, segmenter, pos,lemma, candidates, xmiWriter);
				
		
		
		
	}
	@Test
	public void test1() throws Exception {
  
		// Run and produce the results to be tested. This is a trick to simplify to write, but values shoudl be check,
		// otherwise the test will be always correct
		logger.info("starting pipeline");
		for (JCas jcas : pipelineFirst) {
			//System.out.println(jcas.getDocumentText());
			for (WSDResult wsdResult : JCasUtil.select(jcas, WSDResult.class)) {
				assertEquals("in correct sense for "+wsdResult.getCoveredText(),
							FirstSenses.get(wsdResult.getCoveredText()),
							wsdResult.getSenses(0).getId() );
				// System.out.print("FirstSenses.put(\""+wsdResult.getCoveredText()+"\",\"" );
				// System.out.println(wsdResult.getSenses(0).getId()+"\");");
			}
			
		}

	}

	@Test
	public void test2() throws Exception {
  
		// Run and produce the results to be tested. This is a trick to simplify to write, but values shoudl be check,
		// otherwise the test will be always correct
		logger.info("starting pipeline");
		for (JCas jcas : pipelineMost) {
			//System.out.println(jcas.getDocumentText());
			for (WSDResult wsdResult : JCasUtil.select(jcas, WSDResult.class)) {
				assertEquals("in correct sense for "+wsdResult.getCoveredText(),
						MostFreqSenses.get(wsdResult.getCoveredText()),
						wsdResult.getSenses(0).getId() );
				// System.out.print("MostFreqSenses.put(\""+wsdResult.getCoveredText()+"\",\"" );
				//	System.out.println(wsdResult.getSenses(0).getId()+"\");");
				
				// System.out.println(senses.get(0));
			}
		}

	}
	@Test
	public void test3() throws Exception {
  
		// Run and produce the results to be tested. This is a trick to simplify to write, but values shoudl be check,
		// otherwise the test will be always correct
		logger.info("starting pipeline");
		for (JCas jcas : pipelineLesk) {
			//System.out.println(jcas.getDocumentText());
			for (WSDResult wsdResult : JCasUtil.select(jcas, WSDResult.class)) {
				 assertEquals("in correct sense for "+wsdResult.getCoveredText(),
						LeskSenses.get(wsdResult.getCoveredText()),
						wsdResult.getSenses(0).getId() );
				 assertEquals("in correct conf "+wsdResult.getCoveredText(),
						LeskConf.get(wsdResult.getCoveredText()),
						wsdResult.getSenses(0).getConfidence()*wsdResult.getSenses().size(), 0.001 );
				  
				 /*
				 System.out.print("LeskSenses.put(\""+wsdResult.getCoveredText()+"\",\"" );
				  System.out.println(wsdResult.getSenses(0).getId()+"\");");				
					System.out.print("LeskConf.put(\""+wsdResult.getCoveredText()+"\"," );
						System.out.println((wsdResult.getSenses(0).getConfidence()*wsdResult.getSenses().size())+");");
				*/
				// System.out.println(senses.get(0));
			}
		}

	}

}
