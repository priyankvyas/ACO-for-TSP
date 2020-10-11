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
    protected ArrayList<City> cityData;
    protected HashMap<Integer, HashMap<Integer, Integer>> sMap;
    protected Solution solution;

    //Ant object constructor
    public Ant(ArrayList<City> cities, HashMap<Integer, HashMap<Integer, Integer>> map){
        cityData = cities;
        sMap = sortMap(map);
    }

    //Sort the HashMap by the least distance between cities
    private HashMap<Integer, HashMap<Integer, Integer>> sortMap(HashMap<Integer, HashMap<Integer, Integer>> map){
        HashMap<Integer, HashMap<Integer, Integer>> sortedMap = new HashMap<>();
        for(int i = 0; i < map.size(); i++){
            HashMap<Integer, Integer> cityMap = map.get(i);
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

    //Create a Solution based on the Nearest Neighbour heuristic 
    public Solution constuctSolution(){
        ArrayList<City> solution = new ArrayList<>();
        Random r = new Random();
        int startPos = r.nextInt(cityData.size());
        solution.add(cityData.get(startPos));
        while(solution.size() != cityData.size()){
            HashMap<Integer, Integer> cityMap = sMap.get(startPos);
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
        this.solution = new Solution(solution, sMap, this);
        return this.solution;
    }

    //Checks if the city exists in the solution already
    private boolean inSolution(int index, ArrayList<City> solution){
        for(int i = 0; i < solution.size(); i++){
            if(solution.get(i).xCoord == cityData.get(index).xCoord && solution.get(i).yCoord == cityData.get(index).yCoord){
                return true;
            }
        }
        return false;
    }

    //Creates a new solution based on the probabilities of the heuristic and the pheromone
    public Solution updateSolution(){
        ArrayList<City> solution = new ArrayList<>();
        Random r = new Random();
        int startPos = r.nextInt(cityData.size());
        solution.add(cityData.get(startPos));
        while(solution.size() != cityData.size()){
            HashMap<Integer, Integer> cityMap = sMap.get(startPos);
            ArrayList<Path> paths = cityData.get(startPos).paths;
            getPheromoneProb(paths, solution);
            getNeighbourProb(cityMap, solution);
            double prob = r.nextDouble();
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
            startPos = nextPos;
            resetProb();
        }
        solution.add(solution.get(0));
        this.solution = new Solution(solution, sMap, this);
        return this.solution;
    }

    //Gets the probabilities of the cities that can be visited based on the pheromone count
    private void getPheromoneProb(ArrayList<Path> paths, ArrayList<City> solution){
        double alpha = 0.5;
        int total = 0;
        for(int i = 0; i < paths.size(); i++){
            if(!inSolution(paths.get(i).toCity, solution)){
                total += paths.get(i).pheromoneCount;
            }
        }
        for(Path path : paths){
            if(!inSolution(path.toCity, solution)){
                double prob = ((double)path.pheromoneCount)/((double)total);
                cityData.get(path.toCity).prob = prob * alpha;
            }
        }
    }

    //Gets the probabilities of the cities that can be visited based on the heuristic
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
                cityData.get((int)cityMap.keySet().toArray()[j]).prob += ((0.1/((double)(cityData.size() - 4))) * beta);
            }
        }
    }
    
    //Clears all the probabilities for a different cities distribution
    private void resetProb(){
        for(City city : cityData){
            city.prob = 0;
        }
    }
}
