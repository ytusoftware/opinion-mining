/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package task1;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

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
    
    // The below side is written to test the operation of the database
    public static void main(String [] args){

        String  input = "iPhone 7; simsiyah, siyah, gümüş, altın ve rose gold renk seçenekleriyle beraber geliyor. Ön kamera çok hoşuma gitti. iPhone 7’nin görünümü iPhone 6 ile neredeyse aynı olduğu için, renk seçeneklerinin önemi oldukça büyük. Apple’ın da lansmanda simsiyah iPhone 7’yi öne çıkarmasının sebebi de bu. \n" +
"\n" +
"iPhone 7 alacak olan kullanıcılar genellikle telefonlarının iPhone 6 ailesinden farklılaşmasını istiyor. Ön kamera berbatmış. Bu yüzden renk seçimlerinde ibre simsiyah ve siyah modellerine kaymış durumda. Simsiyah modelinin çizilmelere karşı daha hassas olacağını da hatırlatalım. iPhone 7’nin kasasında gözle görülen en büyük farklılık arka anten çizgileri. Yatay olarak kasanın altından ve üstünden geçen iki çizgi artık iPhone 7’de yok. Apple burada sadece bir çizgiyi kaldırmanın dışında, tasarımsal olarak fazla bir dokunuş yapmamış. \n" +
"\n" +
"Kasada karşımıza çıkan ikinci farklılık ise kamera tarafında. Ön kamera güzelmiş. Gelişen kamera artık daha büyük, OIS desteği sayesinde çıkıntısı da biraz daha fazla.\n" +
"\n" +
"Arka tarafta yer alan iPhone yazısının altında artık CE, FCC gibi onay sertifikalarının görünmemesi de 6 modellerine göre bir fark olarak yer alıyor.\n" +
"\n" +
"Telefonun alt tarafına indiğimizde de artık 3.5mm’lik jack girişinin olmadığını görüyoruz. Lightning portunun sol tarafında bulunan jack girişi yerine artık sağ taraftaki hoparlör ızgaralarının sol tarafta da devam ettiğini görüyoruz.\n" +
"\n" +
"iPhone 7’de, iPhone 6’ya göre ön tarafta göze batan hiç bir görsel farklılık bulunmuyor. Sadece fark, artık home butonunun fiziksel yani mekanik olmaması.\n" +
"\n" +
"Haptic Engine ile beraber 3 farklı titreşim seviyesiyle bizlere geri bildirim veren yeni dijital home butonu, deneyim açısından fiziksel butonu aratıyor diyemeyiz.  Yeni buton ayrıca artık baskı şiddetini de algılıyor. İleride yeni yazılımlarında baskı şiddetine göre ek fonksiyonlar sunacağını söylersek yanılmayız.\n" +
"\n" +
"138.3 x 67.1 x 7.1 mm ölçüleriyle iPhone 6 modelleriyle aynı olan iPhone 7, çoğu eski kılıfla da uyumlu olacak. iPhone 6s’e göre sadece 5 gram daha hafifleyen iPhone 7, tasarım anlamında ayrıca oldukça önemli bir geliştirmeye de sahip.\n" +
"\n" +
"IP67 sertifikasını destekleyen iPhone 7, artık suya karşı dayanıklı. Apple’ın suya karşı garanti vermediğinin altını çizelim. Garanti verilmese de IP67 sertifikası sayesinde 1 metre derinliğinde yarım saate kadar suya dayandığının, herhangi bir aksilik durumunda garantiye girmeyeceğini tekrar vurgulayalım."; 

        DBOperations operation = new DBOperations();
        operation.startConnection("ProjectDB", "Texts");
        operation.insert(new BasicDBObject("id","0").append("user","Deneme").append("content",input));
        operation.find(new BasicDBObject("id","0"));
        operation.setDBandCollection("ProjectDB", "Opinions");
        operation.insert(new BasicDBObject("idRef","0").append("feature","ön kamera").append("positive","5").append("negative","2"));
        operation.find(new BasicDBObject("idRef","0"));
        operation.closeConnection();
    }
    
    
}
