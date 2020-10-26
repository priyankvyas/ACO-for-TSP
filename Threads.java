//Priyank_1297953_Sivaram_1299026

///Thread that runs one ant and handles solution construction
public class Threads extends Thread {
    public boolean isUpdate;
    public Solution solution;
    
    //Thread constructor
    public Threads(Solution solution, boolean isUpdate){
        this.solution = solution;
        this.isUpdate = isUpdate;
    }

    //Constructs the ant solution
    public void run(){
        if(!this.isUpdate){
            this.solution = this.solution.ant.constuctSolution();
        }
        else{
            this.solution = this.solution.ant.updateSolution();
            this.solution = problem.localSearch(this.solution);
        }
        this.solution.ant.solution = this.solution;
    }
}