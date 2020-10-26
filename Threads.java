//Importing all the required packages for the thread
import java.util.ArrayList;

///Thread that runs one ant and handles solution construction
public class Threads extends Thread {
    public ArrayList<City> cities;
    public boolean isUpdate;
    public Ant ant;
    public int bestScore;
    public ArrayList<City> xcity;
    public ArrayList<Solution> bestSolutions = new ArrayList<>();

    //Thread constructor
    public Threads(ArrayList<City> city, boolean isUpdate, Ant ant){
        this.cities = city;
        this.isUpdate = isUpdate;
        this.ant = ant;
    }

    //Runs optimization for 2 ants
    public void run(){
        if(!this.isUpdate){
            ArrayList<Solution> solutionList = new ArrayList<>();
            ArrayList<City> cityData = this.cities;
            for(int i = 0; i < 2; i++){
                Solution solution = this.ant.constuctSolution();
                solutionList.add(solution);
            }
            solutionList = problem.sortSolutions(solutionList);
            this.bestSolutions = problem.updateBest(solutionList, this.bestSolutions);
            this.bestScore = this.bestSolutions.get(0).score;
            cityData = problem.initiatePheromone(solutionList, cityData);
            cityData = problem.globalUpdatePheromone(solutionList, cityData);
            this.cities = cityData;
        }
        else{
            ArrayList<Solution> solutionList = new ArrayList<>();
            ArrayList<City> cityData = this.cities;
            for(int i = 0; i < 2; i++){
                Solution solution = this.ant.updateSolution();
                solution = problem.localSearch(solution);
                solutionList.add(solution);
            }
            solutionList = problem.sortSolutions(solutionList);
            this.bestSolutions = problem.updateBest(solutionList, this.bestSolutions);
            this.bestScore = this.bestSolutions.get(0).score;
            cityData = problem.initiatePheromone(solutionList, cityData);
            cityData = problem.globalUpdatePheromone(solutionList, cityData);
            this.cities = cityData;
        }
    }
}