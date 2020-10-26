//Priyank_1297953_Sivaram_1299026

# ACO-for-TSP
Please download the JFreeChart package for Java using the link below:  
https://repo1.maven.org/maven2/org/jfree/jfreechart/  
  
Usage: java problem \<filename\> \<numberOfThreads\>  
Here \<filename\> would be one of the *.tsp.txt files provided in the repository and \<numberOfThreads\> is the number of 2 ant sub-colonies for the fine-grained parallelised ant colony.  
Initially a 2-D scatter plot will be displaying the cities in the dataset.   When the mouse is hovered over the points, the cities' index will be displayed.  
The algorithm starts running when the left mouse button is clicked on the plot.  
The console window would display the initial solutions and the improvements.  
In the end, the console window would display the final solution fitness and the plot will show the route taken.