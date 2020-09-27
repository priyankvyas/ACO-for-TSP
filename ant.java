import java.util.ArrayList;
import java.util.Random;

public class Ant {
    private ArrayList<ArrayList> cityData;
    public Ant(ArrayList<ArrayList> cities){
        cityData = cities;
    }

    public ArrayList<ArrayList> constuctSolution(){
        ArrayList<ArrayList> solution = new ArrayList<ArrayList>();
        Random r = new Random();
        int startPos = r.nextInt(cityData.size());
        cityData.get(startPos).add(startPos);
        solution.add(cityData.get(startPos));
        while(solution.size() != cityData.size()){
            int nextPos = r.nextInt(cityData.size());
            while(inSolution(nextPos, solution)){
                nextPos = r.nextInt(cityData.size());
            }
            cityData.get(nextPos).add(nextPos);
            solution.add(cityData.get(nextPos));
        }
        solution.add(solution.get(0));
        return solution;
    }

    public int calculateFitness(ArrayList<ArrayList> solution){
        int distance = 0;
        for(int i = 0; i < solution.size() - 1; i++){
            int startX = (int)solution.get(i).get(0);
            int startY = (int)solution.get(i).get(1);
            int endX = (int)solution.get(i + 1).get(0);
            int endY = (int)solution.get(i + 1).get(1);
            int intDist = (int)Math.sqrt(Math.pow((endX - startX), 2) + Math.pow((endY - startY), 2));
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
}
