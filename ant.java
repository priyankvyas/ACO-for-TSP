import java.lang.reflect.Array;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Ant {
    private ArrayList<City> cityData;
    private HashMap<Integer, HashMap<Integer, Integer>> dMap;
    private Solution solution;
    public Ant(ArrayList<City> cities, HashMap<Integer, HashMap<Integer, Integer>> map){
        cityData = cities;
        dMap = map;
    }

    public Solution constuctSolution(){
        ArrayList<City> solution = new ArrayList<>();
        Random r = new Random();
        HashMap<Integer, HashMap<Integer, Integer>> map = sortMap();
        int startPos = r.nextInt(cityData.size());
        solution.add(cityData.get(startPos));
        while(solution.size() != cityData.size()){
            HashMap<Integer, Integer> cityMap = map.get(startPos);
            int index = 1;
            int nextPos = (int)cityMap.keySet().toArray()[index];
            while(inSolution(nextPos, solution)){
                index++;
                nextPos = (int)cityMap.keySet().toArray()[index];
            }
            solution.add(cityData.get(nextPos));
            startPos = nextPos;
        }
        solution.add(solution.get(0));
        this.solution = new Solution(solution);
        return this.solution;
    }

    public Solution updateSolution(){
        ArrayList<City> solution = new ArrayList<>();
        Random r = new Random();
        HashMap<Integer, HashMap<Integer, Integer>> map = sortMap();
        int startPos = r.nextInt(cityData.size());
        solution.add(cityData.get(startPos));
        while(solution.size() != cityData.size()){
            HashMap<Integer, Integer> cityMap = map.get(startPos);
            int index = 1;
            int nearest = (int)cityMap.keySet().toArray()[index];
            while(inSolution(nearest, solution)){
                index++;
                nearest = (int)cityMap.keySet().toArray()[index];
            }
            ArrayList<City.Path> paths = cityData.get(startPos).paths;
            getPheromoneProb(paths, solution);
            getNeighbourProb(cityMap, solution);

            double prob = (double)r.nextInt(totalProb())/(double)100;
            int nextPos = 0;
            double total = 0;
            for(int i = 0; i < cityData.size(); i++){
                total += cityData.get(i).prob;
                if(total > prob){
                    nextPos = i;
                    break;
                }
            }
            solution.add(cityData.get(nextPos));
            cityData.get(nextPos).prob = 0;
            startPos = nextPos;
            resetProb();
        }
        solution.add(solution.get(0));
        this.solution = new Solution(solution);
        return this.solution;
    }

    private void getPheromoneProb(ArrayList<City.Path> paths, ArrayList<City> solution){
        double alpha = 0.5;
        int total = 0;
        for(int i = 0; i < paths.size(); i++){
            total += paths.get(i).pheromoneCount;
        }
        for(City.Path path : paths){
            double prob = (double)path.pheromoneCount/(double)total;
            if(!inSolution(path.toCity, solution)){
                cityData.get(path.toCity).prob = prob * alpha;
            }
        }
    }

    private void getNeighbourProb(HashMap<Integer, Integer> cityMap, ArrayList<City> solution){
        double beta = 0.5;
        int i = 1;
        if(!inSolution((int)cityMap.keySet().toArray()[i], solution)){
            cityData.get((int)cityMap.keySet().toArray()[i]).prob += (0.5 * beta);
        }
        i++;
        if(!inSolution((int)cityMap.keySet().toArray()[i], solution)){
            cityData.get((int)cityMap.keySet().toArray()[i]).prob += (0.3 * beta);
        }
        i++;
        if(!inSolution((int)cityMap.keySet().toArray()[i], solution)){
            cityData.get((int)cityMap.keySet().toArray()[i]).prob += (0.1 * beta);
        }
        i++;
        for(int j = i; j < cityData.size(); j++){
            if(!inSolution((int)cityMap.keySet().toArray()[j], solution)){
                cityData.get((int)cityMap.keySet().toArray()[j]).prob += (((double)0.1/(double)(cityData.size() - 4)) * beta);
            }
        }
    }

    private int totalProb(){
        int total = 0;
        for(City city : cityData){
            total += (city.prob * 100);
        }
        return total + 1;
    }
    
    private void resetProb(){
        for(City city : cityData){
            city.prob = 0;
        }
    }

    private boolean inSolution(int index, ArrayList<City> solution){
        for(int i = 0; i < solution.size(); i++){
            if(solution.get(i).xCoord == cityData.get(index).xCoord && solution.get(i).yCoord == cityData.get(index).yCoord){
                return true;
            }
        }
        return false;
    }

    private HashMap<Integer, HashMap<Integer, Integer>> sortMap(){
        HashMap<Integer, HashMap<Integer, Integer>> sortedMap = new HashMap<>();
        for(int i = 0; i < dMap.size(); i++){
            HashMap<Integer, Integer> cityMap = dMap.get(i);
            List<Map.Entry<Integer, Integer>> list = new LinkedList<Map.Entry<Integer, Integer>>(cityMap.entrySet());
            Collections.sort(list, new Comparator<Map.Entry<Integer, Integer>>(){
                public int compare(Map.Entry<Integer, Integer> city1, Map.Entry<Integer, Integer> city2){
                    return (city1.getValue()).compareTo(city2.getValue());
                } 
            });
            HashMap <Integer, Integer> temp = new LinkedHashMap<Integer, Integer>();
            for (Map.Entry<Integer, Integer> entry : list){
                temp.put(entry.getKey(), entry.getValue());
            }
            sortedMap.put(i, temp);
        }
        return sortedMap;
    }

    protected class Solution{
        protected ArrayList<City> sol;
        protected int score;
        protected Solution(ArrayList<City> solution){
            this.sol = solution;
            this.score = calculateFitness();
        }

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
}
