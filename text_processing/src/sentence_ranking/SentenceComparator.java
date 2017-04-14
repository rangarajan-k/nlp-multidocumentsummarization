package sentence_ranking;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class SentenceComparator implements Comparator<SentenceInfo> {
	
	private List<Integer> weights = new ArrayList<>(); 
	
	public SentenceComparator(){
		System.out.println("printing weights");
		Random r = new Random();
		for( int i = 0; i < 4; i++){
			weights.add(r.nextInt(100) + 1);
			System.out.println(weights.get(i));
		}
		
		
	}

	@Override
	public int compare(SentenceInfo o1, SentenceInfo o2) {
		
		Double o1TotalSum = weights.get(0) * o1.getTfWeighing() + weights.get(1) *o1.getTitleMatch() + o1.getNounCount() + o1.getVerbCount() + weights.get(2) *o1.getProperNounCount() + weights.get(3) *o1.getNumeralsCount();
		Double o2TotalSum = weights.get(0) * o2.getTfWeighing() + weights.get(1) *o2.getTitleMatch() + o2.getNounCount() + o2.getVerbCount() + weights.get(2) *o2.getProperNounCount() + weights.get(3) *o2.getNumeralsCount();;
		return -1 * o1TotalSum.compareTo(o2TotalSum);
	}

}
