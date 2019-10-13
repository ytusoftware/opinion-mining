/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.opmining;

import ca.pfv.spmf.algorithms.frequentpatterns.fpgrowth.AlgoFPGrowth;
import ca.pfv.spmf.patterns.itemset_array_integers_with_count.Itemset;
import ca.pfv.spmf.patterns.itemset_array_integers_with_count.Itemsets;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;
import zemberek.morphology.TurkishMorphology;
import zemberek.morphology.analysis.SingleAnalysis;
import zemberek.morphology.analysis.WordAnalysis;
import zemberek.tokenization.Token;
import zemberek.tokenization.TurkishSentenceExtractor;
import zemberek.tokenization.TurkishTokenizer;

/**
 *
 * @author cetintekin
 */
public class FeatureExtraction {
    
    
    private Itemsets result;                                       /* Holds SPMF Apriori Algo. results (itemsets) */                           
    private HashMap<String, Integer> possibleFeatureIndexMap;      /* Holds mapping of candidate features to their ids (ids are used for Apriori algorithm). */
    private int frequencyThreshold;                                /* Holds the frequency threshold for Apriori itemset generation (Default = 3) */
    
    
    
    public FeatureExtraction() {
        
        this.possibleFeatureIndexMap = new HashMap<>();
        this.frequencyThreshold = 6;
                
        
    }
    
    
    
    /* Sets frequency threshold for the Apriori Algorithm */
    public void setFrequencyThreshold(int freqThld) {
        this.frequencyThreshold = freqThld;
    }
    
    
    
    /* Gets frequency threshold for the Apriori Algorithm */
    public int getFrequencyThreshold() {
        return this.frequencyThreshold;
    }   
    
    
    
    /* Gets keys (features) from the id of the feature in the feature-id HashMap */
    private Object getKeyFromValue(HashMap hm, Integer value) {
        
        for (Object o : hm.keySet()) {
            if (hm.get(o).equals(value)) {
                return o;
            }
        }
        return null;
    }
    
    
    /* This method returns the SingleAnalysis result for the word which is noun */
    private static SingleAnalysis isNoun(WordAnalysis analysis){
        int i = 0;
        List <SingleAnalysis> result = analysis.getAnalysisResults();
         
        while(i< result.size() && result.get(i).getPos().shortForm.compareTo("Noun")!=0){
            i += 1;
        }
        
        if(i<result.size()){
            return result.get(i);
        }else{
            return null;
        }
   
    }  
    
    
    
    /* Writes min heap tree elements to the Apriori algorithm input text file. (min heap tree is used for cumulatively storing numerical ids of features in the increasing order) */ 
    private void writeHeapTree(PriorityQueue<Integer> minHeap, PrintWriter out) {
        
        Integer id;
        
        while( (id=minHeap.poll()) != null ) {
            out.print(id);
            out.print(' ');
        }    
        out.println();   
        
    }
    
    
    
    /* Gets root directory of the project to save text file */
    private String fileToPath(String filename) throws UnsupportedEncodingException{
	return System.getProperty("user.dir")+"/aprioriInput.txt";
    }
    
    
    
    /* Prints features found by Apriori algoritm */
    public void printAprioriFeatures() {
        
        String feature;      /* Final features after Apriori algorithm */
        
        
        /* Printing the real features found by Apriori algorithm according to min support */
        System.out.println("---------Extracted features--------");
        for (List<Itemset> level : result.getLevels()) {
            if (!level.isEmpty()) {
                for (Itemset itemset : level) {
                    for (int i = 0; i < itemset.size(); i++) {
                        feature = (String) getKeyFromValue(possibleFeatureIndexMap, itemset.get(i));
                        System.out.print(feature+" ");
                    }
                    System.out.println();
                }
            }
        } 

    }
    
    
    
    /* 
        * This method preparing aprioriInput.txt file as an input file for the SPMF Æpriori algo.
        * Takes all texts from MongoDB respectively.
        * Performs stopword removal and stemming as preprocessing operations on the texts.
        * Adds all words which have noun form to the aprioriInput.txt file.
    */
    
    private int extractCandidateFeatures() throws UnsupportedEncodingException, FileNotFoundException, IOException {
        
        TurkishSentenceExtractor extractor = TurkishSentenceExtractor.DEFAULT;     /* Sentence extractor from paragraph */
        TurkishTokenizer tokenizer = TurkishTokenizer.DEFAULT;                     /* Word extractor from sentence */
        TurkishMorphology morphology = TurkishMorphology.createWithDefaults();     /* Used for finding stems of words */                                                      
        PrintWriter out;                                                           /* Used for writing candidate feature ids to the text file to make Apriori algo. work */
        List<String> sentences;                                                    /* Holds sentences extracted from paragraph */
        List<Token> tokens;                                                        /* Holds words extracted from sentence */
        WordAnalysis results;                                                      /* Used for holding the result of Turkish morphological analysis */
        int generalFeatureIndex;                                                   /* Used for labeling the candidate features for the use of Apriori algo. work */
        SingleAnalysis sa;
        PriorityQueue<Integer> minHeap = new PriorityQueue<>();                    /* This min heap tree is used for getting the candidate feature ids in the increasing order */
        ArrayList<String> allTexts = new ArrayList<>();                            /* Used for storing the all texts */
        int sentenceCnt;
        List<String> stopWords;                                                    /* This list includes turkish stop words */
        int aprioriId;
        
       
        
  
        
        /* Initializing the text file writer (This file is used by SPMF lib's Apriopri algo later.) */
        out = new PrintWriter(fileToPath("aprioriInput.txt"));
        
        /* Reading stop words from text file */
        Collections.emptyList();
        stopWords = Files.readAllLines(Paths.get("stopwords.txt"), StandardCharsets.UTF_8);

        
        /* Opening DB connection and getting the texts */
        DBOperations operation = new DBOperations();
        operation.startConnection("ProjectDB", "Texts");
        operation.getAllTexts(allTexts);
        
        
        sentenceCnt = 0;
        generalFeatureIndex = 1;
        
               
        
        /* All texts in the MongoDB is traversed respectively */
        for (String text : allTexts) {
            
            sentences = extractor.fromParagraph(text);
            sentenceCnt += sentences.size();
            
            /* All sentences in the paragraph are traversed respectively. */
            for (String sentence : sentences) {      
                
                tokens = tokenizer.tokenize(sentence);

                /* All words in the text is traversed */
                for (Token token : tokens) {
                    
                    if (!stopWords.contains(token.getText())) {                       
                        
                        /* Stemming the single word.. */
                        results =  morphology.analyze(token.getText());                        

                        /* This control is required due to empty analysis result */
                        if( !results.getAnalysisResults().isEmpty() ) {

                            //sa = results.getAnalysisResults().get(0);
                            sa = isNoun(results);
                            /* Checking if the initial word of the sentence is noun or not */
                            if (sa != null) {

                                /* If the initial candidate feature is being found for the first time, then a new id is given to that candidate feature by */
                                /* using generalFeatureIndex */
                                if (!possibleFeatureIndexMap.containsKey(sa.getStem())) {                           
                                    /* The candidate feature is added to the general "candidate feature-id" map */
                                    possibleFeatureIndexMap.put(sa.getStem(), generalFeatureIndex++);

                                }
                                
                                /* Getting the id of the candidate feature from the map */
                                aprioriId = possibleFeatureIndexMap.get(sa.getStem());

                                /* Checking if this candidate feature is appeared more than once in this transaction table row (SPMF Apriori algo. won't work otherwise.) */
                                if (!minHeap.contains(aprioriId)) {
                                    /* Then, adding the id of the candidate feature to the min heap tree. (this is for giving transaction row ids in the ascending order to the Apriori algo.) */
                                    minHeap.add(aprioriId);
                                }    

                            }

                        } 

                        
                    }


                }
                /* Writing candidate feature ids to the Apriori algo. input text file */
                writeHeapTree(minHeap, out);
            }      
                
        }
        
        out.close();
        operation.closeConnection();
        
        return sentenceCnt;

    }
    
    
    /* By using possibleFeatureIndexMap filled by extractCandidateFeatures, runs SPMF lib Apriori method to extract features */
    public void extractAprioriFeatures() throws UnsupportedEncodingException, IOException {
       
        String input;       /* Holds the absolute path for input file for the use of Apriori algorithm */
        String output;      /* This variable can be used for printing the Apriori algorithm results (stats) to a text file */
        int sentenceCnt;
              
         
        
        /* Extracting candidate features */
        sentenceCnt = this.extractCandidateFeatures();
        System.out.println(sentenceCnt);
        
        
        /* Opening the candidate feature transaction table */
        input = fileToPath("aprioriInput.txt");
	output = null;   // No text file output is requested

		
	double minsup = this.frequencyThreshold/((double)100); // means a minsup of 2 transaction (we used a relative support)
		
	/* Applying the Apriori algorithm */
	AlgoFPGrowth algorithm = new AlgoFPGrowth();
		
	/* Uncomment the following line to set the maximum pattern length (number of items per itemset, e.g. 3 ) */
        //apriori.setMaximumPatternLength(3);
		
	//Itemsets result = null;
                
        this.result = algorithm.runAlgorithm(input, output, minsup);

        /* Uncomment to see the Apriori algorith stats */
        //algorithm.printStats();
        //result.printItemsets(algorithm.getDatabaseSize());
                	       
        
    }
    
    
}
