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
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
/**
 * Write a description of class PHRA here.
 *
 * @author (Nate Beal)
 * @version (7/19/20)
 */
public class TrainValidateShuffled
{
    // instance variables - replace the example below with your own
    long t1; long t2;
    String date;
    String sNumOfHorses;
    int totalRacesProcessed = 0;
    int priorRaceNum;
    int totalDataPointsProcessed;
    String raceNum;
    int totalPagesProcessed = 0;
    int totalPagesRemoved = 0;
    int numProcessed = 0;
    int pageCount = 0;
    
    //CONSTANTS
    int numberOfDesiredRaces = 100;    
    int didNotFinishPenalty = 5;
    int firstRaceSpeedValue = 45;
    double totalDistBehindWeight = 1.2;
    double offsetWeight = 5;
    int numberOfRacesToAverage = 4; //Number of races use in the rolling ave calculation for avespeed
    int newDistRaceMinimum = 4;
    int newDistMinimum = 3;
    
    //Changes depending on number of params being evaluated
    int NUMBEROFPARAMS = 25;
    
    //Most Important ArryaList
    ArrayList<ArrayList<String>> formattedData = new ArrayList<ArrayList<String>>();
    boolean toggleFinalRank = true;
    
    //Horse Specific Data
    double finalTimeTrackRecordDiff;
    double totalDistBehind = 0.0;
    ArrayList<String> allHorses = new ArrayList<String>();
    ArrayList<Integer> appearances = new ArrayList<Integer>();
    ArrayList<ArrayList<Integer>> recordHistory = new ArrayList<ArrayList<Integer>>();
    ArrayList<ArrayList<String>> distances = new ArrayList<ArrayList<String>>();
    ArrayList<ArrayList<String>> newDistanceEvaluations = new ArrayList<ArrayList<String>>();
    ArrayList<ArrayList<Double>> SPRAT = new ArrayList<ArrayList<Double>>();
    ArrayList<Double> AVESPRAT = new ArrayList<Double>();
    
    //W/P/S
    ArrayList<Integer> Wins = new ArrayList<Integer>();
    ArrayList<Integer> Place = new ArrayList<Integer>();
    ArrayList<Integer> Show = new ArrayList<Integer>();
    ArrayList<Integer> WPS = new ArrayList<Integer>();
    /**
     * Constructor for objects of class PHRAc
     */calcu
    public TrainValidateShuffled()
    {}

    public static void main() throws IOException 
    {
        String[] docs = new String[] {"3-1-20 AQU Race 2"};
        ArrayList<String> dv = new ArrayList<String>(); dv.add("NIL");
        
        TrainValidateShuffled demo = new TrainValidateShuffled();
        //demo.run("AQU Jan'18-Feb'20", docs, dv);
        demo.run("AQU 19-20", docs, dv);
    }
    
    public void run(String fileExt, String[] testFilesExt, ArrayList<String> desiredVariables) throws IOException 
    {
        System.out.println("Developing Training and Validation Set CSV: ");
        t1 = System.currentTimeMillis();
        File myFile = new File("C:/Users/natha/Desktop/Machine Learning/1. Left Off/PHRA/" + fileExt + ".pdf");
        
        
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
            long t = System.currentTimeMillis();
            t = (t-t1)/1000;
            //System.out.println("    - Finished processing pdfs (" + numPages + " races)         (" + t + "seconds)");
        }
        
        //Evaluates Each Document Seperately
        ArrayList<ArrayList<String>> values = new ArrayList<ArrayList<String>>();
        String currentRace;
        int min = 0;
        int lim = numPages+1;
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
                
                //Race Specific Data/General Data
                date = retrieveDate(lines);
                raceNum = raceNumber(lines);
                finalTimeTrackRecordDiff = retrieveTRFTDifferential(lines);
                String trackType = trackType(lines);
                String weather = weather(lines);
                String distance = distance(lines);                      //True Double
                //String numHorses = sNumOfHorses;
                String trackCondition = trackCondition(lines);  
                totalDistBehind = 0.0;
                
                //Horse Data
                ArrayList<String> generalData = new ArrayList<String>();
                generalData.add(date); generalData.add(raceNum); generalData.add(trackType);
                generalData.add(weather); generalData.add(trackCondition); generalData.add(distance);
                
                ArrayList<ArrayList<String>> racerData = racerData(lines);
                String numHorses = sNumOfHorses; generalData.add(numHorses);
                
                //int numOfHorses = jockeys.length; System.out.println(numOfHorses);
            
                //System.out.println("Date: " + date + "      Race Number: " + raceNum);
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
                    dataPrep(numHorses, generalData, racerData);
                    numProcessed++;
                }
            }else{
                totalPagesRemoved++;
                //System.out.println("Page " + i + " is blocked");
            }
        } 
        //printData();
        //System.out.println("Waiting to Write to CSV");
        writeToCSV();
        dataLog();
        t2 =  System.currentTimeMillis();
        long totalTime = (t2-t1)/1000;
        System.out.println("Time Elapsed: " + totalTime + " seconds");
        
        //Starts Evaluating Test Set
        
        TestSet demo2 = new TestSet(); long t4; long t3;
        for(int i = 0; i < testFilesExt.length; i++)
        {
            t3 = System.currentTimeMillis();
            String file = testFilesExt[i];
            demo2.run(file, i);
            t4 = System.currentTimeMillis();
            System.out.print("Time Elapsed: " + (t4-t3)/1000 + " seconds");
            totalTime = totalTime + (t4-t3)/1000;
        }
        
        
        System.out.println("");
        System.out.println("");
        System.out.println("Races Processed: " + totalPagesProcessed);
        System.out.println("Pages Removed: " + totalPagesRemoved);
        System.out.println("Number of races output: " + numProcessed);
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

    public void dataLog()
    {
        System.out.println("    - Writing to Data Log");
        String separationKey = "!&!";
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
                writer.write(outputStr +  ""  + separationKey);
                writer.write("\r\n");

                outputStr = "NewDist: ";
                ArrayList<String> nD = newDistanceEvaluations.get(i);
                for(int j = 0; j < nD.size(); j++)
                {
                    outputStr = outputStr + nD.get(j) + " ";
                }
                writer.write(outputStr +  ""  + separationKey);
                writer.write("\r\n");
                
                outputStr = "Record: ";
                ArrayList<Integer> record = recordHistory.get(i);
                for(int j = 0; j < record.size(); j++)
                {
                    outputStr = outputStr + record.get(j) + " ";
                }
                writer.write(outputStr +  ""  + separationKey);
                writer.write("\r\n");
                
                outputStr = "Sprat: ";
                ArrayList<Double> spra = SPRAT.get(i);
                for(int j = 0; j < spra.size(); j++)
                {
                    outputStr = outputStr + spra.get(j) + " ";
                }
                writer.write(outputStr +  ""  + separationKey);
                writer.write("\r\n");
                
                writer.write("AVESPRAT: " + AVESPRAT.get(i) + " " + separationKey);
                writer.write("\r\n");
                writer.write("$"); // Termination Key
                writer.write("\r\n");
            }
            writer.close();
            System.out.println("    - Finished Writing to Data Log (Horse Info.txt)");
        } catch (IOException e) {
          System.out.println(e.getMessage());
        }
    }
    
    public double retrieveTRFTDifferential(String [] lines)//Retrieve Final Time and track record differential
    {
        String[] dataLinePieces;
        int index = -1;
        
        doubleloop:
        for(int i = 0; i < lines.length; i++)
        {
            try{
                dataLinePieces = lines[i].split(" ");
            }catch (Exception e)
            {
                System.out.println(e);
                System.out.println(pageCount);
                System.out.println(lines[i-1]);
            }
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
        runUpIndex:
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
                    break runUpIndex;
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
        System.out.println(pageCount);
        return -1;
    }
    
    public void printData()
    {   
        //System.out.println("Write Data");
        for(int i = 0; i < formattedData.size(); i++)
        {
            ArrayList<String> matchup = formattedData.get(i);
            for(int j = 0; j < matchup.size(); j++)
            {
                System.out.print(matchup.get(j) + ", ");
            }
            System.out.println("");
        }
    }
    
    public void dataPrep(String horsesInRace, ArrayList<String> genData, ArrayList<ArrayList<String>> racerData)
    {
        ArrayList<ArrayList<String>> localRace = new ArrayList<ArrayList<String>>(); 
        for(int i = 0; i < racerData.size(); i ++)
        {
            ArrayList<String> temp = racerData.get(i);
            for(int j = 0; j < temp.size(); j++)
            {
                //System.out.print(temp.get(j) + ", ");
            }
            //System.out.println("");
        }

        int count = 0;
        for(int x = 1; x < Integer.valueOf(horsesInRace)+1; x++)
        {
            for(int y = x+1; y < Integer.valueOf(horsesInRace)+1; y++)
            {
                //System.out.print(x + "," + y + "   ");
                count++;
            }
            //System.out.println("");
        }

        int numOfMatchups = 0;
        for(int i = 1; i < Integer.valueOf(horsesInRace); i++)
        {
            numOfMatchups = numOfMatchups + i;
        }

        //Random Toggler to decide which side the winning horse os put on
        Random rand = new Random();
        int num = rand.nextInt(2);   
        if(num == 0)
        {toggleFinalRank = false;}else{
            if(num == 1)
            {toggleFinalRank = true;}
        }
        
        for(int i = 0; i < Integer.valueOf(horsesInRace); i++)
        {
            ArrayList<String> temp = new ArrayList<String>();
            ArrayList<String> horse1Data = racerData.get(i);
            for(int j = 0; j < genData.size(); j++)
            {
                temp.add(genData.get(j));
            }
            
            if(toggleFinalRank == false)
            {
                for(int x = 0; x < horse1Data.size(); x++)
                {
                    //System.out.print
                    temp.add(horse1Data.get(x));
                }                
                for(int j = i+1; j < Integer.valueOf(horsesInRace); j++)
                {
                    ArrayList<String> cO = new ArrayList<String>();
                    for(int w = 0; w < temp.size(); w++)
                    {
                        cO.add(temp.get(w));
                    }
                    //Collections.copy(cO,temp);
                    
                    ArrayList<String> horse2Data = racerData.get(j);
                    for(int y = 0; y < horse2Data.size(); y++)
                    {
                        cO.add(horse2Data.get(y));
                    }
                    cO.add("1");
                    //formattedData.add(cO);
                    localRace.add(cO);
                }
            }else{
                for(int j = i+1; j < Integer.valueOf(horsesInRace); j++)
                {
                    ArrayList<String> horse2Data = racerData.get(j);
                    
                    ArrayList<String> cO = new ArrayList<String>();
                    for(int q = 0; q < genData.size(); q++)
                    {
                        cO.add(genData.get(q));
                    }
                    
                    for(int y = 0; y < horse2Data.size(); y++)
                    {
                        cO.add(horse2Data.get(y));
                    }
                    
                    for(int x = 0; x < horse1Data.size(); x++)
                    {
                        //System.out.print
                        cO.add(horse1Data.get(x));
                    }
                    
                    cO.add("2");
                    //formattedData.add(cO);
                    localRace.add(cO);
                }
            }
            toggleFinalRank = !toggleFinalRank;
            //System.out.println(toggleFinalRank);
        }
        
        //Shuffles within local race
        Collections.shuffle(localRace);
        for(int i = 0; i < localRace.size(); i++)
        {
            formattedData.add(localRace.get(i));
        }
        //System.out.println("----------------------------------------------------------------------------------------------------------------------------");
    }
    
    public void writeToCSV()
    {
        //System.out.println("HERE");
        //Collections.shuffle(formattedData);
        System.out.println("    - Writing to CSV");
        try (PrintWriter writer = new PrintWriter(new File("Train & Validate Shuffled.csv"))) 
        {
            StringBuilder sb = new StringBuilder();
            
            //VARIABLES
            sb.append("date,"); sb.append("raceNum,");
            //Track Vars
            sb.append("trackType,"); sb.append("weather,"); sb.append("trackCondition,"); 
            sb.append("dist,"); sb.append("numHorses,");
            //Jockey 1
            sb.append("lastRace1,"); sb.append("pastRace1,"); sb.append("pastTrack1,");  sb.append("pastPos1,");
            sb.append("hName1,"); sb.append("jName1,");  sb.append("weight1,"); sb.append("m1,"); sb.append("e1,");
            sb.append("polePos1,");sb.append("odds1,"); sb.append("fav1,"); 
            sb.append("newDist1,"); sb.append("asr1,");  sb.append("lsr1,");
            //Jockey 2
            sb.append("lastRace2,"); sb.append("pastRace2,"); sb.append("pastTrack2,");  sb.append("pastPos2,");
            sb.append("hName2,"); sb.append("jName2,");  sb.append("weight2,"); sb.append("m2,"); sb.append("e2,");
            sb.append("polePos2,");sb.append("odds2,"); sb.append("fav2,");
            sb.append("newDist2,"); sb.append("asr2,");  sb.append("lsr2,");
            sb.append("finalrank,");
            sb.append('\n');
            //sb.append(',');sb.append("Name");
           
            
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
            writer.write(sb.toString());
            System.out.println("    - Finished writing to CSV (Train & Validate Shuffled.csv)");
        } catch (FileNotFoundException e) {
          System.out.println(e.getMessage());
        }
    }
    
    public ArrayList<ArrayList<String>> racerData(String[] lines)
    {
        int lIndex = -1;
        int uIndex = -1;
        ArrayList<ArrayList<String>> horses = new ArrayList<ArrayList<String>>();
        String[] dataLinePieces;
        String dist = distance(lines);
        
        //Determines Lower Index Bound
        weightLoop:
        for(int i = 0; i < lines.length; i++)
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
                    break weightLoop;
                }
            }
        }
        //First Place Horse
        //System.out.println(lines[lIndex]);
        
        //Deterermine Upper Index Bound
        determineBound:
        for(int i = lIndex; i < lIndex+20;i++)
        {
            dataLinePieces = lines[i].split(" ");
            //System.out.println(lines[i]);
            for(int j = 0; j < dataLinePieces.length; j++)
            {
                //System.out.println(dataLinePieces[j]);
                if(dataLinePieces[j].equals("Fractional"))
                {
                    //Offset of one  Run-Up:
                    //System.out.println("FLAGGED");
                    //System.out.println(i); System.out.println(lines[i]);
                    uIndex = i-1;
                    break determineBound;
                }
            }
        }
        
        //Last Place Horse
        //System.out.println(lines[uIndex]);
        secondaryFilter:
        if(uIndex == -1)
        {
            //System.out.println(pageCount);
            for(int i = lIndex; i < lIndex+20;i++)
            {
                dataLinePieces = lines[i].split(":");
                //System.out.println(lines[i]);
                for(int j = 0; j < dataLinePieces.length; j++)
                {
                    //System.out.println(dataLinePieces[j]);
                    if(dataLinePieces[j].equals("Final Time"))
                    {
                        uIndex = i-1;
                        break secondaryFilter;
                    }
                }
            }
        }
        int numOfHorses = (uIndex-lIndex)+1;
        sNumOfHorses = numOfHorses + "";
        String[] jockeys = new String[numOfHorses];
        
        String timeSinceLR = ""; String[] pastPerformance = new String[3]; 
        String[] horseAndJockey = new String[2]; String weight = ""; String polePosition = ""; 
        String medication = ""; String equipment = "";
        String odds = "";   String favorite = "";
        ArrayList<String> oddFav = new ArrayList<String>();
        double place = 1.0;
        for(int i = lIndex; i < uIndex+1; i++)
        {
            ArrayList<String> indivHorse = new ArrayList<String>();
            dataLinePieces = lines[i].split(" ");
            
            timeSinceLR     = lastRace(dataLinePieces[0], date);                          //1st Param
            pastPerformance = pastPerformance(dataLinePieces[1], timeSinceLR);      //2nd, 3rd & 4th Param
            horseAndJockey  = horseAndJockey(dataLinePieces);                        // 5 & 6
            weight          = weight(dataLinePieces);   
            polePosition    = polePos(dataLinePieces);
            medication      = getMedication(dataLinePieces);
            equipment       = getEquipment(dataLinePieces);
            oddFav          = retrieveOdd(dataLinePieces);
            odds            = oddFav.get(0);
            favorite        = oddFav.get(1);
            //System.out.println("Here");
            
            indivHorse.add(timeSinceLR); indivHorse.add(pastPerformance[0]); 
            indivHorse.add(pastPerformance[1]); indivHorse.add(pastPerformance[2]); 
            indivHorse.add(horseAndJockey[0]); indivHorse.add(horseAndJockey[1]); 
            indivHorse.add(weight); indivHorse.add(medication);
            indivHorse.add(equipment); indivHorse.add(polePosition); 
            indivHorse.add(odds); indivHorse.add(favorite);
            
            horses.add(indivHorse);
            
            String horse = "";
            boolean frBool = false;
            if(dataLinePieces[0].equals("---"))
            {
                frBool = true;
            }
            horse = horse(dataLinePieces, dist);
            ArrayList<String> hData = refreshHorseList(horse, place, numOfHorses, dist, frBool, lines, lIndex, uIndex);
            
            //Adds NewDistance, AVESPRAT and LASTSPRAT
            for(int j = 0; j < hData.size(); j++)
            {
                String temp = hData.get(j);
                indivHorse.add(temp);
            }
            
            place++;
        }
        //System.out.println("---------------------");
        return horses;
    }
    
    public ArrayList<String> retrieveOdd(String[] dataPieces)
    {
        float odds = -1;
        int oddsIndex = -1;
        String isFavorited = "0";
        //Retrieves last occurance of period
        for(int i = 2; i < dataPieces.length; i++)
        {
            String[] tempVal = ((dataPieces[i]).split(""));
            for(int j = 0; j < tempVal.length-1; j++)
            {
                if( (tempVal[j]).equals(".") )
                {
                    oddsIndex = i;
                }
            }
        }
        String sOdds = "";
        try{
            String test1 = dataPieces[oddsIndex];
            String test2 = dataPieces[oddsIndex+1];
        } catch (Exception e)
        {
            System.out.println(e);
            System.out.println(pageCount);
        }
        sOdds = sOdds + dataPieces[oddsIndex] + dataPieces[oddsIndex+1];
        //System.out.println(sOdds);
        
        //Isolates Odds
        String o = "";
        String[] pieces = sOdds.split("");
        oddsLoop:
        for(int i = 0; i < pieces.length; i++)
        {
            if(pieces[i].equals("."))
            {
                o = o + "." + pieces[i+1] + "" + pieces[i+2];
                if(pieces[i+3].equals("*"))
                {
                    isFavorited = "1";
                }
                break oddsLoop;
            }else{
                o = o + pieces[i];
            }
        }
        //System.out.println(o);
        //System.out.println(isFavorited);
        ArrayList<String> returnArr = new ArrayList<String>();
        returnArr.add(o); returnArr.add(isFavorited);
        return returnArr;
    }
    
    public String getMedication(String[] dataPieces)
    {
        //A,B,C,L,M https://www.equibase.com/newfan/codes.cfm
        int indexParanth = -1;
        boolean firstParanth = false;
        for(int i = 0; i < dataPieces.length-1; i++)
        {
            String[] dPieces = dataPieces[i].split("");
            for(int j = 0; j < dPieces.length; j++)
            {
                if(dPieces[j].equals(")"))
                {
                    indexParanth = i + 2;
                    break;
                }
            }
        }
        
        String medication = "";
        findPP:
        for(int i = indexParanth; i < dataPieces.length-1; i++)
        {
            int ascii = (int)dataPieces[i].charAt(0);
            //System.out.println(" char: " + dataPieces[i] + "  ASCII: " + ascii);
            if(ascii == 65 || ascii == 66 || ascii == 67 || ascii == 76 || ascii == 77)
            {
                medication = medication + dataPieces[i];
            }else{
                if(ascii > 47 && ascii < 58 && !(dataPieces[i].equals(" ")))
                {
                    break findPP;
                }
            }
        }
        
        if(medication.equals(""))
        {
            medication = "None";
        }
        
        //System.out.println("M: " + medication);
        return medication;
    }
    
    public String getEquipment(String[] dataPieces)
    {
        //A,B,C,L,M https://www.equibase.com/newfan/codes.cfm
        int[] asciiOfEquip = new int[] {97,98,99,102,103,104,105,106,107,108,110,111,
                                        112,113,114,115,116,118,119,87,120,121,122};
        int indexParanth = -1;
        boolean firstParanth = false;
        for(int i = 0; i < dataPieces.length-1; i++)
        {
            String[] dPieces = dataPieces[i].split("");
            for(int j = 0; j < dPieces.length; j++)
            {
                if(dPieces[j].equals(")"))
                {
                    indexParanth = i + 2;
                    break;
                }
            }
        }
        
        String equipment = "";
        findEquip:
        for(int i = indexParanth; i < dataPieces.length-1; i++)
        {
            int ascii = (int)dataPieces[i].charAt(0);
            //System.out.println(" char: " + dataPieces[i] + "  ASCII: " + ascii);
            for(int j = 0; j < asciiOfEquip.length-1; j++)
            {
                int asciiCheck = asciiOfEquip[j];
                if(asciiCheck == ascii)
                {
                    equipment = equipment + dataPieces[i];
                }else{
                    if(ascii > 47 && ascii < 58 && !(dataPieces[i].equals(" ")))
                    {
                        break findEquip;
                    }
                }
            }
        }
        
        if(equipment.equals(""))
        {
            equipment = "None";
        }
        
        //System.out.println("E: " + equipment);
        return equipment;
    }
    
    public String polePos(String[] dataPieces)
    {
        String polePosition = "ERROR";
        int indexParanth = -1;  int indexPeriod = -1;
        boolean firstParanth = false;  boolean firstPeriod = false;
        for(int i = 0; i < dataPieces.length-1; i++)
        {
            String[] dPieces = dataPieces[i].split("");
            for(int j = 0; j < dPieces.length; j++)
            {
                if(dPieces[j].equals(")"))
                {
                    indexParanth = i + 2;
                    break;
                }
            }
        }
        
        // System.out.println("Paranth: " + indexParanth + "  Period: " + indexPeriod);
        
        //Between two indeces
        String polePos = "";
        findPP:
        for(int i = indexParanth; i < dataPieces.length-1; i++)
        {
            int ascii = (int)dataPieces[i].charAt(0);
            //System.out.println(" char: " + dataPieces[i] + "  ASCII: " + ascii);
            if(ascii > 47 && ascii < 58 && !(dataPieces[i].equals(" ")))
            {
                polePos = polePos + dataPieces[i];
                break findPP;
            }
        }
        
        if(polePos.equals(""))
        {
            for(int i = indexParanth; i < indexPeriod; i++)
            {
                int ascii = (int)dataPieces[i].charAt(0);
                System.out.println(" char: " + dataPieces[i] + "  ASCII: " + ascii);
            }
        }
        
        //System.out.println(polePos);
        return polePos;
    }
    
    public int checkIfME(String me)
    {
        String[] meS = new String[] {"a","b","c","f","g","h","i","j","k","l","n",
        "o","p","q","r","s","v","W","w","x","y","z"};
        for(int i = 0; i < meS.length-1; i++)
        {
            if(meS[i].equals(me))
            {
                return -1;
            }
        }        
        return 1;
    }
    
    public String[] horseAndJockey(String[] dataPieces)
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
        
        hAndJ[0] = hName;
        hAndJ[1] = jName;
        //System.out.println("Horse: " + hName + "  Jockey: " + jName);
        if(jName == "" && !hName.equals(""))
        {
            String [] returnStr = new String[2];
            returnStr[1] = "ERROR";
            returnStr[0] = jName;
            return returnStr;
        }
        
        if(hName == "" && !jName.equals(""))
        {
            String [] returnStr = new String[2];
            returnStr[0] = "ERROR";
            returnStr[1] = jName;
            return returnStr;
        }
        
        if(hName == "" && jName == "")
        {
            String [] returnStr = new String[2];
            returnStr[0] = "ERROR";
            returnStr[1] = "ERROR";
            return returnStr;
        }
        
        return hAndJ;
    }
    
    public String weight(String[] dataPieces)
    {
        int[] weightAndIndex = new int[] {-1,-1};
        int wIndex = -1;
        for(int i = 2; i < dataPieces.length; i++)
        {
            String[] tempVal = ((dataPieces[i]).split(""));
            if( (tempVal[tempVal.length-1]).equals(")") )
            {
                wIndex = i+1;
            }
        }
        //System.out.println(dataPieces[wIndex]);
        
        try{
            String[] weightPieces = (dataPieces[wIndex]).split("");
        } catch (Exception e)
        {
            System.out.println("PAGE: " + pageCount);
        }
        String[] weightPieces = (dataPieces[wIndex]).split("");
        String cWeight = "";
        for(int i = 0; i < weightPieces.length; i++)
        {
            String[] cWSplit = cWeight.split("");
            if(weightPieces[i].equals("0") || weightPieces[i].equals("1") || weightPieces[i].equals("2") || weightPieces[i].equals("3") 
            || weightPieces[i].equals("4") || weightPieces[i].equals("5") || weightPieces[i].equals("6") || weightPieces[i].equals("7") 
            || weightPieces[i].equals("8") || weightPieces[i].equals("9") && (cWSplit.length < 4))
            {
                cWeight = cWeight + weightPieces[i];
            }else{
                if(cWSplit.length >= 4)
                {
                    showError();
                    System.out.println("ERROR: CRITICAL - WEIGHT");
                }
            }
        }
        //System.out.print(cWeight);
        
        //return Integer.valueOf(cWeight);
        return cWeight;
    }
    
    public String[] pastPerformance(String groupedData, String timeSinceLR)
    {
        if(timeSinceLR.equals("-1"))
        {
            String[] firstRace = new String[] {"-1", "-1", "-1"};
            //System.out.println("PR: " + firstRace[0] + "    PT: " + firstRace[1] + "   FP: " + firstRace[2]);
            return firstRace;
        }
        String [] cleanData = new String[3];
        String[] comps = groupedData.split("");
        boolean tripped = false;
        
        String pastRace = "";
        String pastTrack = "";
        String pastFinishingPos = "";
        
        for(int i = 0; i < comps.length; i++)
        {
            if(!comps[i].equals("0") && !comps[i].equals("1") && !comps[i].equals("2") && !comps[i].equals("3") && !comps[i].equals("4") && !comps[i].equals("5") && !comps[i].equals("6")
            && !comps[i].equals("7") && !comps[i].equals("8") && !comps[i].equals("9"))
            {
                pastTrack = pastTrack + comps[i];
                tripped = true;
            }else{
                if(tripped == false)
                {
                    pastRace = pastRace + comps[i];
                }else{
                    if(tripped == true)
                    {
                        pastFinishingPos = pastFinishingPos + comps[i];
                    }
                }
            }
        }
        cleanData[0] = pastRace;
        cleanData[1] = pastTrack;
        cleanData[2] = pastFinishingPos;
        
        //Unknown Race Number
        char temp1 = ((comps[0]).toLowerCase()).charAt(0); char temp2 = ((comps[1]).toLowerCase()).charAt(0); char temp3 = ((comps[2]).toLowerCase()).charAt(0);
        int ascii1 = (int)(temp1); int ascii2 = (int)(temp2); int ascii3 = (int)(temp3);
        if( (ascii1 >= 97 && ascii1 <= 122) && (ascii2 >= 97 && ascii2 <= 122) && (ascii3 >= 97 && ascii3 <= 122) )
        {
            cleanData[0] = "u"; 
            pastRace = cleanData[0];
            cleanData[1] = comps[0] + comps[1] + comps[2];
            pastTrack = cleanData[1];
            cleanData[2] = comps[3];
            pastFinishingPos = cleanData[2];
        }
        
        
        if(pastFinishingPos.equals("") || pastRace.equals("") || pastTrack.equals(""))
        {
            //System.out.println(pastRace + "," + pastTrack + "," +  pastFinishingPos);
            if(!cleanData[1].equals("") && !cleanData[2].equals(""))
            {
                cleanData[0] = "u";
                //System.out.println(cleanData[0] + "," + cleanData[1] + "," +cleanData[2]);
            }else{
                String errMessage[] = new String[1]; errMessage[0] = "ERROR";
                showError();
                System.out.println("ERROR: pastPerformance()");
                
                //System.out.println(Integer.valueOf(comps[0]));
                for(int i = 0; i < comps.length; i++)
                {
                    System.out.println(comps[i]);
                    char temp = ((comps[i]).toLowerCase()).charAt(0);
                }
                System.out.println(pageCount);
                return errMessage;
            }
        }
        return cleanData;
    }
    
    public String lastRace(String oDate, String currentDate)
    {
        if(oDate.equals("---"))
        {
            //System.out.println("Current Date: " + currentDate + "   Last Raced: FR");
            return "-1";
        }
        String[] comps = oDate.split("");
        //System.out.println(oDate);
        String month = "";
        String day = "";
        String year = "";
        boolean tripped = false;
        for(int i = 0; i < comps.length; i++)
        {
            //System.out.println(comps[i]);
            if(!comps[i].equals("0") && !comps[i].equals("1") && !comps[i].equals("2") && !comps[i].equals("3") && !comps[i].equals("4") && !comps[i].equals("5") && !comps[i].equals("6")
            && !comps[i].equals("7") && !comps[i].equals("8") && !comps[i].equals("9"))
            {
                month = month + comps[i];
                tripped = true;
            }else{
                if(tripped == false)
                {
                    day = day + comps[i];
                }else{
                    if(tripped == true)
                    {
                        year = year + comps[i];
                    }
                }
            }
        }
        
        if(year.equals("20") || year.equals("19") || year.equals("18") || year.equals("17")
        || year.equals("16") || year.equals("15"))
        {
            year = "20" + "" + year;
            //System.out.println("YEAR: " + year);
        }else{
            showError();
            System.out.println("ERROR: OLDER THAN ANTICIPATED : " + year);
            year = "-1";
            return("ERROR");
        }
        
        if(month.equals("Jan"))
        {month = "01";}
        if(month.equals("Feb"))
        {month = "02";}
        if(month.equals("Mar"))
        {month = "03";}
        if(month.equals("Apr"))
        {month = "04";}
        if(month.equals("May"))
        {month = "05";}
        if(month.equals("Jun"))
        {month = "06";}
        if(month.equals("Jul"))
        {month = "07";}
        if(month.equals("Aug"))
        {month = "08";}
        if(month.equals("Sep"))
        {month = "09";}
        if(month.equals("Oct"))
        {month = "10";}
        if(month.equals("Nov"))
        {month = "11";}
        if(month.equals("Dec"))
        {month = "12";}
        boolean isMonthCode = false;
        if(Integer.parseInt(month) > 0 && Integer.parseInt(month) < 13)
        {
            isMonthCode = true;
        }
        if(isMonthCode == false)
        {
            showError();
            System.out.println("ERROR: lastRace(), PROB SPELLING ABRV"); return("ERROR");
        }
        
        if((day.split("")).length == 1)
        {
            day = "0"+day;
        }else{}
        String newDate = day+"/"+month+"/"+year;
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate raceDate = LocalDate.parse(currentDate, formatter);
        LocalDate lastRacedDate = LocalDate.parse(newDate, formatter);
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tenSecondsLater = now.plusSeconds(10);
     
        int diff = (int)(ChronoUnit.DAYS.between(lastRacedDate, raceDate));
        
        //System.out.println("Current Date: " + currentDate + "   Last Raced: " + newDate);
        //System.out.println(diff);
        String sDiff = diff + "";
        //System.out.println(sDiff);
        return sDiff;
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
        
        if(dataLinePieces[0].equals("One") && dataLinePieces[1].equals("And") 
        && dataLinePieces[2].equals("One") && dataLinePieces[3].equals("Fourth"))
        {
            //return 1.125;
            return "12500";
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
        
        if(dataLinePieces[0].equals("One") && dataLinePieces[1].equals("And") && 
        dataLinePieces[2].equals("Five") && dataLinePieces[3].equals("Eighth"))
        {
            //return 1;
            return "16250";
        }
        
        if(dataLinePieces[0].equals("One") && dataLinePieces[1].equals("And") && 
        dataLinePieces[2].equals("Three") && dataLinePieces[3].equals("Fourth"))
        {
            //return 1;
            return "17500";
        }
        
        if(dataLinePieces[0].equals("About") && dataLinePieces[1].equals("Two") && 
        dataLinePieces[2].equals("And") && dataLinePieces[3].equals("One") 
        &&  dataLinePieces[4].equals("Sixteenth"))
        {
            //return 1;
            return "20500";
        }
        
        if(dataLinePieces[0].equals("Two") && dataLinePieces[1].equals("And") && 
        dataLinePieces[2].equals("One") && dataLinePieces[3].equals("Sixteenth"))
        {
            //return 1;
            return "20625";
        }
        
        if(dataLinePieces[0].equals("About") && dataLinePieces[1].equals("Two") && 
        dataLinePieces[2].equals("And") && dataLinePieces[3].equals("Three")
        && dataLinePieces[4].equals("Eighth"))
        {
            //return 1;
            return "23000";
        }
        
        if(dataLinePieces[0].equals("Two") && dataLinePieces[1].equals("And") && 
        dataLinePieces[2].equals("Three") && dataLinePieces[3].equals("Eighth"))
        {
            //return 1;
            return "23750";
        }
        
        //Checks Distance Type
        //System.out.println(dataLinePieces[0]);
        showError();
        System.out.print("ERROR: distance() - MISSING DISTANCE VALUE OF:  "); 
        System.out.println(lines[index]);
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
            //if(raceNum.equals("1") || raceNum.equals("2") || raceNum.equals("3") || raceNum.equals("4") || raceNum.equals("5") 
            //|| raceNum.equals("6") || raceNum.equals("7") || raceNum.equals("8") || raceNum.equals("9"))
            if(ascii1 >= 49 && ascii1 <= 57)
            {
                if(ascii2 >= 48 && ascii2 <= 57)
                {
                    return (raceNum+raceNumPart2+"");
                }
            }
        }
        
        //int ascii0 = (int)raceNum.charAt(0);
        if(raceNum.equals("1") || raceNum.equals("2") || raceNum.equals("3") || raceNum.equals("4") || raceNum.equals("5") || raceNum.equals("6")  || raceNum.equals("7") ||
        raceNum.equals("8") || raceNum.equals("9") || raceNum.equals("10") || raceNum.equals("11") || raceNum.equals("12") || raceNum.equals("13") || raceNum.equals("14") )
        {
            return raceNum;
        }
        
        showError();
        System.out.println("ERROR: raceNumber() - FILTERED NON NUMBER OR RACE NUMBER TOO HIGH. FILTERED VALUE: " + raceNum);
        return "ERROR";
    }
    
    public String weather(String[] lines)
    {
        int index = -1;
        String[] dataLinePieces;
        for(int i = 0; i < lines.length-1; i++)
        {
            dataLinePieces = lines[i].split(" ");
            if(dataLinePieces[0].equals("Weather:"))
            {
                //System.out.println(dataLinePieces[0]);
                index = i;
            }
        }
        
        if(index == -1)
        {
            showError();
            System.out.println("ERROR: weather() - Could not find weather");
            return "ERROR";
        }
        dataLinePieces = lines[index].split(" ");
        return dataLinePieces[1];
    }
    
    public String trackCondition(String[] lines)
    {
        int index = -1;
        String[] dataLinePieces;
        for(int i = 0; i < lines.length-1; i++)
        {
            dataLinePieces = lines[i].split(" ");
            if(dataLinePieces[0].equals("Weather:"))
            {
                //System.out.println(dataLinePieces[0]);
                index = i;
            }
        }
        
        dataLinePieces = lines[index].split(" "); //Splits Line into words seperated by " "
        dataLinePieces = dataLinePieces[dataLinePieces.length-1].split(" ");
        //String trackCondition = dataLinePieces[dataLinePieces.length-1];
        String trackCondition = dataLinePieces[0]; //System.out.println(trackCondition);
        String[] temp = trackCondition.split("");
        
        /* TRACK CONDITION FILTER */
        //Fast
        if(temp.length == 5 && temp[0].equals("F") && temp[1].equals("i") && temp[2].equals("r") &&
        temp[3].equals("m"))
        {
            return "Firm";
        }
        
        //Firm
        if(temp.length == 5 && temp[0].equals("F") && temp[1].equals("a") && temp[2].equals("s") &&
        temp[3].equals("t"))
        {
            return "Fast";
        }
        
        if(temp.length == 5 && temp[0].equals("G") && temp[1].equals("o") && temp[2].equals("o") &&
        temp[3].equals("d"))
        {
            return "Good";
        }
        
        if(temp.length == 5 && temp[0].equals("S") && temp[1].equals("o") && temp[2].equals("f") &&
        temp[3].equals("t"))
        {
            return "Soft";
        }
        
        //Muddy
        if(temp.length == 6 && temp[0].equals("M") && temp[1].equals("u") && temp[2].equals("d") &&
        temp[3].equals("d") && temp[4].equals("y"))
        {
            return "Muddy";
        }
        
        //Yield
        if(temp.length == 9 && temp[0].equals("Y") && temp[1].equals("i") && temp[2].equals("e") &&
        temp[3].equals("l") && temp[4].equals("d") && temp[5].equals("i"))
        {
            return "Yielding";
        }
        
        //Sloppy
        if(temp.length == 7 && temp[0].equals("S") &&  temp[1].equals("l") && temp[2].equals("o") 
        && temp[3].equals("p") && temp[4].equals("p"))
        {
            return "Sloppy";
        }
        
        //Sloppy (Sealed)
        if(temp.length == 9 && temp[1].equals("S") && temp[2].equals("e") && temp[3].equals("a")
        && temp[4].equals("l") && temp[5].equals("e") && temp[6].equals("d"))
        {
            dataLinePieces = lines[index].split(" ");
            trackCondition = dataLinePieces[3].split(" ")[0];
            temp = trackCondition.split("");
            if(temp.length == 6 && temp[0].equals("S") &&  temp[1].equals("l") && temp[2].equals("o")
            &&  temp[3].equals("p"))
            {
                return "Sloppy (Sealed)";
            }
        }
        
        //Pick out 'Muddy (Sealed)'
        dataLinePieces = lines[index].split(" ");
        trackCondition = dataLinePieces[3].split(" ")[0];
        if(trackCondition.equals("Muddy"))
        {
            return "Muddy (Sealed)";
        }
        
        //Pick Out 'Good (Sealed)'
        if(trackCondition.equals("Good"))
        {
            return "Good (Sealed)";
        }
        
        if(trackCondition.equals("Fast"))
        {
            return "Fast (Sealed)";
        }
        
        //Spells out Track Condition if fails 
        for(int i = 0; i < temp.length-1; i++)
        {
            System.out.print(temp[i] +",");
        }
        System.out.print(temp.length);
        
        showError();
        System.out.println("ERROR: trackCondition() - NEW TRACK CONDITION " + trackCondition);
        return "ERROR";
    }
    
    
    //HORSE EVALS
    public ArrayList<String> refreshHorseList(String currHorse, double place, double numberOfRaceHorses, String dist, boolean firstRace, String[] lines, int lowerBound, int upperBound)
    {
        ArrayList<String> returnList = new ArrayList<String>();
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
                ArrayList<String> newD = new ArrayList<String>(); String nd = "";
                if(firstRace == true)
                {
                    nd = "0";
                    newD.add("-1");
                    //System.out.println("FR hit");
                }else{
                    newD.add("0"); nd = "0";
                }
                newDistanceEvaluations.add(newD);
                
                //Evaluate AVESPRAT
                double sprat = calculateSPRAT(lines, place, lowerBound, upperBound);
                ArrayList<Double> sprats = new ArrayList<Double>(); sprats.add(sprat); SPRAT.add(sprats);
                AVESPRAT.add(sprat);
                
                int sr = (int) Math.rint(sprat);
                String frValue = Integer.toString(firstRaceSpeedValue);
                returnList.add(nd); returnList.add(frValue); returnList.add(frValue);
            }else{
                appearances.set(index,appearances.get(index)+1);
                updateRecord(currHorse, (int)place);
                updateDistances(currHorse, dist);
                String ndReturn       = updateNewDist(currHorse, dist);
                String spratReturn    = updateSPRAT(currHorse, calculateSPRAT(lines, place, lowerBound, upperBound));
                String avespratReturn = updateAVESPRAT(currHorse, calculateSPRAT(lines, place, lowerBound, upperBound));
                returnList.add(ndReturn); returnList.add(spratReturn); returnList.add(avespratReturn);
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
            ArrayList<String> newD = new ArrayList<String>(); String nd = "";
            if(firstRace == true)
            {
                nd = "0";
                newD.add("-1");
                //System.out.println("FR hit");
            }else{
                nd = "0";
                newD.add("0");
            }
            newDistanceEvaluations.add(newD);
            
            double sprat = calculateSPRAT(lines, place, lowerBound, upperBound);
            ArrayList<Double> sprats = new ArrayList<Double>(); sprats.add(sprat); SPRAT.add(sprats);
            AVESPRAT.add(sprat);
            
            
            //Round Sprat to whole number
            int sr = (int) Math.round(sprat);
            String frValue = Integer.toString(firstRaceSpeedValue);
            returnList.add(nd); returnList.add(frValue); returnList.add(frValue);
            
            //System.out.println(sprat);
            //System.out.println(sr);
        }
        
        return returnList;
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
                dist = Integer.toString(didNotFinishPenalty);
                totalDistBehind = totalDistBehind + didNotFinishPenalty;
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
        
        double offset = finalTimeTrackRecordDiff*offsetWeight;
        double horseSpeedRating = 100.0 - offset - (totalDistBehindWeight*totalDistBehind);
        //System.out.println("Speed rating for horse: " + horseSpeedRating + "   TimeDiffer: " + finalTimeTrackRecordDiff);
        BigDecimal bd = new BigDecimal(horseSpeedRating).setScale(2, RoundingMode.HALF_UP);
        horseSpeedRating = bd.doubleValue();
        
        
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
    
    
    public String updateAVESPRAT(String hName, double s)
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
        if(speedRatings.size() >= numberOfRacesToAverage)
        {
            for(int i = speedRatings.size()-numberOfRacesToAverage; i < speedRatings.size(); i++)
            {
                total = total + speedRatings.get(i);
            }
            avgSprat = total/numberOfRacesToAverage;
        }else{
            for(int i = 0; i < speedRatings.size(); i++)
            {
                total = total + speedRatings.get(i);
            }
            avgSprat = total/speedRatings.size();
        }
        double pastAvg =  AVESPRAT.get(flagIndex);
        BigDecimal bd = new BigDecimal(pastAvg).setScale(2, RoundingMode.HALF_UP);
        pastAvg = bd.doubleValue();
        
        bd = new BigDecimal(avgSprat).setScale(2, RoundingMode.HALF_UP);
        avgSprat = bd.doubleValue();
        AVESPRAT.set(flagIndex, avgSprat);
        
        //Should return past average priot to new entry
        int sr = (int) Math.rint(pastAvg);
        return Integer.toString(sr);
    }
    
    public String updateSPRAT(String hName, double s)
    {
        int flagIndex = -1;
        for(int i = 0; i < allHorses.size(); i++)
        {
            if(allHorses.get(i).equals(hName))
            {
                flagIndex = i;
            }
        }
        
        ArrayList<Double> localS = SPRAT.get(flagIndex);
        double lastSpeedRating = localS.get(localS.size()-1);
        SPRAT.get(flagIndex).add(s);
        
        //Should return past speed rating priot to new entry
        int sr = (int)Math.rint(lastSpeedRating);
        return Integer.toString(sr);
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
        if(horsesDistances.size() < newDistRaceMinimum)
        {
           newDistanceEvaluations.get(flagIndex).add("0");
           return "0";
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
            if(count >= newDistMinimum)
            {
                newDistanceEvaluations.get(flagIndex).add("1");
                return "1";
            }else{
                newDistanceEvaluations.get(flagIndex).add("0");
                return "0";
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
            showError();
            return "ERROR";
        }
        
        return hName;
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