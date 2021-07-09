import java.util.*;
class Graph{
    class Edge{
        int v,w;
        public Edge(int v,int w)
        {
            this.v=v; this.w=w;
        }
        @Override
        public String toString()
        {
            return "("+v+","+w+")";
        }
    }
    List<Edge> G[];
    public Graph(int n)
    {
        G=new LinkedList[n];
        for(int i=0;i<G.length;i++)
            G[i]=new LinkedList<Edge>();
    }
    boolean isConnected(int u,int v)
    {
        for(Edge i: G[u])
            if(i.v==v) return true;
        return false;
    }
    
    boolean hasEdges(int u)
    {
        for(Edge i: G[u])
        {
            //System.out.println(i.w);//Prints Out weights
            if(i.w >= 1 || i.w <= 1)
            {
                return true;
            }
        }
        return false;
    }
    
    String degreeDiff(int u)
    {
        int outDegree = 0;
        for(Edge i: G[u])
        {
            //System.out.println(i.w);//Prints Out weights
            if(i.w > 1)
            {
                outDegree++;
            }
        }
        
        int inDegree = 0;
        for(Edge i: G[u])
        {
            if(i.w < 1)
            {
                inDegree++;
            }
        }
        
        if(inDegree > 0 || outDegree > 0) //Node has edge
        {
            int degreeDifference = (outDegree - inDegree);
            return Integer.toString(degreeDifference);
        }else{
            return "NO EDGES";
        }
    }

    double degreeDiffAverage(int u)
    {
        int numberOfEdges = 0;
        int outDegree = 0;
        for(Edge i: G[u])
        {
            //System.out.println(i.w);//Prints Out weights
            if(i.w > 1)
            {
                outDegree++;
                numberOfEdges++;
            }
        }
        
        int inDegree = 0;
        for(Edge i: G[u])
        {
            if(i.w < 1)
            {
                inDegree++;
                numberOfEdges++;
            }
        }
        
        double degreeDifferenceAve = ((outDegree - inDegree)/numberOfEdges);
        return (degreeDifferenceAve);
    }
    
    void addEdge(int u,int v,int w)
    {
        //Winning edge (Pos weight) from u->v
        G[u].add(0,new Edge(v,w)); 
        
        //Losing edge (Neg weight) from u->v
        G[v].add(0,new Edge(u,(w*-1))); 
    }
    @Override
    public String toString(){
        String result="";
        for(int i = 0; i < G.length; i++)
        {
            result+=i+": "+G[i]+"\n";
        }
        return result;
    }
}

public class GraphExample 
{
    public static void main() 
    {
        //Size of Graph
        int graphSize = 10;
        Graph g = new Graph(30);
        Random rn = new Random();
        for(int i = 0; i < 10; i++)
        {
            int v = rn.nextInt(graphSize-1);
            int u = rn.nextInt(graphSize-1);
            int w = rn.nextInt(10);
            g.addEdge(u,v,w);
        }

        System.out.println(g);
        System.out.println(g.isConnected(9,3));
        
        System.out.println(g.degreeDiff(0));
        System.out.println(g.degreeDiff(5));
        System.out.println(g.hasEdges(8));
        
        int u = 2;
        int v = 3;
        if(g.hasEdges(u) == true)
        {
            double z = g.degreeDiffAverage(u);
            System.out.println(z);
        }
    }
}