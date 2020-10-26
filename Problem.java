//Priyank_1297953_Sivaram_1299026

//Importing all the required packages for the problem class
import java.io.*;
import java.util.*;
import org.jfree.data.xy.XYSeries;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.chart.annotations.XYTextAnnotation;
import java.awt.*; 
import javax.swing.*; 

///Serial version of the Ant System using the elitist strategy for the symmetric travelling salesperson problem
class problem {
    //Contains the data from the file
    static ArrayList<String> dataArray = new ArrayList<String>();
    //Contains the city objects and their coordinates
    static ArrayList<City> cityData = new ArrayList<>();
    //Contains the distance between every pair of cities on the map
    static HashMap<Integer, HashMap<Integer, Integer>> cityMap = new HashMap<>();
    //Creates the Colony object
    static Colony colony = new Colony();
    //Contains all the best solutions found throughout the run
    static ArrayList<Solution> bestSolutions = new ArrayList<>();
    //Number of ants for testing
    static int numAnts = 20;

    public static void main(String[] args){
        if(args.length != 1){
            System.err.println("Usage: java problem <filename>");
        }
        //Read in the data and parse it into the member variables
        String file = args[0];
        readFile(file);
        dataArray = getCoords(dataArray);
        cityData = plotCities(dataArray);
        cityMap = createHashMap(cityData);
        initiateColony();
        //Plot the cities on a 2D scatter plot
        Plot plot = new Plot("Cities");
        plot.setSize(800,400);
        plot.setLocationRelativeTo(null);
        plot.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        plot.setVisible(true);
    }

    //Reads the data from the file and parses it into the dataArray
    public static void readFile(String file){
        try{
            File problemFile = new File(file);
            Scanner reader = new Scanner(problemFile);
            if(problemFile.exists()){
                System.out.println(problemFile.getName());
                while(reader.hasNextLine()){
                    String data = reader.nextLine();
                    dataArray.add(data);
                }
                reader.close();
            }
        }
        catch(FileNotFoundException e){
            System.out.println("File was not found");
            e.printStackTrace();
        }
    }

    //Processes the raw data to extract the coordinates for the cities
    public static ArrayList<String> getCoords(ArrayList<String> list){
        int i = 0;
        while(!list.get(i).equals("NODE_COORD_SECTION")){
            list.remove(i);
        }
        list.remove(i);
        list.remove(list.size() - 1);
        return list;
    }

    //Initialises City objects and creates a list of cities from the data
    public static ArrayList<City> plotCities(ArrayList<String> list){
        ArrayList<City> coord = new ArrayList<>();
        for(int i = 0; i < list.size(); i++){
            String record = list.get(i);
            String[] coords = record.split(" ");
            City city = new City(Integer.parseInt(coords[1]), Integer.parseInt(coords[2]), i);
            coord.add(city);
        }
        return coord;
    }

    //Calculate the distance between every pair of cities and add it to a hashmap
    public static HashMap<Integer, HashMap<Integer, Integer>> createHashMap(ArrayList<City> cities){
        HashMap<Integer, HashMap<Integer, Integer>> map = new HashMap<>();
        for(int i = 0; i < cities.size(); i++){
            HashMap<Integer, Integer> dMap = new HashMap<>();
            int startX = cities.get(i).xCoord;
            int startY = cities.get(i).yCoord;
            for(int j = 0; j < cities.size(); j++){
                int endX = cities.get(j).xCoord;
                int endY = cities.get(j).yCoord;
                int distance = (int)Math.sqrt(Math.pow((endX - startX), 2) + Math.pow((endY - startY), 2));
                dMap.put(j, distance);
            }
            map.put(i, dMap);
        }
        return map;
    }

    //Creates a number of ants and adds them to the colony
    public static void initiateColony(){
        for(int i = 0; i < cityData.size(); i++){
            Ant ant = new Ant(cityData, cityMap, i);
            Colony.ants.add(ant);
        }
    }

    //Starts the colony and constructs the initial solutions using nearest neighbours
    public static void deployColony(){
        ArrayList<Solution> solutionList = new ArrayList<>();
        for(int i = 0; i < numAnts; i++){
            Solution solution = Colony.ants.get(i).constuctSolution();
            Colony.ants.get(i).solution = solution;
            solutionList.add(solution);
        }
        solutionList = sortSolutions(solutionList);
        int bestScore = solutionList.get(0).score;
        Solution bestSolution = solutionList.get(0);
        Colony.bestAnt = bestSolution.ant;
        bestSolutions = updateBest(solutionList, bestSolutions);
        System.out.print("Initital Score: " + bestScore + "\n");
        initiatePheromone(solutionList);
        globalUpdatePheromone(solutionList);
    }

    //Runs local search on the solutions to find the best improvement solution in the 2-opt neighbourhood
    private static Solution localSearch(Solution solution){
        Solution initSolution = solution;
        Solution bestSolution = initSolution;
        Ant ant = initSolution.ant;
        HashMap<Integer, HashMap<Integer, Integer>> map = initSolution.dMap;
        int bestScore = bestSolution.score;
        for(int i = 0; i < initSolution.sol.size() - 1; i++){
            for(int j = i + 2; j < initSolution.sol.size() - 2; j++){
                @SuppressWarnings("unchecked")
                ArrayList<City> temp = (ArrayList<City>)initSolution.sol.clone();
                City end1 = temp.get(i + 1);
                City end2 = temp.get(j);
                int k = i + 1;
                while(temp.get(j) != end1){
                    temp.add(k, end2);
                    k++;
                    temp.remove(j + 1);
                    end2 = temp.get(j);
                }
                Solution newSolution = new Solution(temp, map, ant);
                if(newSolution.score <= bestScore){
                    bestScore = newSolution.score;
                    bestSolution = newSolution;
                }
            }
        }
        return bestSolution;
    }

    //Updates the list of best solutions
    private static ArrayList<Solution> updateBest(ArrayList<Solution> solutionList, ArrayList<Solution> solutionBest){
        int i = 0;
        while(solutionBest.size() <= solutionList.size()/4){
            solutionBest.add(solutionList.get(i));
            i++;
        }
        for(Solution solution : solutionList){
            if(!solutionBest.contains(solution)){
                solutionBest.add(solution);
                solutionBest = sortSolutions(solutionBest);
                solutionBest.remove(solutionBest.size() - 1);
            }
        }
        return solutionBest;
    }

    //Sort the solution lists based on the scores
    public static ArrayList<Solution> sortSolutions(ArrayList<Solution> solutions){
        ArrayList<Solution> temp = new ArrayList<>();
        while(solutions.size() != 0){
            int bestScore = 10000000;
            Solution bestSolution = solutions.get(0);
            for(int i = 0; i < solutions.size(); i++){
                int score = solutions.get(i).score;
                if(score <= bestScore){
                    bestScore = score;
                    bestSolution = solutions.get(i);
                }
            }
            temp.add(bestSolution);
            solutions.remove(solutions.indexOf(bestSolution));
        }
        return temp;
    }

    //Initiates the pheromone counts for all the paths taken
    public static void initiatePheromone(ArrayList<Solution> solutions){
        for(int i = 0; i < solutions.size(); i++){
            for(int j = 0; j + 1 < solutions.get(i).sol.size(); j++){
                City fromCity = (City)solutions.get(i).sol.get(j);
                City toCity = (City)solutions.get(i).sol.get(j + 1);
                cityData.get(fromCity.index).createPath(fromCity.index, toCity.index);
                cityData.get(toCity.index).createPath(toCity.index, fromCity.index);
            }
        }
    }

    //Deploys the ants to create new solutions based on the pheromone deposits
    public static void beginOptimization(){
        ArrayList<Solution> solutionList = new ArrayList<>();
        for(int i = 0; i < numAnts; i++){
            Solution solution = Colony.ants.get(i).updateSolution();
            solution = localSearch(solution);
            Colony.ants.get(i).solution = solution;
            solutionList.add(solution);
        }
        solutionList = sortSolutions(solutionList);
        int bestScore = solutionList.get(0).score;
        Solution bestSolution = solutionList.get(0);
        Colony.bestAnt = bestSolution.ant;
        bestSolutions = updateBest(solutionList, bestSolutions);
        System.out.print("Best Score: " + bestScore + "\n");
        initiatePheromone(solutionList);
        globalUpdatePheromone(solutionList);
    }

    //Updates the pheromone values for the next iteration
    public static void globalUpdatePheromone(ArrayList<Solution> solutions){
        for(City city : cityData){
            city.evaporatePheromones();
        }
        growth(solutions);
    }

    //Growth function with the elitist strategy
    private static void growth(ArrayList<Solution> solutions){
        double g = 0;
        solutions.add(0, bestSolutions.get(0));
        for(int j = 0; j < solutions.size(); j++){
            Solution solution = solutions.get(j);
            if(j == 0){
                g = 2 * (cityData.size() / (j + 1));
            }
            else{
                g = cityData.size() / (j + 1);
            }
            for(int i = 0; i < solution.sol.size() - 1; i++){
                City city = solution.sol.get(i);
                City toCity = solution.sol.get(i + 1);
                for(Path path : city.paths){
                    if(path.toCity == toCity.index){
                        path.pheromoneCount += g;
                        break;
                    }
                }
            }
        }
    }

    //Plot class that visualizes the problem set and the solutions
    protected static class Plot extends JFrame{
        private static final long serialVersionUID = 6294689542092367723L;
        XYTextAnnotation annot;
        XYDataset dataset = createDataset(cityData);
        JFreeChart chart = ChartFactory.createScatterPlot("Cities for TSP", "X-Axis", "Y-Axis", dataset);
        XYPlot scatterplot = (XYPlot)chart.getPlot();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        ChartPanel panel = new ChartPanel(chart);
        
        //Plot object that handles the events of the plot
        public Plot(String title){
            super(title);
            scatterplot.setBackgroundPaint(new Color(255,255,255));
            panel.addChartMouseListener(new ChartMouseListener(){
                @Override
                public void chartMouseClicked(ChartMouseEvent me){
                    if(me.getTrigger().getButton() == java.awt.event.MouseEvent.BUTTON1){
                        int i = 0;
                        long start = System.currentTimeMillis();
                        System.out.println("Start time: " + start);
                        while(i != 100){
                            if(renderer.getSeriesLinesVisible(0) == null || !renderer.getSeriesLinesVisible(0)){
                                deployColony();
                            }
                            else{
                                beginOptimization();
                            }
                            i++;
                            scatterplot.setDataset(createDataset(Colony.bestAnt.solution.sol));
                            renderer.setSeriesLinesVisible(0, true);
                            scatterplot.setRenderer(renderer);
                            setContentPane(panel);
                        }
                        scatterplot.setDataset(createDataset(bestSolutions.get(0).sol));
                        System.out.print("Final Score: " + bestSolutions.get(0).score + "\n");
                        System.out.println("Time elapsed: " + (start - System.currentTimeMillis()));
                        renderer.setSeriesLinesVisible(0, true);
                        scatterplot.setRenderer(renderer);
                        setContentPane(panel);
                    }
                }
                @Override
                public void chartMouseMoved(ChartMouseEvent me){
                    if(me.getEntity() instanceof XYItemEntity){
                        if(renderer.getSeriesLinesVisible(0) == null || !renderer.getSeriesLinesVisible(0)){
                            if(scatterplot.getAnnotations().size() != 0){
                                scatterplot.removeAnnotation(annot);
                            }
                            XYItemEntity i = (XYItemEntity)me.getEntity();
                            double xAnnot = dataset.getXValue(i.getSeriesIndex(), i.getItem());
                            double yAnnot = dataset.getYValue(i.getSeriesIndex(), i.getItem());
                            annot = new XYTextAnnotation(Integer.toString(i.getItem() + 1), xAnnot, yAnnot + 100);
                            scatterplot.addAnnotation(annot);
                        }
                        else{
                            if(scatterplot.getAnnotations().size() != 0){
                                scatterplot.removeAnnotation(annot);
                            }
                            XYItemEntity i = (XYItemEntity)me.getEntity();
                            double xAnnot = scatterplot.getDataset().getXValue(i.getSeriesIndex(), i.getItem());
                            double yAnnot = scatterplot.getDataset().getYValue(i.getSeriesIndex(), i.getItem());
                            annot = new XYTextAnnotation(Integer.toString(Colony.bestAnt.solution.sol.get(i.getItem()).index + 1), xAnnot, yAnnot + 100);
                            scatterplot.addAnnotation(annot);
                        }
                    }
                }
            });
            setContentPane(panel);
        }

        //Creates the datapoints from the given list of cities
        private XYDataset createDataset(ArrayList<City> dataArray){
            XYSeriesCollection dataset = new XYSeriesCollection();
            XYSeries series = new XYSeries("Cities", false);
            for(int i = 0; i < dataArray.size(); i++){
                series.add(dataArray.get(i).xCoord, dataArray.get(i).yCoord);
            }
            dataset.addSeries(series);
            return dataset;
        }
    }
}