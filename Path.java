//Priyank_1297953_Sivaram_1299026

///Path object that represents the possible routes from one city to another
public class Path {
    protected double pheromoneCount;
    protected int fromCity;
    protected int toCity;

    //Path object constructor
    protected Path(int city1, int city2, double upper){
        this.fromCity = city1;
        this.toCity = city2;
        this.pheromoneCount = upper;
    }
}