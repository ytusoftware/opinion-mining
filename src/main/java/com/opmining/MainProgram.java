/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.opmining;

//import java.io.IOException;
import com.mongodb.BasicDBObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.lang.Integer;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;


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
    public static void main(String[] args) throws UnsupportedEncodingException, IOException, InterruptedException {

        ArrayList<String> allTexts = new ArrayList<>();         /* Holds all texts from MongoDB */
        ArrayList<String> opMiningTexts = new ArrayList<>();    /* Holds all texts from MongoDB */                 
        HashSet<String> deviceFeatures = new HashSet<>();       /* Holds device aspects extracted from MongoDB tester comments */
        DBOperations op = new DBOperations();                   /* MongoDB operation class object */
        FeatureExtraction extractor;                            /* Aspect extraction class object */
        OpinionMining om;                                       /* Opinion miner class object */
        String initialDocumentId = null;                        /* Holds the id of the fist document which is not opinion mined */
        List<String> opMiningInfo = null;                       /* Holds the info about previous op mining process */
        int prevNumDocuments;                                   /* Holds previous the number of documents in Mongo starting with the checkpoint document */
        int numDocuments;                                       /* Holds the number of documents in Mongo starting with the checkpoint document */
        PrintWriter out; 
        String nextId;
        

        
        
        op.startConnection("ProjectDB", "Texts2");        
        opMiningInfo = Files.readAllLines(Paths.get("OpMiningInfo.txt"), StandardCharsets.UTF_8);
        
        
        /* Checking if Opinion Mining info txt file is empty. If it is, then the file is initialized */
        if (opMiningInfo.isEmpty()) {       
            OpinionMining.saveInfo(op.findFirstId(), op.getAllTextsCount());            
        }
        
        op.closeConnection();
        

        /* Opinion Mining job loop */
        while (true) {
            
            System.out.println("Job loop tester!!");
            
            /* First clearing the lists */
            opMiningTexts.clear();
            allTexts.clear();
            
            
            /* 
             * Previous Opinion Mining info is read from the text file 
             * First element is the checkpoint document id 
             * Second one is the # of documents collected starting with the checkpoint document) 
            */
            opMiningInfo = Files.readAllLines(Paths.get("OpMiningInfo.txt"), StandardCharsets.UTF_8);
            initialDocumentId = opMiningInfo.get(0); // a.k.a. --> checkpoint document id
            prevNumDocuments = Integer.parseInt(opMiningInfo.get(1)); // # of documents ready for op mining when previous check is occured

            /* Connecting to the MongoDB */
            op.startConnection("ProjectDB", "Texts2");
            
            /* Getting initial op mining documents starting with checkpoint document and number */
            numDocuments = op.getOpMiningTexts(initialDocumentId, opMiningTexts);
            
            /* 
             * If the # of documents is greater than  100 OR the change rate between initial # and previous # greater
             * than 50 percent, opinion mining is done.
             */
            if ( (numDocuments >= 100) || (numDocuments > prevNumDocuments*1.5)) {               

                /* Reading all comments */
                op.getAllTexts(allTexts);

                
                /* Extracting features */
                extractor = new FeatureExtraction(allTexts);
                extractor.setFrequencyThreshold(2);
                extractor.extractAprioriFeatures();
                deviceFeatures = extractor.getAprioriFeaturesAsSet();

                extractor.printAprioriFeatures();
                
                /* Opinion Mining */
                om = new OpinionMining(deviceFeatures, opMiningTexts);
                om.startOpinionMining();

                //TODO 3: cihaz marka/model, raport baslangic/bitis tarihleri, comment sayisi bilgileri rapor objesine eklenecek
                om.writeResultsToMongoDB();
                System.out.println(om.getResultsAsJSON());
                
                /* Updating the info txt file */
                nextId = op.findNextId(initialDocumentId, numDocuments);               
                OpinionMining.saveInfo(nextId, op.getOpMiningTexts(nextId, opMiningTexts));
                                             

            }
            
            /* Checkpoint id is not changed and num of documents is updated in the txt file */
            else {
                OpinionMining.saveInfo(opMiningInfo.get(0), numDocuments);
            }
            
            /* Closing the database collection */
            op.closeConnection();
            
            /* Sleeping 5 minutes --> 300.000 ms */
            Thread.sleep(300000);

        }      
        
                        
    }
          
}
