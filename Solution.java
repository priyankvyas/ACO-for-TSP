import java.util.ArrayList;
import java.util.HashMap;

public class Solution {
    protected ArrayList<City> sol;
    protected int score;
    protected HashMap<Integer, HashMap<Integer, Integer>> dMap;
    protected Ant ant;

    //Solution object constructor
    protected Solution(ArrayList<City> solution, HashMap<Integer, HashMap<Integer, Integer>> map, Ant ant){
        this.sol = solution;
        this.dMap = map;
        this.score = calculateFitness();
        this.ant = ant;
    }

    //Calculates the total distance of the path travelled in the solution
    protected int calculateFitness(){
        int distance = 0;
        for(int i = 0; i < this.sol.size() - 1; i++){
            int startCity = (int)this.sol.get(i).index;
            int endCity = (int)this.sol.get(i + 1).index;
            int intDist = dMap.get(startCity).get(endCity);
            distance += intDist;
        }
        return distance;
    }    
}
