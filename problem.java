import java.io.*;
import java.util.*;
import java.awt.Color;
import java.awt.event.*;
import javax.swing.JFrame;
import org.jfree.data.xy.XYSeries;
import javax.swing.SwingUtilities;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import java.util.HashMap;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import javax.swing.WindowConstants;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.entity.PlotEntity;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.data.xy.XYSeriesCollection;
import java.awt.event.MouseEvent;
import org.jfree.chart.annotations.XYAnnotation;
import org.jfree.chart.annotations.XYTextAnnotation;

public class problem{
    static ArrayList<String> dataArray = new ArrayList<String>();
    static ArrayList<ArrayList> cityData = new ArrayList<ArrayList>();
    static HashMap<Integer, HashMap<Integer, Integer>> cityMap = new HashMap<>();
    public static void main(String[] args){
        if(args.length != 1){
            System.err.println("Usage: java problem <filename>");
        }
        String file = args[0];
        readFile(file);
        dataArray = getCoords(dataArray);
        cityData = plotCities(dataArray);
        cityMap = createHashMap(cityData);

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

    public static ArrayList<ArrayList> plotCities(ArrayList<String> list){
        ArrayList<ArrayList> coord = new ArrayList<ArrayList>();
        int initPheromone = 0;
        for(int i = 0; i < list.size(); i++){
            String record = list.get(i);
            String[] coords = record.split(" ");
            ArrayList<Integer> city = new ArrayList<Integer>();
            city.add(Integer.parseInt(coords[1]));
            city.add(Integer.parseInt(coords[2]));
            city.add(initPheromone);
            coord.add(city);
        }
        return coord;
    }

    public static HashMap createHashMap(ArrayList<ArrayList> cities){
        HashMap<Integer, HashMap<Integer, Integer>> map = new HashMap<>();
        for(int i = 0; i < cities.size(); i++){
            HashMap<Integer, Integer> dMap = new HashMap<>();
            int startX = (int)cities.get(i).get(0);
            int startY = (int)cities.get(i).get(1);
            for(int j = 0; j < cities.size(); j++){
                int endX = (int)cities.get(j).get(0);
                int endY = (int)cities.get(j).get(1);
                int distance = (int)Math.sqrt(Math.pow((endX - startX), 2) + Math.pow((endY - startY), 2));
                dMap.put(j, distance);
            }
            map.put(i, dMap);
        }
        return map;
    }

    private static class Plot extends JFrame{
        private static final long serialVersionUID = 6294689542092367723L;
        XYTextAnnotation annot;
        ArrayList<ArrayList> solution1;
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
                    Ant ant1 = new Ant(cityData, cityMap);
                    solution1 = ant1.constuctSolution();
                    System.out.println(solution1);
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
                            annot = new XYTextAnnotation(Integer.toString((int)solution1.get(i.getItem()).get(3) + 1), xAnnot, yAnnot + 100);
                            scatterplot.addAnnotation(annot);
                        }
                    }
                }
            });
            setContentPane(panel);
        }

        private XYDataset createDataset(ArrayList<ArrayList> dataArray){
            XYSeriesCollection dataset = new XYSeriesCollection();
            XYSeries series = new XYSeries("Cities", false);
            for(int i = 0; i < dataArray.size(); i++){
                series.add((int)dataArray.get(i).get(0), (int)dataArray.get(i).get(1));
            }
            dataset.addSeries(series);
            return dataset;
        }
    }
}