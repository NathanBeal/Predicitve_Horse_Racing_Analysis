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
 * Write a description of class ManualEntryTestSet here.
 *
 * @author (your name)
 * @version (a version number or a date)
 */
public class ManualEntryTestSet
{
    public ManualEntryTestSet(){}
    ArrayList<ArrayList<String>> RaceDayData = new ArrayList<ArrayList<String>>();
    ArrayList<ArrayList<String>> formattedData = new ArrayList<ArrayList<String>>();
    
    public static void main() throws IOException 
    {
        String FILE = "3-1-20 AQU Race 4"; //HORSEJOCKEYDATA
        
        ManualEntryTestSet demo = new ManualEntryTestSet();
        demo.run(FILE);
    }
    
    public void run(String fileExt) throws IOException 
    {
        //fillRaceDayArray();
        runUI();
        formattedData = completeDocument(RaceDayData);
        writeToCSV();
    }
    
    public void runUI()
    {
        Scanner scan = new Scanner(System.in);
        boolean DONE = false;
        boolean favoriteLock = false;
        while(DONE != true)
        {
            System.out.println("Generating New Race:");
            
            System.out.println("What is today's date (mm/dd/yyyy)?");
            String date = scan.next();
            
            System.out.println("What race number is this?");
            String raceNum = scan.next();
            
            System.out.println("How Many Horses Are in the race?");
            int numHorses = scan.nextInt();
            
            //Data Entry
            for(int i = 0; i < numHorses; i++)
            {
                ArrayList<String> currHorse = new ArrayList<String>();
                
                System.out.println("What's the horses name?");
                String hName = scan.next();
                
                System.out.println("What's the jockeys name (last/first)?");
                String jName = scan.next();
                
                System.out.println("What are the odds?");
                String odds = scan.next();
                
                //Locks out other favorites if one is entered
                String fav = "0";
                if(favoriteLock == false)
                {
                    System.out.println("Is this horse the favorite (Y/N)?");
                    fav = scan.next();
                    if(fav.equals("Y") || fav.equals("y"))
                    {
                        fav = "1";
                        favoriteLock = true;
                    }else{
                        fav = "0";
                    }
                }
                
                //System.out.flush();
                currHorse.add(hName); currHorse.add(jName); currHorse.add(odds); currHorse.add(fav);
                RaceDayData.add(currHorse);
                System.out.println(""); System.out.println("HORSES");
                for(int j = 0; j < RaceDayData.size(); j++)
                {
                    String horse = "" + (j+1) +". ";
                    for(int k = 0; k < RaceDayData.get(j).size(); k++)
                    {
                        ArrayList<String> t = RaceDayData.get(j);
                        horse = horse + t.get(k) + "  ";
                    }
                    System.out.println(horse);
                }
            }
            
            favoriteLock = false;
            //Data Repeat
            for(int i = 0; i < RaceDayData.size(); i++)
            {
                ArrayList<String> currHorse = RaceDayData.get(i);
                System.out.println("Horse:  " + currHorse.get(0));
            }
            System.out.println("");
            
            //Confirmation
            System.out.println("Is this information Correct (Y/N)");
            String confirmation = scan.next();
            
            if(confirmation.equals("Y") || confirmation.equals("y"))
            {
                DONE = true;
            }
        }
    }
    
    public void fillRaceDayArray()
    {
        //HORSES INITS (HORSE NAME, JOCKEY, ODDS, FAVORITE)
        //1
        ArrayList<String> horse1 = new ArrayList<String>();
        horse1.add("Ujjayi"); horse1.add("Carmouche, Kendrick"); horse1.add("2.90"); horse1.add("0");
        RaceDayData.add(horse1);
        //2.
        ArrayList<String> horse2 = new ArrayList<String>();
        horse2.add("Jennemily"); horse2.add("Rosario, Joel"); horse2.add("3.1"); horse2.add("0");
        RaceDayData.add(horse2);
        //3.
        ArrayList<String> horse3 = new ArrayList<String>();
        horse3.add("Jennemily"); horse3.add("Rosario, Joel"); horse3.add("3.1"); horse3.add("0");
        //4.
        /*
        ArrayList<String> horse2 = new ArrayList<String>();
        horse2.add("Daphne Moon"); horse2.add("Rosario, Joel"); horse2.add("3.1"); horse2.add("0");
        //5.
        ArrayList<String> horse2 = new ArrayList<String>();
        horse2.add("Jennemily"); horse2.add("Rosario, Joel"); horse2.add("3.1"); horse2.add("0");
        //6.
        ArrayList<String> horse2 = new ArrayList<String>();
        horse2.add("Jennemily"); horse2.add("Rosario, Joel"); horse2.add("3.1"); horse2.add("0");
        //7.
        ArrayList<String> horse2 = new ArrayList<String>();
        horse2.add("Jennemily"); horse2.add("Rosario, Joel"); horse2.add("3.1"); horse2.add("0");
        //8.
        ArrayList<String> horse2 = new ArrayList<String>();
        horse2.add("Jennemily"); horse2.add("Rosario, Joel"); horse2.add("3.1"); horse2.add("0");
        //9.
        ArrayList<String> horse2 = new ArrayList<String>();
        horse2.add("Jennemily"); horse2.add("Rosario, Joel"); horse2.add("3.1"); horse2.add("0");
        //10.
        ArrayList<String> horse2 = new ArrayList<String>();
        horse2.add("Jennemily"); horse2.add("Rosario, Joel"); horse2.add("3.1"); horse2.add("0");
        */
    
    }
    
    //Loads array list up with current data as well as searches through last recorded/historied data to get full picture
    public ArrayList<ArrayList<String>> completeDocument(ArrayList<ArrayList<String>> dayOfData)
    {
        ArrayList<ArrayList<String>> rtnData = new ArrayList<ArrayList<String>>();
        
        return rtnData;
    }
    
    public void writeToCSV()
    {
        //System.out.println("HERE");
        //Collections.shuffle(formattedData);
        System.out.println("    2. Writing to CSV");
        int offset = 0;
        try (PrintWriter writer = new PrintWriter(new File("ManualEntryTest.csv"))) 
        {
            StringBuilder sb = new StringBuilder();
            
            //VARIABLES
            sb.append("date,"); sb.append("raceNum,"); sb.append("hName,"); sb.append("jName,"); sb.append("odds,"); sb.append("fav,");sb.append("pastPos,");  
            sb.append("last3,"); sb.append("last5,"); sb.append("last10,");
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
    }
}
