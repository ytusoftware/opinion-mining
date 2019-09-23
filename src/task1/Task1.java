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

/**
 *
 * @author cetintekin
 */
public class Task1 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        
        /* Cumleden kelime ve paragraftan cumle extractorlari */
        TurkishSentenceExtractor extractor = TurkishSentenceExtractor.DEFAULT;
        TurkishTokenizer tokenizer = TurkishTokenizer.DEFAULT;
        TurkishMorphology morphology = TurkishMorphology.createWithDefaults();
        
        
        /* Ornek paragraf */
        String input = "Telefonun ekranının parlaklığını çok beğendim. Gerçekten çok etkileyici bir görünüşe sahip.";   
        
        /* Cumleler extract ediliyor */
        List<String> sentences = extractor.fromParagraph(input);
        //System.out.println(sentences);
        
        /* Elde edilen her cumlenin kelimeleri sirayla extract edilip kokenleriyle birlikte ekrana basiliyor */
        List<Token> tokens;
        WordAnalysis results;
        int sentenceId = 1;
        
        for (String sentence : sentences) {
            System.out.println(sentenceId + ". Cumle Sonuclari: ");
            tokens = tokenizer.tokenize(sentence);
            
            /* Halihazirda tokenize edilmis cumlenin kelime bilgileri ekrana basiliyor */
            for (Token token : tokens) {
                results =  morphology.analyze(token.getText());
                if(!results.getAnalysisResults().isEmpty())
                    System.out.println(token.getText()+ " - " + results.getAnalysisResults().get(0).getPos().shortForm); // Kelime ve morfolojik kokeni ekrana basiliyor                
            }
            sentenceId++;
        }
        
        
    }
    
}
