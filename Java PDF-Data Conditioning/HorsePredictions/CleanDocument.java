import java.util.*;
import java.util.Date;
import java.time.*;
import java.io.*;
import java.util.Calendar;
import java.io.IOException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
/**
 * Write a description of class CleanDocument here.
 *
 * @author (your name)
 * @version (a version number or a date)
 */
public class CleanDocument
{
    // instance variables - replace the example below with your own
    int pageCount = 0;
    ArrayList<Integer> bootPages = new ArrayList<Integer>();
    ArrayList<Integer> postProcessing = new ArrayList<Integer>();
    int totalPagesProcessed = 0;
    int totalPagesRemoved = 0;
    
    public CleanDocument(){}

    public static void main() throws IOException 
    {
        //String FILE = "AQU 2017-2020";
        String FILE = "AQU 2017-2020";
        //String FILE = "AQU 1-1-17";
        //String FILE = "3-1-20 AQU";
        CleanDocument demo = new CleanDocument();
        demo.run(FILE);
    }
    
    public void run(String fileExt) throws IOException 
    {
        System.out.println("Cleaning Document: " + fileExt);
        String entireFile = "C:/Users/natha/Desktop/ML/AQU Testing/" + fileExt + ".pdf";
        String newFile = fileExt + "-EDITED";
        File myFile = new File(entireFile);
        
        String text;
        String[] pages;
        int numPages;
        
        try (PDDocument doc = PDDocument.load(myFile)) 
        {
            System.out.println("    - Loading pdf files");
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
            }else{
                totalPagesRemoved++;
                bootPages.add(pageCount);
            }
        } 
        
        if( bootPages.get(bootPages.size()-1) == bootPages.get(bootPages.size()-2) )
        {
            bootPages.remove(bootPages.size());
        }
        
        for(int i = 0; i < bootPages.size(); i++)
        {
            if(i % 30 == 0)
            {
                //System.out.println("");
            }
            //System.out.print(bootPages.get(i) + ", ");
        }
        System.out.println("");
        
        removePages(bootPages, entireFile, newFile);
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
    
    public void removePages(ArrayList<Integer> pageIndeces, String readFile, String newFile) throws IOException
    {
        System.out.println("    - Removing pages");
        //Loading an existing document 
        String editedFile = "C:/Users/natha/Desktop/ML/AQU Testing/" + newFile + ".pdf";
        File file = new File(readFile);
        try (PDDocument document = PDDocument.load(file)) 
        {
            //Listing the number of existing pages
            int noOfPages= document.getNumberOfPages();
            if (document.isEncrypted()) 
            {
                try {
                    document.setAllSecurityToBeRemoved(true);
                }
                catch (Exception e) {
                    System.out.println(e);
                }
            }
            //System.out.println("    - Number Of Page: " + noOfPages);
               
            //Removing the pages
            System.out.println("        * Removing Pages: ");
            for(int i = pageIndeces.size(); i > 0; i--)
            {
                int pageIndex = pageIndeces.get(i-1);
                if(i % 30 == 0)
                {
                    System.out.println("    ");
                }
                System.out.print(pageIndex + ", ");
                document.removePage(pageIndex-1);
            }
            //document.removePage(14-1);
            //Saving the document
            document.save(editedFile);
        
            //Closing the document
            document.close();
        }catch (FileNotFoundException e) {
          System.out.println(e.getMessage());
        }
        
        dueDilligence(editedFile);
    }
    
    public void dueDilligence(String newFileName) throws IOException
    {
        System.out.println("    - Double Checking");
        File myFile = new File(newFileName);
        
        String text;
        String[] pages;
        int numPages;
        
        try (PDDocument doc = PDDocument.load(myFile)) 
        {
            System.out.println("        * Reading edited pdf");
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
        
        int min = 0;
        int lim = numPages+1;
        String currentRace;
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
            }else{
                totalPagesRemoved++;
                postProcessing.add(pageCount);
            }
        } 
        
         for(int i = 0; i < postProcessing.size(); i++)
        {
            if(i % 30 == 0)
            {
                System.out.println("");
            }
            System.out.print(bootPages.get(i) + ", ");
        }
        System.out.println("    - Finished Checking");
    }
}
