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
/**
 * Write a description of class PHRA here.
 *
 * @author (your name)
 * @version (a version number or a date)
 */
public class JockeyStatistics
{
    // instance variables - replace the example below with your own
    String date;
    String sNumOfHorses;
    int totalRacesProcessed = 0;
    int priorRaceNum;
    int totalDataPointsProcessed;
    String raceNum;
    int pageCount = 0;
   
    //Changes depending on number of params being evaluated
    int NUMBEROFPARAMS = 25;
    double TQP = 0.0;
    
    //Most Important ArryaList
    ArrayList<ArrayList<String>> formattedStringData = new ArrayList<ArrayList<String>>();
    
    //All Jockeys
    ArrayList<String> allJockeys = new ArrayList<String>();
    ArrayList<Integer> appearances = new ArrayList<Integer>();
    ArrayList<ArrayList<Integer>> placements = new ArrayList<ArrayList<Integer>>();
    
    //Quality Poinys
    ArrayList<Double> QualityPoints = new ArrayList<Double>();
    ArrayList<Double> AverageQualityPoints = new ArrayList<Double>();
    ArrayList<Double> NetNuetralQualityPoints = new ArrayList<Double>();
    ArrayList<Double> AverageNetNuetralQualityPoints = new ArrayList<Double>();
    boolean toggleFinalRank = true;
    
    //Placement History
    ArrayList<ArrayList<Integer>> recordHistory = new ArrayList<ArrayList<Integer>>();
    
    //W/P/S
    ArrayList<Integer> Wins = new ArrayList<Integer>();
    ArrayList<Integer> Place = new ArrayList<Integer>();
    ArrayList<Integer> Show = new ArrayList<Integer>();
    ArrayList<Integer> WPS = new ArrayList<Integer>();
    
    //Over the past n races
    ArrayList<String> past3Str     = new ArrayList<String>();
    ArrayList<String> past5Str     = new ArrayList<String>();
    ArrayList<String> past10Str    = new ArrayList<String>();
    
    
    /**
     * Constructor for objects of class PHRA
     */
    public JockeyStatistics()
    {
        // initialise instance variables
    }

    public static void main() throws IOException 
    {
        //String FILE = "AQU Jan'18-Feb'20"; 
        String FILE = "3-1-20 AQU";
        
        JockeyStatistics demo = new JockeyStatistics();
        demo.run(FILE);
    }
    
    public void run(String fileExt) throws IOException 
    {
        System.out.println("Processing Jockey Statistics:");
        File myFile = new File("C:/Users/natha/Desktop/ML/AQU Testing/" + fileExt + ".pdf");
        
        String text;
        String[] pages;
        int numPages;
        try (PDDocument doc = PDDocument.load(myFile)) 
        {
            System.out.println("    - Processing pdf files");
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

            //System.out.println("Text size: " + text.length() + " Pages: " + numPages);
            //System.out.println(""); System.out.println("");
        }
        
        //Evaluates Each Document Seperately
        ArrayList<ArrayList<String>> values = new ArrayList<ArrayList<String>>();
        String currentRace;
        int min = 0;
        int lim = numPages+1;
        
        int numOHorsers = -1;
        //Flips through each race
        
        for (pageCount = 1; pageCount < lim; pageCount++)
        {
            //Temp ArrayList to collect Values of each race/page
            ArrayList<String> tempArr = new ArrayList<String>();
            
            currentRace = pages[pageCount];
            String[] lines = currentRace.split("\n");
            boolean isRaceData = checkDocument(lines);
            if(isRaceData == true)
            {
                //Race Specific Data/General Data
                date = retrieveDate(lines);
                raceNum = raceNumber(lines);
                
                //General Data: Date, Race Number, Number Of Horses
                ArrayList<String> generalData = new ArrayList<String>();
                generalData.add(date); generalData.add(raceNum);
                
                numOHorsers = racerData(lines);
                String numHorses = sNumOfHorses; generalData.add(numHorses);
            }
        } 
        QPCalcs();
        processWPS(allJockeys.size());
        processPastPerf();
        printData();
        printRankedByQP();
        //System.out.println("Waiting to Write to CSV");
        formatData();
        writeToCSV();
        //System.out.println("Number of Races Processed: " + totalRacesProcessed);
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
    
    public void formatData()
    {
        for(int i = 0; i < allJockeys.size(); i++)
        {
            ArrayList<String> data = new ArrayList<String>();
            
            //data.add(date); data.add(raceNum);
            double winPercent = (10000 * Wins.get(i))/appearances.get(i);
            //winPercent = winPercent * 100;
            int iWinPercent = (int) winPercent;
            //System.out.println("Win %: " + winPercent);

            data.add(allJockeys.get(i)); data.add(String.valueOf(appearances.get(i))); data.add(String.valueOf(Wins.get(i))); data.add(String.valueOf(Place.get(i)));
            data.add(String.valueOf(Show.get(i))); 
            data.add(String.valueOf((10000 * Wins.get(i))/appearances.get(i))); data.add(String.valueOf((10000 * WPS.get(i))/appearances.get(i)));
            
            //Points
            data.add(String.valueOf(QualityPoints.get(i))); data.add(String.valueOf(AverageQualityPoints.get(i))); data.add(String.valueOf(NetNuetralQualityPoints.get(i)*1000));
            data.add(String.valueOf(AverageNetNuetralQualityPoints.get(i)*1000));
            //
            data.add(past3Str.get(i)); data.add(past5Str.get(i)); data.add(past10Str.get(i)); 
            
            formattedStringData.add(data);
        }
        
        /*
        System.out.println("Attempting To Rank");
        ArrayList<ArrayList<String>> rank = new ArrayList<ArrayList<String>>();
        ArrayList<ArrayList<String>> temp = formattedStringData;
        int max = 0;
        int index = -1;
        int count = 1;
        while(temp.size() != 0)
        {
            for(int i = 0; i < temp.size(); i++)
            {
                if(Integer.valueOf(temp.get(i).get(2)) >= max)
                {
                    max = Integer.valueOf(temp.get(i).get(2));
                    index = i;
                }
            }
            rank.add(temp.get(index));
            System.out.println(count + ": " + temp.get(index).get(0));
            temp.remove(index);
            count++;
        }
        */
        
        
        
        
    }
    
    public void writeToCSV()
    {
        try (PrintWriter writer = new PrintWriter(new File("Jockey Statistics.csv"))) 
        {
            System.out.println("    - Writing to CSV");
            StringBuilder sb = new StringBuilder();
            
            //VARIABLES
            //sb.append("date,"); sb.append("raceNum,");
            //Track Vars
            sb.append("JockeyName,"); sb.append("NumOfRaces,"); sb.append("Win,"); 
            sb.append("Place,"); sb.append("Show,"); sb.append("WinPercent,"); //sb.append("Win % (x10000),"); 
            sb.append("Top3Percent,"); // sb.append("Top 3 % (x10000),");
            
            //Points
            sb.append("QP,"); sb.append("AQP,"); sb.append("NNQP,"); sb.append("ANNQP,"); //x1000
            
            sb.append("Last3,"); sb.append("Last5,"); sb.append("Last10,");
            
            sb.append('\n');
            //sb.append(',');sb.append("Name");
            
            //Cycles through each horses data
            //int size = formattedDoubleData.get(0).size() + formattedStringData.get(0).size();
            for(int i = 0; i < formattedStringData.size(); i++)
            {
                int sIndex = 0;
                int dIndex = 0;
                for(int j = 0; j < formattedStringData.get(i).size(); j++)
                {
                    ArrayList<String> sData = formattedStringData.get(i);
  
                    sb.append(sData.get(j));
                    sb.append(',');
 
                }
                sb.append('\n');
            }
           
            /*
            for(int i = 0; i < formattedData.size(); i++)
            {
                ArrayList<String> writeDataStr = formattedData.get(i);
                for(int j = 0; j < writeDataStr.size(); j++)
                {
                    sb.append(writeDataStr.get(j));
                    sb.append(',');
                }
                sb.append('\n');
            }
            */
            writer.write(sb.toString());
            System.out.println("    - Finished Writing to CSV (Jockey Statistics)");
            //System.out.println("Finished Writing to Jockey Statistics.CSV");
        } catch (FileNotFoundException e) {
          System.out.println(e.getMessage());
        }
    }
    
    public void processPastPerf()
    {
        for(int i = 0; i < recordHistory.size(); i++)
        {
            //System.out.println(allJockeys.get(i) + ":  ");
            ArrayList<Integer> history = recordHistory.get(i);
            
            String past3 = "";
            String past5 = "";
            String past10 = "";
            
            //Past 3
            if(history.size() >= 3)
            {
                for(int j = 0; j < 3; j++)
                {
                    //System.out.print(history.get(i) + "   ");
                    int index = (history.size()-3) + j;
                    //System.out.print(recordHistory.get(i).get(index) + "   ");
                    
                    String place = String.valueOf(recordHistory.get(i).get(index));
                    
                    if(j == 0)
                    {
                        past3 = past3 + place;
                    }else{
                        past3 = past3 + "$" + place;
                    }
                }
            }else{
                ArrayList<String> temp = new ArrayList<String>();
                for(int j = 0; j < history.size(); j++)
                {
                    temp.add(String.valueOf(history.get(j)));
                }
                
                while(temp.size() < 3)
                {
                    temp.add(0, "u");
                }

                for(int j = 0; j < temp.size(); j++)
                {
                    if(j == 0)
                    {
                        past3 = past3 + temp.get(j);
                    }else{
                        past3 = past3 + "$" + temp.get(j);
                    }
                }
            }
            //System.out.println(past3);
            
            //Past 5
            if(history.size() >= 5)
            {
                for(int j = 0; j < 5; j++)
                {
                    //System.out.print(history.get(i) + "   ");
                    int index = (history.size()-5) + j;
                    //System.out.print(recordHistory.get(i).get(index) + "   ");
                    
                    String place = String.valueOf(recordHistory.get(i).get(index));
                    
                    if(j == 0)
                    {
                        past5 = past5 + place;
                    }else{
                        past5 = past5 + "$" + place;
                    }
                }
            }else{
                ArrayList<String> temp = new ArrayList<String>();
                for(int j = 0; j < history.size(); j++)
                {
                    temp.add(String.valueOf(history.get(j)));
                }
                
                while(temp.size() < 5)
                {
                    temp.add(0, "u");
                }

                for(int j = 0; j < temp.size(); j++)
                {
                    if(j == 0)
                    {
                        past5 = past5 + temp.get(j);
                    }else{
                        past5 = past5 + "$" + temp.get(j);
                    }
                }
            }
            //System.out.println(past5);
            
            //Past 10
            if(history.size() >= 10)
            {
                for(int j = 0; j < 10; j++)
                {
                    //System.out.print(history.get(i) + "   ");
                    int index = (history.size()-10) + j;
                    //System.out.print(recordHistory.get(i).get(index) + "   ");
                    
                    String place = String.valueOf(recordHistory.get(i).get(index));
                    
                    if(j == 0)
                    {
                        past10 = past10 + place;
                    }else{
                        past10 = past10 + "$" + place;
                    }
                }
            }else{
                ArrayList<String> temp = new ArrayList<String>();
                for(int j = 0; j < history.size(); j++)
                {
                    temp.add(String.valueOf(history.get(j)));
                }
                
                while(temp.size() < 10)
                {
                    temp.add(0, "u");
                }

                for(int j = 0; j < temp.size(); j++)
                {
                    if(j == 0)
                    {
                        past10 = past10 + temp.get(j);
                    }else{
                        past10 = past10 + "$" + temp.get(j);
                    }
                }
            }
            //System.out.println(past10);
            
            //
            past3Str.add(past3);
            past5Str.add(past5);
            past10Str.add(past10);
        }
    }
    
    public void QPCalcs()
    {
        for(int i = 0; i < QualityPoints.size(); i++)
        {
            double aQP = QualityPoints.get(i)/appearances.get(i);
            AverageQualityPoints.add(aQP);
        }
        
        for(int i = 0; i < NetNuetralQualityPoints.size(); i++)
        {
            double aNNQP = NetNuetralQualityPoints.get(i)/appearances.get(i);
            AverageNetNuetralQualityPoints.add(aNNQP);
        }
    }
    
    public void printData()
    {   
        //System.out.println("Print Data");
        //System.out.println("Write Data");
        for(int i = 0; i < allJockeys.size(); i++)
        { 
            //System.out.println(allJockeys.get(i) + "    Number of Appearances: " + appearances.get(i));
            //System.out.println("Total QP : " + QualityPoints.get(i) + "    Average QP: " + AverageQualityPoints.get(i) + "      Nuetral QP: " + NetNuetralQualityPoints.get(i));
            //System.out.println("Wins: " + Wins.get(i) + "     Places: " + Place.get(i) + "       Shows: " + Show.get(i));

            //Racing Record
            ArrayList<Integer> record = recordHistory.get(i);
            for(int j = 0; j < record.size(); j++)
            {
                //System.out.print(record.get(j) + " ");
            }
            
            //System.out.println(""); //System.out.println("");
        }

        //System.out.println(TQP);
    }
    
    public void printRankedByQP()
    {  
        ArrayList<Double> processesNNQP = new ArrayList<Double>();
        for (int i = 0; i < NetNuetralQualityPoints.size(); i++)
        {
            processesNNQP.add(NetNuetralQualityPoints.get(i));
        }
        
        ArrayList<String> processesJockeys = allJockeys;
        
        ArrayList<String> top5Jocks = new ArrayList<String>();
        ArrayList<Double> top5NNQP = new ArrayList<Double>();
        
        Collections.sort(processesNNQP);
        // Highest Ranked are at back of Array

        //int index = 
        //System.out.println(processesNNQP.size());
        for(int i = 0; i < 10; i++)
        {
            int index = processesNNQP.size() - i - 1;
            //System.out.println(processesNNQP.get(index));
            top5NNQP.add(processesNNQP.get(index));
        }
        
        for(int i = 0; i < top5NNQP.size(); i++)
        {
            for(int j = 0; j < NetNuetralQualityPoints.size(); j++)
            {
                if(top5NNQP.get(i) == NetNuetralQualityPoints.get(j))
                {
                    //System.out.println(j);
                    top5Jocks.add(processesJockeys.get(j));
                }
            }
        }
        
        for(int i = 0; i < top5NNQP.size(); i++)
        {
            //System.out.println((i+1) + ". " + top5Jocks.get(i) + "      NNQP: " + top5NNQP.get(i));
        }
        
        
        //System.out.println((i+1) + ". " + rankedJockeys.get(i) + "      NNQP: " + NNQP.get(i));
    }
    
    public void initWPS(int numJ)
    {
        for(int i = 0; i < numJ; i++)
        {
            Wins.add(0);
            Place.add(0);
            Show.add(0);
            WPS.add(0);
        }
    }
    
    public void processWPS(int numJ)
    {
        initWPS(numJ);
        for(int i = 0; i < recordHistory.size(); i ++)
        {
            ArrayList<Integer> racersHistory = recordHistory.get(i);
            
            for(int j = 0; j < racersHistory.size(); j++)
            {
                int place = racersHistory.get(j);
                
                //Win Place SHow Accumulator
                if(place == 1 || place == 2 || place == 3)
                {
                    WPS.set(i, WPS.get(i) + 1);
                }
                
                if(place == 1)
                {
                    Wins.set(i, Wins.get(i) + 1);
                }else{
                    if(place == 2)
                    {
                        Place.set(i, Place.get(i) + 1);
                    }else{
                        if(place == 3)
                        {
                            Show.set(i, Show.get(i) + 1);
                        }
                    }
                }
            }
        }
    }
   
    public void refreshJockeyList(String currJockey, double place, double numberOfRaceHorses)
    {
        boolean evenDis = false;
        double QPWEIGHTING = 1;
        //double QPWEIGHTING2 = 1 + (7-(place)+1)/(numberOfRaceHorses);
        
        double QP = (double)(((numberOfRaceHorses - place) + 1)/numberOfRaceHorses) * QPWEIGHTING;
        if(allJockeys.size() != 0 )
        {
            boolean tripped = false;
            int index = -1;
            for(int i = 0; i < allJockeys.size(); i++)
            {
                if(currJockey.equals(allJockeys.get(i)))
                {
                    tripped = true;
                    index = i;
                }
            }
            
            if(tripped == false)
            {
                allJockeys.add(currJockey);
                appearances.add(1);
                QualityPoints.add(QP);
                
                //Net Neutral Quality Points
                NetNuetralQualityPoints.add(determineQualityPoints(place, numberOfRaceHorses));
                ArrayList<Integer> racePos = new ArrayList<Integer>(); racePos.add((int)place);
                recordHistory.add(racePos);
            }else{
                appearances.set(index,appearances.get(index)+1);
                double iQP = QualityPoints.get(index);
                double tQP = iQP + QP;
                QualityPoints.set(index,tQP);
                NetNuetralQualityPoints.set(index, NetNuetralQualityPoints.get(index) + determineQualityPoints(place, numberOfRaceHorses));
                updateRecord(currJockey, (int)place);
            }
        }else{
            allJockeys.add(currJockey);
            appearances.add(1);
            QualityPoints.add(QP);
            NetNuetralQualityPoints.add(determineQualityPoints(place, numberOfRaceHorses));
            
            ArrayList<Integer> racePos = new ArrayList<Integer>(); racePos.add(1);
            recordHistory.add(racePos);
        }
        
    }
    
    public void updateRecord(String j, int place)
    {
        int index = -1;
        for(int i = 0; i < allJockeys.size(); i++)
        {
            if(j.equals(allJockeys.get(i)))
            {
                index = i;
            }
        }
        
        recordHistory.get(index).add(place);
    }
    
    public double determineQualityPoints(double place, double numberOfRaceHorses)
    {
        double NeutralQP = -1;
        double QPWEIGHTING = 1.0;
        double mid = numberOfRaceHorses/2;
        if((numberOfRaceHorses % 2 == 0)) 
        {
            if(place <= mid)
            {
                NeutralQP = (((mid) - place + 1)/numberOfRaceHorses) * QPWEIGHTING;
            }else{
                if(place > (numberOfRaceHorses/2))
                {
                    NeutralQP = (((place-mid) / numberOfRaceHorses) * -1) * QPWEIGHTING;
                }
            }
        }else{
            if(place < mid)
            {
                NeutralQP = (((mid + 1) - place)/numberOfRaceHorses) * QPWEIGHTING;
            }else{
                if(place > (numberOfRaceHorses/2 + 1))
                {
                    NeutralQP = (((place-numberOfRaceHorses/2) / numberOfRaceHorses) * -1) * QPWEIGHTING;
                }else{
                    NeutralQP = 0;
                }
            }
        }
        TQP = NeutralQP + TQP;
        //NeutralQP = Math.round(NeutralQP, 3);
        //System.out.println(NeutralQP);
        return NeutralQP;
    }
    
    public int racerData(String[] lines)
    {
        int lIndex = -1;
        int uIndex = -1;
        ArrayList<ArrayList<String>> horses = new ArrayList<ArrayList<String>>();
        String[] dataLinePieces;
        
        //Determines Lower Index Bound
        for(int i = 0; i < 30; i++)
        {
            dataLinePieces = lines[i].split(" ");
            //System.out.println(lines[i]);
            for(int j = 0; j < dataLinePieces.length; j++)
            {
                //System.out.println(dataLinePieces[j]);
                if(dataLinePieces[j].equals("Wgt"))
                {
                    //Offset of one
                    lIndex = i+1;
                }
            }
        }
        //First Place Horse
        //System.out.println(lines[lIndex]);
        
        //Deterermine Upper Index Bound
        for(int i = lIndex; i < lIndex+15;i++)
        {
            dataLinePieces = lines[i].split(" ");
            //System.out.println(lines[i]);
            for(int j = 0; j < dataLinePieces.length; j++)
            {
                //System.out.println(dataLinePieces[j]);
                if(dataLinePieces[j].equals("Fractional"))
                {
                    //Offset of one
                    //System.out.println("FLAGGED");
                    //System.out.println(i); System.out.println(lines[i]);
                    uIndex = i-1;
                }
            }
        }
        
        //Last Place Horse
        //System.out.println(lines[uIndex]);
        double numOfHorses = (uIndex-lIndex)+1;
        sNumOfHorses = numOfHorses + "";
        //System.out.println("Number Of Horses: " + numOfHorses);
        try{
            String[] jockeys = new String[(int)numOfHorses];
        }catch (Exception e)
        {
            System.out.println(e);
            showError();
        }
        String[] jockeys = new String[(int)numOfHorses];
        
        String jockey = ""; 
        
        //Cycles through all Jockeys in a race
        ArrayList<String> tempListOfJockeys = new ArrayList<String>();
        double place = 1.0;
        for(int i = lIndex; i < uIndex+1; i++)
        {
            ArrayList<String> indivHorse = new ArrayList<String>();
            dataLinePieces = lines[i].split(" ");
            
            jockey = jockey(dataLinePieces);
            refreshJockeyList(jockey, place, numOfHorses);
            place++;
        }
        
        return (int)numOfHorses;
    }

    
    public String jockey(String[] dataPieces)
    {
        String[] hAndJ = new String[] {"ERROR","ERROR"};
        
        //Jockey's Name (Last Namr, First Name)
        int jIndex1 = -1;
        int jIndex2 = -1;
        for(int i = 2; i < dataPieces.length; i++)
        {
            String[] tempVal = ((dataPieces[i]).split(""));
            if( (tempVal[0]).equals("(") )
            {
                //System.out.println()
                jIndex1 = i;
            }
            
            if( (tempVal[tempVal.length-1]).equals(")") )
            {
                jIndex2 = i+1;
            }
        }
        String jName = "";
        for(int i = jIndex1; i < (jIndex2); i++)
        {
            jName = (jName.split(",")[0]) + "/" + dataPieces[i];
        }
        String[] jNamePieces = jName.split("");
        jName = "";
        for(int i = 2; i < jNamePieces.length-1; i++)
        {
            jName = jName + jNamePieces[i]; 
        }
        //System.out.println(jName);

        if(jName == "" || (jName.split("").length < 2))
        {
            showError();
            System.out.println("ERROR: Name not readable");
            return "ERROR";
        }
        
        return jName;
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
            if(raceNum.equals("1") || raceNum.equals("2") || raceNum.equals("3") || raceNum.equals("4") || raceNum.equals("5") 
            || raceNum.equals("6") || raceNum.equals("7") || raceNum.equals("8") || raceNum.equals("9"))
            {
                if(raceNumPart2.equals("1") || raceNumPart2.equals("2") || raceNumPart2.equals("3") || raceNumPart2.equals("4") || raceNumPart2.equals("5") 
                || raceNumPart2.equals("6")  || raceNumPart2.equals("7") || raceNumPart2.equals("8") || raceNumPart2.equals("9") | raceNumPart2.equals("0"))
                {
                    return (raceNum+raceNumPart2+"");
                }
            }
        }
        
        if(raceNum.equals("1") || raceNum.equals("2") || raceNum.equals("3") || raceNum.equals("4") || raceNum.equals("5") || raceNum.equals("6")  || raceNum.equals("7") ||
        raceNum.equals("8") || raceNum.equals("9") || raceNum.equals("10") || raceNum.equals("11") || raceNum.equals("12") || raceNum.equals("13") || raceNum.equals("14") )
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
        
        try{
            String date = dataLinePieces[1];
        }catch (Exception e){
            System.out.println(e);
            System.out.println(pageCount);
        }
        String date = dataLinePieces[1];
        //System.out.println(date);
        dataLinePieces = date.split(" ");
        
        String day = (dataLinePieces[1].split(","))[0];
        if((day.split("")).length == 1)
        {
            day = "0"+day;
        }else{}
        String year = (dataLinePieces[2].split(" "))[0];
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
        //System.out.println(year);
        //System.out.println(day);
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