//Importing all the required packages for the ant class
import java.util.*;

///Ant object that handles the construction of the solutions
public class Ant {
    protected ArrayList<City> cityData;
    protected HashMap<Integer, HashMap<Integer, Integer>> sMap;
    protected Solution solution;
    protected int id;

    //Ant object constructor
    public Ant(ArrayList<City> cities, HashMap<Integer, HashMap<Integer, Integer>> map, int id){
        cityData = cities;
        sMap = sortMap(map);
        this.id = id;
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
        int startPos = id;
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
    private boolean inSolution(int ind, ArrayList<City> solution){
        for(int i = 0; i < solution.size(); i++){
            if(solution.get(i).index == cityData.get(ind).index){
                return true;
            }
        }
        return false;
    }

    //Creates a new solution based on the probabilities of the heuristic and the pheromone
    public Solution updateSolution(){
        ArrayList<City> solution = new ArrayList<>();
        int startPos = id;
        Random r = new Random();
        solution.add(cityData.get(startPos));
        while(solution.size() != cityData.size()){
            HashMap<Integer, Integer> cityMap = sMap.get(startPos);
            ArrayList<Path> paths = cityData.get(startPos).paths;
            getProb(paths, cityMap);
            paths = sortPath(paths, startPos);
            double prob = r.nextDouble();
            int nextPos = 0;
            double total = 0;
            for(int i = 0; i < paths.size(); i++){
                total += cityData.get(paths.get(i).toCity).prob;
                if(total > prob && !inSolution(paths.get(i).toCity, solution)){
                    nextPos = paths.get(i).toCity;
                    break;
                }
                else if(total > prob && inSolution(paths.get(i).toCity, solution)){
                    nextPos = getNearest(cityMap, solution);
                    break;
                }
            }
            if(nextPos == 0){
                if(inSolution(nextPos, solution)){
                    nextPos = getNearest(cityMap, solution);
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

    //Calculates the probability of the city being chosen based on the pheromone deposits
    private void getProb(ArrayList<Path> paths, HashMap<Integer, Integer> cityMap){
        Object[] map = cityMap.keySet().toArray();
        double total = 0;
        double alpha = 0.5;
        double beta = 0.5;
        for(Path path : paths){
            cityData.get(path.toCity).prob += Math.pow(path.pheromoneCount, alpha);
            double rank = 0;
            for(int i = 0; i < map.length; i++){
                if((int)map[i] == path.toCity){
                    rank = i;
                    break;
                }
            }
            cityData.get(path.toCity).prob /= Math.pow(rank, beta);
            total += cityData.get(path.toCity).prob;
        }
        for(Path path : paths){
            cityData.get(path.toCity).prob /= total; 
        }

    }

    //Sorts the list of paths of the city based on their pheromone values
    private ArrayList<Path> sortPath(ArrayList<Path> paths, int ind){
        ArrayList<Path> temp = new ArrayList<>();
        while(paths.size() != 0){
            double prob = 0;
            Path best = paths.get(0);
            for(int i = 0; i < paths.size(); i++){
                double newProb = cityData.get(paths.get(i).toCity).prob;
                if(newProb >= prob){
                    prob = newProb;
                    best = paths.get(i);
                }
            }
            temp.add(best);
            paths.remove(paths.indexOf(best));
        }
        cityData.get(ind).paths = temp;
        return temp;
    }

    //Gets the next nearest city to a given city that is not present in the solution
    private int getNearest(HashMap<Integer, Integer> cityMap, ArrayList<City> solution){
        Object[] map = cityMap.keySet().toArray();
        for(int i = 0; i < map.length; i++){
            if(!inSolution((int)map[i], solution)){
                return (int)map[i];
            }
        }
        return -1;
    }
    
    //Clears all the probabilities for a different cities distribution
    private void resetProb(){
        for(City city : cityData){
            city.prob = 0;
        }
    }
}