import java.util.ArrayList;
import java.util.Random;

public class ant {
    private ArrayList<ArrayList> cityData;
    public ant(ArrayList<ArrayList> cities){
        cityData = cities;
    }

    public ArrayList<Integer> constuctSolution(){
        ArrayList<Integer> solution = new ArrayList<Integer>();
        Random r = new Random();
        int startPos = r.nextInt(cityData.get(0).size());
        solution.add(startPos);
        while(solution.size() != cityData.get(0).size()){
            int nextPos = r.nextInt(cityData.get(0).size());
            while(inSolution(nextPos, solution)){
                nextPos = r.nextInt(cityData.get(0).size());
            }
            solution.add(nextPos);
        }
        return solution;
    }

    private boolean inSolution(int index, ArrayList<Integer> solution){
        for(int i = 0; i < solution.size(); i++){
            if(solution.get(i) == index){
                return true;
            }
        }
        return false;
    }
}
