import java.io.*;
import java.util.*;
import java.awt.Color;
import javax.swing.JFrame;
import org.jfree.data.xy.XYSeries;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import java.util.HashMap;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import javax.swing.WindowConstants;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.chart.annotations.XYTextAnnotation;

public class problem{
    static ArrayList<String> dataArray = new ArrayList<String>();
    static ArrayList<City> cityData = new ArrayList<>();
    static HashMap<Integer, HashMap<Integer, Integer>> cityMap = new HashMap<>();
    static ArrayList<Ant> colony = new ArrayList<>();
    static int numAnts = 0;
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

        Plot plot = new Plot("Cities");
        plot.setSize(800,400);
        plot.setLocationRelativeTo(null);
        plot.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        plot.setVisible(true);
    }

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

    public static ArrayList<String> getCoords(ArrayList<String> list){
        int i = 0;
        while(!list.get(i).equals("NODE_COORD_SECTION")){
            list.remove(i);
        }
        list.remove(i);
        list.remove(list.size() - 1);
        return list;
    }

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

    public static void initiateColony(){
        for(int i = 0; i < numAnts; i++){
            Ant ant = new Ant(cityData, cityMap);
            colony.add(ant);
        }
    }

    public static void deployColony(){
        ArrayList<ArrayList> solutions = new ArrayList<>();
        int bestScore = 1000000;
        ArrayList<City> bestSolution = new ArrayList<>();
        for(int i = 0; i < colony.size(); i++){
            ArrayList<City> solution = colony.get(i).constuctSolution();
            int score = colony.get(i).calculateFitness(solution);
            if(score <= bestScore){
                bestScore = score;
                bestSolution = solution;
            }
            solutions.add(solution);
        }
        globalUpdatePheromone(solutions);
    }

    public static void globalUpdatePheromone(ArrayList<ArrayList> solutions){
        for(int i = 0; i < solutions.size(); i++){
            for(int j = 0; j + 1 < solutions.get(i).size(); j++){
                City fromCity = (City)solutions.get(i).get(j);
                City toCity = (City)solutions.get(i).get(j + 1);
                cityData.get(fromCity.index).createPath(fromCity.index, toCity.index);
            }
        }
        for(City city : cityData){
            city.compressPaths();
            city.evaporatePheromones();
            // for(City.Path path : city.paths){
            //     System.out.println(path.fromCity + " " + path.toCity + " " + path.pheromoneCount);
            // }
        }
    }
    
    private static class Plot extends JFrame{
        private static final long serialVersionUID = 6294689542092367723L;
        XYTextAnnotation annot;
        ArrayList<City> solution1;
        XYDataset dataset = createDataset(cityData);
        JFreeChart chart = ChartFactory.createScatterPlot("Cities for TSP", "X-Axis", "Y-Axis", dataset);
        XYPlot scatterplot = (XYPlot)chart.getPlot();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        ChartPanel panel = new ChartPanel(chart);
        
        public Plot(String title){
            super(title);
            scatterplot.setBackgroundPaint(new Color(255,255,255));
            panel.addChartMouseListener(new ChartMouseListener(){
                @Override
                public void chartMouseClicked(ChartMouseEvent me){
                    deployColony();
                    Ant ant1 = new Ant(cityData, cityMap);
                    solution1 = ant1.constuctSolution();
                    ant1.calculateFitness(solution1);
                    scatterplot.setDataset(createDataset(solution1));
                    renderer.setSeriesLinesVisible(0, true);
                    scatterplot.setRenderer(renderer);
                    setContentPane(panel);
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
                            annot = new XYTextAnnotation(Integer.toString(solution1.get(i.getItem()).index + 1), xAnnot, yAnnot + 100);
                            scatterplot.addAnnotation(annot);
                        }
                    }
                }
            });
            setContentPane(panel);
        }

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