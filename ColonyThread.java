import java.util.ArrayList;

public class ColonyThread extends Thread{
    public ArrayList<City> cities;
    public int id;
    public int bestScore;
    public Solution bestSolution;
    public ColonyThread(ArrayList<City> city, int id){
        this.cities = city;
        this.id = id;
    }

    public void run(){
        ArrayList<Object> returnValue = new ArrayList<>();
        returnValue = problem.deployColony(this.cities);
        this.cities = (ArrayList<City>)returnValue.get(0);
        this.bestScore = (int)returnValue.get(1);
        this.bestSolution = (Solution)returnValue.get(2);
        int i = 1;
        while(i != 100){
            returnValue = problem.beginOptimization(this.cities);
            this.cities = (ArrayList<City>)returnValue.get(0);
            if((int)returnValue.get(1) < this.bestScore){
                this.bestScore = (int)returnValue.get(1);
                this.bestSolution = (Solution)returnValue.get(2);
            }
            i++;
        }
        System.out.println("Colony " + this.id + " Best Score: " + this.bestScore);
    }
}
