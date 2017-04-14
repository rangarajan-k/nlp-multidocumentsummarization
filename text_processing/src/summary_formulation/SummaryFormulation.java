package summary_formulation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import edu.cmu.lti.lexical_db.ILexicalDatabase;
import edu.cmu.lti.lexical_db.NictWordNet;
import edu.cmu.lti.ws4j.RelatednessCalculator;
import edu.cmu.lti.ws4j.impl.Lesk;
import edu.cmu.lti.ws4j.util.WS4JConfiguration;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

public class SummaryFormulation {
	
	public void formulateSummary(ArrayList<String> tempsentences) throws IOException {
	
		String input_path = "models/tmpsummary.txt";
		String output_path = "models/final_summary.txt";
		
		FileWriter fw = new FileWriter(output_path);
		
		FileWriter tempfw = new FileWriter(input_path);
		for (String s : tempsentences){
			tempfw.write(s+"\r\n");
		}
		tempfw.close();
				
		double scorematrix[][] = CompareSentences.compareSentences(input_path);
		
		//Get the order of sentences by clustering with KMeans
		List<Integer> finalorder = summary_formulation.KMeans.startKMeans(scorematrix,tempsentences.size());
		System.out.println("Final order size " + finalorder.size() + "\n");
		
		ArrayList<String> finalsentences = new ArrayList<String>();
		
		for (int i : finalorder){
			finalsentences.add(tempsentences.get(i));
			fw.write(tempsentences.get(i));
		}
		fw.close();
	}
}
