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
import java.io.FileWriter;
import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
/**
 * Write a description of class PHRA here.
 *
 * @author (your name)
 * @version (a version number or a date)
 */
public class HorseStats
{
    // instance variables - replace the example below with your own
    String date;
    String sNumOfHorses;
    int totalRacesProcessed = 0;
    int priorRaceNum;
    int totalDataPointsProcessed;
    String raceNum;
    String distance;
    double finalTimeTrackRecordDiff;
    double totalDistBehind = 0.0;
   
    //Changes depending on number of params being evaluated
    int NUMBEROFPARAMS = 25;
    double TQP = 0.0;
    
    //Most Important ArryaList
    ArrayList<ArrayList<String>> formattedStringData = new ArrayList<ArrayList<String>>();
    
    //All Horses
    ArrayList<String> allHorses = new ArrayList<String>();
    ArrayList<Integer> appearances = new ArrayList<Integer>();
    ArrayList<ArrayList<Integer>> placements = new ArrayList<ArrayList<Integer>>();
    ArrayList<ArrayList<String>> distances = new ArrayList<ArrayList<String>>();
    ArrayList<ArrayList<String>> newDistanceEvaluations = new ArrayList<ArrayList<String>>();
    ArrayList<ArrayList<Double>> SPRAT = new ArrayList<ArrayList<Double>>();
    ArrayList<Double> AVESPRAT = new ArrayList<Double>();
    
    //Placement History
    ArrayList<ArrayList<Integer>> recordHistory = new ArrayList<ArrayList<Integer>>();
    
    //W/P/S
    ArrayList<Integer> Wins = new ArrayList<Integer>();
    ArrayList<Integer> Place = new ArrayList<Integer>();
    ArrayList<Integer> Show = new ArrayList<Integer>();
    ArrayList<Integer> WPS = new ArrayList<Integer>();
    
    
    /**
     * Constructor for objects of class PHRA
     */
    public HorseStats()
    {}

    public static void main() throws IOException 
    {
        HorseStats demo = new HorseStats();
        demo.run();
    }
    
    public void run() throws IOException 
    {
        System.out.println("Processing Horse Statistics");
        String fileExt = "3-15-20 AQU"; //"Jan'19-Feb'20"; //"3-15-20 AQU";
        File myFile = new File("C:/Users/natha/Desktop/PHRA/" + fileExt + ".pdf");
        
        String text; String[] pages; int numPages;
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

            System.out.println("Text size: " + text.length() + " Pages: " + numPages);
            System.out.println(""); System.out.println("");
        }
        
        //===============================================================================================================================================================================
        
        //Evaluates Each Document Seperately
        ArrayList<ArrayList<String>> values = new ArrayList<ArrayList<String>>();
        String currentRace;
        int min = 0;
        int lim = numPages+1;
        
        int numOHorsers = -1; String trackRecord;
        //Flips through each race
        for (int i = 1; i < lim; i++)
        {
            
            //Temp ArrayList to collect Values of each race/page
            ArrayList<String> tempArr = new ArrayList<String>();
            
            currentRace = pages[i];
            String[] lines = currentRace.split("\n");
            
            //Race Specific Data/General Data
            date = retrieveDate(lines);
            raceNum = raceNumber(lines);
            finalTimeTrackRecordDiff = retrieveTRFTDifferential(lines);
            //System.out.println("DATE: " + date + "  RaceNum: " + raceNum);
            //General Data: Date, Race Number, Number Of Horses
            ArrayList<String> generalData = new ArrayList<String>();
            generalData.add(date); generalData.add(raceNum);
            
            numOHorsers = racerData(lines);
            String numHorses = sNumOfHorses; generalData.add(numHorses);
            totalDistBehind = 0.0;
        } 
        //printData();
        dataLog();
        processWPS(allHorses.size());
        formatData();
        
        tryMe();
        //letsPlotSomeData();
        writeToCSV();
    }
    
    public double retrieveTRFTDifferential(String [] lines)//Retrieve Final Time and track record differential
    {
        String[] dataLinePieces;
        int index = -1;
        
        doubleloop:
        for(int i = 0; i < 50; i++)
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
                    break doubleloop;
                }
            }
        }
        
        //Track record Index
        
        dataLinePieces = lines[index].split(" On The ");
        dataLinePieces = dataLinePieces[1].split("Track Record");
        dataLinePieces = dataLinePieces[1].split(" - ");
        dataLinePieces = dataLinePieces[1].split(" ");
        //System.out.println("TRACK RECORD: " + dataLinePieces[0]); //Contains Track Record
        String TRACKRECORD = dataLinePieces[0];
        
        //Final Time
        String[] dataPieces;
        int index2 = -1;
        for(int i = 0; i < lines.length-1; i++)
        {
            dataPieces = lines[i].split(" ");
            //System.out.println(lines[i]);
            for(int j = 0; j < dataPieces.length-1; j++)
            {
                //System.out.println(dataLinePieces[j]);
                if(dataPieces[j].equals("Run-Up:"))
                {
                    //Index is offset by 2, for 2 lines
                    index2 = i;
                }
            }
        }
        index2 = index2 - 2;
        dataPieces = lines[index2].split("Final Time: ");
        
        if(dataPieces.length <= 1)
        {
            //System.out.println(dataPieces[0]);
            index2 = index2 + 1;
            dataPieces = lines[index2].split("Final Time: ");
            //System.out.println(dataPieces[1]);
        }
        
        
        dataPieces = dataPieces[1].split(" ");
        //System.out.println("Final Timers: " + dataPieces[0]);
        String FINALTIME = dataPieces[0];
        
        int ftLength = FINALTIME.split("").length;
        int trLength = TRACKRECORD.split("").length;
        
        //System.out.println("Track Record: " + TRACKRECORD + "   Final Time: " + FINALTIME);
        //System.out.println("Track Record length: " + trLength + "   Final Time length: " + ftLength);
        
        double timeDiff = 0.0;
        double trDouble = 0.0; double ftDouble = 0.0;
        if(trLength == 7 && ftLength == 8)
        {
            String[] TRPieces = TRACKRECORD.split(":");
            String[] ftPieces = FINALTIME.split(":");
            
            double TRMin = (Double.valueOf(TRPieces[0]) * 60);
            double TRSec =  Double.valueOf(TRPieces[1].substring(0,2));
            double TRfSec =  ((Double.valueOf(TRPieces[1].substring(3,5)))/100);
            
            double FTMin = (Double.valueOf(ftPieces[0]) * 60);
            double FTSec =  Double.valueOf(ftPieces[1].substring(0,2));
            double FTfSec =  ((Double.valueOf(ftPieces[1].substring(3,5)))/100);
            
            
            trDouble = TRMin + TRSec + TRfSec;
            ftDouble = FTMin + FTSec + FTfSec;
            
            double timeDifferential = ftDouble - trDouble;
            return timeDifferential;
        }
        
        //Sub 1-Minute Races & Records
        if(trLength == 5 && ftLength == 6)
        {
            String[] TRPieces = TRACKRECORD.split(".");
            String[] ftPieces = FINALTIME.split(".");
            
            double TR =  Double.valueOf(TRACKRECORD);
            double FT =  Double.valueOf(FINALTIME);
            
            double timeDifferential = FT - TR;
            //System.out.println("TR :" + TR + "   FT: " + FT + "  TDIFF: " + timeDifferential);
            
            return timeDifferential;
        }
        //Return the Differential
        
        //System.out.println("Track Record Converted: " + trDouble);
        //System.out.println("Final Time Converted: " + ftDouble);
        //System.out.println("Time Diff: " + timeDifferential);
        
        System.out.println("ERROR: TIMEDIFF()");
        return -1;
    }
    
    
    public void tryMe()
    {
        
        double count0s = 0;    double percentage0s = 0;
        double count1s = 0;    double percentage1s = 0;
        for(int i = 0; i < allHorses.size(); i++)
        {
            String horse = allHorses.get(i);
            
            //Past NewDist Values
            ArrayList<Integer> record = recordHistory.get(i);
            ArrayList<String> nD = newDistanceEvaluations.get(i);
            if(nD.size() >= 3 || nD.get(0).equals("FR"))
            {
                for(int j = 0; j < record.size(); j++)
                {
                    if(nD.get(j).equals("0") || nD.get(j).equals("FR"))
                    {
                        count0s++;
                        if(Integer.valueOf(record.get(j)) <= 3)
                        {
                            percentage0s++;
                        }
                    }else{
                        count1s++;
                        if(Integer.valueOf(record.get(j)) <= 3)
                        {
                            percentage1s++;
                        }
                    }
                }
            }
            
        }
        
        System.out.println("Evals:");
        System.out.println("NEWDIST = 0: " + (percentage0s/count0s) + " Count: " + count0s);
        System.out.println("NEWDIST = 1: " + (percentage1s/count1s) + " Count: " + count1s);
    }
    
    public void formatData()
    {
        for(int i = 0; i < allHorses.size(); i++)
        {
            ArrayList<String> data = new ArrayList<String>();
            
            //data.add(date); data.add(raceNum);
            data.add(allHorses.get(i)); data.add(String.valueOf(appearances.get(i))); data.add(String.valueOf(Wins.get(i))); data.add(String.valueOf(Place.get(i)));
            data.add(String.valueOf(Show.get(i))); 
            data.add(String.valueOf((10000 * Wins.get(i))/appearances.get(i))); data.add(String.valueOf((10000 * WPS.get(i))/appearances.get(i)));
            
            //Points
            //data.add(String.valueOf(QualityPoints.get(i)*1000)); data.add(String.valueOf(AverageQualityPoints.get(i)*1000)); data.add(String.valueOf(NetNuetralQualityPoints.get(i)*1000));
            //data.add(String.valueOf(AverageNetNuetralQualityPoints.get(i)*1000));
            //
            
            formattedStringData.add(data);
        }
    }
    
    public void writeToCSV()
    {
        try (PrintWriter writer = new PrintWriter(new File("Horse Statistics.csv"))) 
        {
            StringBuilder sb = new StringBuilder();
            
            //VARIABLES
            //sb.append("date,"); sb.append("raceNum,");
            //Track Vars
            sb.append("JockeyName,"); sb.append("NumOfRaces,"); sb.append("Win,"); 
            sb.append("Place,"); sb.append("Show,"); sb.append("WinPercent,"); sb.append("Top3Percent,"); //sb.append("NewDistance,");
            sb.append("NewDist,"); sb.append("Avesprat,"); sb.append("LSprat,");
            sb.append('\n');
            
            //Cycles through each horses data
            for(int i = 0; i < formattedStringData.size(); i++)
            {
                for(int j = 0; j < formattedStringData.get(i).size(); j++)
                {
                    ArrayList<String> sData = formattedStringData.get(i);
                    sb.append(sData.get(j));
                    sb.append(','); 
                }
                sb.append('\n');
            }
            writer.write(sb.toString());
            System.out.println("Finished Writing to Horse Statistics.CSV");
        } catch (FileNotFoundException e) {
          System.out.println(e.getMessage());
        }
    }
    
    public void dataLog()
    {
        try
        {
            FileWriter writer = new FileWriter(new File("Horse Info.txt"));
            StringBuilder sb = new StringBuilder();
            for(int i = 0; i < allHorses.size(); i++)
            {                 
                //System.out.println(allHorses.get(i)); 
                writer.write(allHorses.get(i));
                writer.write("\r\n");
                
                String outputStr = "Dist: ";
                ArrayList<String> d = distances.get(i);
                for(int j = 0; j < d.size(); j++)
                {
                    outputStr = outputStr + d.get(j) + " ";
                }
                writer.write(outputStr);
                writer.write("\r\n");

                outputStr = "NewDist: ";
                ArrayList<String> nD = newDistanceEvaluations.get(i);
                for(int j = 0; j < nD.size(); j++)
                {
                    outputStr = outputStr + nD.get(j) + " ";
                }
                writer.write(outputStr);
                writer.write("\r\n");
                
                outputStr = "Record: ";
                ArrayList<Integer> record = recordHistory.get(i);
                for(int j = 0; j < record.size(); j++)
                {
                    outputStr = outputStr + record.get(j) + " ";
                }
                writer.write(outputStr);
                writer.write("\r\n");
                
                outputStr = "Sprat: ";
                ArrayList<Double> spra = SPRAT.get(i);
                for(int j = 0; j < spra.size(); j++)
                {
                    outputStr = outputStr + spra.get(j) + " ";
                }
                writer.write(outputStr);
                writer.write("\r\n");
                
                writer.write("AVESPRAT: " + AVESPRAT.get(i));
                writer.write("\r\n");
                writer.write("\r\n");
            }
            writer.close();
            System.out.println("Finished Writing to Horse Info.txt");
        } catch (IOException e) {
          System.out.println(e.getMessage());
        }
    }
    
    
    public void printData()
    {   
        //System.out.println("Print Data");
        //System.out.println("Write Data");
        for(int i = 0; i < allHorses.size(); i++)
        { 
            ArrayList<String> nD = newDistanceEvaluations.get(i);
            if(nD.size() >= 4)
            {
                System.out.println(allHorses.get(i)); 
                ArrayList<String> d = distances.get(i);
                for(int j = 0; j < d.size(); j++)
                {
                    System.out.print(d.get(j) + " ");
                }
                System.out.println("");

                for(int j = 0; j < nD.size(); j++)
                {
                    System.out.print(nD.get(j) + " ");
                }
                System.out.println("");

                ArrayList<Integer> record = recordHistory.get(i);
                for(int j = 0; j < record.size(); j++)
                {
                    System.out.print(record.get(j) + " ");
                }
                System.out.println("");
                
                ArrayList<Double> spra = SPRAT.get(i);
                for(int j = 0; j < spra.size(); j++)
                {
                    System.out.print(spra.get(j) + " ");
                }
                System.out.println("");
            }
            
            /*
            if (appearances.get(i) > 2)
            {
                System.out.println(allHorses.get(i) + "    Number of Appearances: " + appearances.get(i));
                
                ArrayList<String> d = distances.get(i);
                for(int j = 0; j < d.size(); j++)
                {
                    System.out.print(d.get(j) + " ");
                }
                
                System.out.println("");
                ArrayList<String> nD = newDistanceEvaluations.get(i);
                if(nD.size() >= 4)
                {
                    for(int j = 0; j < nD.size(); j++)
                    {
                        System.out.print(nD.get(j) + " ");
                    }
                    System.out.println("");
                }
                
                
                //Racing Record
                ArrayList<Integer> record = recordHistory.get(i);
                for(int j = 0; j < record.size(); j++)
                {
                    System.out.print(record.get(j) + " ");
                }
                
                
                 //System.out.println("");
            }
            */
            
        }

        System.out.println(TQP);
    }
    
    public String distance(String[] lines)
    {
        int index = -1;
        String[] dataLinePieces;
        for(int i = 0; i < 20; i++)
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
                }
            }
        }
        
        //System.out.println(lines[index]);
        dataLinePieces = lines[index].split(" On The ");
        dataLinePieces = dataLinePieces[0].split(" ");
        
        //System.out.println(dataLinePieces[0]);
        String sDist = "";
        
        /* Could not return a string value with a . or / so multiplied distance by x10000 */
        if(dataLinePieces[0].equals("Four") && dataLinePieces[1].equals("And") && dataLinePieces[2].equals("One") 
        && dataLinePieces[3].equals("Half") && dataLinePieces[4].equals("Furlongs"))
        {
            return "5625";
        }
        
        if(dataLinePieces[0].equals("Five") && dataLinePieces[1].equals("And") && dataLinePieces[2].equals("One") 
        && dataLinePieces[3].equals("Half") && dataLinePieces[4].equals("Furlongs"))
        {
            return "6875";
        }
        
        if(dataLinePieces[0].equals("Six") && dataLinePieces[1].equals("Furlongs"))
        {
            //return 0.75;
            return "7500";
        }
        
        if(dataLinePieces[0].equals("Six") && dataLinePieces[1].equals("And"))
        {
            //return 0.8125;
            return "8125";
        }
        
        if(dataLinePieces[0].equals("Seven"))
        {
            //return 0.875;
            return "8750";
        }
        
        if(dataLinePieces[0].equals("One") && dataLinePieces[1].equals("Mile"))
        {
            //return 1;
            return "10000";
        }
        
        if(dataLinePieces[0].equals("One") && dataLinePieces[1].equals("And") && dataLinePieces[2].equals("One")
         && dataLinePieces[3].equals("Sixteenth"))
        {
            //return 1.125;
            return "10625";
        }
        
        if(dataLinePieces[0].equals("One") && dataLinePieces[1].equals("And") && dataLinePieces[2].equals("Three")
         && dataLinePieces[3].equals("Sixteenth"))
        {
            //return 1.125;
            return "11875";
        }
        
        if(dataLinePieces[0].equals("One") && dataLinePieces[1].equals("And") && dataLinePieces[2].equals("One")
         && dataLinePieces[3].equals("Eighth"))
        {
            //return 1.125;
            return "11250";
        }
        
        if(dataLinePieces[0].equals("One") && dataLinePieces[1].equals("And") && 
        dataLinePieces[2].equals("Five") && dataLinePieces[3].equals("Sixteenth"))
        {
            //return 1;
            return "13125";
        }
        
        if(dataLinePieces[0].equals("One") && dataLinePieces[1].equals("And") && 
        dataLinePieces[2].equals("Three") && dataLinePieces[3].equals("Eighth"))
        {
            //return 1;
            return "13750";
        }
        
        if(dataLinePieces[0].equals("One") && dataLinePieces[1].equals("And") && 
        dataLinePieces[2].equals("One") && dataLinePieces[3].equals("Half"))
        {
            //return 1;
            return "15000";
        }
        
        //Checks Distance Type
        //System.out.println(dataLinePieces[0]);
        showError();
        System.out.println("ERROR: distance() - MISSING DISTANCE VALUE OF:  "); System.out.print(lines[index]);
        return "ERROR";
    }
       
    public void refreshHorseList(String currHorse, double place, double numberOfRaceHorses, String dist, boolean firstRace, String[] lines, int lowerBound, int upperBound)
    {
        boolean evenDis = false;
        if(allHorses.size() != 0 )
        {
            boolean tripped = false;
            int index = -1;
            for(int i = 0; i < allHorses.size(); i++)
            {
                if(currHorse.equals(allHorses.get(i)))
                {
                    tripped = true;
                    index = i;
                }
            }
            
            if(tripped == false)
            {
                allHorses.add(currHorse);
                appearances.add(1);
                
                //Net Neutral Quality Points
                //NetNuetralQualityPoints.add(determineQualityPoints(place, numberOfRaceHorses));
                ArrayList<Integer> racePos = new ArrayList<Integer>(); racePos.add((int)place);
                recordHistory.add(racePos);
                ArrayList<String> dis = new ArrayList<String>(); dis.add(dist);
                distances.add(dis);
                
                //New Dist
                ArrayList<String> newD = new ArrayList<String>(); 
                if(firstRace == true)
                {
                    newD.add("FR");
                    //System.out.println("FR hit");
                }else{
                    newD.add("0");
                }
                newDistanceEvaluations.add(newD);
                
                //Evaluate AVESPRAT
                double sprat = calculateSPRAT(lines, place, lowerBound, upperBound);
                ArrayList<Double> sprats = new ArrayList<Double>(); sprats.add(sprat); SPRAT.add(sprats);
                AVESPRAT.add(sprat);
            }else{
                appearances.set(index,appearances.get(index)+1);
                updateRecord(currHorse, (int)place);
                updateDistances(currHorse, dist);
                updateNewDist(currHorse, dist);
                updateSPRAT(currHorse, calculateSPRAT(lines, place, lowerBound, upperBound));
                updateAVESPRAT(currHorse, calculateSPRAT(lines, place, lowerBound, upperBound));
            }
        }else{
            allHorses.add(currHorse);
            appearances.add(1);
            ArrayList<Integer> racePos = new ArrayList<Integer>(); racePos.add(1);
            recordHistory.add(racePos);

            //Distances
            ArrayList<String> dis = new ArrayList<String>(); dis.add(dist);
            distances.add(dis);
            
            //New Dist
            ArrayList<String> newD = new ArrayList<String>();
            if(firstRace == true)
            {
                newD.add("FR");
                //System.out.println("FR hit");
            }else{
                newD.add("0");
            }
            newDistanceEvaluations.add(newD);
            
            double sprat = calculateSPRAT(lines, place, lowerBound, upperBound);
            ArrayList<Double> sprats = new ArrayList<Double>(); sprats.add(sprat); SPRAT.add(sprats);
            AVESPRAT.add(sprat);
        }
    }
    
    public double calculateSPRAT(String[] lines, double place, int lBound, int hBound)
    {
        //System.out.println("Time Differ: " + finalTimeTrackRecordDiff);
        String[] numOfItems = (lines[lBound-1]).split(" PP ");
        numOfItems = numOfItems[1].split(" Odds");
        numOfItems = numOfItems[0].split(" ");
        
        String dist = "";
        
        if(numOfItems.length == 4 ||  numOfItems.length == 5 || numOfItems.length == 6)//&& numOfItems[3].equals("Str"))
        {
            //System.out.print(lines[lBound+(int)place-1]);
            String[] finishCall = lines[lBound+(int)place-1].split("");
            int indexBracket = -1;
            int indexPeriod = -1;
            for(int i = 0; i < finishCall.length-1; i++)
            {
                if(finishCall[i].equals("."))
                {
                    indexPeriod= i+3;
                }
            }
            
            for(int i = 0; i < finishCall.length-1; i++)
            {
                if(finishCall[i].equals(")"))
                {
                    indexBracket = i+2;
                    break;
                }
            }
            
            
            
            String substr = lines[lBound+(int)place-1].substring(indexBracket,indexPeriod);
            finishCall = substr.split(" ");
            
            int indexSecondSpace = -1; int countSecondSpace = 0;
            String dater = "" + finishCall[finishCall.length-3] + " " 
            + finishCall[finishCall.length-2];
            //System.out.println("THIS: " + dater);
            if(!finishCall[finishCall.length-2].equals("---"))
            {
                String firstHalf = "" + finishCall[finishCall.length-3];
                String secondHalf = "" + finishCall[finishCall.length-2];
                boolean firstHalfDiv = false; 
                int indexFirstHalfDivCount = 0; int indexFirstHalfExpreshCount = 0;
                boolean secondHalfDiv = false; boolean secondHalfExpresh = false;
                int indexSecondHalfDivCount = 0;  int indexSecondHalfExpreshCount = 0;
                for(int i = 0; i < firstHalf.split("").length-1; i++)
                {
                    if(firstHalf.split("")[i].equals("/"))
                    {
                        firstHalfDiv = true;
                        indexFirstHalfDivCount++;
                    }
                }
                for(int i = 0; i < secondHalf.split("").length-1; i++)
                {
                    if(secondHalf.split("")[i].equals("/"))
                    {
                        secondHalfDiv = true;
                        indexSecondHalfDivCount++;
                    }
                    if(secondHalf.split("")[i].equals("e") || secondHalf.split("")[i].equals("s"))
                    {
                        secondHalfExpresh = true;
                    }
                }
                
                boolean tripped = false;
                if(secondHalfDiv == true && secondHalf.split("").length == 4 &&secondHalf.split("")[0].equals(Integer.toString((int)place)))
                {
                    tripped = true;
                    dist = secondHalf.substring(1,secondHalf.split("").length);
                    //System.out.println("Position: " + (int)place + "   by a distance of: " + dist + "  Distance behind first: " + totalDistBehind);
                }
                
                if(secondHalfDiv == true && secondHalf.split("").length == 3)
                {
                    if(firstHalf.split("").length == 2)
                    {
                        tripped = true;
                        dist = "" + firstHalf.split("")[firstHalf.split("").length-1] + " " + secondHalf.substring(0,secondHalf.split("").length);
                        //System.out.println("Position: " + (int)place + "   by a distance of: "+ dist + "  Distance behind first: " + totalDistBehind);
                    }else{
                        if(firstHalf.split("").length == 3)
                        {
                            tripped = true;
                            dist = "" + firstHalf.substring(1,firstHalf.split("").length) + " " + secondHalf.substring(0,secondHalf.split("").length);
                            //System.out.println("Position: " + (int)place + "   by a distance of: " + dist + "  Distance behind first: " + totalDistBehind);
                        }
                    }
                }
                
                if(secondHalfDiv == false && secondHalf.split("").length == 3)
                {
                    tripped = true;  dist = "" + secondHalf.substring(1,secondHalf.split("").length);
                    //System.out.println("Position: " + (int)place + "   by a distance of: " + dist + "  Distance behind first: " + totalDistBehind);
                }
                
                //Whole Number furlong distances
                if(secondHalfDiv == false && secondHalf.split("").length == 2)
                {
                    tripped = true;  dist = "" + secondHalf.substring(1,secondHalf.split("").length);
                    //System.out.println("Position: " + (int)place + "   by a distance of: " + dist + "  Distance behind first: " + totalDistBehind);
                }
                
                //Last Place Horeseys (1-9)
                if(secondHalfDiv == false && secondHalf.split("").length == 1)
                {
                    tripped = true;
                    dist = "0";
                    //System.out.println(secondHalf);
                    //System.out.println("Position (Last Horse): " + (int)place + "  Distance behind first: " + totalDistBehind);
                }
                
                //Handles Expressions (Nose, Neck and Head)
                if(secondHalfExpresh == true)
                {
                    tripped = true;
                    dist = secondHalf.substring(1,secondHalf.split("").length);
                    //System.out.println("Position: " + (int)place + "   by a distance of: " + dist + "  Distance behind first: " + totalDistBehind);
                }
                
                //Greater than 10 Horses Racing
                if((int)place >= 10)
                {
                    //System.out.println("hereer");
                    if(tripped == false)
                    {
                        if(secondHalfDiv == false && secondHalf.split("").length == 4 && secondHalf.substring(0,2).equals(Integer.toString((int)place)))
                        {
                            tripped = true;
                            dist = "" + secondHalf.substring(2,secondHalf.split("").length);
                            //System.out.println("Position: " + (int)place + "   by a distance of: " + dist + "  Distance behind first: " + totalDistBehind);
                        }
                        
                        if(secondHalfDiv == true && secondHalf.split("").length == 3)
                        {
                            tripped = true;
                            dist = "" + firstHalf.substring(2,firstHalf.split("").length) + " " + secondHalf.substring(0,secondHalf.split("").length);
                            //System.out.println("Position: " + (int)place + "   by a distance of: " + dist + "  Distance behind first: " + totalDistBehind);
                        }
                        
                        if(secondHalfDiv == true && secondHalf.split("").length == 2)
                        {
                            tripped = true;
                            dist = "" + firstHalf.substring(2,firstHalf.split("").length) + " " + secondHalf.substring(0,secondHalf.split("").length);
                            //System.out.println("Position: " + (int)place + "   by a distance of: " + dist + "  Distance behind first: " + totalDistBehind);
                        }
                        
                        if(secondHalfDiv == true && secondHalf.split("").length == 5 && secondHalf.substring(0,2).equals(Integer.toString((int)place)))
                        {
                            tripped = true;
                            dist = secondHalf.substring(2,secondHalf.split("").length);
                            //System.out.println("Position: " + (int)place + "   by a distance of: " + dist + "  Distance behind first: " + totalDistBehind);
                        }
                        
                        //Here
                        if(secondHalfExpresh == true)
                        {
                            tripped = true;
                            dist = secondHalf.substring(1,secondHalf.split("").length);
                            System.out.println("Position: " + (int)place + "   by a distance of: " + dist + "  Distance behind first: " + totalDistBehind);
                        }
                        
                        if(tripped != true)
                        {
                            System.out.println("ISSUES");
                            System.out.println(firstHalf + "  " + secondHalf);
                        }
                    }
                }
                
                //Ties
                if(secondHalfDiv == true && secondHalf.split("").length == 4 &&secondHalf.split("")[0].equals(Integer.toString((int)place-1)))
                {
                    tripped = true;
                    dist = secondHalf.substring(1,secondHalf.split("").length);
                    //System.out.println("Position: " + (int)place + "   by a distance of: " + dist + "  Distance behind first: " + totalDistBehind);
                }
                if(!tripped)
                {
                    System.out.println("Date: " + date + "  RaceNum: " + raceNum);
                    System.out.println("EVALUATE: " + dater);
                }
            }else{
                //System.out.println("Horse did not finish race");
                dist = "15";
                totalDistBehind = totalDistBehind + 15;
            }
        }
        //Split up points of call for finish
        double translatedDistance = 0.0;
        String[] pieces = dist.split("");
        boolean hasFraction = false;    boolean hasExpresh = false;
        for(int i = 0; i < pieces.length-1; i++)
        {
            if(pieces[i].equals("/"))
            { 
               hasFraction = true;
            }
            if(pieces[i].equals("s") || pieces[i].equals("e"))
            { 
               hasExpresh = true;
            }
        }
        
        double offset = finalTimeTrackRecordDiff/.2;
        double horseSpeedRating = 100.0 - offset - 1.2*totalDistBehind;
        //System.out.println("Speed rating for horse: " + horseSpeedRating + "   TimeDiffer: " + finalTimeTrackRecordDiff);
        
        
        
        double fractional = 0.0;
        boolean hasCalc = false;
        if(hasFraction == false && hasExpresh == false)
        {
            hasCalc = true;
            translatedDistance = translatedDistance + Double.valueOf(dist);
        }else{
            if(hasFraction == true && hasExpresh == false)
            {
                int numOfPieces = dist.split(" ").length;
                
                if(numOfPieces == 1)
                {
                    hasCalc = true;
                    double numerator = (double)Double.valueOf(dist.split(" ")[0].split("/")[0]);
                    double denominator = (double)Double.valueOf(dist.split(" ")[0].split("/")[1]);
                    fractional = numerator/denominator;
                    translatedDistance = translatedDistance + fractional;
                }else{
                    if(numOfPieces == 2)
                    {
                        hasCalc = true;
                        String[] compleFrac = dist.split(" ");
                        translatedDistance = translatedDistance + (double)Double.valueOf(compleFrac[0]);
                        double numerator = (double)Double.valueOf(compleFrac[1].split("/")[0]);
                        double denominator = (double)Double.valueOf(compleFrac[1].split("/")[1]);
                        fractional = numerator/denominator;
                        translatedDistance = translatedDistance + fractional;
                    }
                }
            }
            if(hasExpresh == true)
            {
                if(dist.equals("Nose"))
                {
                    hasCalc = true;
                    translatedDistance = translatedDistance + 0.05;
                }
                if(dist.equals("Head"))
                {
                    hasCalc = true;
                    translatedDistance = translatedDistance + 0.2;
                }
                if(dist.equals("Neck"))
                {
                    hasCalc = true;
                    translatedDistance = translatedDistance + 0.3;
                }
            }
        }
        if(!hasCalc)
        {
            boolean redo = false;
            String[] dExpresh = dist.split("");
            if(dExpresh[0].equals("0") || dExpresh[0].equals("1") || dExpresh[0].equals("2") 
            || dExpresh[0].equals("3") || dExpresh[0].equals("4") && (dExpresh[1].equals("N") || dExpresh[1].equals("H")))
            {
                String editedDist = dist.substring(1, dist.split("").length);
                //System.out.println("NEWDIST:" + editedDist);
                //System.out.println("Date: " + date + "   RaceNum: " + raceNum);
                if(editedDist.equals("Nose"))
                {
                    redo = true;
                    translatedDistance = translatedDistance + 0.05;
                    //System.out.println("Position: " + (int)place + "   by a distance of: " + editedDist + "  Distance behind first: " + totalDistBehind);
                }
                if(editedDist.equals("Head") && redo == false)
                {
                    redo = true;
                    translatedDistance = translatedDistance + 0.2;
                    //System.out.println("Position: " + (int)place + "   by a distance of: " + editedDist + "  Distance behind first: " + totalDistBehind);
                }
                if(editedDist.equals("Neck") && redo == false)
                {
                    redo = true;
                    translatedDistance = translatedDistance + 0.3;
                    //System.out.println("Position: " + (int)place + "   by a distance of: " + editedDist + "  Distance behind first: " + totalDistBehind);
                }
                //System.out.println("-------------------------------------------------------------------");
            }
            
            if(redo == false)
            {
                System.out.println("Sommen not calculater");
                System.out.println("Date: " + date + "   RaceNum: " + raceNum);
                System.out.println("Dist: " + dist + "   Place: " + (int)place);
                System.out.println("-------------------------------------------------------------------");
            }
        }
        
        totalDistBehind = totalDistBehind + translatedDistance;
        
        //System.out.println("-------------------------------------------------------------------");
        return horseSpeedRating;
    }
    
    public void updateAVESPRAT(String hName, double s)
    {
        int flagIndex = -1;
        for(int i = 0; i < allHorses.size(); i++)
        {
            if(allHorses.get(i).equals(hName))
            {
                flagIndex = i;
            }
        }
        
        double total = 0;
        double avgSprat = 0;
        ArrayList<Double> speedRatings = SPRAT.get(flagIndex);
        if(speedRatings.size() >= 4)
        {
            for(int i = speedRatings.size()-4; i < speedRatings.size(); i++)
            {
                total = total + speedRatings.get(i);
            }
            avgSprat = total/4;
        }else{
            for(int i = 0; i < speedRatings.size(); i++)
            {
                total = total + speedRatings.get(i);
            }
            avgSprat = total/speedRatings.size();
        }
        AVESPRAT.set(flagIndex, avgSprat);
    }
     
    public void updateSPRAT(String hName, double s)
    {
        int flagIndex = -1;
        for(int i = 0; i < allHorses.size(); i++)
        {
            if(allHorses.get(i).equals(hName))
            {
                flagIndex = i;
            }
        }
        
        SPRAT.get(flagIndex).add(s);
    }
    
    public String updateNewDist(String hName, String dist)
    {
        //Retrieves index of horse name which is where its pas history is.
        int flagIndex = -1;
        for(int i = 0; i < allHorses.size(); i++)
        {
            if(allHorses.get(i).equals(hName))
            {
                flagIndex = i;
            }
        }
        
        //Retrieves past history from distances
        ArrayList<String> horsesDistances = distances.get(flagIndex);
        int thyDistance = Integer.parseInt(dist);
        if(horsesDistances.size() < 4)
        {  
           newDistanceEvaluations.get(flagIndex).add("0");
           return"";
            
        }else{
            int count = 0;
            //Last four races
            for(int i = 0; i < horsesDistances.size(); i++)
            {
                int tempDist = Integer.parseInt(horsesDistances.get(i));
                if(tempDist >= thyDistance)//= 10000) //thyDistance)
                {
                    count++;
                }
            }
            
            // N
            if(count >= 3)
            {
                newDistanceEvaluations.get(flagIndex).add("1");
                return"";
            }else{
                newDistanceEvaluations.get(flagIndex).add("0");
                return"";
            }
        }
    }
    
    public void updateRecord(String j, int place)
    {
        int index = -1;
        for(int i = 0; i < allHorses.size(); i++)
        {
            if(j.equals(allHorses.get(i)))
            {
                index = i;
            }
        }
        
        recordHistory.get(index).add(place);
    }
    
    public void updateDistances(String j, String dist)
    {
        int index = -1;
        for(int i = 0; i < allHorses.size(); i++)
        {
            if(j.equals(allHorses.get(i)))
            {
                index = i;
            }
        }
        
        distances.get(index).add(dist);
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
    
    public int racerData(String[] lines)
    {
        int lIndex = -1;
        int uIndex = -1;
        ArrayList<ArrayList<String>> horses = new ArrayList<ArrayList<String>>();
        String[] dataLinePieces;
        String dist = distance(lines); 
        
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
        String[] jockeys = new String[(int)numOfHorses];
        
        String horse = ""; 
        
        //Cycles through all Jockeys in a race
        ArrayList<String> tempListOfJockeys = new ArrayList<String>();
        double place = 1.0; 
        for(int i = lIndex; i < uIndex+1; i++)
        {
            ArrayList<String> indivHorse = new ArrayList<String>();
            dataLinePieces = lines[i].split(" ");
            
            boolean frBool = false;
            if(dataLinePieces[0].equals("---"))
            {
                frBool = true;
            }
            horse = horse(dataLinePieces, dist);
            refreshHorseList(horse, place, numOfHorses, dist, frBool, lines, lIndex, uIndex);
            place++;
        }
        
        return (int)numOfHorses;
    }
    
    public String horse(String[] dataPieces, String dist)
    {
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
        
        //Horse Name
        String hName = "";
        //showError();
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
        String date = dataLinePieces[1];
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
        {nMonth = "01";}else{
            if(sMonth.equals("February"))
            {nMonth = "02";}else{
                if(sMonth.equals("March"))
                {nMonth = "03";}else{
                    if(sMonth.equals("April"))
                    {nMonth = "04";}else{
                        if(sMonth.equals("May"))
                        {nMonth = "05";}else{
                            if(sMonth.equals("June"))
                            {nMonth = "06";}else{
                                if(sMonth.equals("July"))
                                {nMonth = "07";}else{
                                    if(sMonth.equals("August"))
                                    {nMonth = "08";}else{ 
                                        if(sMonth.equals("September"))
                                        {nMonth = "09";}else{
                                            if(sMonth.equals("October"))
                                            {nMonth = "10";}else{
                                                if(sMonth.equals("November"))
                                                {nMonth = "11";}else{ 
                                                    if(sMonth.equals("December"))
                                                    {nMonth = "12";}else{ 
                                                        nMonth = "-1";
                                                    }
                                                }
                                            }
                                        }
                                    }                 
                                }
                            }
                        }
                    }
                }
            }
        }
        
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
        System.out.println("Date: " + date + "      Race Number: " + raceNum + 
        "        Number of Horses: " + sNumOfHorses);
    }
}