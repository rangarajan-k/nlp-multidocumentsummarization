package summary_formulation;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Point {

    private double x = 0;
    private int cluster_number = 0;

    public Point(double x)
    {
        this.setX(x);
    }
    
    public void setX(double x) {
        this.x = x;
    }
    
    public double getX()  {
        return this.x;
    }
     
    public void setCluster(int n) {
        this.cluster_number = n;
    }
    
    public int getCluster() {
        return this.cluster_number;
    }
    
    //Calculates the distance between two points. Here it is the similarity score between two sentences
    protected static double getSimilarity(Point p, Point centroid, double[][] similaritymatrix) {
        return similaritymatrix[(int) centroid.getX()][(int) p.getX()];
    }
    
    //Creates random point
    protected static Point createRandomPoint(int min, int max) {
    	Random r = new Random();
    	double x = Math.round (min + (max - min) * r.nextDouble());
    	return new Point(x);
    }
    
    protected static List<Point> createRandomPoints(int min, int max, int number) {
    	List<Point> points = new ArrayList<Point>(number);
    	for(int i = 0; i < number; i++) {
    		points.add(createRandomPoint(min,max));
    	}
    	return points;
    }
    
    protected static List<Point> createSentencePoints(int min, int max, int number){
    	List<Point> points = new ArrayList<Point>(number);
    	for (int i = 0; i < number; i++){
    		double x = i;
    		points.add(new Point(x));
    	}
    	
    	return points;
    }
    
    public String toString() {
    	return "("+x+")";
    }
}
