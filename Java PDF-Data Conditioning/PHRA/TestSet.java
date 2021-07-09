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
/*
 * TEST SET IS IDENTICAL TO TRAINING AND VALIDATION WITH THE EXCEPTION THAT FINAL RANK
 * PARAMETER IS REMOVED AND NOT WRITTEN TO THE CSV
 */
/**
 * Write a description of class PHRA here.
 *
 * @author (your name)
 * @version (a version number or a date)
 */
public class TestSet
{
    // instance variables - replace the example below with your own
    String date;
    String sNumOfHorses;
    int totalRacesProcessed = 0;
    int priorRaceNum;
    int totalDataPointsProcessed;
    String raceNum;
   
    //Changes depending on number of params being evaluated
    int NUMBEROFPARAMS = 25;
    
    //Most Important ArryaList
    ArrayList<ArrayList<String>> formattedData = new ArrayList<ArrayList<String>>();
    boolean toggleFinalRank = true;
    /**
     * Constructor for objects of class PHRA
     */
    public TestSet()
    {
        // initialise instance variables
    }

    public static void main() throws IOException 
    {
        TestSet demo = new TestSet();
        demo.run();
    }
    
    public void run() throws IOException 
    {
        System.out.println("Developing Training and Validation Set CSV");
        //File myFile = new File("src/mai/resources/wwii.pdf");
        String fileExt = "3-14-20 Race 9 Test";
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

            System.out.println("Text size: " + text.length() + " Pages: " + numPages);
        }
        
        //Evaluates Each Document Seperately
        ArrayList<ArrayList<String>> values = new ArrayList<ArrayList<String>>();
        String currentRace;
        int min = 0;
        int lim = numPages+1;
        for (int i = 1; i < lim; i++)
        {
            //Temp ArrayList to collect Values of each race/page
            ArrayList<String> tempArr = new ArrayList<String>();
            
            currentRace = pages[i];
            String[] lines = currentRace.split("\n");
            
            
            //Race Specific Data/General Data
            date = retrieveDate(lines);
            raceNum = raceNumber(lines);
            String trackType = trackType(lines);
            String weather = weather(lines);
            String distance = distance(lines);                      //True Double
            //String numHorses = sNumOfHorses;
            String trackCondition = trackCondition(lines);  
            
            
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
                if(Integer.valueOf(raceNum) == 1 && i > 2)
                {
                    totalRacesProcessed = totalRacesProcessed + priorRaceNum;
                }
            }
            
            //Preps Data to Write to CSV
            dataPrep(numHorses, generalData, racerData);
        } 
        //printData();
        System.out.println("Waiting to Write to CSV");
        writeToCSV();
        //System.out.println("Number of Races Processed: " + totalRacesProcessed);
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
        //System.out.println("Number of matchups (Experimental): " + count);
        
        int numOfMatchups = 0;
        for(int i = 1; i < Integer.valueOf(horsesInRace); i++)
        {
            numOfMatchups = numOfMatchups + i;
        }
        //System.out.println("Number of matchups (Theory): " + numOfMatchups);
        
        //Initialize Each "Matchup" with general data
        
        //System.out.print(
        Random rand = new Random();
        int num = rand.nextInt(2);
        //System.out.println(num);
        if(num == 0)
        {
            toggleFinalRank = false;
        }else{
            if(num == 1)
            {
                toggleFinalRank = true;
            }
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
                    //cO.add("1");
                    formattedData.add(cO);
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
                    
                    //cO.add("2");
                    formattedData.add(cO);
                }
            }
            toggleFinalRank = !toggleFinalRank;
            //System.out.println(toggleFinalRank);
        }
        
        //System.out.println("----------------------------------------------------------------------------------------------------------------------------");
    }
    
    
    public void writeToCSV()
    {
        //System.out.println("HERE");
        try (PrintWriter writer = new PrintWriter(new File("Test.csv"))) 
        {
            StringBuilder sb = new StringBuilder();
            
            //VARIABLES
            sb.append("date,"); sb.append("raceNum,");
            //Track Vars
            sb.append("trackType,"); sb.append("weather,"); sb.append("trackCondition,"); 
            sb.append("dist,"); sb.append("numHorses,");
            //Jockey 1
            sb.append("lastRace1,"); sb.append("pastRace1,"); sb.append("pastTrack1,");  sb.append("pastPos1,");
            sb.append("hName1,"); sb.append("jName1,");  sb.append("weight1,");  sb.append("polePos1,");sb.append("odds1,");  
            //Jockey 2
            sb.append("lastRace2,"); sb.append("pastRace2,"); sb.append("pastTrack2,");  sb.append("pastPos2,");
            sb.append("hName2,"); sb.append("jName2,");  sb.append("weight2,");  sb.append("polePos2,");sb.append("odds2,"); 
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
            //sb.append("1");
            //sb.append(',');
            //sb.append(" Ghimire");
            //sb.append('\n');
        
            writer.write(sb.toString());
            System.out.println("Finished Writing to CSV");
    
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
        int numOfHorses = (uIndex-lIndex)+1;
        sNumOfHorses = numOfHorses + "";
        //System.out.println("Number Of Horses: " + numOfHorses);
        String[] jockeys = new String[numOfHorses];
        
        String timeSinceLR = ""; String[] pastPerformance = new String[3]; 
        String[] horseAndJockey = new String[2]; String weight = ""; String polePosition = "";  
        String odds = ""; 
        for(int i = lIndex; i < uIndex+1; i++)
        {
            ArrayList<String> indivHorse = new ArrayList<String>();
            dataLinePieces = lines[i].split(" ");
            
            timeSinceLR     = lastRace(dataLinePieces[0], date);                          //1st Param
            pastPerformance = pastPerformance(dataLinePieces[1], timeSinceLR);      //2nd, 3rd & 4th Param
            horseAndJockey  = horseAndJockey(dataLinePieces);                        // 5 & 6
            weight          = weight(dataLinePieces);                       // int   // 7
            polePosition    = polePosition(dataLinePieces);    // int
            
            odds           = retrieveOdds(dataLinePieces);     // float
            //System.out.println("Here");
            
            indivHorse.add(timeSinceLR); indivHorse.add(pastPerformance[0]); indivHorse.add(pastPerformance[1]);
            indivHorse.add(pastPerformance[2]); indivHorse.add(horseAndJockey[0]); indivHorse.add(horseAndJockey[1]);
            indivHorse.add(weight); indivHorse.add(polePosition); indivHorse.add(odds);
            
            horses.add(indivHorse);
        }
        
        
        return horses;
    }
    
    public String retrieveOdds(String[] dataPieces)
    {
        float odds = -1;
        int oddsIndex = -1;
        boolean isFavorited = false;
        
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
        
        sOdds = sOdds + dataPieces[oddsIndex] + dataPieces[oddsIndex+1];
        //System.out.println(sOdds);
        //Handling the "Favorite Star"
        String[] oddPieces = sOdds.split("");
        sOdds = "";
        int offset = 0;
        for(int i = 0; i < oddPieces.length-2-offset; i++)
        {
            if(oddPieces[i].equals("0") || oddPieces[i].equals("1") || oddPieces[i].equals("2") || oddPieces[i].equals("3") 
            || oddPieces[i].equals("4") || oddPieces[i].equals("5") || oddPieces[i].equals("6") || oddPieces[i].equals("7") 
            || oddPieces[i].equals("8") || oddPieces[i].equals("9") || oddPieces[i].equals("."))
            {
                if((sOdds.split("")).length > 4)
                {
                    break;
                }else{
                    sOdds = sOdds + oddPieces[i];
                }
            }else{
                if(oddPieces[i].equals("*"))
                {
                     isFavorited = true;
                     break;
                }
                
                if(oddPieces[i].equals(" "))
                {
                    break;
                }
            }
        }
        
        //System.out.println(sOdds);
        odds = Float.valueOf(sOdds);
        if(odds == -1)
        {
            showError();
            System.out.println("ERROR: retreiveOdds()");
        }
        return sOdds;
    }
    
    public String polePosition(String[] dataPieces)
    {
        String polePosition = "ERROR";
        int index = -1;
        
        for(int i = 3; i < dataPieces.length-1; i++)
        {
            String[] comps = dataPieces[i].split("");
            //System.out.println(dataPieces[i]);
            
            //for(int j = 0; j < comps.length -1; j++)
            //{
            if(dataPieces[i].equals("b") || dataPieces[i].equals("f") || dataPieces[i].equals("h") ||
            dataPieces[i].equals("bf") || dataPieces[i].equals("L") || dataPieces[i].equals("-")
            || dataPieces[i].equals("bh") || dataPieces[i].equals("g") || dataPieces[i].equals("a")
            || dataPieces[i].equals("v") || dataPieces[i].equals("fv") || dataPieces[i].equals("ab")
            )
            {
                //System.out.println("FLAG");
                index = i + 1;
            }
        }
        //System.out.println("Pole Position: " + dataPieces[index]);
        
        String sPolePos = dataPieces[index] + "";
        //System.out.println(sPolePos);
        if(!sPolePos.equals("1") && !sPolePos.equals("2") && !sPolePos.equals("3") 
        && !sPolePos.equals("4") && !sPolePos.equals("5") && !sPolePos.equals("6") 
        && !sPolePos.equals("7") && !sPolePos.equals("8") && !sPolePos.equals("9") 
        && !sPolePos.equals("10") && !sPolePos.equals("11") && !sPolePos.equals("12")
        && !sPolePos.equals("13") && !sPolePos.equals("14") && !sPolePos.equals("15") )
        {
            System.out.println("ERROR: polePosition() - POLEPOS differs from variables " 
            + sPolePos);
            showError();
            return "ERROR";
        }
        
        return sPolePos;
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
        if(timeSinceLR.equals("FR"))
        {
            String[] firstRace = new String[] {"FR", "FR", "FR"};
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
            String errMessage[] = new String[1]; errMessage[0] = "ERROR";
            showError();
            
            System.out.println("ERROR: pastPerformance()");
            //System.out.println(Integer.valueOf(comps[0]));
            for(int i = 0; i < comps.length-1; i++)
            {
                System.out.println(comps[i]);
                char temp = ((comps[i]).toLowerCase()).charAt(0);
                //System.out.println((int)temp);
            }
            
            return errMessage;
        }
        
        //System.out.println("GroupedData: " +  groupedData + "   PR: " + pastRace + "    PT: " + pastTrack + "   FP: " +
        //pastFinishingPos);
        
        return cleanData;
    }
    
    public String lastRace(String oDate, String currentDate)
    {
        if(oDate.equals("---"))
        {
            //System.out.println("Current Date: " + currentDate + "   Last Raced: FR");
            return "FR";
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
        if(year.equals("20"))
        {
            year = "2020";
        }else{
            if(year.equals("19"))
            {
                year = "2019";
            }else{
                if(year.equals("18"))
                {
                    year = "2018";
                }else{
                    if(year.equals("17"))
                    {
                        year = "2017";
                    }else{
                        year = "-1";
                        showError();
                        System.out.println("ERROR: OLDER THAN ANTICIPATED"); return("ERROR");
                        }
                }
            }
        }
        if(month.equals("Jan"))
        {month = "01";}else{
            if(month.equals("Feb"))
            {month = "02";}else{
                if(month.equals("Mar"))
                {month = "03";}else{
                    if(month.equals("Apr"))
                    {month = "04";}else{
                        if(month.equals("May"))
                        {month = "05";}else{
                            if(month.equals("Jun"))
                            {month = "06";}else{
                                if(month.equals("Jul"))
                                {month = "07";}else{
                                    if(month.equals("Aug"))
                                    {month = "08";}else{
                                        if(month.equals("Sep"))
                                        {month = "09";}else{
                                            if(month.equals("Oct"))
                                            {month = "10";}else{
                                                if(month.equals("Nov"))
                                                {month = "11";}else{
                                                    if(month.equals("Dec"))
                                                    {month = "12";}else{
                                                        showError();
                                                        System.out.println("ERROR: lastRace(), PROB SPELLING ABRV"); return("ERROR");
                                                    }}}}}}}}}}}
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
        
        showError();
        System.out.println("ERROR: trackType() - NEW TRACK TYPE " + trackType);
        return "ERROR";
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