package redundancy_removal;

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
import io.cortical.retina.model.Text;
import io.cortical.retina.rest.ApiException;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.*;

public class NewRedundancyRemoval {

    static final TokenizerFactory TOKENIZER_FACTORY = IndoEuropeanTokenizerFactory.INSTANCE;
    static final SentenceModel SENTENCE_MODEL = new MedlineSentenceModel();

    public static void startRedundancyRemoval() {
        List<String> fileNames = identifyingFilesToBeRead();
        Set<String> categories = new HashSet<>();
        List<String> titles = new ArrayList<>();

        creatingDocumentsDump(fileNames, categories, titles);

        List<List<String>> sentences = new ArrayList<>();


        sentences = readingSentences(sentences);

        FullClient fullClient = new FullClient("34a8c160-14f4-11e7-b22d-93a4ae922ff1", "en_associative");
        List<Compare.CompareModel> compareModels = new ArrayList<Compare.CompareModel>();
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

        for (i = 0; i < sentences.size(); i++) {

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
        PrintWriter printWriter = null;
        try {
            printWriter =  new PrintWriter(new FileOutputStream(new File("C:\\Testing\\set1.tmp"),true));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        for (Map.Entry e : maxMap.entrySet()) {
            if (maxMap.get(e.getValue()) == e.getKey()) {

                printWriter.println("-------------------------------------------------------");
                printWriter.println("First Sentence " + e.getKey() + " " + convertListToString(sentences.get((int) e.getKey())) + " Second Sentence " + e.getValue() + " " + convertListToString(sentences.get((int) e.getValue())));
                printWriter.println("Score " + scores.get(count));
                printWriter.println("-------------------------------------------------------");

            }
            count++;
        }
        printWriter.println();
        printWriter.println("-------------------------------------------------------");
        printWriter.println("Category of the documents");

        for (String s : categories) {
            printWriter.println(s);
        }
        printWriter.println("-------------------------------------------------------");
        printWriter.println();
        printWriter.println("-------------------------------------------------------");
        for (String s : titles) {
            printWriter.println(s);
        }
        printWriter.println("-------------------------------------------------------");
        printWriter.println();


        printWriter.println();
        printWriter.close();


    }


    private static Compare.CompareModel getCompareModels(List<String> strings, List<String> strings1) {
        String firstSentence = convertListToString(strings);
        String secondSentence = convertListToString(strings1);

        Compare.CompareModel comparison1 = new Compare.CompareModel(ExpressionFactory.text(firstSentence), ExpressionFactory.text(secondSentence));

        return comparison1;

    }

    private static void compareSentencesWithCorticol(List<String> firstSentenceList, List<String> secondSentenceList) {
        FullClient fullClient = new FullClient("34a8c160-14f4-11e7-b22d-93a4ae922ff1", "en_associative");

        String firstSentence = convertListToString(firstSentenceList);
        String secondSentence = convertListToString(secondSentenceList);
        Metric result = null;
        try {
            result = fullClient.compare(ExpressionFactory.text(firstSentence), ExpressionFactory.text(secondSentence));

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (ApiException e) {
            e.printStackTrace();
        }

        System.out.println();

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
            URLConnection myURLConnection = myURL.openConnection();
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
        int count = 0;
        for (String word : sentenceList) {
            sentence = sentence.append(word);
        }
        return sentence.toString();
    }

    private static List<List<String>> readingSentences(List<List<String>> sentences) {
        File file = new File("C:\\Testing\\set1.tmp");
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

    private static void creatingDocumentsDump(List<String> fileNames, Set<String> categories, List<String> titles) {
        PrintWriter printWriter = null;

        try {
            printWriter = new PrintWriter("C:\\Testing\\" + "set1" + ".tmp", "UTF-8");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        for (String fileName : fileNames) {
            BufferedReader br = null;
            try {
                br = new BufferedReader(new FileReader("C:\\Testing\\" + fileName));
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
                    if (line.contains("<CATEGORY>")) {

                        categories.add(line.replace("<CATEGORY>", "").replace("</CATEGORY>", ""));

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

                    if (line.contains("--")) {
                        line = line.split("--")[1];
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
        File folder = new File("C:\\Testing");
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
