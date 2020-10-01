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
    public Ant(ArrayList<City> cities, HashMap<Integer, HashMap<Integer, Integer>> map){
        cityData = cities;
        dMap = map;
    }

    public ArrayList<City> constuctSolution(){
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
        return solution;
    }

    public int calculateFitness(ArrayList<City> solution){
        int distance = 0;
        for(int i = 0; i < solution.size() - 1; i++){
            int startCity = (int)solution.get(i).index;
            int endCity = (int)solution.get(i + 1).index;
            int intDist = dMap.get(startCity).get(endCity);
            distance += intDist;
        }
        return distance;
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
}
