public class Path {
    protected double pheromoneCount;
    protected int fromCity;
    protected int toCity;

    //Path object constructor
    protected Path(int city1, int city2){
        this.fromCity = city1;
        this.toCity = city2;
        this.pheromoneCount = 0;
    }
}
