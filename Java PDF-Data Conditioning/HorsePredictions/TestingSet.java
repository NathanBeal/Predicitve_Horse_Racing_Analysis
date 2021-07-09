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
 * Write a description of class TestingSet here.
 *
 * @author (your name)
 * @version (a version number or a date)
 */
public class TestingSet
{


    /**
     * Constructor for objects of class TestingSet
     */
    public TestingSet()
    {
    }

    public static void main() throws IOException 
    {
        ArrayList<ArrayList<String>> testSet = new ArrayList<ArrayList<String>>();
        
        TestingSet demo = new TestingSet();
        demo.run(testSet);
    }
    
    public void run(ArrayList<ArrayList<String>> testSet) throws IOException 
    {
        System.out.println("    - Writing to CSV");
        try (PrintWriter writer = new PrintWriter(new File("Testing.csv"))) 
        {
            StringBuilder sb = new StringBuilder();
            
            //VARIABLES
            sb.append("date,"); sb.append("raceNum,"); sb.append("hName,"); sb.append("jName,"); sb.append("odds,"); sb.append("fav,");sb.append("pastPos,");  
            sb.append("last3,"); sb.append("last5,"); sb.append("last10,"); 
            sb.append('\n');
            
            // the -1 lops off the trifecta 
            for(int i = 0; i < testSet.size(); i++)
            {
                ArrayList<String> writeDataStr = testSet.get(i);
                for(int j = 0; j < writeDataStr.size()-1; j++)
                {
                    sb.append(writeDataStr.get(j));
                    sb.append(',');
                }
                sb.append('\n');
            }
            writer.write(sb.toString());
            System.out.println("    - Finished writing to CSV (Testing.csv)");
        } catch (FileNotFoundException e) {
          System.out.println(e.getMessage());
        }
        
        // Predictions of each Horse
        for(int i = 0; i < testSet.size(); i++)
        {
            int length = testSet.get(0).size()-1;
            String outcome = testSet.get(i).get(length);
            String horse = testSet.get(i).get(2);
            //System.out.print("Horse: " + horse + "  TRIF: " + outcome);
        }
    }
}
