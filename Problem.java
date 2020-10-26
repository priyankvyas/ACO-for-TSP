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

///Parallel Fine-grained version of the Ant Colony Optimization algorithm for the symmetric travelling salesperson problem
class problem {
    //Contains the data from the file
    static ArrayList<String> dataArray = new ArrayList<String>();
    //Contains the city objects and their coordinates
    static ArrayList<City> cityData = new ArrayList<>();
    //Contains the distance between every pair of cities on the map
    static HashMap<Integer, HashMap<Integer, Integer>> cityMap = new HashMap<>();
    //Number of threads
    static int numThreads = 0;
    //Contains the threads that run the 2 ant sub-colonies
    static ArrayList<Threads> threads = new ArrayList<>();
    //The global best solution and score variables
    static Solution globalBestSolution;
    static int globalBest = 0;

    public static void main(String[] args){
        //Parse in the filename and number of threads
        if(args.length != 2){
            System.err.println("Usage: java problem <filename> <numThreads>");
        }
        //Read the problem set file
        String file = args[0];
        numThreads = Integer.parseInt(args[1]);
        readFile(file);
        //Parse the data and create a 2D scatter plot of the cities
        dataArray = getCoords(dataArray);
        cityData = plotCities(dataArray);
        cityMap = createHashMap(cityData);
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

    //Creates a shallow copy of the list of cities
    public static ArrayList<City> createShallow(){
        ArrayList<City> shallowCity = new ArrayList<>();
        for(City city : cityData){
            shallowCity.add((City)city.clone());
        }
        return shallowCity;
    }

    //Starts the initial ant solution construction and updates the pheromones
    public static Solution deployColony(){
        ArrayList<ArrayList<City>> pheromoneMaps = new ArrayList<>();
        for(int i = 0; i < numThreads; i++){
            ArrayList<City> cities = createShallow();
            Ant ant = new Ant(cities, cityMap, i);
            Threads t = new Threads(cities, false, ant);
            t.start();
            threads.add(t);
        }
        int bestScore = 1000000;
        Solution bestSolution = new Solution(cityData, cityMap, new Ant(cityData, cityMap, 0));
        for(Threads t : threads){
            try{
                t.join();
                for(Solution solution : t.bestSolutions){
                    if(solution.score <= bestScore){
                        bestScore = solution.score;
                        bestSolution = solution;
                    }
                }
                pheromoneMaps.add(t.cities);
            }
            catch(InterruptedException e){
                System.err.println("Thread interrupted");
            }
        }
        for(ArrayList<City> map : pheromoneMaps){
            for(int i = 0; i < map.size(); i++){
                City city = map.get(i);
                City avgCity = cityData.get(i);
                for(int j = 0; j < city.paths.size(); j++){
                    Path path = city.paths.get(j);
                    if(avgCity.inCity(path)){
                        avgCity.getPath(path).pheromoneCount += path.pheromoneCount;
                        avgCity.getPath(path).pheromoneCount /= 2;
                    }
                    else{
                        avgCity.createPath(avgCity.index, path.toCity);
                        cityData.get(path.toCity).createPath(path.toCity, avgCity.index);
                    }
                }
            }
        }
        System.out.print("Initital Score: " + bestScore + "\n");
        globalBest = bestScore;
        globalBestSolution = bestSolution;
        return bestSolution;
    }

    //Performs the 2-opt local search on the ant solution to get the best improvement solution in
    //the neighbourhood
    @SuppressWarnings("unchecked")
    public static Solution localSearch(Solution solution){
        Solution initSolution = solution;
        Solution bestSolution = initSolution;
        Ant ant = initSolution.ant;
        HashMap<Integer, HashMap<Integer, Integer>> map = initSolution.dMap;
        int bestScore = bestSolution.score;
        for(int i = 0; i < initSolution.sol.size() - 1; i++){
            for(int j = i + 2; j < initSolution.sol.size() - 2; j++){
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

    //Updates the list of best solutions found
    public static ArrayList<Solution> updateBest(ArrayList<Solution> solutionList, ArrayList<Solution> solutionBest){
        int i = 0;
        while(solutionBest.size() < solutionList.size()){
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
    public static ArrayList<City> initiatePheromone(ArrayList<Solution> solutions, ArrayList<City> cities){
        for(int i = 0; i < solutions.size(); i++){
            for(int j = 0; j + 1 < solutions.get(i).sol.size(); j++){
                City fromCity = (City)solutions.get(i).sol.get(j);
                City toCity = (City)solutions.get(i).sol.get(j + 1);
                cities.get(fromCity.index).createPath(fromCity.index, toCity.index);
                cities.get(toCity.index).createPath(toCity.index, fromCity.index);
            }
        }
        return cities;
    }

    //Optimization method of ant colony that creates updated solutions based on the pheromone
    //deposited by the ants
    public static Solution beginOptimization(){
        ArrayList<ArrayList<City>> pheromoneMaps = new ArrayList<>();
        threads.removeAll(threads);
        for(int i = 0; i < numThreads; i++){
            ArrayList<City> cities = createShallow();
            Ant ant = new Ant(cities, cityMap, i);
            Threads t = new Threads(cities, true, ant);
            t.start();
            threads.add(t);
        }
        int bestScore = 1000000;
        Solution bestSolution = new Solution(cityData, cityMap, new Ant(cityData, cityMap, 0));
        for(Threads t : threads){
            try{
                t.join();
                for(Solution solution : t.bestSolutions){
                    if(solution.score <= bestScore){
                        bestScore = solution.score;
                        bestSolution = solution;
                    }
                }
                pheromoneMaps.add(t.cities);
            }
            catch(InterruptedException e){
                System.err.println("Thread interrupted");
            }
        }
        for(ArrayList<City> map : pheromoneMaps){
            for(int i = 0; i < map.size(); i++){
                City city = map.get(i);
                City avgCity = cityData.get(i);
                for(int j = 0; j < city.paths.size(); j++){
                    Path path = city.paths.get(j);
                    if(avgCity.inCity(path)){
                        avgCity.getPath(path).pheromoneCount += path.pheromoneCount;
                        avgCity.getPath(path).pheromoneCount /= 2;
                    }
                    else{
                        avgCity.createPath(avgCity.index, path.toCity);
                        cityData.get(path.toCity).createPath(path.toCity, avgCity.index);
                    }
                }
            }
        }
        System.out.print("Best Score: " + bestScore + "\n");
        if(bestScore < globalBest){
            globalBest = bestScore;
            globalBestSolution = bestSolution;
        }
        return bestSolution;
    }

    //Updates the pheromone values after each iteration of solution creation
    public static ArrayList<City> globalUpdatePheromone(ArrayList<Solution> solutions, ArrayList<City> cities){
        for(City city : cities){
            city.evaporatePheromones();
        }
        growth(solutions);
        return cities;
    }

    //Growth function of the pheromone update rule to increment pheromone values of good
    //solutions
    private static void growth(ArrayList<Solution> solutions){
        for(int j = 0; j < solutions.size(); j++){
            Solution solution = solutions.get(j);
            double g = cityData.size() / (j + 1);
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

    ///Plot class of that creates the visualization of the problem set and the solution
    protected static class Plot extends JFrame{
        private static final long serialVersionUID = 6294689542092367723L;
        XYTextAnnotation annot;
        XYDataset dataset = createDataset(cityData);
        JFreeChart chart = ChartFactory.createScatterPlot("Cities for TSP", "X-Axis", "Y-Axis", dataset);
        XYPlot scatterplot = (XYPlot)chart.getPlot();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        ChartPanel panel = new ChartPanel(chart);
        
        //Plot constructor object that handles the event listener
        public Plot(String title){
            super(title);
            Solution solution = new Solution(cityData, cityMap, new Ant(cityData, cityMap, 0));
            scatterplot.setBackgroundPaint(new Color(255,255,255));
            panel.addChartMouseListener(new ChartMouseListener(){
                @Override
                public void chartMouseClicked(ChartMouseEvent me){
                    if(me.getTrigger().getButton() == java.awt.event.MouseEvent.BUTTON1){
                        int i = 0;
                        Solution solution = new Solution(cityData, cityMap, new Ant(cityData, cityMap, 0));
                        while(i != 1000){
                            if(renderer.getSeriesLinesVisible(0) == null || !renderer.getSeriesLinesVisible(0)){
                                solution = deployColony();
                            }
                            else{
                                solution = beginOptimization();
                            }
                            i++;
                            scatterplot.setDataset(createDataset(solution.sol));
                            renderer.setSeriesLinesVisible(0, true);
                            scatterplot.setRenderer(renderer);
                            setContentPane(panel);
                        }
                        scatterplot.setDataset(createDataset(globalBestSolution.sol));
                        System.out.print("Final Score: " + globalBest + "\n");
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
                            annot = new XYTextAnnotation(Integer.toString(solution.sol.get(i.getItem()).index + 1), xAnnot, yAnnot + 100);
                            scatterplot.addAnnotation(annot);
                        }
                    }
                }
            });
            setContentPane(panel);
        }

        //Creates the datapoints for the scatter plot based on the provided list of cities in order
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