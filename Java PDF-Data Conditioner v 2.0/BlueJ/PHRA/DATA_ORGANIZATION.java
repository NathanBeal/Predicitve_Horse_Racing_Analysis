import java.util.*;
import java.io.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

/**
 * @author (Nathan Beal)
 * @version (1.00)
 */
public class DATA_ORGANIZATION
{
    //Global Vars for Error Message
    String date, raceNum;
    String sNumOfHorses;
    
    //Needed for output
    StringBuilder sb = new StringBuilder();
    Map<String, String> dictionary = new HashMap<String, String>();
    /**
     * Constructor for objects of class DATA_ORGANIZATION
     */
    public DATA_ORGANIZATION()
    {
    }

    public static void main() throws IOException 
    {
        DATA_ORGANIZATION demo = new DATA_ORGANIZATION();
        demo.run();
    }
    
    public void run() throws IOException 
    {
        init_dictionary();
        //File myFile = new File("src/mai/resources/wwii.pdf");
        String fileExt = "3-15-20";
        File myFile = new File("C:/Users/natha/OneDrive/Desktop/GitHub Repos/Predicitve_Horse_Racing_Analysis/Racetrack Data/AQU - Aqueduct Data/3-15-20 AQU.pdf");
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
            ArrayList<ArrayList<String>> DATA = new ArrayList<ArrayList<String>>();
            
            currentRace = pages[i];
            String[] lines = currentRace.split("\n");
            
            //Date
            date = retrieveDate(lines);
            sb.append("DATE:" + date + ","); sb.append('\n'); 
            
            //Race Track Data
            raceNum = raceNumber(lines);
            sb.append("Race Num:" + raceNum + ",");
            String trackType = trackType(lines);
            sb.append("Track Type:" + trackType + ",");
            String weather = weather(lines);
            sb.append("Weather:" + weather + ",");
            String distance = distance(lines);                      //True Double
            sb.append("Dist:" + distance + ",");
            String trackCondition = trackCondition(lines);
            sb.append("Condition:" + trackCondition + ",");
            sb.append('\n'); 
            
            ArrayList<String> generalData = new ArrayList<String>();
            generalData.add(date); generalData.add(raceNum); generalData.add(trackType);
            generalData.add(weather); generalData.add(trackCondition); generalData.add(distance);
            
            ArrayList<ArrayList<String>> racerData = racerData(lines); //MAIN DATA
            for (int j = 0; j < racerData.size(); j++)
            {
                System.out.println(racerData.get(j));
                DATA.add(racerData.get(j));
            }
            
            String numHorses = sNumOfHorses; generalData.add(numHorses);
            writeToCSV(DATA);
        } 
        System.out.println("Finished Writing to CSV");
        //System.out.println("Number of Races Processed: " + totalRacesProcessed);
    }
    
    
    public void writeToCSV(ArrayList<ArrayList<String>> DATA)
    {
        try (PrintWriter writer = new PrintWriter(new File("DATA.csv"))) 
        {
            sb.append("Last_Race,"); sb.append("Past_Race,"); sb.append("Past_Track,");  sb.append("Past_Pos,"); sb.append("Pgm,");
            sb.append("Horse,"); sb.append("Jockey,");  sb.append("Weight,"); sb.append("M/E,"); sb.append("PP,");sb.append("Odds,");sb.append("Fav,");
            sb.append("Comments,"); 
            sb.append('\n');
            
            for(int i = 0; i < DATA.size(); i++)
            {
                ArrayList<String> writeData = DATA.get(i);
                for(int j = 0; j < writeData.size(); j++)
                {
                    sb.append(writeData.get(j));
                    sb.append(',');
                }
                sb.append('\n');
            }
            writer.write(sb.toString());
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
            for(int j = 0; j < dataLinePieces.length; j++)
            {
                if(dataLinePieces[j].equals("Fractional"))
                {
                    uIndex = i-1;
                }
            }
        }
        
        //Last Place Horse
        //System.out.println(lines[uIndex]);
        int numOfHorses = (uIndex-lIndex)+1;
        sNumOfHorses = numOfHorses + "";
        String[] jockeys = new String[numOfHorses];
        
        String timeSinceLR = ""; String[] pastPerf_pgm = new String[4]; 
        String[] horseAndJockey = new String[2]; String weight = ""; String polePosition = ""; 
        String[] odds_fav_com = new String[3];
        for(int i = lIndex; i < uIndex+1; i++)
        {
            ArrayList<String> indivHorse = new ArrayList<String>();
            dataLinePieces  = lines[i].split(" ");
            pastPerf_pgm    = pastPerf_pgm(dataLinePieces);   
            horseAndJockey  = horseAndJockey(dataLinePieces);
            weight          = weight(dataLinePieces); 
            polePosition    = polePosition(dataLinePieces); 
            odds_fav_com    = odds_fav_com(dataLinePieces);

            String last_date_raced = "";
            if(dataLinePieces[0].equals("---"))
            {
                last_date_raced = "FR";
            }else{
                last_date_raced = dataLinePieces[0];
            }
            indivHorse.add(last_date_raced); indivHorse.add(pastPerf_pgm[0]); indivHorse.add(pastPerf_pgm[1]);
            indivHorse.add(pastPerf_pgm[2]); indivHorse.add(pastPerf_pgm[3]); indivHorse.add(horseAndJockey[0]); indivHorse.add(horseAndJockey[1]);
            indivHorse.add(weight); indivHorse.add("M/E"); indivHorse.add(polePosition); indivHorse.add(odds_fav_com[0]); indivHorse.add(odds_fav_com[1]); indivHorse.add(odds_fav_com[2]);
            
            horses.add(indivHorse);
        }
        
        
        return horses;
    }
    
    public String[] odds_fav_com(String[] dataPieces)
    {
        String[] odds_fav_com = new String[3];
        int oddsIndex = -1;
        String isFavorited = "N";
        
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
        int per_count = 0;
        boolean per_flag = false;
        for(int i = 0; i < oddPieces.length-2; i++)
        {
            int asciiChar = (int) oddPieces[i].charAt(0);
            
            if(asciiChar >= 46 && asciiChar <= 57)
            {
                if(per_flag) per_count++;
                if(asciiChar == 46) per_flag = true;
                
                if(per_count > 2)
                {
                    break;
                }else{
                    sOdds = sOdds + oddPieces[i];
                }
            }else{
                if(oddPieces[i].equals("*"))
                {
                     isFavorited = "Y";
                     break;
                }
                
                if(oddPieces[i].equals(" "))
                {
                    break;
                }
            }
        }
        
        String comments = "";
        for(int i = oddsIndex+1; i < dataPieces.length; i++)
        {
            if(i == dataPieces.length-1)
            {
                String[] word_peices = dataPieces[i].split("");
                String temp = "";
                for(int j = 0; j < word_peices.length; j++)
                {
                    int asciiVal = (int) word_peices[j].charAt(0);
                    if(asciiVal >= 97 && asciiVal <= 122 || asciiVal == 44 || (asciiVal >= 46 && asciiVal <= 57) || asciiVal == 39)
                    {
                        if(asciiVal == 44)//removed ',' comma char
                        {
                            temp += "+";
                        }else{
                            temp += word_peices[j];
                        }
                        //}else{
                        //System.out.println("NOT CAUGHT: " + word_peices[j]);
                        //System.out.println(asciiVal);
                    }
                }
                comments = comments + temp;
            }else{
                //Not the last word
                String[] word_peices = dataPieces[i].split("");
                String temp = "";
                for(int j = 0; j < word_peices.length; j++)
                {
                    int asciiVal = (int) word_peices[j].charAt(0);
                    
                    if(asciiVal == 44)//removed ',' comma char
                    {
                        temp += "+";
                    }else{
                        temp += word_peices[j];
                    }
                }
                
                comments = comments + temp + "_";
            }
        }
        
        odds_fav_com[0] = sOdds;
        odds_fav_com[1] = isFavorited;
        odds_fav_com[2] = comments;
        //odds_fav_com[2] = "n/a";
        if(Float.valueOf(sOdds) == -1)
        {
            showError("ERROR: retreiveOdds()");
        }
        return odds_fav_com;
    }
    
    public String polePosition(String[] dataPieces)
    {
        String polePosition = "ERROR";
        int index = -1;
        
        for(int i = 3; i < dataPieces.length-1; i++)
        {
            String[] comps = dataPieces[i].split("");
            //System.out.println(dataPieces[i]);
            
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
        
        String sPolePos = dataPieces[index];
        //System.out.println(sPolePos);
        if(Integer.parseInt(sPolePos) > 15)
        {
            return showError("ERROR: polePosition() - POLEPOS differs from variables " + sPolePos);
        }else{
            return sPolePos;
        }
    }
    
    public String[] horseAndJockey(String[] dataPieces)
    {
        //Jockey's Name (Last Name/First Name)
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
        for(int i = 2; i < jIndex1; i++)
        {
            if(hName == "")
            {
                int asciiVal = (int) dataPieces[i].charAt(0);
                if((asciiVal >= 65 && asciiVal <= 90) || (asciiVal >= 97 && asciiVal <= 122))
                {
                    hName = hName + dataPieces[i];
                }
            }else{
                //Catch Numbers (PGM is sneaking in)
                int asciiVal = (int) dataPieces[i].charAt(0);
                if((asciiVal >= 65 && asciiVal <= 90) || (asciiVal >= 97 && asciiVal <= 122))
                {
                    hName = hName + " " + dataPieces[i];
                }
            }
        }
        
        if(hName == "" && dataPieces[0].equals("---"))
        {
            hName = dataPieces[2];
        }
        
        if(jName.equals("") || hName.equals(""))
        {
            showError("ERROR 404 - Horse/Jockey Name Not found");
            return new String[] {"ERR", "ERR"};
        }
        
        return new String[] {hName, jName};
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
            int asciiVal = weightPieces[i].charAt(0);
            if(asciiVal >= 48 && asciiVal <= 57 && (cWSplit.length < 4))
            {
                cWeight = cWeight + weightPieces[i];
            }else{
                if(cWSplit.length >= 4)
                {
                    return showError("ERROR: CRITICAL - WEIGHT");
                }
            }
        }
        //System.out.print(cWeight);
        
        //return Integer.valueOf(cWeight);
        return cWeight;
    }
    
    public String[] pastPerf_pgm(String[] arr)
    {
        if(arr[0].equals("---"))
        {
            return new String[] {"FR", "FR", "FR", arr[1]};
        }else{
            String[] w = arr[1].split("");
            return new String[] {arr[1].substring(0,1), arr[1].substring(1,w.length-1), arr[1].substring(w.length-1, w.length), arr[2]};
        }
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
        if(trackType.equals("Dirt") || trackType.equals("Turf") || trackType.equals("Outer"))
        {
            return trackType;
        }

        return showError("ERROR: Track type not recognized: " + trackType);
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
        
        String distance = "";
        for(int i = 0; i < dataLinePieces.length; i++)
        {
            if(i != dataLinePieces.length - 1)
            {
                distance = distance + dataLinePieces[i] + " ";
            }else{
                distance += dataLinePieces[i];
            }
        }

        /* Furlongs -> Yards */
        /* 1 Furlong = 1/8 Mile */
        /* 1 Furlong = 220 yards*/
        if(distance.equals("Four And One Half Furlongs")) return "990";
        if(distance.equals("Five And One Half Furlongs")) return "1210";
        if(distance.equals("Six Furlongs")) return "1320";
        if(distance.equals("Six And One Half Furlongs")) return "1430";
        if(distance.equals("Seven Furlongs")) return "1540";
        if(distance.equals("One Mile")) return "1760";
        
        return showError("ERROR: Non registered distance of: '" + distance +"'");
    }
    
    public String raceNumber(String[] lines)
    {
        String dataLine = lines[0];
        String[] dataLinePieces = dataLine.split("Race ");
        
        dataLinePieces = dataLinePieces[1].split("");
        String race_num = "";
        for(int i = 0; i < dataLinePieces.length; i++)
        {
            int asciiVal = dataLinePieces[i].charAt(0);
            if((asciiVal >= 48 && asciiVal <= 57)) race_num+= dataLinePieces[i];
        }
        
        if(Integer.parseInt(race_num) > 15)
        {
            return showError("ERROR: Race number higher than antic value: " + raceNum);
        }else{
            return race_num;
        }
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
            return showError("ERROR: weather() - Could not find weather");
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
        String condition = "";
        String[] temp = trackCondition.split("");
        for(int i = 0; i < temp.length; i++)
        {
            int asciiVal = (int) temp[i].charAt(0);
            if((asciiVal >= 65 && asciiVal <= 90) || (asciiVal >= 97 && asciiVal <= 122) || asciiVal == 40 || asciiVal == 41) condition+= temp[i];
            
            /* For conditions that have a space in the middle "Sloppy (Sealed)"
            if(asciiVal == 32 && i <(temp.length-4))
            {}*/
        }
        //System.out.println("Cond:" + condition);
        
        return condition;
    }
    
    public String retrieveDate(String[] lines)
    {
        String [] dataLinePieces = lines[0].split(" - ");
        String date = dataLinePieces[1];
        dataLinePieces = date.split(" ");
        
        String day = (dataLinePieces[1].split(","))[0];
        if((day.split("")).length == 1) day = "0"+day;

        String year = (dataLinePieces[2].split(" "))[0];
        String month = dictionary.get(dataLinePieces[0].substring(0,3));
        if(month != null)
        {
            return (day+"/"+month+"/"+year);
        }else{
            return showError("ERROR: Could not characterize date correctly");
        }
    }
    
    public void init_dictionary()
    {
        dictionary.put("Jan", "01");
        dictionary.put("Feb", "02");
        dictionary.put("Mar", "03");
        dictionary.put("Apr", "04");
        dictionary.put("May", "05");
        dictionary.put("Jun", "06");
        dictionary.put("Jul", "07");
        dictionary.put("Aug", "08");
        dictionary.put("Sep", "09");
        dictionary.put("Oct", "10");
        dictionary.put("Nov", "11");
        dictionary.put("Dec", "12");
    }
  
    
    public String showError(String message)
    {
        System.out.println("Date: " + date + "      Race Number: " + raceNum);
        System.out.println(message);
        return "ERR";
    }
}
