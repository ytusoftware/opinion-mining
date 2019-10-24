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
import java.util.HashMap;
import java.util.List;
import zemberek.tokenization.Token;
import zemberek.tokenization.TurkishSentenceExtractor;
import zemberek.tokenization.TurkishTokenizer;

/**
 *
 * @author cetintekin
 * 
 * 
 * This class is responsible for extracting statistical information from paragraph.
 */
public class StatExtraction {
    
    private String text;                                                /* Holds the paragraph */
    private int sentenceCnt;                                            /* Total # of sentences in the text */
    private int wordCnt;                                                /* Total # of words in the text */
    private int negativeWordCnt;                                        /* Total # of negative words in text */
    private int positiveWordCnt;                                        /* Total # of positive words in text */
    private List<String> sentences;                                     /* Holds sentences extracted from paragraph */
    private ArrayList<String> positiveWords;                            /* Holds positive word tokens extracted from sentences */
    private ArrayList<String> negativeWords;                            /* Holds megative word tokens extracted from sentences */
    
    
    
    public StatExtraction() {
        this.text = null;
        this.sentences = null;
        this.negativeWords = new ArrayList<>();
        this.positiveWords = new ArrayList<>();
        this.positiveWordCnt = 0;
        this.negativeWordCnt = 0;
        this.sentenceCnt = 0;
        this.wordCnt = 0;
    }
    
    
    /* 
        * Extracts statistics from paragraph.
        * Stores positive/negative words and sentences in seperate lists.
    */
    
    public void extractInfo() throws IOException {
        TurkishSentenceExtractor extractor = TurkishSentenceExtractor.DEFAULT;     /* Sentence extractor from paragraph */
        TurkishTokenizer tokenizer = TurkishTokenizer.DEFAULT;                     /* Word extractor from sentence */
        List<String> allWords;                                                     /* Holds words extracted from sentence */
        List<String> allPositiveWords;                                             /* For holding all possible positive words */
        List<String> allNegativeWords;                                             /* For holding all possible negative words */
        
        
        
        
        if (text == null) {
            System.err.println("No text is assigned!!");
        }
        
        
        else if (sentences == null) {          
            /* Extracting sentences */
            this.sentences = extractor.fromParagraph(text);
            this.sentenceCnt = sentences.size();
            
            /* Extracting all words */
            allWords = tokenizer.tokenizeToStrings(text);
            this.wordCnt = allWords.size();
            
            /* Getting the list of all positive-negative words */
            allPositiveWords = Files.readAllLines(Paths.get("positive-words.txt"), StandardCharsets.UTF_8);
            allNegativeWords = Files.readAllLines(Paths.get("negative-words.txt"), StandardCharsets.UTF_8);
            
            
            /* Extracting positive - negative words */
            for (String word : allWords) {
                
                if (allPositiveWords.contains(word)) {
                    this.positiveWords.add(word);
                    this.positiveWordCnt++;
                }
                
                else if(allNegativeWords.contains(word)) {
                    this.negativeWords.add(word);
                    this.negativeWordCnt++;
                }

            }
        }
    }
    
    
    /* Changes the base text for the class instance */
    public void setText(String text) throws IOException {
        
        this.text = text;
        this.sentences = null;
        this.negativeWords.clear();
        this.positiveWords.clear();
        this.negativeWordCnt = 0;
        this.positiveWordCnt = 0;
    }
    
    
    /* Returns total # of sentences */
    public int getSentenceCnt() throws IOException {
        
        //extractInfo();
	return this.sentenceCnt;
    }
    
    
    /* Returns total # of words */
    public int getWordCnt() throws IOException {
        
        //extractInfo();
        return this.wordCnt;
    }
    
    /* Returns total # of negative words */
    public int getNegativeWordCnt() throws IOException {
        
        //extractInfo();
        return this.negativeWordCnt;
    }
    
    /* Returns total # of positive words */
    public int getPositiveWordCnt() throws IOException {
        
        //extractInfo();
        return this.positiveWordCnt;
    }
    
    public ArrayList<String> getNegativeWordList() {
        return this.negativeWords;
    }
    
    public ArrayList<String> getPositiveWordList() {
        return this.positiveWords;
    }
    
}
