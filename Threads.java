import java.util.ArrayList;

public class Threads extends Thread {
    public boolean isUpdate;
    public Solution solution;
    public Threads(Solution solution, boolean isUpdate){
        this.solution = solution;
        this.isUpdate = isUpdate;
    }

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
