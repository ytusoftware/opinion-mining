/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.opmining;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.text.Document;

/**
 *
 * @author USER
 */
public class DBOperations {
    
    MongoClient mongoClient;
    DB database;
    DBCollection  collection;
    Boolean isClosed; // this variable is used to know that connection situation(close or open)
    
    public DBOperations(){
        // firstly connection is closed
        this.isClosed = true;
    }
    
    // To use db operations method, firstly you have to start a connection
    public void startConnection(String dbName,String collection){
        // The connection may already be opened, let's check it
        if(this.isClosed){
            this.mongoClient= new MongoClient(new MongoClientURI("mongodb://localhost:27017"));
            this.database = this.mongoClient.getDB(dbName);
            this.collection = this.database.getCollection(collection);
            this.isClosed = false;
        }else{
            System.err.println("The connection is already opened!!!");
        }
    }
    
    // This method provides to operate different database or collection without close the connection
    public void setDBandCollection(String dbName,String collection){
        // The connection should be opened to use this method
        if(!this.isClosed){
            this.database = this.mongoClient.getDB(dbName);
            this.collection = this.database.getCollection(collection);
        }else{
            System.err.println("Firstly, you have to start connection!!!");
        }
    }
    
    // This method search the given parameter in the database, then write the results on the console
    // And returns the start cursor, you can use the cursor to operate something on the results.
    public DBCursor find(DBObject element){
        // To use this method, the connection should be opened
        if(!this.isClosed){
            DBCursor cursor = this.collection.find(element);
            DBCursor startCursor = cursor;
            while(cursor.hasNext()){
                System.out.println(cursor.next());
            }
            return startCursor;
        }else{
            System.err.println("Firstly you have to create connection!!!");
            return null;
        }
    }
    
    // This method provides to insert the given object to the database
    public void insert(DBObject addedElement){
        // To use this method, the connection should be opened
        if(!this.isClosed){
            this.collection.insert(addedElement);
        }else{
            System.err.println("Firstly you have to create connection!!!");
        }
    }
    
    // This method deletes the given element from the database
    public void delete(DBObject removedElement){
        // To delete something from database, the connection has to be opened
        if(!this.isClosed){
            this.collection.remove(removedElement);
        }else{
            System.err.println("Firstly you have to create connection!!!");
        }
    }
    
    // This method updates the object of old version to the object of new version
    // But you have to be careful, it writes new version on the old version
    // This means oldVersion = newVersion ( assignment operation ) 
    public void update(DBObject oldVersion,DBObject newVersion){
        if(!this.isClosed){
            this.collection.update(oldVersion, newVersion);   
        }else{
            System.err.println("Firstly you have to create connection!!!");            
        }
    }
    
    // With this method, you can close the connection
    public void closeConnection(){
        // The connection may already be closed, Let's check it
        if(!this.isClosed){
            this.mongoClient.close();
            this.isClosed = true;
        }else{
            System.err.println("The connection is already closed!!!");            
        }
    }
    
    /*  Fetches all texts from the initial collection's documents */
    public void getAllTexts(ArrayList<String> allText) {
        
        if(!this.isClosed) {                     
            /* Performing a read operation on the collection. */
            DBCursor cursor = this.collection.find();
                    
            try {
                while(cursor.hasNext()) {
                    allText.add((String) cursor.next().get("content"));

                }
            } finally {
                cursor.close();
            }
        }
        else {
            System.err.println("Firstly you have to create connection!!!");  
        }

    }
    /*
    // The below side is written to test the operation of the database
    public static void main(String [] args){

        String  input =  "Her yıl yeni işlemcisini de tanıtan Apple, en yeni iPhone modellerinde en güncel işlemcisini kullanmaya devam ediyor. Apple iPhone 11 de gücünü A13 Bionic’ten alıyor. 6 çekirdekli işlemcinin çekirdekleri 2.66 GHz hızında çalışıyor. A12 Bionic’e göre yüzde 20 daha fazla performans sunan bu işlemci ayrıca yüzde 40 daha enerji tasarrufuna sahip. İşlemci üçüncü nesil Neural Engine ile geliyor.\n" +
"\n" +
"RAM ve depolama alanı tarafın baktığımızda, telefonun 64 GB, 128 GB ve 256 GB olmak üzere üç farklı depolama alanı alternatifi olduğunu görüyoruz. Apple, telefonlarında kullandığı RAM’i açıklamıyor. Ancak, ortaya çıkan testler bize telefonda 4 GB RAM kullanıldığını gösteriyor. iPhone XR modelinde 3 GB RAM’in kullanıldığını da belirtelim.";

        DBOperations operation = new DBOperations();
        operation.startConnection("ProjectDB", "Texts");
        operation.insert(new BasicDBObject("id","4").append("user","Deneme3").append("content",input));
        //operation.find(new BasicDBObject("id","0"));
        //operation.setDBandCollection("ProjectDB", "Features");
        //operation.insert(new BasicDBObject("idRef","0").append("name","ön kamera").append("positiveCnt","5").append("negativeCnt","2"));
        //operation.find(new BasicDBObject("idRef","0"));
        operation.closeConnection();
    }
    
    */
}
