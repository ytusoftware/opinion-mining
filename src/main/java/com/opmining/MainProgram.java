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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

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
        op.startConnection("CasperTEYDEB", "opinionMiningApp_comment");

           

	// Extracting Statistical Information for each text in the Database
	System.out.println("------------------------------------------------------");
	StatExtraction statExtractor = new StatExtraction();
	DBCursor cursor = op.findAll();
	int i = 1;
	
	Publisher publisher = new Publisher();// This object publish the raw data to the Kafka Topic which name is test for now
	Subscriber subscriber = new Subscriber("commentStream");// This object listen to the Kafka Topic which name is test, to take the raw data
	
        System.out.println("Veritabanindaki comment ler publish ediliyor");
        System.out.println("------------------------------------------------------");
	//We read the data from The Database for now, then publish them to the Kafka Topic
	while(cursor.hasNext()){
            JSONObject json = new JSONObject();
            DBObject dbobject = cursor.next();
            String content = (String)dbobject.get("content");
            String testerId =(String)dbobject.get("testerId");
            String testDate = (String)dbobject.get("testDate");
            String deviceName = (String)dbobject.get("deviceName");
            String companyName =(String)dbobject.get("companyName"); 
            json.put("content", content);
            json.put("testerId",testerId);
            json.put("testDate",testDate);
            json.put("deviceName",deviceName);
            json.put("companyName",companyName);
            System.out.println("Data--->"+json.toString());
            publisher.publish("commentStream",json.toString());
	}
        
        publisher.flush();
        
        DBOperations db = new DBOperations();
        db.startConnection("CasperTEYDEB", "opinionMiningApp_product");
                    
        // Subscriber should listen the Kafka Topic in the infinite loop
        while(true){
            
            System.out.println("------------------------------------------------------");           
            System.out.println("Kafka dinlenmeye devam ediliyor...");
            System.out.println("------------------------------------------------------");
            
            ArrayList<String> contents = subscriber.fetchAllData();

            // Let's create a new Publisher to publish the statistical information to the new Kafka Topic
            Publisher publisher2 = new Publisher();

            // Let's create a new Subscriber to listen to the statistical information from the new Kafka Topic
            Subscriber subscriber2 = new Subscriber("commentStatStream");	
            System.out.println("------------------------------------------------------");

            System.out.println("Kafka Topicten alinan comment ler için İstatistiksel"
                    + "Bilgi Çikarimi Yapilip, Yeni bir Kafka Topic e atiliyor");            
            System.out.println("------------------------------------------------------");
            // Let's extract the statistical information from the data to send the Kafka Topic
            for(String cont : contents){
                try {
                    JSONParser parser = new JSONParser();
                    JSONObject data = (JSONObject)parser.parse(cont);
                    String testDate = (String)data.get("testDate");
                    String testerId = (String)data.get("testerId");
                    String deviceName = (String)data.get("deviceName");
                    String companyName = (String)data.get("companyName");
                    String cont1 = (String)data.get("content");
                    statExtractor.setText(cont1);
                    statExtractor.extractInfo();
                    JSONObject json = new JSONObject();
                    json.put("companyName",companyName);
                    json.put("deviceName",deviceName);
                    json.put("testerId",testerId);
                    json.put("testDate",testDate);
                    json.put("content",cont1);
                    json.put("sentenceCount",statExtractor.getSentenceCnt());
                    json.put("wordCount",statExtractor.getWordCnt());
                    json.put("positiveWordCount",statExtractor.getPositiveWordCnt());
                    json.put("negativeWordCount",statExtractor.getNegativeWordCnt());
                    publisher2.publish("commentStatStream",json.toString());
                     // Let's write the statistical information to the database         
                    op.update(new BasicDBObject("content",cont1).append("testerId",testerId),
                            new BasicDBObject("companyName",companyName).append("deviceName",deviceName)
                                    .append("testerId",testerId).append("testDate",testDate).append("content",cont1)
                                    .append("sentenceCount",json.get("sentenceCount")).append("wordCount",json.get("wordCount"))
                                    .append("positiveWordCount",json.get("positiveWordCount")).append("negativeWordCount",json.get("negativeWordCount"))
                    );
                    // When we first met with the device with this name, We should append it to the productDb
                    if(!deviceFeatures.contains(deviceName)){
                        deviceFeatures.add(deviceName);
                        DBCursor dbCursor = op.find(new BasicDBObject("content",cont1));
                        db.insert(new BasicDBObject("checkPoint",dbCursor.curr().get("_id")).append("deviceName",deviceName).append("prevCount",1).append("companyName",companyName)); 
                    }
                } catch (ParseException ex) {
                    Logger.getLogger(MainProgram.class.getName()).log(Level.SEVERE, null, ex);
                }
            }	
            publisher2.flush();
            
            System.out.println("------------------------------------------------------");
        
            System.out.println("İstatistiksel bilgilerin Bulunduğu Topicten Veriler Alinip Ekranda Gösteriliyor");
            System.out.println("------------------------------------------------------");
            
            ArrayList <String> conts = subscriber2.fetchAllData();
            for(String t : conts){
                    System.out.println(t);
            }

        }
    }

}
