package summary_formulation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.aliasi.sentences.MedlineSentenceModel;
import com.aliasi.sentences.SentenceModel;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.Files;
import com.fasterxml.jackson.core.JsonProcessingException;

import io.cortical.retina.client.FullClient;
import io.cortical.retina.core.Compare;
import io.cortical.retina.model.ExpressionFactory;
import io.cortical.retina.model.Metric;
import io.cortical.retina.rest.ApiException;

public class CompareSentences {
	
	
	static final TokenizerFactory TOKENIZER_FACTORY = IndoEuropeanTokenizerFactory.INSTANCE;
    static final SentenceModel SENTENCE_MODEL = new MedlineSentenceModel();
    
    public static double[][] compareSentences(String input_path) {
        
        List<List<String>> sentences = new ArrayList<>();


        sentences = readingSentences(sentences,input_path);

        FullClient fullClient = new FullClient("34a8c160-14f4-11e7-b22d-93a4ae922ff1", "en_associative");
        
        int i = 0;
        int j = 0;

        double scorematrix[][] = new double[sentences.size()][sentences.size()];
        
        for (i = 0; i < sentences.size(); i++) {

            for (j = 0; j < sentences.size(); j++) {
            
            	String firstSentence = convertListToString(sentences.get(i));
            	String secondSentence = convertListToString(sentences.get(j));
            	
            	double score = compareSentencesWithCorticol(firstSentence,secondSentence, fullClient);
            	
            	scorematrix[i][j] = score;
            
            }
        }
        return scorematrix;
    }

    
    private static double compareSentencesWithCorticol(String firstSentence, String secondSentence, FullClient fullClient) {
       
        Metric result = null;
        try {
            result = fullClient.compare(ExpressionFactory.text(firstSentence), ExpressionFactory.text(secondSentence));

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (ApiException e) {
            e.printStackTrace();
        }
        
        return result.getCosineSimilarity();

    }
    
	private static List<List<String>> readingSentences(List<List<String>> sentences, String input_path) {
        File file = new File(input_path);
        String text = null;
        try {
            text = Files.readFromFile(file, "ISO-8859-1");
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<String> tokenList = new ArrayList<String>();
        List<String> whiteList = new ArrayList<String>();
        Tokenizer tokenizer = TOKENIZER_FACTORY.tokenizer(text.toCharArray(), 0, text.length());
        tokenizer.tokenize(tokenList, whiteList);


        String[] tokens = new String[tokenList.size()];
        String[] whites = new String[whiteList.size()];
        tokenList.toArray(tokens);
        whiteList.toArray(whites);
        int[] sentenceBoundaries = SENTENCE_MODEL.boundaryIndices(tokens, whites);

        if (sentenceBoundaries.length < 1) {
            return sentences;
        }
        int sentStartTok = 0;
        int sentEndTok = 0;
        for (int i = 0; i < sentenceBoundaries.length; ++i) {
            sentEndTok = sentenceBoundaries[i];
            List<String> sentence = new ArrayList<>();
            for (int j = sentStartTok; j <= sentEndTok; j++) {
                sentence.add(tokens[j] + whites[j + 1]);
            }


            sentences.add(sentence);
            sentStartTok = sentEndTok + 1;
        }
        return sentences;
    }
	private static String convertListToString(List<String> sentenceList) {
        StringBuilder sentence = new StringBuilder("");
        for (String word : sentenceList) {
            sentence = sentence.append(word);
        }
        return sentence.toString();
    }
}
