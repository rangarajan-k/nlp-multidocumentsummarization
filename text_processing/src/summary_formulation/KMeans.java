package summary_formulation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import summary_formulation.Point;

public class KMeans {

	//Number of Clusters. This metric should be related to the number of points
    private static int NUM_CLUSTERS;    
    //Number of Points
    private static int NUM_POINTS;
    //Min and Max X and Y
    private static int MIN_COORDINATE;
    private static int MAX_COORDINATE;
    
    private List<Point> points;
    private List<Cluster> clusters;
    
    public static double[][] similaritymatrix;
    
    public KMeans() {
    	this.points = new ArrayList<Point>();
    	this.clusters = new ArrayList<Cluster>();    	
    }
    
    public static ArrayList<Integer> startKMeans(double[][] matrix,int num) {
    	
    	//num points is the number of sentences
    	NUM_POINTS = num;
    	NUM_CLUSTERS = 3;
    	
    	MIN_COORDINATE = 0;
    	MAX_COORDINATE = num;
    	
    	similaritymatrix = matrix;
    	
    	KMeans kmeans = new KMeans();
    	kmeans.init();
    	ArrayList<Integer> finalorder = kmeans.calculate();
    	
    	return finalorder;
    }
    
    //Initializes the process
    public void init() {
    	
    	//Create each sentence as a Point
    	points = Point.createSentencePoints(MIN_COORDINATE,MAX_COORDINATE,NUM_POINTS);
    	
    	//points = Point.createRandomPoints(MIN_COORDINATE,MAX_COORDINATE,NUM_POINTS);
    	
    	//Create Clusters
    	//The first sentence is always the first centroid of the cluster
    	
    	//Set Random Centroids
    	for (int i = 0; i < NUM_CLUSTERS; i++) {
    		Cluster cluster = new Cluster(i);
 
    		if (i == 0 ){
    			Point centroid = new Point(0);
    			cluster.setCentroid(centroid);
    		}
    		else{
    			Point centroid = Point.createRandomPoint(MIN_COORDINATE,MAX_COORDINATE);
    			cluster.setCentroid(centroid);
    		}
    		
    		clusters.add(cluster);
    	}
    	
    	//Print Initial state
    	plotClusters();
    }

	private void plotClusters() {
    	for (int i = 0; i < NUM_CLUSTERS; i++) {
    		Cluster c = clusters.get(i);
    		c.plotCluster();
    	}
    }
    
	//The process to calculate the K Means, with iterating method.
    public ArrayList<Integer> calculate() {
        boolean finish = false;
        int iteration = 0;      
        // Add in new data, one at a time, recalculating centroids with each new one. 
        while(!finish) {
        	//Clear cluster state
        	clearClusters();
             	
        	//Assign points to the closer cluster
        	assignCluster();
                	
        	iteration++;
        	
        	System.out.println("#################");
        	System.out.println("Iteration: " + iteration);
        	plotClusters();
        	    	
        	if(iteration == 1) {
        		finish = true;
        	}
        }
        //Add the clusters and points in an array and return. The points will be used as index in the master list
        //of sentences to order them
        
        return returnFinalOrder();
    }
    
    private ArrayList<Integer> returnFinalOrder(){
        ArrayList<Integer> finalorder = new ArrayList<Integer>();
        List<Double> centroids = getCentroidPoints();
        
        for(Cluster cluster : clusters) {
        	ArrayList<Integer> temporder = new ArrayList<Integer>();
        	finalorder.add((int) cluster.getCentroid().getX());
        	List<Point> points = cluster.getPoints();
        	for (Point point : points){
        		if(!finalorder.contains(point.getX()) && !centroids.contains(point.getX())){
        			temporder.add((int) point.getX());
        		}
        	}
        	Collections.sort(temporder);
        	Collections.reverse(temporder);
        	finalorder.addAll(temporder);
        }
        return finalorder;
    }
    private void clearClusters() {
    	for(Cluster cluster : clusters) {
    		cluster.clear();
    	}
    }
    
    private List<Double> getCentroidPoints(){
    	List<Double> centroids = new ArrayList<Double>(NUM_CLUSTERS);
    	for(Cluster cluster : clusters) {
    		Point aux = cluster.getCentroid();
    		centroids.add(aux.getX());
    	}
    	return centroids;
    }
    private void assignCluster() {
        double min = 0; 
        int cluster = 0;                 
        double similarity = 0.0; 
        
        List<Double> centroids = getCentroidPoints();
        
        for(Point point : points) {
        	
        	if (centroids.contains(point.getX()))
        		continue;
        	min = 0;
            for(int i = 0; i < NUM_CLUSTERS; i++) {
            	Cluster c = clusters.get(i);
            	if (point.getX() == c.getCentroid().getX()){
            		continue;
            	}
                similarity = Point.getSimilarity(point, c.getCentroid(), similaritymatrix);
                if(similarity > min){
                    min = similarity;
                    cluster = i;
                }
            }
            point.setCluster(cluster);
            clusters.get(cluster).addPoint(point);
        }
    }
    
    private void calculateCentroids() {
        for(Cluster cluster : clusters) {
            double sumX = 0;
            List<Point> list = cluster.getPoints();
            int n_points = list.size();
            
            for(Point point : list) {
            	sumX += point.getX();
            }
            
            Point centroid = cluster.getCentroid();
            if(n_points < 0) {
            	double newX = sumX / n_points;
                centroid.setX(newX);
            }
        }
    }
}
