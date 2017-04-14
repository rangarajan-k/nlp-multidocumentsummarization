package redundancy_removal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.aliasi.sentences.MedlineSentenceModel;
import com.aliasi.sentences.SentenceModel;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.Files;
import com.fasterxml.jackson.core.JsonProcessingException;

import constants.Constants;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import io.cortical.retina.client.FullClient;
import io.cortical.retina.core.Compare;
import io.cortical.retina.model.ExpressionFactory;
import io.cortical.retina.model.Metric;
import io.cortical.retina.rest.ApiException;
import sentence_ranking.SentenceInfo;
import sentence_ranking.SentenceWeighing;
import summarization.ReturnValue;

public class RedundancyRemoval {

    static final TokenizerFactory TOKENIZER_FACTORY = IndoEuropeanTokenizerFactory.INSTANCE;
    static final SentenceModel SENTENCE_MODEL = new MedlineSentenceModel();
    public static final String FILE_NAME = "/Users/sukuv/Desktop/Text/sample_set/" + "set1" + ".tmp";


    public static ReturnValue startRedundancyRemoval(String category,String maintitle) {
        List<String> fileNames = identifyingFilesToBeRead();
        List<String> titles = new ArrayList<>();

        creatingDocumentsDump(fileNames, titles);




        List<List<String>> sentences = new ArrayList<>();


        sentences = readingSentences(sentences);

        FullClient fullClient = new FullClient("34a8c160-14f4-11e7-b22d-93a4ae922ff1", "en_associative");
        List<Compare.CompareModel> currentCompareModels = new ArrayList<Compare.CompareModel>();
        Metric[] compareBulk = new Metric[sentences.size()];


        int count = 0;
        int i = 0;
        int j = 0;


        Map<Integer, Integer> maxMap = new LinkedHashMap<>();
        List<Double> scores = new ArrayList<>();
        double maxscore = 0.0;
        int index1 = 0;
        int index2 = 0;

//        sentences.size()
        for (i = 0; i <  sentences.size(); i++) {

            for (j = 0; j < sentences.size(); j++) {
                currentCompareModels.add(getCompareModels(sentences.get(i), sentences.get(j)));
            }


            try {


                compareBulk = fullClient.compareBulk(currentCompareModels);
                System.out.println("Compared " + i);

                for (int n = 0; n < sentences.size(); n++) {
                    if (i != n && (compareBulk[n].getCosineSimilarity() > maxscore)) {
                        maxscore = compareBulk[n].getCosineSimilarity();
                        index1 = i;
                        index2 = n;
                    }
                }
                maxMap.put(index1, index2);
                scores.add(maxscore);
                maxscore = 0.0;
                currentCompareModels.clear();

            } catch (JsonProcessingException e) {
                e.printStackTrace();
            } catch (ApiException e) {
                e.printStackTrace();
            } catch (Exception e) {
                System.out.println();
            }
        }
        count = 0;


        MaxentTagger maxentTagger = new MaxentTagger("models/wsj-0-18-bidirectional-distsim.tagger");
        List<String> stop_words = SentenceWeighing.getStopWordsList();
        SentenceWeighing.returnValue = new ReturnValue(null,titles,category);
        SentenceWeighing.setWordnetVariables();
        new Constants();
        Set<Integer> indexesToRemove = new TreeSet<>();


        for (Map.Entry e : maxMap.entrySet()) {
            if (maxMap.get(e.getValue()) == e.getKey()) {
                PrintWriter printWriter = null;
                try {
                     printWriter = new PrintWriter("/Users/sukuv/Desktop/Text/sample_set/red.tmp");
                } catch (FileNotFoundException e1) {
                    e1.printStackTrace();
                }
                BufferedReader br = null;
                try {
                     br = new BufferedReader(new FileReader("/Users/sukuv/Desktop/Text/sample_set/red.tmp"));
                } catch (FileNotFoundException e1) {
                    e1.printStackTrace();
                }

                printWriter.println(convertListToString(sentences.get((int) e.getKey())));
                printWriter.println(convertListToString(sentences.get((int) e.getValue())));
                printWriter.close();

                List<List<HasWord>> rsentences = MaxentTagger.tokenizeText(br);


                SentenceWeighing.returnValue = new ReturnValue(null,titles,category);
                List<SentenceInfo> sentenceInfos = SentenceWeighing.getSentenceInfos(maxentTagger,category,rsentences,stop_words);
                try {
                    if (sentenceInfos.get(1).getIndex() == 0) {
                        indexesToRemove.add((int) e.getKey());
                    } else {
                        indexesToRemove.add((int) e.getValue());
                    }
                    System.out.println();
                }
                catch ( Exception e1){
                    System.out.println();
                }


               /* printWriter.println("-------------------------------------------------------");
                printWriter.println("First Sentence " + e.getKey() + " " + convertListToString(sentences.get((int) e.getKey())) + " Second Sentence " + e.getValue() + " " + convertListToString(sentences.get((int) e.getValue())));
                printWriter.println("Score " + scores.get(count));
                printWriter.println("-------------------------------------------------------");*/


            }
            count++;
        }
        System.out.println();
        int indexshifter = 0;
        List<Integer> sortedList = new ArrayList<Integer>(indexesToRemove);
        Collections.sort(sortedList);
        for( Integer index : sortedList ){

            sentences.remove(index - indexshifter);
            indexshifter++;
        }
      /*  printWriter.println();
        printWriter.println("-------------------------------------------------------");
        printWriter.println("Category of the documents");


        printWriter.println("-------------------------------------------------------");
        printWriter.println();
        printWriter.println("-------------------------------------------------------");
        for (String s : titles) {
            printWriter.println(s);
        }
        printWriter.println("-------------------------------------------------------");
        printWriter.println();


        printWriter.println();
        printWriter.close();*/
        
        PrintWriter printWriter = null;
        try {
			 printWriter = new PrintWriter("/Users/sukuv/Desktop/Text/sample_set/set2.tmp");
			 for (List<String> sentence : sentences){
				 printWriter.println(convertListToString(sentence));
			 }
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}finally{
			printWriter.close();
		}

        
       return new ReturnValue(sentences, titles, new String());


    }


    private static Compare.CompareModel getCompareModels(List<String> strings, List<String> strings1) {
        String firstSentence = convertListToString(strings);
        String secondSentence = convertListToString(strings1);

        Compare.CompareModel comparison1 = new Compare.CompareModel(ExpressionFactory.text(firstSentence), ExpressionFactory.text(secondSentence));

        return comparison1;

    }


    private static double compareSentencesWithUMBC(List<String> firstSentenceList, List<String> secondSentenceList) {
        String firstSentence = convertListToString(firstSentenceList);
        String secondSentence = convertListToString(secondSentenceList);

        firstSentence = firstSentence.replaceAll(" ", "%20").replaceAll("\\s", "");
        secondSentence = secondSentence.replaceAll(" ", "%20").replaceAll("\\s", "");


        try {

            String url = "http://swoogle.umbc.edu/StsService/GetStsSim?operation=api";
            url = url + "&phrase1=" + firstSentence + "&phrase2=" + secondSentence;
            URL myURL = new URL(url);
     
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(myURL.openStream()));

            String inputLine;
            while ((inputLine = in.readLine()) != null) {

                return new Double(inputLine);
            }

            in.close();


        } catch (MalformedURLException e) {
            System.out.println();
        } catch (IOException e) {
            System.out.println();
        }
        return 0;
    }


    private static String convertListToString(List<String> sentenceList) {
        StringBuilder sentence = new StringBuilder("");
        for (String word : sentenceList) {
            sentence = sentence.append(word);
        }
        return sentence.toString();
    }

    private static List<List<String>> readingSentences(List<List<String>> sentences) {
        File file = new File("/Users/sukuv/Desktop/Text/sample_set/set1.tmp");
        String text = null;
        try {
            text = Files.readFromFile(file, "ISO-8859-1");
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("INPUT TEXT: ");
        //System.out.println(text);

        List<String> tokenList = new ArrayList<String>();
        List<String> whiteList = new ArrayList<String>();
        Tokenizer tokenizer = TOKENIZER_FACTORY.tokenizer(text.toCharArray(), 0, text.length());
        tokenizer.tokenize(tokenList, whiteList);

        System.out.println(tokenList.size() + " TOKENS");
        System.out.println(whiteList.size() + " WHITESPACES");

        String[] tokens = new String[tokenList.size()];
        String[] whites = new String[whiteList.size()];
        tokenList.toArray(tokens);
        whiteList.toArray(whites);
        int[] sentenceBoundaries = SENTENCE_MODEL.boundaryIndices(tokens, whites);

        System.out.println(sentenceBoundaries.length
                + " SENTENCE END TOKEN OFFSETS");

        if (sentenceBoundaries.length < 1) {
            System.out.println("No sentence boundaries found.");
            return sentences;
        }
        int sentStartTok = 0;
        int sentEndTok = 0;
        for (int i = 0; i < sentenceBoundaries.length; ++i) {
            sentEndTok = sentenceBoundaries[i];
            //System.out.println("SENTENCE " + (i + 1) + ": ");
            List<String> sentence = new ArrayList<>();
            for (int j = sentStartTok; j <= sentEndTok; j++) {
                sentence.add(tokens[j] + whites[j + 1]);
            }


            sentences.add(sentence);


            // System.out.println();
            sentStartTok = sentEndTok + 1;
        }
        return sentences;
    }

    private static void creatingDocumentsDump(List<String> fileNames, List<String> titles) {
        PrintWriter printWriter = null;

        try {
            printWriter = new PrintWriter(FILE_NAME, "UTF-8");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        for (String fileName : fileNames) {
            BufferedReader br = null;
            try {
                br = new BufferedReader(new FileReader("/Users/sukuv/Desktop/Text/sample_set/" + fileName));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }


            boolean include = false, startread = false;

            try {
                StringBuilder sb = new StringBuilder();
                String line = null;
                try {
                    line = br.readLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                while (line != null) {
                    if (line.contains("<TEXT>")) {
                        startread = true;
                    }
                    if (line.contains("</TEXT")) {
                        break;
                    }

                    if (line.contains("<HEADLINE>")) {
                        System.out.println();
                        try {
                            if (line.equals("<HEADLINE>")) {
                                br.readLine();
                                br.readLine();
                                titles.add(br.readLine().replace("&HT;", ""));
                            } else {
                                titles.add(line.replace("<HEADLINE>", "").replace("</HEADLINE>", ""));
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                    if (line.contains("<P>") || line.contains("</P>") || line.contains("<TEXT>") || line.contains("</TEXT")) {
                        include = false;
                    } else {
                        include = true;
                    }


                    if (startread && include) {

                        printWriter.println(line);

                    }

                    try {
                        line = br.readLine();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            } finally {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            System.out.println();
        }
        printWriter.close();
    }

    private static List<String> identifyingFilesToBeRead() {
        File folder = new File("/Users/sukuv/Desktop/Text/sample_set/");
        File[] listOfFiles = folder.listFiles();
        List<String> fileNames = new ArrayList<>();

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                System.out.println("File " + listOfFiles[i].getName());
                fileNames.add(listOfFiles[i].getName());
            } else if (listOfFiles[i].isDirectory()) {
                System.out.println("Directory " + listOfFiles[i].getName());
            }
        }
        return fileNames;
    }
}
