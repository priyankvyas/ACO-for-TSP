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
    private ArrayList<ArrayList> cityData;
    private HashMap<Integer, HashMap<Integer, Integer>> dMap;
    public Ant(ArrayList<ArrayList> cities, HashMap<Integer, HashMap<Integer, Integer>> map){
        cityData = cities;
        dMap = map;
    }

    public ArrayList<ArrayList> constuctSolution(){
        ArrayList<ArrayList> solution = new ArrayList<ArrayList>();
        Random r = new Random();
        HashMap<Integer, HashMap<Integer, Integer>> map = sortMap();
        int startPos = r.nextInt(cityData.size());
        if(cityData.get(startPos).size() != 4){
            cityData.get(startPos).add(startPos);
        }
        solution.add(cityData.get(startPos));
        while(solution.size() != cityData.size()){
            HashMap<Integer, Integer> cityMap = map.get(startPos);
            int index = 1;
            int nextPos = (int)cityMap.keySet().toArray()[index];
            while(inSolution(nextPos, solution)){
                index++;
                nextPos = (int)cityMap.keySet().toArray()[index];
            }
            if(cityData.get(nextPos).size() != 4){
                cityData.get(nextPos).add(nextPos);
            }
            solution.add(cityData.get(nextPos));
            startPos = nextPos;
        }
        solution.add(solution.get(0));
        return solution;
    }

    public int calculateFitness(ArrayList<ArrayList> solution){
        int distance = 0;
        for(int i = 0; i < solution.size() - 1; i++){
            int startCity = (int)solution.get(i).get(3);
            int endCity = (int)solution.get(i + 1).get(3);
            int intDist = dMap.get(startCity).get(endCity);
            distance += intDist;
        }
        System.out.println(distance);
        return distance;
    }

    private boolean inSolution(int index, ArrayList<ArrayList> solution){
        for(int i = 0; i < solution.size(); i++){
            if(solution.get(i).get(0) == cityData.get(index).get(0) && solution.get(i).get(1) == cityData.get(index).get(1)){
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
                public int compare(Map.Entry<Integer, Integer> o1, Map.Entry<Integer, Integer> o2){
                    return (o1.getValue()).compareTo(o2.getValue());
                } 
            });
            HashMap <Integer, Integer> temp = new LinkedHashMap<Integer, Integer>();
            for (Map.Entry<Integer, Integer> aa : list){
                temp.put(aa.getKey(), aa.getValue());
            }
            sortedMap.put(i, temp);
        }
        return sortedMap;
    }
}
