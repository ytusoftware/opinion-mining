/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package task1;

//import java.io.IOException;
import java.util.List;
import zemberek.morphology.TurkishMorphology;
import zemberek.morphology.analysis.WordAnalysis;
import zemberek.tokenization.Token;
import zemberek.tokenization.TurkishSentenceExtractor;
import zemberek.tokenization.TurkishTokenizer;
import ca.pfv.spmf.algorithms.frequentpatterns.apriori.AlgoApriori;
import ca.pfv.spmf.patterns.itemset_array_integers_with_count.Itemset;
import ca.pfv.spmf.patterns.itemset_array_integers_with_count.Itemsets;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import zemberek.morphology.analysis.SingleAnalysis;


/**
 *
 * @author cetintekin
 */
public class Task1 {

    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws UnsupportedEncodingException, IOException {
        
                
        int frequencyThreshold;                                                    /* The minimum frequency value which will be used for determining frequent nouns. Example values: 2,3,4 ..  */
        String input;                                                              /* Holds the paragraph text */
        int sentenceCount;                                                         /* Total number of sentences extracted from paragraph */
        HashMap<String, Integer> possibleFeatureIndexMap = new HashMap<>();        /* Holds candidate features and their ids (ids are used for Apriori algorithm) */
        
                      
        
        
        /* Example paragraph (Iphone review) */
        input = "iPhone 7; simsiyah, siyah, gümüş, altın ve rose gold renk seçenekleriyle beraber geliyor. Ön kamera çok hoşuma gitti. iPhone 7’nin görünümü iPhone 6 ile neredeyse aynı olduğu için, renk seçeneklerinin önemi oldukça büyük. Apple’ın da lansmanda simsiyah iPhone 7’yi öne çıkarmasının sebebi de bu. \n" +
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
        
       
        /* Setting the Apriori threshold frequency */
        frequencyThreshold = 3;
     
        
        /* Extracting all candidate features from text (Looking at the nouns) */
        sentenceCount = extractCandidateFeatures(input, possibleFeatureIndexMap);
        
        /* After extracting the candidate features, real features are found by using Apriori algorithm */
        getAprioriFeatures(frequencyThreshold, sentenceCount, possibleFeatureIndexMap);
        
        
    }
    
    /* This method returns the SingleAnalysis result for the word which is noun */
    public static SingleAnalysis isNoun(WordAnalysis analysis){
        int i = 0;
        List <SingleAnalysis> result = analysis.getAnalysisResults();
         
        while(i< result.size() && result.get(i).getPos().shortForm.compareTo("Noun")!=0){
            i += 1;
        }
        
        if(i<result.size()){
            return result.get(i);
        }else{
            return null;
        }
   
    }
    
    
    /* Gets root directory of the project to save text file. Takes the target paragraph (text) as parameter */
    public static int extractCandidateFeatures(String input, HashMap<String, Integer> possibleFeatureIndexMap) throws UnsupportedEncodingException, FileNotFoundException {
        
        TurkishSentenceExtractor extractor = TurkishSentenceExtractor.DEFAULT;     /* Sentence extractor from paragraph */
        TurkishTokenizer tokenizer = TurkishTokenizer.DEFAULT;                     /* Word extractor from sentence */
        TurkishMorphology morphology = TurkishMorphology.createWithDefaults();     /* Used for finding stems of words */                                                      
        PrintWriter out;                                                           /* Used for writing candidate feature ids to the text file to make Apriori algo. work */
        List<String> sentences;                                                    /* Holds sentences extracted from paragraph */
        List<Token> tokens;                                                        /* Holds words extracted from sentence */
        WordAnalysis results;                                                      /* Used for holding the result of Turkish morphological analysis */
        int generalFeatureIndex;                                                   /* Used for labeling the candidate features for the use of Apriori algo. work */
        int initialFeatureIndex;                                                   /* Holds the last given highest id to the candidate features */
        SingleAnalysis sa;
        
        
        
        System.out.println(morphology.analyze("Güç").getAnalysisResults().get(2));
       /* Initializing the text file writer */
        out = new PrintWriter(fileToPath("test.txt"));
        
        /* Sentences are extracted from paragraph */
        sentences = extractor.fromParagraph(input);

        
        generalFeatureIndex = 1;
        
        /* All sentences in the paragraph are traversed respectively. */
        for (String sentence : sentences) {
            tokens = tokenizer.tokenize(sentence);
            
            /* Halihazirda tokenize edilmis cumlenin kelime bilgileri ekrana basiliyor */
            for (Token token : tokens) {
                
                results =  morphology.analyze(token.getText());                              

                /* This control is required due to empty analysis result */
                if( !results.getAnalysisResults().isEmpty() ) {
                    
                    //sa = results.getAnalysisResults().get(0);
                    sa = isNoun(results);
                    /* Checking if the initial word of the sentence is noun or not */
                    if (sa != null) {
                        
                        /* If the initial candidate feature is already found, then its id (index) is given to the Apriori algo */
                        if (possibleFeatureIndexMap.containsKey(sa.getStem())) {
                            initialFeatureIndex = possibleFeatureIndexMap.get(sa.getStem());
                        }
                        /* Otherwise, a new id is given to the initial candidate feature */
                        else {
                            initialFeatureIndex = generalFeatureIndex;
                            generalFeatureIndex++;
                            possibleFeatureIndexMap.put(sa.getStem(), initialFeatureIndex);
                        }

                        /* Then, adding the list index of possible feature to the transaction table of Apriori algorithm. (The library of Apriori algo. takes input from text file as integer values) */
                        out.print(initialFeatureIndex);
                        out.print(' ');
                    }
                    
                }          
                        
            }
            out.println();
        }
        out.close();
        
        return sentences.size();
    }
    
   
    /* Gets root directory of the project to save text file */
    public static void getAprioriFeatures(int freqThreshold, int sentenceCount, HashMap<String, Integer> possibleFeatureIndexMap) throws UnsupportedEncodingException, IOException {
       
        String input;       /* Holds the absolute path for input file for the use of Apriori algorithm */
        String output;      /* This variable can be used for printing the Apriori algorithm results (stats) to a text file */
        String feature;     /* Final features after Apriori algorithm */
        
      
    
        
        /* Opening the candidate feature transaction table */
        input = fileToPath("test.txt");
	output = null;   // No text file output is requested

		
	double minsup = freqThreshold/((double)sentenceCount); // means a minsup of 2 transaction (we used a relative support)
		
	/* Applying the Apriori algorithm */
	AlgoApriori algorithm = new AlgoApriori();
		
	/* Uncomment the following line to set the maximum pattern length (number of items per itemset, e.g. 3 ) */
        //apriori.setMaximumPatternLength(3);
		
	Itemsets result = null;
                
        result = algorithm.runAlgorithm(minsup, input, output);

        /* Uncomment to see the Apriori algorith stats */
        //algorithm.printStats();
        
        List<List<Itemset>> transTableLevels = result.getLevels();
        
                
	//result.printItemsets(algorithm.getDatabaseSize());
        
        /* Printing the real features found by Apriori algorithm according to min support */
        System.out.println("---------Extracted features--------");
        for (List<Itemset> level : result.getLevels()) {
            if (!level.isEmpty()) {
                for (Itemset itemset : level) {
                    for (int i = 0; i < itemset.size(); i++) {
                        feature = (String) getKeyFromValue(possibleFeatureIndexMap, itemset.get(i));
                        System.out.print(feature+" ");
                    }
                    System.out.println();
                }
            }
        } 

    }
    
     
    
    /* Gets root directory of the project to save text file */
    public static String fileToPath(String filename) throws UnsupportedEncodingException{
	System.out.println("filename : " + filename);
	URL url = Task1.class.getResource(filename);
	return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
    }
    
    /* Gets keys (features) from the id of the feature in the feature-id HashMap */
    public static Object getKeyFromValue(HashMap hm, Integer value) {
        for (Object o : hm.keySet()) {
            if (hm.get(o).equals(value)) {
                return o;
            }
        }
        return null;
    }
    
}
