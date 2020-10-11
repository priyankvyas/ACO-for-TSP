import java.io.*;
import java.util.*;
import java.util.HashMap;
// import java.awt.Color;
// import javax.swing.JFrame;
// import org.jfree.data.xy.XYSeries;
// import org.jfree.chart.ChartPanel;
// import org.jfree.chart.JFreeChart;
// import org.jfree.chart.plot.XYPlot;
// import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
// import org.jfree.data.xy.XYDataset;
// import javax.swing.WindowConstants;
// import org.jfree.chart.ChartFactory;
// import org.jfree.chart.ChartMouseEvent;
// import org.jfree.chart.ChartMouseListener;
// import org.jfree.chart.entity.XYItemEntity;
// import org.jfree.data.xy.XYSeriesCollection;
// import org.jfree.chart.annotations.XYTextAnnotation;

class problem {
    //Contains the data from the file
    static ArrayList<String> dataArray = new ArrayList<String>();
    //Contains the city objects and their coordinates
    static ArrayList<City> cityData = new ArrayList<>();
    //Contains the distance between every pair of cities on the map
    static HashMap<Integer, HashMap<Integer, Integer>> cityMap = new HashMap<>();
    //Number of ants in the colony
    static int numAnts = 0;
    //Creates the Colony object
    static Colony colony = new Colony();

    public static void main(String[] args){
        if(args.length != 2){
            System.err.println("Usage: java problem <filename> <numAnts>");
        }
        String file = args[0];
        numAnts = Integer.parseInt(args[1]);
        readFile(file);
        dataArray = getCoords(dataArray);
        cityData = plotCities(dataArray);
        cityMap = createHashMap(cityData);
        initiateColony();
        deployColony();
        // Plot plot = new Plot("Cities");
        // plot.setSize(800,400);
        // plot.setLocationRelativeTo(null);
        // plot.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        // plot.setVisible(true);
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
        for(int i = 0; i < numAnts; i++){
            Ant ant = new Ant(cityData, cityMap);
            Colony.ants.add(ant);
        }
    }

    public static void deployColony(){
        ArrayList<Solution> solutionList = new ArrayList<>();
        for(int i = 0; i < Colony.ants.size(); i++){
            Solution solution = Colony.ants.get(i).constuctSolution();
            solutionList.add(solution);
        }
        solutionList = sortSolutions(solutionList);
        int bestScore = solutionList.get(0).score;
        Solution bestSolution = solutionList.get(0);
        Colony.bestAnt = bestSolution.ant;
        System.out.print("Initital Score: " + bestScore + "\n");
        initiatePheromone(solutionList);
        beginOptimization();
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

    public static void beginOptimization(){
        int j = 0;
        while(j != 10){
            ArrayList<Solution> solutionList = new ArrayList<>();
            for(int i = 0; i < Colony.ants.size(); i++){
                Solution solution = Colony.ants.get(i).updateSolution();
                solutionList.add(solution);
            }
            solutionList = sortSolutions(solutionList);
            int bestScore = solutionList.get(0).score;
            Solution bestSolution = solutionList.get(0);
            Colony.bestAnt = bestSolution.ant;
            System.out.print("Best Score: " + bestScore + "\n");
            globalUpdatePheromone(solutionList);
            j++;
        }
    }

    public static void globalUpdatePheromone(ArrayList<Solution> solutions){
        for(City city : cityData){
            city.evaporatePheromones();
        }
        initiatePheromone(solutions);
        for(int i = 0; i < solutions.size(); i++){
            Solution solution = solutions.get(i);
            double ind = (double)i;
            double size = (double)solutions.size();
            double rate = ind/size;
            for(City city : solution.sol){
                if(rate <= 0.25){
                    city.increasePheromone(1);
                }
                else if(rate <= 0.5){
                    city.increasePheromone(2);
                }
                else if(rate <= 0.75){
                    city.increasePheromone(3);
                }
                else{
                    city.increasePheromone(0);
                }
            }
        }
    }
    
    // private static class Plot extends JFrame{
    //     private static final long serialVersionUID = 6294689542092367723L;
    //     XYTextAnnotation annot;
    //     Solution solution1;
    //     XYDataset dataset = createDataset(cityData);
    //     JFreeChart chart = ChartFactory.createScatterPlot("Cities for TSP", "X-Axis", "Y-Axis", dataset);
    //     XYPlot scatterplot = (XYPlot)chart.getPlot();
    //     XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
    //     ChartPanel panel = new ChartPanel(chart);
        
    //     public Plot(String title){
    //         super(title);
    //         scatterplot.setBackgroundPaint(new Color(255,255,255));
    //         panel.addChartMouseListener(new ChartMouseListener(){
    //             @Override
    //             public void chartMouseClicked(ChartMouseEvent me){
    //                 deployColony();
    //                 Ant ant1 = colony.bestAnt;
    //                 solution1 = ant1.solution;
    //                 scatterplot.setDataset(createDataset(solution1.sol));
    //                 renderer.setSeriesLinesVisible(0, true);
    //                 scatterplot.setRenderer(renderer);
    //                 setContentPane(panel);
    //             }
    //             @Override
    //             public void chartMouseMoved(ChartMouseEvent me){
    //                 if(me.getEntity() instanceof XYItemEntity){
    //                     if(renderer.getSeriesLinesVisible(0) == null || !renderer.getSeriesLinesVisible(0)){
    //                         if(scatterplot.getAnnotations().size() != 0){
    //                             scatterplot.removeAnnotation(annot);
    //                         }
    //                         XYItemEntity i = (XYItemEntity)me.getEntity();
    //                         double xAnnot = dataset.getXValue(i.getSeriesIndex(), i.getItem());
    //                         double yAnnot = dataset.getYValue(i.getSeriesIndex(), i.getItem());
    //                         annot = new XYTextAnnotation(Integer.toString(i.getItem() + 1), xAnnot, yAnnot + 100);
    //                         scatterplot.addAnnotation(annot);
    //                     }
    //                     else{
    //                         if(scatterplot.getAnnotations().size() != 0){
    //                             scatterplot.removeAnnotation(annot);
    //                         }
    //                         XYItemEntity i = (XYItemEntity)me.getEntity();
    //                         double xAnnot = scatterplot.getDataset().getXValue(i.getSeriesIndex(), i.getItem());
    //                         double yAnnot = scatterplot.getDataset().getYValue(i.getSeriesIndex(), i.getItem());
    //                         annot = new XYTextAnnotation(Integer.toString(solution1.sol.get(i.getItem()).index + 1), xAnnot, yAnnot + 100);
    //                         scatterplot.addAnnotation(annot);
    //                     }
    //                 }
    //             }
    //         });
    //         setContentPane(panel);
    //     }

    //     private XYDataset createDataset(ArrayList<City> dataArray){
    //         XYSeriesCollection dataset = new XYSeriesCollection();
    //         XYSeries series = new XYSeries("Cities", false);
    //         for(int i = 0; i < dataArray.size(); i++){
    //             series.add(dataArray.get(i).xCoord, dataArray.get(i).yCoord);
    //         }
    //         dataset.addSeries(series);
    //         return dataset;
    //     }
    // }
}