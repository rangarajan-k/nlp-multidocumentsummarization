package sentence_ranking;


//Add numeric count/sentence
//Sentence Length to consider
public class SentenceInfo {
	String sentence;
	String stemmedSentence ;
	Double tfWeighing;
	Double titleMatch;
	Double posWeighing;
	Integer nouns;
	Integer verbs;
	Integer properNouns;
	Integer numerals;
    Integer index;
	
	public SentenceInfo(String sent, int nounCount, int verbCount, int properNounCount, int numeralsCount) {
		sentence = sent;
		nouns = nounCount;
		verbs = verbCount;
		properNouns = properNounCount;
		numerals = numeralsCount;
		stemmedSentence = null;
		tfWeighing = null;
		titleMatch = null;
		posWeighing = null;
	}

	public void setStemmedSentence(String stemSentence) {
		stemmedSentence = stemSentence;
	}
	
	public void settfWeighing(Double tfValue) {
		tfWeighing = tfValue;
	}

	public void setTitleMatch(double titleTfIdf) {
		titleMatch = titleTfIdf;
	}

	public Double getTfWeighing() {
		return tfWeighing;
	}
	
	public Double getTitleMatch() {
		return titleMatch;
	}

	public Double getNounCount() {
		return nouns.doubleValue();
	}
	
	public Double getVerbCount() {
		return verbs.doubleValue();
	}
	
	public Double getNumeralsCount() {
		return numerals.doubleValue();
	}
	
	public Double getProperNounCount() {
		return properNouns.doubleValue();
	}


    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }
}
