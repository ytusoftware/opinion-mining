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
        
        //FeatureExtraction extractor = new FeatureExtraction();
        
        //extractor.setFrequencyThreshold(5);
        //extractor.extractAprioriFeatures();  // Default freq threshold percentage is 6. (see setFrequencyThreshold() method)    
        //extractor.printAprioriFeatures();
        
        ArrayList<String> allTexts = new ArrayList<>();
        OpinionMining om = new OpinionMining();
        int i = 1;
        
        DBOperations op = new DBOperations();
        op.startConnection("ProjectDB", "Texts");
        op.getAllTexts(allTexts);
       
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
        }
        
              
        
    }
          
}
