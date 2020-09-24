import java.io.*;
import java.util.*;
import java.awt.Color;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class problem{
    static ArrayList<String> dataArray = new ArrayList<String>();
    public static void main(String[] args){
        if(args.length != 1){
            System.err.println("Usage: java problem <filename>");
        }
        String file = args[0];
        readFile(file);
        dataArray = getCoords(dataArray);
        ArrayList<ArrayList> data = plotCities(dataArray);
        SwingUtilities.invokeLater(() -> {
            Plot plot = new Plot("Cities", data);
            plot.setSize(800,400);
            plot.setLocationRelativeTo(null);
            plot.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            plot.setVisible(true);
        });
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

    private static class Plot extends JFrame{
        private static final long serialVersionUID = 6294689542092367723L;

        public Plot(String title, ArrayList<ArrayList> dataArray){
            super(title);
            XYDataset dataset = createDataset(dataArray);
            JFreeChart chart = ChartFactory.createScatterPlot("Cities for TSP", "X-Axis", "Y-Axis", dataset);
            XYPlot scatterplot = (XYPlot)chart.getPlot();
            scatterplot.setBackgroundPaint(new Color(255,255,255));
            ChartPanel panel = new ChartPanel(chart);
            setContentPane(panel);
        }

        private XYDataset createDataset(ArrayList<ArrayList> dataArray){
            XYSeriesCollection dataset = new XYSeriesCollection();
            XYSeries series = new XYSeries("Cities");
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