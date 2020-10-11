import java.util.ArrayList;
// import java.util.Random;

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
                paths.get(i).pheromoneCount++;
                return;
            }
        }
        Path path = new Path(city1, city2);
        paths.add(path);
    }

    public void evaporatePheromones(){
        // Percentage Evaporation
        for(Path path: paths){
            int rate = 2; // 50%
            path.pheromoneCount = path.pheromoneCount / rate;
        }
    }

    public void increasePheromone(int rate){
        if(rate != 0){
            for(Path path: paths){
                path.pheromoneCount += (path.pheromoneCount / rate);
            }
        }
    }
}
