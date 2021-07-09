import java.io.BufferedReader;

import java.io.BufferedWriter;

import java.io.FileReader;

import java.io.FileWriter;

import java.util.Hashtable;

import java.util.LinkedList;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.algorithms.scoring.PageRank;

import edu.uci.ics.jung.algorithms.scoring.PageRankWithPriors;

import edu.uci.ics.jung.graph.DirectedSparseGraph;

import edu.uci.ics.jung.graph.Graph;

import edu.uci.ics.jung.graph.util.EdgeType;

public class Hub_Authority_Score_using_HITS

{
    
    double [][] weigth = new double[10][10];
    int [] prior = new int[10];

    public void calculate_HITS()

 { 

         for(int i=0;i<10;i++)
        
         {
        
         for(int j=0;j<10;j++)
        
         {
        
             weigth[i][j]=0.0000000;
    
        }

    }

 weigth[0][1] = 1;

 weigth[0][2] = 1;

 weigth[0][3] = 1;

 

 weigth[1][0] = 1;

 weigth[1][2] = 1;

 weigth[1][8] = 1;

 

 weigth[2][1] = 1;

 weigth[2][2] = 1;

 weigth[2][9] = 1;

 

 weigth[3][1] = 1;

 weigth[3][4] = 1;

 weigth[3][5] = 1;

 

 weigth[4][3] = 1;

 weigth[4][5] = 1;

 weigth[4][9] = 1;

 

 weigth[5][3] = 1;

 weigth[5][4] = 1;

 weigth[5][6] = 1;

 

 weigth[6][5] = 1;

 weigth[6][7] = 1;

 weigth[6][8] = 1;

 

 weigth[7][6] = 1;

 weigth[7][8] = 1;

 weigth[7][9] = 1;

 

 weigth[8][1] = 1;

 weigth[8][6] = 1;

 weigth[8][7] = 1;

 

 weigth[9][2] = 1;

 weigth[9][4] = 1;

 weigth[9][7] = 1;

 

prior[0]=1;

prior[1]=0;

prior[2]=0;

prior[3]=0;

prior[4]=0;

prior[5]=1;

prior[6]=0;

prior[7]=0;

prior[8]=0;

prior[9]=0;

   

 Graph<Integer, String> g = new DirectedSparseGraph<Integer, String> ();

 for(int i=0;i<10;i++)

 {

 g.addVertex(i);

 }

 

 for(int r=0;r<10;r++)

 {

 for(int c=0;c<10;c++)

 {

 if(weigth[r][c]>0.0000000)

 {

 String tmp_edge = r+"->"+c;

 g.addEdge(tmp_edge, r, c, EdgeType.DIRECTED);

 }

 }

 }

    Transformer<String, Double> edge_weigths = 

            new Transformer<String, Double>()

            {

         @Override

                 public Double transform(String e) 

                 {

                     String[] split = e.split("->");           

                     return weigth[Integer.parseInt(split[0])][Integer.parseInt(split[1])];

                 }           

            };

  Transformer<Integer, Double> vertex_prior = 

          new Transformer<Integer, Double>()

          {            

       @Override

               public Double transform(Integer v) 

               {                        

                   return (double) prior[v];            

               }           

          };

                 

                   //Use JUNG library to calculate Hub and Authority score using HITS

    

  HITS<Integer, String> HTS = new HITS<Integer, String>(g, edge_weigths, 0.85);

  HTS.getVertexPriors();

  HTS.setMaxIterations(30);

  HTS.evaluate();

  System.out.println("HITS SCORE => # of iterations used "+PR.getIterations());

  System.out.println("Vertex -0 "+HTS.getVertexScore(0));

  System.out.println("Vertex -1 "+HTS.getVertexScore(1));

  System.out.println("Vertex -2 "+HTS.getVertexScore(2));

  System.out.println("Vertex -3 "+HTS.getVertexScore(3));

  System.out.println("Vertex -4 "+HTS.getVertexScore(4));

  System.out.println("Vertex -5 "+HTS.getVertexScore(5));

  System.out.println("Vertex -6 "+HTS.getVertexScore(6));

  System.out.println("Vertex -7 "+HTS.getVertexScore(7));

  System.out.println("Vertex -8 "+HTS.getVertexScore(8));

  System.out.println("Vertex -9 "+HTS.getVertexScore(9));

 }

 public static void main(String[] args) 

 { 

    Hub_Authority_Score_using_HITS   hits1 = new Hub_Authority_Score_using_HITS();

    hits1.calculate_HITS();

    System.out.println("Operation complete");

 }

}