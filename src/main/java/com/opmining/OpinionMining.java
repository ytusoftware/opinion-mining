/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.opmining;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import zemberek.morphology.TurkishMorphology;
import zemberek.morphology.analysis.WordAnalysis;
import zemberek.tokenization.TurkishSentenceExtractor;
import zemberek.tokenization.TurkishTokenizer;
import org.json.JSONObject;


/**
 *
 * @author cetintekin
 * 
 * This class is responsible for opinion mining and related operations.
 */
public class OpinionMining {
        
    private HashSet<String> deviceFeatures;                             /* Holds all device features extracted by using Apriori algorithm */
    private HashSet<String> positiveOpinionWords;                       /* All positive opinion words */
    private HashSet<String> negativeOpinionWords;                       /* All negative opinion words */
    private ArrayList<String> allTextsFromDB;                           /* Holds all paragraphs read from MongoDB */
    private HashMap<String, ArrayList<Integer>> aspectBasedResults;     /* Holds aspect-based op mining results: {aspect:[posCnt, negCnt]} */
    
    
    
    public OpinionMining(HashSet<String> deviceFeatures, ArrayList<String> allTextsFromDB) {
        this. deviceFeatures = deviceFeatures;
        this.allTextsFromDB = allTextsFromDB;
        this.positiveOpinionWords = new HashSet<>();
        this.negativeOpinionWords = new HashSet<>();
    }
    
    /* Reads positive and negative opinion words from text files and store in opinionWords */
    private void readOpinionWords() throws IOException {
        
        List<String> positiveWords;         /* Holds all positive words read from text files */
        List<String> negativeWords;
        
        /* Reading opinion words from files */
        positiveWords = Files.readAllLines(Paths.get("positive-words.txt"), StandardCharsets.UTF_8);
        negativeWords = Files.readAllLines(Paths.get("negative-words.txt"), StandardCharsets.UTF_8);
        
        /* Transferring words to the hash set data structure. (to get O(1) search complexity) */
        this.positiveOpinionWords.addAll(positiveWords);
        this.negativeOpinionWords.addAll(negativeWords);
        
        
    }
    
    
    /* Transform word list supplied to method to their stems (kelime koku) */
    private void getStems(List<String> words, ArrayList<String> stemmedWords) {
        
        WordAnalysis results;                                                       /* Used for holding the result of Turkish morphological analysis */
        TurkishMorphology morphology = TurkishMorphology.createWithDefaults();      /* Used for finding stems of words */
        
                
        for (int i = 0; i < words.size(); i++) {

            /* Stemming the single word.. (kok bulunuyor) */
            results =  morphology.analyze(words.get(i));       
            
            if (!results.getAnalysisResults().isEmpty()) {
                /* Getting the first of analysis results (the stem) and putting in the stemmed words list */
                stemmedWords.add(results.getAnalysisResults().get(0).getStem());
            }
            
            else {
                stemmedWords.add(words.get(i));
            }          

        }
    }
    
    
    /* Spots the device features in the given sentence then stores their positions in the sentence */
    private void spotSentenceDeviceFeatures(ArrayList<String> sentence, HashMap<String,Integer> sentenceDeviceFeatures) {
        
        int i;
        String word;                    /* Holds the initial word */
        String adjacentWord;            /* Holds the neighbor word (one upper word) of the initial word */       
        
        
      
        i = 0;
        while (i < sentence.size() ) {
            
            word = sentence.get(i);
            
            /* Checking if the initial word is length one aspect or not */
            if (deviceFeatures.contains(word)) {
                
                /* Checking with combination of the adjacent word and the inital word to detect if these two words together form a length two aspect */
                if( (i+1) < sentence.size()) {
                    adjacentWord = sentence.get(i+1);
                    
                    /* Checking if the combination form an aspect length two */
                    if (deviceFeatures.contains(word+" "+adjacentWord)) {
                        sentenceDeviceFeatures.put(word+" "+adjacentWord, i);
                        i++; // double increment to pass the adjacent word in the next iteration 
                       
                    }
                }
                /* If no adjacent word then add just the single word */
                else {
                   sentenceDeviceFeatures.put(word, i);
                }
            }
            i++;

        }
    }
    
    
    /* Spots the opinion words in the given sentence then stores their positions and score effects (1, -1) in the sentence */
    private void spotSentenceOpinionWords(List<String> sentence, ArrayList<Integer> sentenceOpinionWordsPos, ArrayList<Integer> sentenceOpinionWordsScores) {
        
        String word;                     /* The initial word */
        int score;                       /* The opinion word score like -1, +1, +2, -2 */
               
        
        for (int i = 0; i < sentence.size(); i++) {
            word = sentence.get(i);
            
            if (positiveOpinionWords.contains(word) || negativeOpinionWords.contains(word)) {
                /* The opinion word position (index) is added */
                sentenceOpinionWordsPos.add(i);
                
                score = 1;
                
                if (negativeOpinionWords.contains(word)) {
                    score = -1;
                }
                
                /* Checking if next word is an opinion shifter (degil) */
                /* TODO: opinion shifter eklenebilir? */
                if ( (i+1 < sentence.size()) && sentence.get(i+1).equalsIgnoreCase("değil") ) {
                    score *= -1;
                }
                /* Otherwise, checking previous word if it is level increaser (cok) */
                else if ( ((i-1) >= 0) && sentence.get(i-1).equalsIgnoreCase("çok")){
                    score *= 2;
                }
                
                /* The final score is added */
                sentenceOpinionWordsScores.add(score);
            }
            
        }
        
    }
    
    
    /* Computing aggregation of opinion words on seperate aspects */ 
    private void calculateAggregation(HashMap<String,Integer> sentenceDeviceFeatures, ArrayList<Integer> sentenceOpinionWordsPos, ArrayList<Integer> sentenceOpinionWordsScores) {
        
        double aggregation;
        int distance;
        int opinionScore;
        int prevGeneralScore;
        
        /* Traversing all aspects in the sentence to calculate aggregation of opinion words */
        for (String deviceFeature : sentenceDeviceFeatures.keySet()) {
            
            aggregation = 0.0;
            
            /* Calculating aggregation of all opinion words to the initial aspect */
            for (int i = 0; i < sentenceOpinionWordsScores.size(); i++) {               
                
                distance = sentenceDeviceFeatures.get(deviceFeature) - sentenceOpinionWordsPos.get(i);
                distance = Math.abs(distance);
                opinionScore = sentenceOpinionWordsScores.get(i);
                
                aggregation += (double)opinionScore/distance;
                
            }
            if (!aspectBasedResults.containsKey(deviceFeature)) {
                
                aspectBasedResults.put(deviceFeature, new ArrayList<>(Arrays.asList(0,0)));
            }
            
            /* Adding the final score to the general results */
            if (aggregation > 0.0) {
                /* Positive opinion count is increased */
                prevGeneralScore = aspectBasedResults.get(deviceFeature).get(0);
                aspectBasedResults.get(deviceFeature).set(0, prevGeneralScore+1);
            }
            else{
                /* Negative opinion count is increased */
                prevGeneralScore = aspectBasedResults.get(deviceFeature).get(1);
                aspectBasedResults.get(deviceFeature).set(1, prevGeneralScore+1);
            }
            
        }     
        
    }
    
    
    /* Returns opinion mining results as JSON string. */
    public String getResultsAsJSON() {
        
        JSONObject totalJsonReportObj= new JSONObject();
        JSONObject singleJsonReportObj;
        String key;
        
        for (String aspect : aspectBasedResults.keySet()) {
            
            singleJsonReportObj= new JSONObject()
                                 .put("positiveCnt", aspectBasedResults.get(aspect).get(0))
                                 .put("negativeCnt", aspectBasedResults.get(aspect).get(1));
                                         
            totalJsonReportObj.put(aspect,singleJsonReportObj);

        }
        
        return totalJsonReportObj.toString();
    }
    
    
    /* Starts opinion mining on texts in the allTextsFromDB */
    public HashMap<String, ArrayList<Integer>> startOpinionMining() throws IOException {
        
        TurkishSentenceExtractor extractor = TurkishSentenceExtractor.DEFAULT;     /* Sentence extractor from paragraph */
        TurkishTokenizer tokenizer = TurkishTokenizer.DEFAULT;                     /* Word extractor from sentence */
        List<String> sentences;                                                    /* Holds sentences extracted from paragraph */
        List<String> words;                                                        /* Holds words extracted from sentence */
        ArrayList<String> stemmedWords;                                            /* Holds the stems of words */
        HashMap<String,Integer> sentenceDeviceFeatures;                            /* Holds spotted device features and their positions (index) in the sentence */
        ArrayList<Integer> sentenceOpinionWordsPos;                                /* Holds sentence opinion words positions in the sentence */
        ArrayList<Integer> sentenceOpinionWordsScores;                             /* Holds sentence opinion words scores (-1, +1, +2, -2) */
        
        
        
        
        /* Reading opinion words to hash sets */
        this.readOpinionWords();
        
        
        /* Allocating the objects */
        sentenceDeviceFeatures = new HashMap<>();
        sentenceOpinionWordsPos = new ArrayList<>();
        sentenceOpinionWordsScores = new ArrayList<>();
        stemmedWords = new ArrayList<>();
        this.aspectBasedResults = new HashMap<>();
        
        
        for (String paragraph : this.allTextsFromDB) {
            
            /* Sentences are extracted from paragraph */
            sentences = extractor.fromParagraph(paragraph);
            for (String sentence : sentences) {
                
                /* Clearing the sentence-specific maps before starting to process */
                sentenceDeviceFeatures.clear();
                sentenceOpinionWordsPos.clear();
                sentenceOpinionWordsScores.clear();
                stemmedWords.clear();
                
                /* Words are extracted from sentence */
                words = tokenizer.tokenizeToStrings(sentence);
                
                /* Transforming words to their stems */
                getStems(words, stemmedWords);
                
                spotSentenceDeviceFeatures(stemmedWords, sentenceDeviceFeatures);
                spotSentenceOpinionWords(words, sentenceOpinionWordsPos, sentenceOpinionWordsScores);
                
                /* Computing aggregation of opinion words on seperate aspects */ 
                calculateAggregation(sentenceDeviceFeatures, sentenceOpinionWordsPos, sentenceOpinionWordsScores);
                
            }
            
        }
        
        return this.aspectBasedResults;
        
    }
    
    
    
}
