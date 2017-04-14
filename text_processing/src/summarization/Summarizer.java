package summarization;

import redundancy_removal.RedundancyRemoval;
import summarization.ReturnValue;
import sentence_ranking.SentenceWeighing;

import java.io.FileNotFoundException;


public class Summarizer {
    public static void main(String[] args) {


        ReturnValue returnValue = RedundancyRemoval.startRedundancyRemoval("Health and Safety", "Agent Orange");
        returnValue.setCategory("Health and Safety");
        returnValue.setMainTitle("Agent Orange");
        SentenceWeighing sentenceWeighing = new SentenceWeighing(returnValue);
        try {
            sentenceWeighing.startSentenceWeighing();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
