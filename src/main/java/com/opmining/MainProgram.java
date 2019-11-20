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


	// Extracting Statistical Information for each text in the Database
	System.out.println("------------------------------------------------------");
	StatExtraction statExtractor = new StatExtraction();
	DBCursor cursor = op.findAll();
	int i = 1;
	
	Publisher publisher = new Publisher();// This object publish the raw data to the Kafka Topic which name is test for now
	Subscriber subscriber = new Subscriber("raw");// This object listen to the Kafka Topic which name is test, to take the raw data
	
	//We read the data from The Database for now, then publish them to the Kafka Topic
	while(cursor.hasNext()){
            String content = (String)cursor.next().get("content");
            System.out.println("Data--->"+content);
            publisher.publish("raw",content);
	}
        
        publisher.flush();

	ArrayList<String> contents = subscriber.fetchAllData();
	
	// Let's create a new Publisher to publish the statistical information to the new Kafka Topic
	Publisher publisher2 = new Publisher();
	
	// Let's create a new Subscriber to listen to the statistical information from the new Kafka Topic
	Subscriber subscriber2 = new Subscriber("stat");	
	
	// Let's extract the statistical information from the data to send the Kafka Topic
        for(String cont : contents){
            statExtractor.setText(cont);
            statExtractor.extractInfo();
            String data = "{Text:"+cont+",Sentence Counts:"+statExtractor.getSentenceCnt()+",Word Counts:"+statExtractor.getWordCnt()+
            ",Positive Words:"+statExtractor.getPositiveWordCnt()+",Negative Words:"+statExtractor.getNegativeWordCnt()+"}";
            publisher2.publish("stat",data);
        }	
        publisher2.flush();
        
	ArrayList <String> conts = subscriber2.fetchAllData();
	for(String t : conts){
		System.out.println(t);
	}


    }

}
