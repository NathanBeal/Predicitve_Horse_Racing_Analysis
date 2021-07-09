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
 * Write a description of class Main here.
 *
 * @author (your name)
 * @version (a version number or a date)
 */
public class Main
{
    // instance variables - replace the example below with your own
    private int x;
    String[] allVariables;
    /**
     * Constructor for objects of class Main
     */
    public Main()
    {
        allVariables = new String[] {"date","raceNum","trackType","weather","trackCondition", "dist", "numHorses",
                                      "lastRace", "pastRace", "pastTrack","pastPos","hName","jName","weight","m","e","polePos","odds","fav","newDist","asr1","lsr1"};
    }

    public static void main() throws IOException 
    {
        // Main File
        // String mainFile = "AQU Jan'18-Feb'20";//"3-15-20 AQU";
        String mainFile = "SAR 17-19";
        //Adds Desired Variables
        ArrayList<String> dV = new ArrayList();
        dV.add("date"); dV.add("raceNum"); 
        dV.add("trackType"); dV.add("weather"); dV.add("trackCondition"); dV.add("dist");
        dV.add("pastPos"); dV.add("hName"); dV.add("jName");
        dV.add("numHorses"); dV.add("weight"); dV.add("odds"); dV.add("fav");

        //Test Files Pulling From
        //String[] testFiles = new String[] {"3-1-20 AQU Race 1","3-1-20 AQU Race 2","3-1-20 AQU Race 3","3-1-20 AQU Race 4","3-1-20 AQU Race 5","3-1-20 AQU Race 6",
        //"3-1-20 AQU Race 7","3-1-20 AQU Race 8","3-1-20 AQU Race 9"};
        String[] testFiles = new String[] {"3-1-20 AQU Race 3"};
        
        TrainValidateShuffled demo = new TrainValidateShuffled();
        long t0 = System.currentTimeMillis();
        demo.run(mainFile, testFiles, dV);
        long t1 = System.currentTimeMillis();
        long totalTime = ((t1-t0)/1000);
        System.out.println("");
        
        /*
        JockeyStats demo1 = new JockeyStats();
        t0 = System.currentTimeMillis();
        demo1.run(mainFile);
        t1 = System.currentTimeMillis();
        System.out.println("Time Elapsed: " + ((t1-t0)/1000) + " seconds");
        totalTime = totalTime + ((t1-t0)/1000);
        */
    }
}
