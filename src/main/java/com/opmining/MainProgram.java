/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.opmining;

//import java.io.IOException;
import com.mongodb.BasicDBObject;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import com.mongodb.DB;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

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

        /* Extracting features */
        FeatureExtraction extractor = new FeatureExtraction(allTexts);
        extractor.setFrequencyThreshold(3);
        extractor.extractAprioriFeatures();
        deviceFeatures = extractor.getAprioriFeaturesAsSet();

        extractor.printAprioriFeatures();

        /* Opinion Mining */
        OpinionMining om = new OpinionMining(deviceFeatures, allTexts);
        omResults = om.startOpinionMining();
        System.out.println(omResults);


	// Extracting Statistical Information for each text in the Database
	System.out.println("------------------------------------------------------");
	StatExtraction statExtractor = new StatExtraction();
	DBCursor cursor = op.findAll();
	int i = 1;
	while(cursor.hasNext()){
		String cont = (String)cursor.next().get("content");
		statExtractor.setText(cont);
		statExtractor.extractInfo();
		System.out.println(i+".text from database");
		System.out.println("-------------------------");
		System.out.println("Sentence Counts:"+statExtractor.getSentenceCnt());
		System.out.println("Word Counts:"+statExtractor.getWordCnt());
		System.out.println("Positive Words:"+statExtractor.getPositiveWordCnt());
		System.out.println("Negative Words:"+statExtractor.getNegativeWordCnt());
		i += 1;
	}




    }

}
