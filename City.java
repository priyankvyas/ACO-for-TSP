import java.util.ArrayList;

import jdk.incubator.foreign.MemoryLayout.PathElement;

public class City {
    int xCoord;
    int yCoord;
    int index;
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
