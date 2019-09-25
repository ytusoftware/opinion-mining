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
        
        /* Cumleden kelime ve paragraftan cumle extractorlari */
        TurkishSentenceExtractor extractor = TurkishSentenceExtractor.DEFAULT;
        TurkishTokenizer tokenizer = TurkishTokenizer.DEFAULT;
        TurkishMorphology morphology = TurkishMorphology.createWithDefaults();
        HashMap<String, Integer> possibleFeatureIndexMap = new HashMap<>();
        String feature;
        
        
        //OutputStream os = new FileOutputStream("test.txt");
        PrintWriter out = new PrintWriter(fileToPath("test.txt"));
        
        
        /* Ornek paragraf */
        String input = "iPhone 7; simsiyah, siyah, gümüş, altın ve rose gold renk seçenekleriyle beraber geliyor. iPhone 7’nin görünümü iPhone 6 ile neredeyse aynı olduğu için, renk seçeneklerinin önemi oldukça büyük. Apple’ın da lansmanda simsiyah iPhone 7’yi öne çıkarmasının sebebi de bu. \n" +
"\n" +
"iPhone 7 alacak olan kullanıcılar genellikle telefonlarının iPhone 6 ailesinden farklılaşmasını istiyor. Bu yüzden renk seçimlerinde ibre simsiyah ve siyah modellerine kaymış durumda. Simsiyah modelinin çizilmelere karşı daha hassas olacağını da hatırlatalım. iPhone 7’nin kasasında gözle görülen en büyük farklılık arka anten çizgileri. Yatay olarak kasanın altından ve üstünden geçen iki çizgi artık iPhone 7’de yok. Apple burada sadece bir çizgiyi kaldırmanın dışında, tasarımsal olarak fazla bir dokunuş yapmamış. \n" +
"\n" +
"Kasada karşımıza çıkan ikinci farklılık ise kamera tarafında. Gelişen kamera artık daha büyük, OIS desteği sayesinde çıkıntısı da biraz daha fazla.\n" +
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
        
        /* Cumleler extract ediliyor */
        List<String> sentences = extractor.fromParagraph(input);
        //sentences.size();
        //System.out.println(sentences);
        
        /* Elde edilen her cumlenin kelimeleri sirayla extract edilip kokenleriyle birlikte ekrana basiliyor */
        List<Token> tokens;
        WordAnalysis results;
        int sentenceId = 1;
        int generalFeatureIndex = 1;
        int initialFeatureIndex;
        
        for (String sentence : sentences) {
            tokens = tokenizer.tokenize(sentence);
            
            /* Halihazirda tokenize edilmis cumlenin kelime bilgileri ekrana basiliyor */
            for (Token token : tokens) {
                
                results =  morphology.analyze(token.getText());
                
                //System.out.println(results.getAnalysisResults().get(0).getStem());
                
                
                
                /* Checking if the initial word of the sentence is noun or not */
                if( !results.getAnalysisResults().isEmpty() ) {
                    
                    SingleAnalysis sa = results.getAnalysisResults().get(0);
                    
                    if (sa.getPos().shortForm.compareTo("Noun") == 0) {
                        if (possibleFeatureIndexMap.containsKey(sa.getStem())) {
                            initialFeatureIndex = possibleFeatureIndexMap.get(sa.getStem());
                        } /* Adding to the possible feature list */ else {
                            initialFeatureIndex = generalFeatureIndex;
                            generalFeatureIndex++;
                            possibleFeatureIndexMap.put(sa.getStem(), initialFeatureIndex);
                        }

                        /* Then, adding the list index of possible feature to the transaction table of Apriori algorithm. */
                        out.print(initialFeatureIndex);
                        out.print(' ');
                    }
                    
                }          
                        
            }
            out.println();
        }
        out.close();
        
        String input_n = fileToPath("test.txt");
	String output = null;
	// Note : we here set the output file path to null
	// because we want that the algorithm save the 
	// result in memory for this example.
		
	double minsup = 3/((double)sentences.size()); // means a minsup of 2 transaction (we used a relative support)
        System.out.println("Minsup: "+minsup);
		
	// Applying the Apriori algorithm
	AlgoApriori algorithm = new AlgoApriori();
		
	// Uncomment the following line to set the maximum pattern length (number of items per itemset, e.g. 3 )
        //apriori.setMaximumPatternLength(3);
		
	Itemsets result = null;
                
        result = algorithm.runAlgorithm(minsup, input_n, output);

        algorithm.printStats();
        
        List<List<Itemset>> transTableLevels = result.getLevels();
        
                
	//result.printItemsets(algorithm.getDatabaseSize());
        
        /* Printing the features found by Apriori algorithm */
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
    
    public static String fileToPath(String filename) throws UnsupportedEncodingException{
	System.out.println("filename : " + filename);
	URL url = Task1.class.getResource(filename);
	return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
    }
    
    public static Object getKeyFromValue(HashMap hm, Integer value) {
        for (Object o : hm.keySet()) {
            if (hm.get(o).equals(value)) {
                return o;
            }
        }
        return null;
    }
    
}
