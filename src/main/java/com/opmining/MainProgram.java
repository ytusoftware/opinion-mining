/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.opmining;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
        DBCursor devices;                                       /* MongoDB cursor holds device list */
        String deviceName;
        

     
        
        /* Opinion Mining job loop (periodic) */
        while (true) {           
            
            System.out.println("Job loop tester!!");
            
            /* Starting the db connection */
            op.startConnection("CasperTEYDEB", "opinionMiningApp_product");
            
            /* Getting device list */
            devices = op.getAllDevices();
            
            /* Checking for each device */
            while (devices.hasNext()) {

                /* First clearing the lists */
                opMiningTexts.clear();
                allTexts.clear();
                
                /* Getting previous check info for that device */
                DBObject next = devices.next();
                deviceName = next.get("deviceName").toString();
                initialDocumentId = next.get("checkpoint").toString(); // a.k.a. --> checkpoint document id
                prevNumDocuments = Integer.parseInt(next.get("prevCount").toString()); // # of documents ready for op mining when previous check is occured
                
                System.out.println("deviceName: "+deviceName);
                
                /* Switching to comment collection */
                op.setDBandCollection("CasperTEYDEB", "opinionMiningApp_comment");

                /* Getting initial op mining documents starting with checkpoint document for that device */
                numDocuments = op.getOpMiningTexts(initialDocumentId, opMiningTexts, deviceName);
                
                /* 
                 * If the # of documents is greater than  100 OR the change rate between initial # and previous # greater
                 * than 50 percent, opinion mining is done.
                 */
                if ((numDocuments >= 100) || (numDocuments > prevNumDocuments * 1.5)) {

                    /* Reading all comments for that device */
                    op.getAllTexts(allTexts, deviceName);

                    /* Extracting features for the device */
                    extractor = new FeatureExtraction(allTexts);
                    extractor.setFrequencyThreshold(2);
                    extractor.extractAprioriFeatures();
                    deviceFeatures = extractor.getAprioriFeaturesAsSet();

                    extractor.printAprioriFeatures();

                    /* Opinion Mining */
                    om = new OpinionMining(deviceFeatures, opMiningTexts, deviceName);
                    om.startOpinionMining();

                    /* Writing to MongoDB */
                    om.writeResultsToMongoDB();
                    System.out.println(om.getResultsAsJSON());

                    /* Updating the info in the Product collection */
                    nextId = op.findNextId(initialDocumentId, numDocuments, deviceName);
                    OpinionMining.saveInfo(nextId, op.getOpMiningTexts(nextId, opMiningTexts, deviceName), deviceName);

                }
                
                /* Checkpoint id is not changed and num of documents is updated in device collection */
                else {
                    OpinionMining.saveInfo(initialDocumentId, numDocuments, deviceName);
                }

            }
            
            /* Closing the database collection */
            op.closeConnection();
            
            /* Sleeping 5 minutes --> 300.000 ms */
            Thread.sleep(60000);

        }      
        
                        
    }
          
}
 