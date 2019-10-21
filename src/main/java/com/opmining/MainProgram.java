/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.opmining;

//import java.io.IOException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;


/**
 *
 * @author cetintekin
 */
public class MainProgram {

    /**
     * @param args the command line arguments
     * @throws java.io.UnsupportedEncodingException
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws UnsupportedEncodingException, IOException {
        
        ArrayList<String> allTexts = new ArrayList<>();
        HashSet<String> deviceFeatures = new HashSet<>();
        HashMap<String, ArrayList<Integer>> omResults = new HashMap<>();
        
        /* Reading all paragraphs */
        DBOperations op = new DBOperations();
        op.startConnection("ProjectDB", "Texts");
        op.getAllTexts(allTexts);

        /* Extracting features */
        FeatureExtraction extractor = new FeatureExtraction(allTexts);
        extractor.setFrequencyThreshold(3);
        extractor.extractAprioriFeatures();
        deviceFeatures = extractor.getAprioriFeaturesAsSet();
        
        extractor.printAprioriFeatures();
        
        /* Opinion Mining */
        OpinionMining om = new OpinionMining(deviceFeatures, allTexts);
        omResults = om.startOpinionMining();
        System.out.println(om.getResultsAsJSON());
        
       
        
        
        /*StatExtraction om = new StatExtraction();
        int i = 1;
        

       
        for (String allText : allTexts) {
            om.setText(allText);
            System.out.println(i +". Paragraf İstatistikleri");
            System.out.println("------------------------------");
            System.out.println("Cümle sayısı: " + om.getSentenceCnt());
            System.out.println("Kelime sayısı: " + om.getWordCnt());
            System.out.println("Olumlu kelime sayısı: " + om.getPositiveWordCnt());
            System.out.println("Olumsuz kelime sayısı: " + om.getNegativeWordCnt());
            System.out.println("Olumsuz kelimeler: " + om.getNegativeWordList());
            System.out.println("Olumlu kelimeler: " + om.getPositiveWordList());
            System.err.println();
            
            i++;
        }*/
        
       
                        
    }
          
}
