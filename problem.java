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
    public static void main(String[] args){
        if(args.length != 1){
            System.err.println("Usage: java problem <filename>");
        }
        String file = args[0];
        readFile(file);
        dataArray = getCoords(dataArray);
        cityData = plotCities(dataArray);

        Plot plot = new Plot("Cities");
        plot.setSize(800,400);
        plot.setLocationRelativeTo(null);
        plot.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        plot.setVisible(true);

        cityData = initializePheromone();
        ant ant1 = new ant(cityData);
        ArrayList<Integer> solution1 = ant1.constuctSolution();
        System.out.println(solution1);
        plot.changeDataset(solution1);
        
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
        ArrayList<Integer> xCoord = new ArrayList<Integer>();
        ArrayList<Integer> yCoord = new ArrayList<Integer>();
        for(int i = 0; i < list.size(); i++){
            String record = list.get(i);
            String[] coords = record.split(" ");
            xCoord.add(Integer.parseInt(coords[1]));
            yCoord.add(Integer.parseInt(coords[2]));
        }
        ArrayList<ArrayList> data = new ArrayList<ArrayList>();
        data.add(xCoord);
        data.add(yCoord);
        return data;
    }

    public static ArrayList<ArrayList> initializePheromone(){
        ArrayList<Integer> pheromone = new ArrayList<Integer>();
        for(int i = 0; i < cityData.get(0).size(); i++){
            pheromone.add(0);
        }
        cityData.add(pheromone);
        return cityData;
    }

    private static class Plot extends JFrame{
        private static final long serialVersionUID = 6294689542092367723L;
        XYDataset dataset = createDataset(cityData);
        JFreeChart chart = ChartFactory.createScatterPlot("Cities for TSP", "X-Axis", "Y-Axis", dataset);
        XYPlot scatterplot = (XYPlot)chart.getPlot();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        ChartPanel panel = new ChartPanel(chart);
        XYTextAnnotation annot;
        public Plot(String title){
            super(title);
            scatterplot.setBackgroundPaint(new Color(255,255,255));
            panel.addChartMouseListener(new ChartMouseListener(){
                @Override
                public void chartMouseClicked(ChartMouseEvent me){
                    if(me.getEntity() instanceof XYItemEntity){
                        if(!renderer.getSeriesLinesVisible(0)){
                            XYItemEntity i = (XYItemEntity) me.getEntity();
                            double xAnnot = dataset.getXValue(i.getSeriesIndex(), i.getItem());
                            double yAnnot = dataset.getYValue(i.getSeriesIndex(), i.getItem());
                            annot = new XYTextAnnotation(Integer.toString(i.getItem() + 1), xAnnot, yAnnot + 100);
                            scatterplot.addAnnotation(annot);
                        }
                        else{
                            scatterplot.removeAnnotation(annot);
                            XYItemEntity i = (XYItemEntity) me.getEntity();
                            double xAnnot = dataset.getXValue(i.getSeriesIndex(), i.getItem());
                            double yAnnot = dataset.getYValue(i.getSeriesIndex(), i.getItem());
                            annot = new XYTextAnnotation(Integer.toString(i.getItem() + 1), xAnnot, yAnnot + 100);
                            scatterplot.addAnnotation(annot);
                        }
                    }
                }
                @Override
                public void chartMouseMoved(ChartMouseEvent me){
                    if(scatterplot.getAnnotations().size() != 0 && !renderer.getSeriesLinesVisible(0)){
                        scatterplot.removeAnnotation(annot);
                    }
                    else{
                        return;
                    }
                }
            });
            setContentPane(panel);
        }

        public void changeDataset(ArrayList<Integer> solution){
            XYSeriesCollection newDataset = new XYSeriesCollection();
            XYSeries series = new XYSeries("Solution", false);
            for(int i = 0; i < solution.size(); i++){
                series.add((int)cityData.get(0).get(solution.get(i)), (int)cityData.get(1).get(solution.get(i)));
            }
            newDataset.addSeries(series);
            annot = new XYTextAnnotation("Start", (double)series.getX(0), (double)series.getY(0) + 100);
            scatterplot.setDataset(newDataset);
            scatterplot.addAnnotation(annot);
            renderer.setSeriesLinesVisible(0, true);
            scatterplot.setRenderer(renderer);
            setContentPane(panel);
        }

        private XYDataset createDataset(ArrayList<ArrayList> dataArray){
            XYSeriesCollection dataset = new XYSeriesCollection();
            XYSeries series = new XYSeries("Cities", false);
            ArrayList<Integer> xCoord = dataArray.get(0);
            ArrayList<Integer> yCoord = dataArray.get(1);
            for(int i = 0; i < xCoord.size(); i++){
                series.add(xCoord.get(i), yCoord.get(i));
            }
            dataset.addSeries(series);
            return dataset;
        }
    }
}