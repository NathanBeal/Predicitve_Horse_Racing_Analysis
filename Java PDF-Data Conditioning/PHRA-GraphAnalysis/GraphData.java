import java.util.*;
import java.util.Date;
import java.time.*;
import java.io.*;
import java.util.Calendar;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.io.IOException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
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

    double degreeDiffAverage(int u, double appearances)
    {
        double numberOfEdges = 0;
        double outDegree = 0;
        for(Edge i: G[u])
        {
            //System.out.println(i.w);//Prints Out weights
            if(i.w == 1)
            {
                outDegree++;
                numberOfEdges++;
            }
        }
        
        double inDegree = 0;
        for(Edge i: G[u])
        {
            if(i.w == -1)
            {
                inDegree++;
                numberOfEdges++;
            }
        }
        
        double degreeDifferenceAve = (double)(outDegree - inDegree)/(double) appearances;
        //System.out.println(degreeDifferenceAve + "W: " + outDegree + "  L: " + inDegree +" numEdges: " + numberOfEdges);
        return (degreeDifferenceAve);
    }
    
    void addEdge(int u,int v,int w)
    {
        //Winning edge (Pos weight) from u->v
        G[u].add(0,new Edge(v,w)); 
        
        //Losing edge (Neg weight) from u->v
        G[v].add(0,new Edge(u,(w*-1))); 
    }
    
    String returnEdges(int u)
    {
        String result="";
        for(Edge i: G[u])
        {                
            result += "(" + i.v + "," + i.w + ")  ";
        }
        return result;
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

public class GraphData
{
    int pageCount = 0;
    int numProcessed = 0;
    int totalPagesProcessed = 0;
    int totalRacesProcessed= 0;
    int priorRaceNum;
    
    //CONSTANTS
    int numberOfDesiredRaces = 200;
    
    //Race Specific Variables
    String date;
    String raceNum;
    String sNumOfHorses;
    
    ArrayList<String> allHorses = new ArrayList<String>();
    ArrayList<Double> numberOfRacesPerHorse = new ArrayList<Double>();
    String lastHorseAdded = "";
    public GraphData(){}
    
    public static void main() throws IOException 
    {
        GraphData demo = new GraphData();
        //demo.run("TestFileThis1");
        demo.run("AQU 19-20");
    }
    
    public void run(String fileExt) throws IOException
    {
        System.out.println("Developing Training and Validation Set CSV: ");
        File myFile = new File("C:/Users/natha/Desktop/PHRA/" + fileExt + ".pdf");
        
        String text;
        String[] pages;
        int numPages;
        try (PDDocument doc = PDDocument.load(myFile)) 
        {
            PDFTextStripper stripper = new PDFTextStripper();
            text = stripper.getText(doc);
            numPages = doc.getNumberOfPages();
            pages = new String[(numPages+1)];
            
            for(int i = 0; i < numPages; i++)
            {
                stripper.setStartPage(i);
                stripper.setEndPage(i);
                text = stripper.getText(doc);
                pages[i] = text;
            }
            
            //Adds the Last Page of Data to the Pages Array
            stripper.setStartPage(numPages);
            stripper.setEndPage(numPages);
            text = stripper.getText(doc);
            pages[numPages] = text;
        }
        
        //Evaluates Each Document Seperately
        String currentRace;
        int min = 0;
        int lim = numPages+1;
        ArrayList<ArrayList<ArrayList<String>>> raceMatchups = new ArrayList<ArrayList<ArrayList<String>>>();
        for (pageCount = 1; pageCount < lim; pageCount++)
        {
            //Temp ArrayList to collect Values of each race/page
            ArrayList<String> tempArr = new ArrayList<String>();
            
            currentRace = pages[pageCount];
            String[] lines = currentRace.split("\n");
            
            boolean isRaceData = checkDocument(lines);
            if(isRaceData == true)
            {
                totalPagesProcessed++;
                
                //General info
                date = retrieveDate(lines);
                raceNum = raceNumber(lines);
                String trackType = trackType(lines);
                String distance = distance(lines);                    
                
                //Horse Data
                ArrayList<ArrayList<String>> edgeMatchups = racerData(lines);
                raceMatchups.add(edgeMatchups);

                if(Integer.valueOf(raceNum) != 1)
                {
                    priorRaceNum =  Integer.valueOf(raceNum);
                }else{
                    if(Integer.valueOf(raceNum) == 1 && pageCount > 2)
                    {
                        totalRacesProcessed = totalRacesProcessed + priorRaceNum;
                    }
                }
            
                //Preps Data to Write to CSV
                if(pageCount > (lim-numberOfDesiredRaces))
                {
                    //dataPrep(numHorses, generalData, racerData);
                    numProcessed++;
                }
            }
        } 
        System.out.println("");System.out.println("");
        
        //Handling the Graph
        int graphSize = allHorses.size();
        Graph g = new Graph(graphSize);
        for(int i = 0; i < raceMatchups.size(); i++)
        {
            ArrayList<ArrayList<String>> singularRaceMatchupData = raceMatchups.get(i);
            for(int j = 0; j < singularRaceMatchupData.size(); j++)
            {
                ArrayList<String> horseMatchups = singularRaceMatchupData.get(j);
                String keyHorse = horseMatchups.get(0);
                
                int uIndex = -1;
                int vIndex = -1;
                findHorseUIndex:
                for(int x = 0; x < allHorses.size(); x++)
                {
                    if(allHorses.get(x).equals(keyHorse))
                    {
                        uIndex = x;
                        break findHorseUIndex;
                    }
                }
                
                for(int k = 1; k < horseMatchups.size(); k++)
                {
                    String losingHorse = horseMatchups.get(k);
                    //System.out.print(losingHorse + " ");
                    findVIndex:
                    for(int y = 0; y < allHorses.size(); y++)
                    {
                        if(allHorses.get(y).equals(losingHorse))
                        {
                            vIndex = y;
                            break findVIndex;
                        }
                    }
                    g.addEdge(uIndex,vIndex,1);
                }
                //System.out.println("");
            }
        }
        
        ArrayList<Double> averages = new ArrayList<Double>();
        for(int i = 0; i < graphSize; i++)
        {
            //System.out.print(allHorses.get(i) + ": ");
            //System.out.println("AVE: " + g.degreeDiffAverage(i, numberOfRacesPerHorse.get(i)) );
            //System.out.println(g.returnEdges(i));
            averages.add(g.degreeDiffAverage(i, numberOfRacesPerHorse.get(i)));
            //System.out.println("Appearances: " + numberOfRacesPerHorse.get(i));
        }
        
        //
        ArrayList<Double> tempAverages = new ArrayList<Double>();
        ArrayList<ArrayList<String>> horseAndAves = new ArrayList<ArrayList<String>>();
        for(int i = 0; i < graphSize; i++)
        {
            ArrayList<String> temp = new ArrayList<String>();
            temp.add(allHorses.get(i));
            temp.add(Integer.toString((int)(averages.get(i) * 1000)));
            tempAverages.add(averages.get(i));
            horseAndAves.add(temp);
        }
        Collections.sort(tempAverages);
        Collections.reverse(tempAverages);
        
        //Organized Order
        int place = 1;
        int numberOfplacesShown = tempAverages.size();
        for(int i = 0; i < 25; i++)
        {
            here:
            for(int j = 0; j < horseAndAves.size(); j++)
            {
                int value = Integer.valueOf(horseAndAves.get(j).get(1));
                int sortedValues = (int)(1000*tempAverages.get(i));
                if(sortedValues == value)
                {
                    System.out.println(place + ": " + horseAndAves.get(j).get(0) + ", " + tempAverages.get(i));
                    horseAndAves.remove(j);
                    place++;
                    break here;
                }
            }
        }
        
        
        //Final Ranking
        /*
        //int place = 1;
        while(tempAverages.size() > 0)
        {
            int maxIndex = -1;
            for(int i = 0; i < tempAverages.size(); i++)
            {
                double maxVal = -100000;
                if(tempAverages.get(i) >= maxVal)
                {
                    maxVal = tempAverages.get(i);
                    maxIndex = i;
                }
            }
            System.out.println(place + ": " + "");
            tempAverages.remove(maxIndex);
            
        }
        */
        
        
        
        
        
        //writeToCSV();
        //dataLog();

        System.out.println("Races Processed: " + totalPagesProcessed);
        System.out.println("Number of races output: " + numProcessed);
    }
    
    public ArrayList<ArrayList<String>> racerData(String[] lines)
    {
        int lIndex = -1;
        int uIndex = -1;
        String[] dataLinePieces;
        String dist = distance(lines);
        
        //Determines Lower Index Bound
        weightLoop:
        for(int i = 0; i < lines.length; i++)
        {
            dataLinePieces = lines[i].split(" ");
            for(int j = 0; j < dataLinePieces.length; j++)
            {
                //System.out.println(dataLinePieces[j]);
                if(dataLinePieces[j].equals("Wgt"))
                {
                    //Offset of one
                    lIndex = i+1;
                    break weightLoop;
                }
            }
        }
        
        //Deterermine Upper Index Bound
        determineBound:
        for(int i = lIndex; i < lIndex+20;i++)
        {
            dataLinePieces = lines[i].split(" ");
            for(int j = 0; j < dataLinePieces.length; j++)
            {
                if(dataLinePieces[j].equals("Fractional"))
                {
                    uIndex = i-1;
                    break determineBound;
                }
            }
        }

        secondaryFilter:
        if(uIndex == -1)
        {
            for(int i = lIndex; i < lIndex+20;i++)
            {
                dataLinePieces = lines[i].split(":");
                for(int j = 0; j < dataLinePieces.length; j++)
                {
                    if(dataLinePieces[j].equals("Final Time"))
                    {
                        uIndex = i-1;
                        break secondaryFilter;
                    }
                }
            }
        }
        
        int numOfHorses = (uIndex-lIndex)+1;
        String horse;
        ArrayList<String> currentRaceHorses = new ArrayList<String>();
        
        for(int i = lIndex; i < uIndex+1; i++)
        {
            dataLinePieces = lines[i].split(" ");
            horse = horse(dataLinePieces); 
            currentRaceHorses.add(horse);
        }
        
        //Creates ArrayList of horses a specific horse beat in the race
        ArrayList<ArrayList<String>> wonAgainst = new ArrayList<ArrayList<String>>();
        for(int i = 0; i < currentRaceHorses.size(); i++)
        {
            ArrayList<String> tempEdges = new ArrayList<String>();
            for(int j = i; j < currentRaceHorses.size(); j++)
            {
                tempEdges.add(currentRaceHorses.get(j));
            }
            wonAgainst.add(tempEdges);
        }
        
        //Prints out horses in race and who theyn won against 
        for(int i = 0; i < currentRaceHorses.size(); i++)
        {
            //System.out.print(currentRaceHorses.get(i) + ": ");
            ArrayList<String> tempEdges = new ArrayList<String>();
            for(int j = 0; j < wonAgainst.get(i).size(); j++)
            {
                //System.out.print(wonAgainst.get(i).get(j) + ", ");
            }
            //System.out.println("");
        }
        
        //Double checks to see if its a new horse and then adds if horse not in records
        for(int i = 0; i < currentRaceHorses.size(); i ++)
        {
            boolean newHorse = true;
            for(int j = 0; j < allHorses.size(); j++)
            {
                if(allHorses.get(j).equals(currentRaceHorses.get(i)))
                {
                    newHorse = false;
                    double appearances = numberOfRacesPerHorse.get(j) + 1.0;
                    numberOfRacesPerHorse.set(j, appearances);
                }
            }
            if(newHorse == true)
            {
                allHorses.add(currentRaceHorses.get(i));
                numberOfRacesPerHorse.add(1.0);
            }
        }
        
        return wonAgainst;
    }
    
    public boolean checkDocument(String[] lines)
    {
        boolean firstLineBool = false;
        boolean tripOne = false;
        int dashCount = 0;
        String firstLine = lines[0];
        String[] firstLineSegs = firstLine.split(" ");
        for(int i = 0; i < firstLineSegs.length; i++)
        {
            //System.out.print(firstLineSegs[i] + ",");
            if(firstLineSegs[i].equals("Race"))
            {
                tripOne = true;
            }
            if(firstLineSegs[i].equals("-"))
            {
                dashCount++;
            }
        }
        if(dashCount >= 2 && tripOne == true)
        {
            return true;
        }
        
        return false;
    }
    
    public String horse(String[] dataPieces)
    {
        String[] hAndJ = new String[] {"ERROR","ERROR"};
        
        //Jockey's Name (Last Namr, First Name)
        int jIndex1 = -1;
        for(int i = 2; i < dataPieces.length; i++)
        {
            String[] tempVal = ((dataPieces[i]).split(""));
            if( (tempVal[0]).equals("(") )
            {jIndex1 = i;}
        }
        
        //Horse Name
        String hName = "";
        for(int i = 3; i < jIndex1; i++)
        {
            //System.out.println(dataPieces[i]);
            
            if(hName == "")
            {
                hName = hName + dataPieces[i];
            }else{
                hName = hName + " " + dataPieces[i];
            }
        }
        
        if(hName == "" && dataPieces[0].equals("---"))
        {
            hName = dataPieces[2];
        }
        //System.out.println("Horse: " + hName + "  Jockey: " + jName);
        
        if(hName == "")
        {
            return "ERROR";
        }
        
        return hName;
    }
    
    public String distance(String[] lines)
    {
        int index = -1;
        String[] dataLinePieces;
        findRecord:
        for(int i = 0; i < lines.length; i++)
        {
            dataLinePieces = lines[i].split(" ");
            //System.out.println(lines[i]);
            for(int j = 0; j < dataLinePieces.length-1; j++)
            {
                //System.out.println(dataLinePieces[j]);
                if(dataLinePieces[j].equals("Record:"))
                {
                    //Index is offset by 2, for 2 lines
                    index = i;
                    break findRecord;
                }
            }
        }
        
        //System.out.println(lines[index]);
        dataLinePieces = lines[index].split(" On The ");
        dataLinePieces = dataLinePieces[0].split(" ");
        
        //System.out.println(dataLinePieces[0]);
        String sDist = "";
        
        /* Could not return a string value with a . or / so multiplied distance by x10000 */
        if(dataLinePieces[0].equals("Four") && dataLinePieces[1].equals("And") 
        && dataLinePieces[2].equals("One") && dataLinePieces[3].equals("Half") 
        && dataLinePieces[4].equals("Furlongs"))
        {return "5625";}
        if(dataLinePieces[0].equals("Five") && dataLinePieces[1].equals("And") 
        && dataLinePieces[2].equals("One") && dataLinePieces[3].equals("Half") 
        && dataLinePieces[4].equals("Furlongs"))
        {return "6875";}
        if(dataLinePieces[0].equals("Six") && dataLinePieces[1].equals("Furlongs"))
        {return "7500";}
        if(dataLinePieces[0].equals("Six") && dataLinePieces[1].equals("And"))
        {return "8125";}
        if(dataLinePieces[0].equals("Seven"))
        {return "8750";}
        if(dataLinePieces[0].equals("One") && dataLinePieces[1].equals("Mile"))
        {return "10000";}
        if(dataLinePieces[0].equals("One") && dataLinePieces[1].equals("And") 
        && dataLinePieces[2].equals("One") && dataLinePieces[3].equals("Sixteenth"))
        {return "10625";}
        if(dataLinePieces[0].equals("One") && dataLinePieces[1].equals("And") 
        && dataLinePieces[2].equals("Three") && dataLinePieces[3].equals("Sixteenth"))
        {return "11875"; }
        if(dataLinePieces[0].equals("One") && dataLinePieces[1].equals("And") 
        && dataLinePieces[2].equals("One") && dataLinePieces[3].equals("Eighth"))
        {return "11250";}
        if(dataLinePieces[0].equals("One") && dataLinePieces[1].equals("And") 
        && dataLinePieces[2].equals("One") && dataLinePieces[3].equals("Fourth"))
        {return "12500";}
        if(dataLinePieces[0].equals("One") && dataLinePieces[1].equals("And") && 
        dataLinePieces[2].equals("Five") && dataLinePieces[3].equals("Sixteenth"))
        {return "13125";}
        if(dataLinePieces[0].equals("One") && dataLinePieces[1].equals("And") && 
        dataLinePieces[2].equals("Three") && dataLinePieces[3].equals("Eighth"))
        {return "13750";}
        if(dataLinePieces[0].equals("One") && dataLinePieces[1].equals("And") && 
        dataLinePieces[2].equals("One") && dataLinePieces[3].equals("Half"))
        {return "15000";}
        if(dataLinePieces[0].equals("One") && dataLinePieces[1].equals("And") && 
        dataLinePieces[2].equals("Five") && dataLinePieces[3].equals("Eighth"))
        {return "16250";}
        if(dataLinePieces[0].equals("One") && dataLinePieces[1].equals("And") && 
        dataLinePieces[2].equals("Three") && dataLinePieces[3].equals("Fourth"))
        {return "17500";}
        if(dataLinePieces[0].equals("About") && dataLinePieces[1].equals("Two") && 
        dataLinePieces[2].equals("And") && dataLinePieces[3].equals("One") 
        &&  dataLinePieces[4].equals("Sixteenth"))
        {return "20500";}
        if(dataLinePieces[0].equals("Two") && dataLinePieces[1].equals("And") && 
        dataLinePieces[2].equals("One") && dataLinePieces[3].equals("Sixteenth"))
        {return "20625";}
        if(dataLinePieces[0].equals("About") && dataLinePieces[1].equals("Two") && 
        dataLinePieces[2].equals("And") && dataLinePieces[3].equals("Three")
        && dataLinePieces[4].equals("Eighth"))
        {return "23000";}
        if(dataLinePieces[0].equals("Two") && dataLinePieces[1].equals("And") && 
        dataLinePieces[2].equals("Three") && dataLinePieces[3].equals("Eighth"))
        {return "23750";}

        showError();
        System.out.print("ERROR: distance() - MISSING DISTANCE VALUE OF:  "); 
        System.out.println(lines[index]);
        return "ERROR";
    }
    
    public String trackType(String[] lines)
    {
        int index = -1;
        String[] dataLinePieces;
        findRecord:
        for(int i = 0; i < lines.length; i++)
        {
            dataLinePieces = lines[i].split(" ");
            //System.out.println(lines[i]);
            for(int j = 0; j < dataLinePieces.length-1; j++)
            {
                //System.out.println(dataLinePieces[j]);
                if(dataLinePieces[j].equals("Record:"))
                {
                    //Index is offset by 2, for 2 lines
                    index = i;
                    break findRecord;
                }
            }
        }
        
        //System.out.println(lines[index]);
        dataLinePieces = lines[index].split(" On The ");
        dataLinePieces = dataLinePieces[1].split(" ");
        
        //Checks Track Type
        String trackType = dataLinePieces[0];
        if(trackType.equals("Dirt"))
        {
            return trackType;
        }
        if(trackType.equals("Turf"))
        {
            return trackType;
        }
        if(trackType.equals("Outer"))
        {
            return trackType;
        }
        if(trackType.equals("Inner"))
        {
            return trackType;
        }
        if(trackType.equals("Hurdle"))
        {
            return trackType;
        }
        
        showError();
        System.out.println("ERROR: trackType() - NEW TRACK TYPE " + trackType);
        return "ERROR";
    }
    
    public String raceNumber(String[] lines)
    {
        String dataLine = lines[0];
        String[] dataLinePieces = dataLine.split("Race ");
        
        dataLinePieces = dataLinePieces[1].split("");
        String raceNum = dataLinePieces[0]; 
        String raceNumPart2 = dataLinePieces[1];
        
        //for(int i = 0; i < dataLinePieces.
        String twoDigRaceNum = "";
        if(dataLinePieces.length == 3)
        {
            //System.out.println("");
            int ascii1 = (int)raceNum.charAt(0);
            int ascii2 = (int)raceNumPart2.charAt(0);
            if(ascii1 >= 49 && ascii1 <= 57)
            {
                if(ascii2 >= 48 && ascii2 <= 57)
                {
                    return (raceNum+raceNumPart2+"");
                }
            }
        }

        if(raceNum.equals("1") || raceNum.equals("2") || raceNum.equals("3") || 
        raceNum.equals("4") || raceNum.equals("5") || raceNum.equals("6")  || 
        raceNum.equals("7") ||raceNum.equals("8") || raceNum.equals("9") || 
        raceNum.equals("10") || raceNum.equals("11") || raceNum.equals("12") || 
        raceNum.equals("13") || raceNum.equals("14") )
        {
            return raceNum;
        }
        
        showError();
        System.out.println("ERROR: raceNumber() - FILTERED NON NUMBER OR RACE NUMBER TOO HIGH. FILTERED VALUE: " + raceNum);
        return "ERROR";
    }
    
    public String retrieveDate(String[] lines)
    {
        String [] dataLinePieces = lines[0].split(" - ");
        
        //System.out.println(dataLinePieces[0]);
        String date = "ERROR";
        try{
            date = dataLinePieces[1];
        }catch(Exception e){
            System.out.println("ERROR: " + e);
            System.out.println("Page #: " + pageCount);
            for(int i = 0; i < dataLinePieces.length; i++)
            {
                System.out.println(dataLinePieces[i]);
            }
        }
        //System.out.println(date);
        dataLinePieces = date.split(" ");
        
        String day = (dataLinePieces[1].split(","))[0];
        if((day.split("")).length == 1)
        {
            day = "0"+day;
        }else{}
        
        //System.out.println(day);
        String year = (dataLinePieces[2].split(" "))[0];
        //System.out.println(year);
        
        String sMonth = dataLinePieces[0];
        String nMonth = "-1";
        if(sMonth.equals("January"))
        {nMonth = "01";}
        if(sMonth.equals("February"))
        {nMonth = "02";}
        if(sMonth.equals("March"))
        {nMonth = "03";}
        if(sMonth.equals("April"))
        {nMonth = "04";}
        if(sMonth.equals("May"))
        {nMonth = "05";}
        if(sMonth.equals("June"))
        {nMonth = "06";}
        if(sMonth.equals("July"))
        {nMonth = "07";}
        if(sMonth.equals("August"))
        {nMonth = "08";}
        if(sMonth.equals("September"))
        {nMonth = "09";}
        if(sMonth.equals("October"))
        {nMonth = "10";}
        if(sMonth.equals("November"))
        {nMonth = "11";}
        if(sMonth.equals("December"))
        {nMonth = "12";}
                    
        if(nMonth =="-1")
        {
            showError();
            System.out.println("ERROR: retrieveDate() - MONTH ERROR, MISSING " + sMonth);
            return "ERROR";
        }
        
        String newDate = day+"/"+nMonth+"/"+year;
        return newDate;
    }
    
    public void showError()
    {
        System.out.println("Page #: " + pageCount + "      Date: " + date + "      Race Number: " 
        + raceNum + "        Number of Horses: " + sNumOfHorses);
    }
}