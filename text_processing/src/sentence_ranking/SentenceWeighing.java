package sentence_ranking;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import constants.Constants;

import edu.cmu.lti.ws4j.util.PorterStemmer;

import edu.smu.tspell.wordnet.Synset;
import edu.smu.tspell.wordnet.WordNetDatabase;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import summarization.ReturnValue;
import summary_formulation.SummaryFormulation;


public class SentenceWeighing {


    public static final String DICT_PATHNAME = "/Users/sukuv/Desktop/WordNet-3.0/dict";
    public static final String FILE_NAME = "/Users/sukuv/Desktop/Text/sample_set/set2.tmp";
    public static ReturnValue returnValue;
    
    
    public SentenceWeighing(ReturnValue returnValue) {
        this.returnValue = returnValue;
    }

    public static  void startSentenceWeighing() throws FileNotFoundException {

        MaxentTagger maxentTagger = new MaxentTagger("models/wsj-0-18-bidirectional-distsim.tagger");

//		Set wordnet variables
        setWordnetVariables();


        BufferedReader br = new BufferedReader(new FileReader(FILE_NAME));

//		List of sentences from the file

        List<List<HasWord>> sentences = MaxentTagger.tokenizeText(br);
//		StopWords List
        List<String> stop_words = getStopWordsList();
//		Sentence Information
        List<SentenceInfo> sentenceInfos = getSentenceInfos(maxentTagger, returnValue.getCategory(), sentences, stop_words);

        int count = 0; 
        int counter = 0;
        String finalList = "";
        ArrayList<String> tempsentences = new ArrayList<String>();
        
        for (SentenceInfo sInfo : sentenceInfos) {
        	int sentenceLength = sInfo.sentence.split(" ").length;
        	if(count < 110 && (sInfo.sentence.split(" ").length + count) < 110){
        		finalList = finalList + sInfo.sentence;
        		tempsentences.add(sInfo.sentence);
        		count = count + sentenceLength;
        	}else if(count >=95 && count <=110){
        		break;
        	}
        	if(counter >5){
        		break;
        	}
        	counter ++;
        }
        
        SummaryFormulation sf = new SummaryFormulation();
        
        try {
			sf.formulateSummary(tempsentences);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        
        System.out.println("********************\n\n");
        System.out.println(finalList);

    }

    public static void setWordnetVariables() {
        File dictFile = new File(DICT_PATHNAME);
        System.setProperty("wordnet.database.dir", dictFile.toString());
    }

    public static List<SentenceInfo> getSentenceInfos(MaxentTagger maxentTagger, String category, List<List<HasWord>> sentences, List<String> stop_words) {

        int index = 0;

        List<String> categoryList = Constants.catQuestions.get(category);
        List<SentenceInfo> sentenceInfos = new ArrayList<SentenceInfo>();
//		List of Words
        List<String> words = new ArrayList<String>();
//		Frequency of words
        HashMap<String, Integer> frequencyMap = new HashMap<String, Integer>();
//		Temporary sentence variable
        String sent;
//		Stemmer
        PorterStemmer stemmer = new PorterStemmer();
        String stemmedSent;

        int nouns;
        int verbs;
        int properNoun;
        int numerals;


        for (List<HasWord> sentWords : sentences) {
            nouns = 0;
            verbs = 0;
            properNoun = 0;
            numerals = 0;
//			Replace all alphanumeric characters in the sentence
            sent = sentWords.toString().replaceAll("[^a-zA-Z0-9\\s.]", "");
            stemmedSent = stemmer.stemSentence(sent);


            for (TaggedWord taggedWord : maxentTagger.tagSentence(sentWords)) {
                String tag = taggedWord.tag();
                if (tag.contains("VB")) {
                    verbs++;
                } else if (tag == "NNP" || tag == "NNPS") {
                    properNoun++;
                } else if (tag == "NN" || tag == "NNS") {
                    nouns++;
                } else if (tag == "CD") {
                    numerals++;
                }
            }

            SentenceInfo sentInfo = new SentenceInfo(sent, nouns, verbs, properNoun, numerals);
            sentInfo.setIndex(index);
            index++;
            sentInfo.setStemmedSentence(stemmedSent);
            sentenceInfos.add(sentInfo);
            words.addAll(Arrays.asList(stemmedSent.split(" ")));
        }

        for (String word : words) {
            frequencyMap.put(word, Collections.frequency(words, word));
        }
        
        

//		Term Frequency Weighing
        for (SentenceInfo sInfo : sentenceInfos) {
            double sum = 0.0;
            for (String word : sInfo.stemmedSentence.split(" ")) {
                if (stop_words.contains(word.toLowerCase())) {
                    continue;
                }
                sum = sum + frequencyMap.get(word);
            }
            if (sum == 0.0) {
                sInfo.settfWeighing(0.0);
            } else {
                sInfo.settfWeighing(1 + Math.log(sum));
            }
        }

//		Title texts of all documents considered - Implement query expansion
        StringBuilder titles = new StringBuilder();
        List<String> titleList = returnValue.getTitles();
        for (String title : titleList) {
            titles.append(title);
        }
        String docTitle = titles.toString();
//        String docTitle = "Vietnam to launch fund for victims of war-era defoliant"
//        		 			+ "Red Cross launches fund for victims" + "Veteran-sponsored village opens for kids, elderly in Vietnam"
//        		 				+ "VA Must Review Agent Orange Cases" + "Study Confirms Agent Orange Reports" + "Agent Orange Again Linked to Cancer"
//        		 			+ "EDITORIAL OBSERVER: AGENT ORANGE IN VIETNAM, 30 YEARS LATER " + "VIETNAM SEES WAR'S LEGACY IN ITS YOUNG";
        Set<String> expandedTitle = getSynsets(new ArrayList<String>(Arrays.asList(docTitle.split(" "))));
        Set<String> categoryTerms = getSynsets(categoryList);

        List<String> queryTerms = new ArrayList<String>();
        queryTerms.addAll(expandedTitle);
        queryTerms.addAll(categoryTerms);

//		Query match
        for (SentenceInfo sInfo : sentenceInfos) {
            double sum = 0.0;
            for (String word : sInfo.sentence.split(" ")) {
                if (queryTerms.contains(word.toLowerCase())) {
                    if (stop_words.contains(word.toLowerCase())) {
                        continue;
                    }
                    sum = sum + sInfo.tfWeighing;
                    break;
                }
            }
            sInfo.setTitleMatch(sum);
        }

        Collections.sort(sentenceInfos, new SentenceComparator());
        return sentenceInfos;
    }

    public static List<String> getStopWordsList() {
        InputStream inputStream =
                SentenceWeighing.class.getResourceAsStream("/conf_files/sw4.txt");
        InputStreamReader streamReader = new InputStreamReader(inputStream);
        if (inputStream != null) {
            return getFileData(streamReader);
        } else {
            return null;
        }
    }

    private static List<String> getFileData(InputStreamReader reader) {
        List<String> fileData = new ArrayList<String>();
        BufferedReader buf_reader;
        String line;

        try {
            buf_reader = new BufferedReader(reader);
            while ((line = buf_reader.readLine()) != null) {
                for (String word : line.split(" ")) {
                    fileData.add(word.replaceAll("^[^a-zA-Z0-9\\s]+|[^a-zA-Z0-9\\s]+$", "").toLowerCase());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return fileData;
    }

    private static Set<String> getSynsets(List<String> words) {
        WordNetDatabase database = WordNetDatabase.getFileInstance();
        Set<String> similarWords = new HashSet<>();
        try {
            for (String word : words) {
                Synset[] synsets = database.getSynsets(word);


                ArrayList<String> wordSynset = new ArrayList<String>();
                for (int i = 0; i < synsets.length; i++) {
                    String[] wordForms = synsets[i].getWordForms();
                    for (int j = 0; j < wordForms.length; j++) {
                        if (!wordSynset.contains(wordForms[j])) {
                            wordSynset.add(wordForms[j]);
                        }
                        if (wordSynset.size() > 10) {
                            break;
                        }
                    }
                    if (wordSynset.size() > 10) {
                        break;
                    }
                }
                similarWords.addAll(wordSynset);
            }
        } catch (Exception e) {
            System.out.println(e.getStackTrace());
        }
        return similarWords;

    }
}
