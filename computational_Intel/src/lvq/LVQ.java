package lvq;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class LVQ {
	private static final int M=6; //number of teams
	private static double n=0.1;  //learning rate
	private static final int epochs =5; //5 treksimata 
	private static double totalDispersion = 0.0;
    private static double previousTotalDispersion = 0.0;
	private static ArrayList<Point> points_array =new ArrayList<>();
	private static ArrayList<Neuron> center_array = new ArrayList<>();
	private static ArrayList<ArrayList<Point>> clusters_array = new ArrayList<>();
	private static ArrayList<Double> dispersions_array  = new ArrayList<>();
	private static ArrayList<Neuron> previousCenters = new ArrayList<>();
    private static ArrayList<ArrayList<Point>> previousClusters = new ArrayList<>();
	
    public static void PointsFromFile() throws FileNotFoundException {
    	File file = new File("trainingProblem2.txt");
        Scanner input = new Scanner(file); 
     
        int count = 0;
        while (input.hasNext()) {
            String word  = input.next();  // x
            String word2 =input.next();  //y
            Point point = new Point(Double.valueOf(word),Double.valueOf(word2));
            points_array.add(point);
           
            count = count + 2;
        }
        System.out.println("Word count: " + count);
        input.close();
    }
    
    public static void createRandomCenters() {
    	for (int i=0; i<M; i++) {
    		int random_number=(int)(Math.random()*points_array.size());
    		Point p = points_array.remove(random_number);
    		Neuron center =new Neuron(p,i);
    		center_array.add(center);
    	}
    }
    //implement euclideian distance
    public static double distance(Point temp_point,Point temp_center) {
    	double dist_x = Math.pow(temp_point.getX()-temp_center.getX(),2);
    	double dist_y= Math.pow(temp_point.getY()-temp_center.getY(),2 );
    	double dist=Math.sqrt(dist_x +dist_y);
    	return dist;
    }
    
    public static Neuron winnerNeuron(Point p) {  //sugkrinw apostaseis neurwnwn apo to point
    	ArrayList<Double> distance_array = new ArrayList<>();
    	int winnerNeuron=0;
    	//apothikeuw apostaseis twn kentrwn  me to p wste na sugkrinw poia exei to mikrotero
    	for (int i=0 ; i<center_array.size(); i++) {
    		distance_array.add(distance(p,center_array.get(i).getWeight())); 
    	}
    	double dis = distance_array.get(0);
    	for (int i=1 ; i<M; i++) {
    		if (distance_array.get(i) < dis ) {
    			dis = distance_array.get(i);
    			winnerNeuron=i;
    		}
    	}
    	return center_array.get(winnerNeuron);
    }
    //update the weight of the point with the weight of the wiinner neuron via the specific learning rate
    public static void train() {
    	for(int i=0 ; i<epochs; i++) {
    		for(int j=0; j<points_array.size(); j++) {
    			Neuron winner = winnerNeuron(points_array.get(j));
    			
    			int index =center_array.indexOf(winner);
    			Point oldWeight = winner.getWeight();
    			double newX = oldWeight.getX() + n*(points_array.get(j).getX()-oldWeight.getX());
    			double newY = oldWeight.getY() + n*(points_array.get(j).getY()-oldWeight.getY());
    			
    			Point newWeight = new Point(newX,newY);
    			center_array.get(index).setWeight(newWeight);
    		}
    		n=n-0.95*n;
    	}
    	
    }
    // put point in cluster(team of neurons) in  which is the winner neuron
    public static void predict() {
    	for (int i = 0; i < M; i++) {
            ArrayList<Point> cluster = new ArrayList<>();
            clusters_array.add(cluster);
        }
    	for (int i =0; i<points_array.size(); i++) {
    		Neuron winner =winnerNeuron(points_array.get(i));
    		clusters_array.get(winner.getTeam()).add(points_array.get(i));
    	}
    }
    //for each cluster adds dispersion to dispersion_array from a  center by calculating distance
    public static void Dispersion() {
    	for (int i = 0; i < clusters_array.size(); i++) {
            double dispersion = 0.0;
            ArrayList<Point> cluster = clusters_array.get(i);
            Neuron center = center_array.get(i);

            for (int j=0; j<cluster.size(); j++) {
                dispersion += distance(cluster.get(j), center.getWeight());
            }
            dispersions_array.add(dispersion);
        }
    	
    }
    
    
    
    
    public static void main(String[] args) throws IOException {
        for (int i = 1; i < 6; i++) {
            System.out.println("Now running for iteration #"  + i);
            PointsFromFile();
            createRandomCenters();
            train();
            predict();
            Dispersion();
            System.out.println(dispersions_array.size());
            for (int j=0; j<dispersions_array.size();j++) {
                totalDispersion += dispersions_array.get(j);
            }

            for (int j=0; j<dispersions_array.size();j++) {
                System.out.println(dispersions_array.get(j));
            }
            System.out.println("Total Dispersion for this iteration=" + totalDispersion);

            if (previousTotalDispersion == 0.0 || previousTotalDispersion > totalDispersion) { // elegxw an uparxei mikrotero dispersion wste na ginoun swsta update
            	
                previousTotalDispersion = totalDispersion;
            
                previousCenters.clear();
                previousClusters.clear();

                for (int j=0; j<center_array.size();j++) {
                	previousCenters.add(center_array.get(j));
                }
          
                for (int j=0; j<clusters_array.size();j++) {
                	previousClusters.add(clusters_array.get(j));
                }
            }

            clusters_array.clear();
            dispersions_array.clear();
            points_array.clear();
            center_array.clear();
            totalDispersion = 0.0;


        }
        System.out.println("Best dispersion:"+previousTotalDispersion);
        File center_file =new File("LVQcenters.txt");
        FileWriter center_writer=new FileWriter(center_file);
        File cluster_file =new File("LVQcluster.txt");
        FileWriter cluster_writer=new FileWriter(cluster_file);
        for(int i=0; i<previousCenters.size(); i++) {
        	center_writer.write(previousCenters.get(i).getWeight().getX()+" "+previousCenters.get(i).getWeight().getY()+"\n");
        }
        for (int i=0; i<previousClusters.size(); i++) {
        	for(int j=0; j<previousClusters.get(i).size(); j++ ) {
        		cluster_writer.write(previousClusters.get(i).get(j).getX()+" "+previousClusters.get(i).get(j).getY()+"\n");
        	}
        	cluster_writer.write("#############"+"\n");
        }
        center_writer.close();
        cluster_writer.close();

    }

    
}
