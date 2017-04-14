package summarization;

import java.util.List;

/**
 * Created by Hariharan on 4/5/2017.
 */
public class ReturnValue {
    List<List<String>> sentences;
    List<String> titles;
    String category;

    String mainTitle;

    public ReturnValue(List<List<String>> sentences, List<String> titles, String category) {
        this.sentences = sentences;
        this.titles = titles;
        this.category = category;

    }

    public List<List<String>> getSentences() {
        return sentences;
    }

    public void setSentences(List<List<String>> sentences) {
        this.sentences = sentences;
    }

    public List<String> getTitles() {
        return titles;
    }

    public void setTitles(List<String> titles) {
        this.titles = titles;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }



    public void setMainTitle(String arg) {
        mainTitle = arg;
    }
}
