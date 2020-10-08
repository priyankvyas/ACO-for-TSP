import java.util.ArrayList;
import java.util.Random;

public class City {
    int xCoord;
    int yCoord;
    int index;
    double prob;
    ArrayList<Path> paths = new ArrayList<>();
    public City(int x, int y, int index){
        this.xCoord = x;
        this.yCoord = y;
        this.index = index;
    }

    public void createPath(int city1, int city2){
        Path path = new Path(city1, city2);
        paths.add(path);
    }

    public void compressPaths(){
        ArrayList<Path> destinations = new ArrayList<>();
        for(Path path: paths){
            if(!path.pathExists(destinations)){
                path.pheromoneCount++;
                destinations.add(path);
            }
            else{
                destinations.get(path.getPath(destinations)).pheromoneCount++;
            }
        }
        this.paths = destinations;
    }

    public void evaporatePheromones(){
        // Percentage Evaporation
        for(Path path: paths){
            int rate = 2; // 50%
            path.pheromoneCount = path.pheromoneCount / rate;
        }

        // Probablistic chance of evaporation
//        Random rand = new Random;
//        for(Path path: paths){
//            boolean evaporate = rand.nextInt(path.pheromoneCount + 1) == 0;
//            if(evaporate){
//                path.pheromoneCount--;
//            }
//        }

        // Constant Decrease
//        for(Path path: paths){
//            path.pheromoneCount--;
//        }
    }

    protected class Path {
        int pheromoneCount;
        int fromCity;
        int toCity;
        protected Path(int city1, int city2){
            this.fromCity = city1;
            this.toCity = city2;
            this.pheromoneCount = 0;
        }

        protected boolean pathExists(ArrayList<Path> paths){
            for(Path p : paths){
                if(this.toCity == p.toCity){
                    return true;
                }
            }
            return false;
        }

        protected int getPath(ArrayList<Path> paths){
            for(int i = 0; i < paths.size(); i++){
                if(this.toCity == paths.get(i).toCity){
                    return i;
                }
            }
            return -1;
        }
    }
}
