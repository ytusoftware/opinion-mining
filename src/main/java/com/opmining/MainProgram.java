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
import zemberek.morphology.TurkishMorphology;


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
        op.startConnection("ProjectDB", "Texts2");
        op.getAllTexts(allTexts);

        /* Extracting features */
        FeatureExtraction extractor = new FeatureExtraction(allTexts);
        extractor.setFrequencyThreshold(2);
        extractor.extractAprioriFeatures();
        deviceFeatures = extractor.getAprioriFeaturesAsSet();
        
        extractor.printAprioriFeatures();
        
        /* Opinion Mining */
        OpinionMining om = new OpinionMining(deviceFeatures, allTexts);
        om.startOpinionMining();
        //om.writeResultsToMongoDB();
        System.out.println(om.getResultsAsJSON());
       
                        
    }
          
}
