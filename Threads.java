public class Threads extends Thread {
    public Solution solution;
    public boolean isUpdate;
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
