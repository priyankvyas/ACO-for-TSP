//Priyank_1297953_Sivaram_1299026

//Importing all the required packages for the city object
import java.util.ArrayList;

///City object that represents the datapoint of the city in the problem set
public class City {
    protected int xCoord;
    protected int yCoord;
    protected int index;
    protected ArrayList<Path> paths = new ArrayList<>();
    double prob;
    
    //City object constructor
    public City(int x, int y, int index){
        this.xCoord = x;
        this.yCoord = y;
        this.index = index;
    }

    //Creates a list of paths based on ant solutions
    public void createPath(int city1, int city2){
        for(int i = 0; i < paths.size(); i++){
            if(paths.get(i).toCity == city2){
                // paths.get(i).pheromoneCount++;
                return;
            }
        }
        Path path = new Path(city1, city2);
        paths.add(path);
    }

    //Evaporates the pheromone counts of the paths
    public void evaporatePheromones(){
        for(Path path: paths){
            double rate = 0.5;
            path.pheromoneCount = (1 - rate) * path.pheromoneCount;
        }
    }
}