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
 * Write a description of class BinaryClassification here.
 *
 * @author (Nathan Beal)
 * @version (1.0.0 - 12/15/20)
 */
public class BinaryClassification
{
    long t1; long t2;
    int pageCount = 0;
    int totalPagesProcessed = 0;
    String date;
    String raceNum;
    int totalRacesProcessed = 0;
    int priorRaceNum;
    int numProcessed = 0;
    int totalPagesRemoved = 0;
    String sNumOfHorses;
    int countryTagsRemoved = 0;
    
    //CONSTANTS
    int numberOfDesiredRaces = 1000;   
    
    
    //SPRAT SHIT
    int didNotFinishPenalty = 5;
    int firstRaceSpeedValue = 45;
    double totalDistBehindWeight = 1.2;
    double offsetWeight = 5;
    int numberOfRacesToAverage = 4; //Number of races use in the rolling ave calculation for avespeed
    int newDistRaceMinimum = 4;
    int newDistMinimum = 3;
    
    ArrayList<ArrayList<String>> formattedData = new ArrayList<ArrayList<String>>();
    ArrayList<ArrayList<String>> jockeyRecordHistory = new ArrayList<ArrayList<String>>();
    ArrayList<Integer> badPageIndex = new ArrayList<Integer>();
    ArrayList<ArrayList<String>> testingSet = new ArrayList<ArrayList<String>>();
    
    ArrayList<Integer> bootPages = new ArrayList<Integer>();
    
    
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
    public BinaryClassification(){}

    public static void main() throws IOException 
    {
        //String FILE = "AQU 2017-2020";
        String FILE = "AQU 2017-2020-EDITED";
        
        BinaryClassification demo = new BinaryClassification();
        demo.run(FILE);
    }
    
    public void run(String fileExt) throws IOException 
    {
        System.out.println("Developing Training and Validation Sets: ");
        t1 = System.currentTimeMillis();
        File myFile = new File("C:/Users/natha/Desktop/ML/AQU Testing/" + fileExt + ".pdf");
        
        String text;
        String[] pages;
        int numPages;
        
        try (PDDocument doc = PDDocument.load(myFile)) 
        {
            System.out.println("    1. Processing pdf files");
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
        
        int additionalGap = 0;
        //int additionalGap = calcAdditionalGap(numPages, pages);
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
                date = retrieveDate(lines)[0]; // [0] is dd/mm/yy (maybe needed for time between last race) [1] is mm/dd/yy
                raceNum = raceNumber(lines);
                String trackType = trackType(lines);
                String weather = weather(lines);
                String distance = distance(lines);                      //True Double
                String trackCondition = trackCondition(lines);  
                 
                //Horse Data
                ArrayList<String> generalData = new ArrayList<String>();
                generalData.add(retrieveDate(lines)[1]); generalData.add(raceNum); generalData.add(trackType);
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
            
                if(pageCount > (lim-lim))//(lim- numberOfDesiredRaces))
                {
                    dataPrep(numHorses, generalData, racerData);
                    numProcessed++;
                }
            }else{
                totalPagesRemoved++;
                bootPages.add(pageCount);
                //System.out.println("Page " + i + " is blocked");
            }
        } 
        //printData();
        //System.out.println("Waiting to Write to CSV");
        writeToCSV();
        
        //Coment s
        System.out.println("    - Comments");
        System.out.println("        * Country Tags Removed:   " + countryTagsRemoved);
        System.out.println("        * Races Processed:        " + numProcessed);
        System.out.println("        * Dead Pages:             " + (lim -numProcessed) );
        
        t2 =  System.currentTimeMillis();
        long totalTime = (t2-t1)/1000;
        System.out.println("    - Time Elapsed: " + totalTime + " seconds");
        System.out.println("");
        //System.out.println(additionalGap + ", NumProces: " + numProcessed);
        
        
        // Stripping Test Race and passing too different class to 
        System.out.println("Developing Test Set: ");
        TestingSet demo2 = new TestingSet(); long t4; long t3;
        t3 = System.currentTimeMillis();
        demo2.run(testingSet);
        t4 = System.currentTimeMillis();
        System.out.print("    - Time Elapsed: " + (t4-t3)/1000 + " seconds");
        totalTime = totalTime + (t4-t3)/1000;

        
        for(int i = 0; i < bootPages.size(); i++)
        {
            if(i % 30 == 0)
            {
                System.out.println("");
            }
            System.out.print(bootPages.get(i) + ", ");
        }
        
    }
    
    public int calcAdditionalGap(int numPages, String[] pages)
    {
        System.out.println("HERE");
        int cleanPages = 0;
        int offset = 0;
        String currentRace;
        while(cleanPages != numberOfDesiredRaces)
        {
            for(int i = (pageCount + 1); i > (numPages+1 - offset - numberOfDesiredRaces); i--)
            {
                offset = 0;
                ArrayList<String> tempArr = new ArrayList<String>();
            
                currentRace = pages[pageCount];
                String[] lines = currentRace.split("\n");
                
                boolean isRaceData = checkDocument(lines);
                if(isRaceData == false)
                {
                    offset++;
                }else{
                    cleanPages++;
                }
                if(cleanPages == 50)
                {
                    int startIndex = numPages+1 - offset - numberOfDesiredRaces;
                    System.out.println("StartIndex: " + startIndex);
                }
            }
        }
        
        int gap = 0;
        
        return gap;
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
            
            timeSinceLR     = lastRace(dataLinePieces[0], date);
            pastPerformance = pastPerformance(dataLinePieces[1], timeSinceLR);       //2nd, 3rd & 4th Param
            horseAndJockey  = horseAndJockey(dataLinePieces);                        // 5 & 6
            oddFav          = retrieveOdd(dataLinePieces);
            odds            = oddFav.get(0);
            favorite        = oddFav.get(1);
            //System.out.println("Here");
            
            indivHorse.add(pastPerformance[2]); 
            indivHorse.add(horseAndJockey[0]); indivHorse.add(horseAndJockey[1]); 
            indivHorse.add(odds); indivHorse.add(favorite);
            
            //Trifecta or not
            if(place <= 3)
            {
                indivHorse.add("1");            
            }else{
                indivHorse.add("0");  
            }
            
            String horse = "";
            boolean frBool = false;
            if(dataLinePieces[0].equals("---"))
            {
                frBool = true;
            }
            horse = horse(dataLinePieces, dist);
            ArrayList<String> hData = refreshHorseList(horse, place, numOfHorses, dist, frBool, lines, lIndex, uIndex);
            
            /*
            //Adds NewDistance, AVESPRAT and LASTSPRAT
            for(int j = 0; j < hData.size(); j++)
            {
                String temp = hData.get(j);
                indivHorse.add(temp);
            }
            */
            boolean checkJockey = checkJockeyList(horseAndJockey[1]);
            String last3 = "0";
            String last5 = "0";
            String last10 = "0";
            if (checkJockey == true) //Jockey is in registry
            {
                last3 = (retrieveJockeyRecord(horseAndJockey[1]).get(7));
                last5 = (retrieveJockeyRecord(horseAndJockey[1]).get(8));
                last10 = (retrieveJockeyRecord(horseAndJockey[1]).get(9));
            }else{//Jockey is not in registry
                addJockey(horseAndJockey[1], (int)place);
            }
            
            indivHorse.add(String.valueOf(last3)); //Accumulation of Past 10 starts as jockey
            indivHorse.add(String.valueOf(last5));
            indivHorse.add(String.valueOf(last10));
            horses.add(indivHorse);
            
            updateJockeyRecord(horseAndJockey[1], (int)place); //Refreshes Jockey Record for next time
            place++;
        }
        //System.out.println("---------------------");
        return horses;
    }
    
    public boolean checkJockeyList(String jockeyName)
    {
        for(int i = 0 ; i < jockeyRecordHistory.size(); i++)
        {
            if(jockeyName.equals(jockeyRecordHistory.get(i).get(0)))
            {
                return true;
            }
        }
        return false;
    }
    
    public ArrayList<String> retrieveJockeyRecord(String jockeyName)
    {
        for(int i = 0 ; i < jockeyRecordHistory.size(); i++)
        {
            if(jockeyName.equals(jockeyRecordHistory.get(i).get(0)))
            {
                return jockeyRecordHistory.get(i);
            }
        }
        ArrayList<String> empty = new ArrayList<String>();
        return empty;
    }
    
    public void addJockey(String jockeyName, int place)
    {
        ArrayList<String> addJockey = new ArrayList<String>();
        
        //Add as many stats as eanted: Name/W/P/S
        addJockey.add(jockeyName); addJockey.add("0"); addJockey.add("0"); addJockey.add("0");
        addJockey.add("1"); addJockey.add("0"); addJockey.add("0"); //Appear/WP/T3P
        addJockey.add("0"); addJockey.add("0"); addJockey.add("0"); //L3/L5/L10
        addJockey.add(String.valueOf(place)); //Compelte History
        if(place < 4 && place > 0)
        {
            addJockey.set(place,String.valueOf(place));
            if(place == 1)
            {
                addJockey.set(5,"1");//WP = 1;
            }
            addJockey.set(6,"1"); //T3P
            addJockey.set(7,"1"); //L3
            addJockey.set(8,"1"); //L5
            addJockey.set(9,"1"); //L10
        }
        
        jockeyRecordHistory.add(addJockey); 
    }
    
    public void updateJockeyRecord(String jockeyName, int place)
    {
        ArrayList<String> jockey = new ArrayList<String>();
        findJockeyLoop:
        for(int i = 0 ; i < jockeyRecordHistory.size(); i++)
        {
            if(jockeyName.equals(jockeyRecordHistory.get(i).get(0)))
            {
                jockey = jockeyRecordHistory.get(i);
                break findJockeyLoop;
            }
        }
        
        //Add an appearance
        jockey.set(4, String.valueOf((Integer.valueOf(jockey.get(4)) + 1)));
        if(place < 4 && place > 0)
        {
            jockey.set(place, String.valueOf((Integer.valueOf(jockey.get(place)) + 1)));
            if(place == 1)
            {
                //double WP = jockey.get(1)/jockey.get(4));
                jockey.set(5, String.valueOf(  (double)(Integer.valueOf(jockey.get(1))/Integer.valueOf(jockey.get(4))  ))); //Winning Percentage
            }
            int WPS = (Integer.valueOf(jockey.get(1))) + (Integer.valueOf(jockey.get(2))) + (Integer.valueOf(jockey.get(3)));
            jockey.set(6, String.valueOf(  (double)(WPS/Integer.valueOf(jockey.get(4))  ))); //TP3
        }
                    
        String newRace = jockey.get(10) + " " + String.valueOf(place);
        //System.out.println("RACES: " + newRace);
        String [] races = newRace.split(" "); 
        //System.out.println("RACES LENGTH: " + races.length);
        //Last 3
        String val3 = "0";
        if(races.length <= 3 && races.length > 0)
        {
            for(int i = 0; i < races.length-1; i++)
            {
                int raceVal = Integer.valueOf(races[i]);
                if(raceVal < 4)
                {
                    raceVal = 1;
                }else{
                    raceVal = 0;
                }
                
                val3 = String.valueOf(Integer.valueOf(val3) + raceVal);
            }
        }else{
            if(races.length > 3)
            {
                for(int i = races.length - 1; i > (races.length - 1) - 3; i--)
                {
                    int raceVal = Integer.valueOf(races[i]);
                    if(raceVal < 4)
                    {
                        raceVal = 1;
                    }else{
                        raceVal = 0;
                    }
                    
                    val3 = String.valueOf( Integer.valueOf(val3) + raceVal);
                }
            }
        }
        
        //Last 5
        String val5 = "0";
        if(races.length <= 5)
        {
            for(int i = 0; i < races.length-1; i++)
            {
                int raceVal = Integer.valueOf(races[i]);
                if(raceVal < 4)
                {
                    raceVal = 1;
                }else{
                    raceVal = 0;
                }
                
                val5 = String.valueOf( Integer.valueOf(val5) + raceVal);
            }
        }else{
            if(races.length > 5)
            {
                for(int i =  races.length-1; i > races.length - 6; i--)
                {
                    int raceVal = Integer.valueOf(races[i]);
                    if(raceVal < 4)
                    {
                        raceVal = 1;
                    }else{
                        raceVal = 0;
                    }
                    
                    val5 = String.valueOf( Integer.valueOf(val5) + raceVal);
                }
            }
        }
        
        
        //Last 10
        String val10 = "0";
        if(races.length <= 10)
        {
            for(int i = 0; i < races.length-1; i++)
            {
                int raceVal = Integer.valueOf(races[i]);
                if(raceVal < 4)
                {
                    raceVal = 1;
                }else{
                    raceVal = 0;
                }
                
                val10 = String.valueOf( Integer.valueOf(val10) + raceVal);
            }
        }else{
            if(races.length > 10)
            {
                for(int i =  races.length-1; i > races.length - 11; i--)
                {
                    int raceVal = Integer.valueOf(races[i]);
                    if(raceVal < 4)
                    {
                        raceVal = 1;
                    }else{
                        raceVal = 0;
                    }
                    
                    val10 = String.valueOf( Integer.valueOf(val10) + raceVal);
                }
            }
        }
        
        jockey.set(7, val3);
        jockey.set(8, val5);
        jockey.set(9, val10);
        jockey.set(10, newRace);
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
        
        for(int i = 0; i < racerData.size(); i++)
        {
            ArrayList<String> singularHorse = racerData.get(i);
            ArrayList<String> assembledData = new ArrayList<String>();
            
            assembledData.add(genData.get(0));       //DATE
            assembledData.add(genData.get(1));       //DATE
            assembledData.add(singularHorse.get(1)); //Horse Name
            assembledData.add(singularHorse.get(2)); //Jockey Name
            assembledData.add(singularHorse.get(3)); //Odds
            assembledData.add(singularHorse.get(4)); //Favorite
            assembledData.add(singularHorse.get(0)); //Past Finish
            assembledData.add(singularHorse.get(6)); //Last 3
            assembledData.add(singularHorse.get(7)); //Last 5
            assembledData.add(singularHorse.get(8)); //Last 10
            assembledData.add(singularHorse.get(5)); //Trifecta Finish
            
            
            localRace.add(assembledData);
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
        System.out.println("    2. Writing to CSV");
        int offset = 0;
        try (PrintWriter writer = new PrintWriter(new File("Data.csv"))) 
        {
            StringBuilder sb = new StringBuilder();
            
            //VARIABLES
            sb.append("date,"); sb.append("raceNum,"); sb.append("hName,"); sb.append("jName,"); sb.append("odds,"); sb.append("fav,");sb.append("pastPos,");  
            sb.append("last3,"); sb.append("last5,"); sb.append("last10,"); sb.append("trifecta,");
            sb.append('\n');
            
            //Used to calculate index to snag a Race from
            int race = Integer.valueOf(formattedData.get(formattedData.size()-1).get(1));
            int index = -1;
            isolateRace:
            for(int i = formattedData.size()-1; i > 0; i--)
            {
                if(race != Integer.valueOf(formattedData.get(i).get(1)))
                {
                    index = i;
                    break isolateRace;
                }
            }
            
            offset = formattedData.size() - index - 1; 
            for(int i = 0; i < formattedData.size() - offset; i++)
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
            System.out.println("    3. Finished writing to CSV (Data.csv)");
        } catch (FileNotFoundException e) {
          System.out.println(e.getMessage());
        }
        
        for(int i = formattedData.size()-offset; i < formattedData.size(); i++)
        {
            ArrayList<String> writeDataStr = formattedData.get(i);
            testingSet.add(writeDataStr);
        }
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
        || year.equals("16") || year.equals("15") || year.equals("14") || year.equals("13") || year.equals("12"))
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
        
        /* PAST FINISHING POSITION TOP 3 */
        if(pastFinishingPos.equals("1") || pastFinishingPos.equals("2") || pastFinishingPos.equals("3"))
        {
            pastFinishingPos = "2";
        }else{
            if(pastFinishingPos.equals("4") || pastFinishingPos.equals("5"))
            {
                pastFinishingPos = "1";
            }else{
                pastFinishingPos = "0";
            }
        }
        //System.out.println("Past Finish Pos EVAl: " + pastFinishingPos);
        cleanData[2] = pastFinishingPos;
        
        
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
        //System.out.println("Old Name: " + jName);
        
        //Horse Name
        String hName = "";
        //showError();
        
        // Fixes Issue where parts of Horses Name was cut off
        int hIndex1 = 0;
        horseNameFirstBound:
        for(int i = jIndex1; i > 0; i--)
        {
            if( (int)(dataPieces[i].charAt(0)) >= 48 && (int)(dataPieces[i].charAt(0)) <= 57)
            {
                hIndex1 = i+1;
                break horseNameFirstBound;
            }
        }
        
        //Remove Country Designation -> So manual entry is less complex
        //System.out.println("PC: " + pageCount + "  DATE: " + date + "  Race Num: " + raceNum);
        if(dataPieces[jIndex1-1].charAt(0) == 40)
        {
            //System.out.println("Removing Country Tag: " + dataPieces[jIndex1-1] + ", " + dataPieces[hIndex1] + " " + dataPieces[hIndex1 + 1]);
            jIndex1--;
        }
        
        for(int i = hIndex1; i < jIndex1; i++)
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
        //System.out.println("Race Name: " + raceNum + "   Horse: " + hName + "  Jockey: " + jName);
        //System.out.println("Horse Name: " + hName);
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
        //System.out.println(pageCount);
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
        // Fixes Issue where parts of Horses Name was cut off
        int hIndex1 = 0;
        horseNameFirstBound:
        for(int i = jIndex1; i > 0; i--)
        {
            if( (int)(dataPieces[i].charAt(0)) >= 48 && (int)(dataPieces[i].charAt(0)) <= 57)
            {
                hIndex1 = i+1;
                break horseNameFirstBound;
            }
        }
        
        //Remove Country Designation -> So manual entry is less complex       
        if(dataPieces[jIndex1-1].charAt(0) == 40)
        {
            countryTagsRemoved++;
            jIndex1--;
        }
        String hName = "";
        for(int i = hIndex1; i < jIndex1; i++)
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
                double sprat = 0;
                //= calculateSPRAT(lines, place, lowerBound, upperBound);
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
                //String spratReturn    = updateSPRAT(currHorse, calculateSPRAT(lines, place, lowerBound, upperBound));
                //String avespratReturn = updateAVESPRAT(currHorse, calculateSPRAT(lines, place, lowerBound, upperBound));
                String spratReturn    = updateSPRAT(currHorse, 0);
                String avespratReturn = updateAVESPRAT(currHorse, 0);
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
            
            double sprat = 0;
            //calculateSPRAT(lines, place, lowerBound, upperBound);
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
    
    public String[] retrieveDate(String[] lines)
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
        if(sMonth.equals("January")){nMonth = "01";}
        if(sMonth.equals("February")){nMonth = "02";}
        if(sMonth.equals("March")){nMonth = "03";}
        if(sMonth.equals("April")){nMonth = "04";}
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
        if(sMonth.equals("December")) {nMonth = "12";}
                    
        if(nMonth =="-1")
        {
            showError();
            System.out.println("ERROR: retrieveDate() - MONTH ERROR, MISSING " + sMonth);
            
            String[] eArr = new String[]  {"ERROR", "ERROR"};
            return eArr;
        }
        
        String newDate = day+"/"+nMonth+"/"+year;
        String regDate = nMonth+"/"+day+"/"+year;
        String[] rArr = new String[]  {newDate, regDate};
        return rArr;
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
    
    public void showError()
    {
        System.out.println("Page #: " + pageCount + "      Date: " + date + "      Race Number: " + raceNum);
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
}