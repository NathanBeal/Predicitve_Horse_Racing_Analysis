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
    
    //Performance Evaluators
    int races_recorded,pages_removed = 0;
    
    //CONSTANTS*****
    int numberOfDesiredRaces = 100;    
    int didNotFinishPenalty = 5;
    int firstRaceSpeedValue = 45;
    double totalDistBehindWeight = 1.2;
    double offsetWeight = 5;
    int numberOfRacesToAverage = 4; //Number of races use in the rolling ave calculation for avespeed
    int newDistRaceMinimum = 4;
    int newDistMinimum = 3;
    double totalDistBehind = 0.0;
    
    
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
        
        //Surface
        //File myFile = new File("C:/Users/natha/OneDrive/Desktop/GitHub Repos/Predicitve_Horse_Racing_Analysis/Racetrack Data/AQU - Aqueduct Data/AQU 2015-2016-EDITED.pdf");
        //File myFile = new File("C:/Users/natha/OneDrive/Desktop/GitHub Repos/Predicitve_Horse_Racing_Analysis/Racetrack Data/AQU - Aqueduct Data/3-15-20 AQU.pdf");
        File myFile = new File("C:/Users/natha/OneDrive/Desktop/GitHub Repos/Predicitve_Horse_Racing_Analysis/Racetrack Data/AQU - Aqueduct Data/AQU 2017-2020-EDITED.pdf");
        
        //Laptop
        //File myFile = new File("C:/Users/natha/Desktop/GitHub Repositories/Active Repos/Predicitve_Horse_Racing_Analysis/Racetrack Data/AQU - Aqueduct Data/3-15-20 AQU.pdf");
        
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
        String last_date = "";
        for (int i = 1; i < lim; i++)
        {
            //Temp ArrayList to collect Values of each race/page
            ArrayList<String> tempArr = new ArrayList<String>();
            ArrayList<ArrayList<String>> DATA = new ArrayList<ArrayList<String>>();
            String[] dist_tr = new String[2];
            
            currentRace = pages[i];
            String[] lines = currentRace.split("\n");
            
            if(checkDocument(lines))//True if document has data
            {
                //Date
                date = retrieveDate(lines);
                if(!last_date.equals(date))
                {
                    sb.append("DATE:" + date + ","); sb.append('\n'); 
                    last_date = date;
                }
                
                //Race Track Data
                sb.append("Race Num:" + raceNumber(lines) + ",");
                sb.append("Track Type:" + trackType(lines) + ",");
                sb.append("Weather:" + weather(lines) + ",");
                dist_tr = distance_trackRecord(lines);                      //True Double
                sb.append("Dist:" + dist_tr[0] + ",");
                sb.append("Condition:" + trackCondition(lines) + ",");
                sb.append("Track Record:" + dist_tr[1] + ",");
                sb.append("Final Time:" + final_time(lines) + ",");
                sb.append('\n'); 
                
                ArrayList<ArrayList<String>> racerData = racerData(lines); //MAIN DATA
                for (int j = 0; j < racerData.size(); j++)
                {
                    //System.out.println(racerData.get(j));//MAIN DATA
                    DATA.add(racerData.get(j));
                }
                
                String numHorses = sNumOfHorses;
                writeToCSV(DATA);
            }
        } 
        System.out.println("Finished Writing to CSV");
        //System.out.println("Number of Races Processed: " + totalRacesProcessed);
    }
    
    
    public void writeToCSV(ArrayList<ArrayList<String>> DATA)
    {
        try (PrintWriter writer = new PrintWriter(new File("DATA.csv"))) 
        {
            sb.append("Last_Race,"); sb.append("Past_Race,"); sb.append("Past_Track,");  sb.append("Past_Pos,"); sb.append("Pgm,");
            sb.append("Horse,"); sb.append("Jockey,");  sb.append("Weight,"); sb.append("M/E,"); sb.append("PP,"); sb.append("Finish,");
            sb.append("Odds,"); sb.append("Fav,"); sb.append("Comments,"); sb.append('\n');
            
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
        String[] horseAndJockey = new String[3]; String weight = ""; String polePosition = ""; 
        String[] odds_fav_com = new String[3];
        String[] weight_arr = new String[2];
        String[] polePos_ME = new String[2];
        String finish = "";
        int horse_num = 1;
        for(int i = lIndex; i < uIndex+1; i++)
        {
            ArrayList<String> indivHorse = new ArrayList<String>();
            dataLinePieces  = lines[i].split(" ");
            pastPerf_pgm    = pastPerf_pgm(dataLinePieces);   
            horseAndJockey  = horseAndJockey(dataLinePieces); //Slapped on ')' index, easier for pole pos and M/E
            weight_arr      = weight(dataLinePieces); 
            polePos_ME      = polePos_ME(dataLinePieces, weight_arr[1]); 
            //finish          = finish(lines[lIndex-1], dataLinePieces, horse_num+"", horseAndJockey[0]);
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
            indivHorse.add(weight_arr[0]); indivHorse.add(polePos_ME[1]); indivHorse.add(polePos_ME[0]); indivHorse.add("FIN"); //indivHorse.add(finish); indivHorse.add(finish);
            indivHorse.add(odds_fav_com[0]); indivHorse.add(odds_fav_com[1]); indivHorse.add(odds_fav_com[2]);
            
            horses.add(indivHorse);
            horse_num++;
        }
        
        return horses;
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
        pages_removed++;
        return false;
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
    
    public String[] polePos_ME(String[] dataPieces, String ind)
    {
        String polePosition = "ERROR";
        String me = "";
        int me_index = Integer.valueOf(ind);
        int index = -1;
        
        // M/E
        int pole_pos_index = 0;
        for(int i = me_index; i <me_index+4; i++)
        {
            String[] comps = dataPieces[i].split("");
            int asciiVal = (int) comps[0].charAt(0);
            //
            if(asciiVal >= 65 && asciiVal <= 90 || asciiVal >= 97 && asciiVal <= 122)
            {
                pole_pos_index = i;
                me += dataPieces[i];
            }
            
            if(asciiVal == 45) pole_pos_index = i;
        }
        if(me.equals("")) me = "N/A";
        
        int pp_index = 0;
        pole_pos_index:
        for(int i = 0; i < dataPieces.length; i++)
        {
            String[] tempVal = ((dataPieces[i]).split(""));
            if((tempVal[tempVal.length-1]).equals(")"))
            {
                pp_index = i+1;
                break pole_pos_index;
            }
        }
        
        for(int i = pp_index; i < pp_index+7; i++)
        {
            int asciiVal = dataPieces[i].split("")[0].charAt(0);
            if(dataPieces[i].split("").length == 1 && (asciiVal >= 48 && asciiVal <= 57))
            {
                //System.out.println("pole pos: " + dataPieces[i] + " ");
                return new String[] {dataPieces[i], me};
            }else{
                if(dataPieces[i].split("").length == 2)
                {
                    int asciiVal1 = dataPieces[i].split("")[0].charAt(0);
                    int asciiVal2 = dataPieces[i].split("")[1].charAt(0);
                    if(asciiVal1 >= 48 && asciiVal1 <= 57 && asciiVal2 >= 48 && asciiVal2 <= 57)
                    {
                        //System.out.println("pole pos: " + dataPieces[i] + " ");
                        return new String[] {dataPieces[i], me};
                    }
                }
            }
        }
        
        System.out.println("ERROR: Pole Pos");
        for(int i = pp_index; i < pp_index+6; i++)
        {
            System.out.print(dataPieces[i] + " " );
        }
        System.out.println("");

        return new String[] {"ERR", "ERR"};
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
        
        return new String[] {hName, jName, (jIndex1+2)+""};
    }
    
    public String[] weight(String[] dataPieces)
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
                    return new String[] {showError("ERROR: CRITICAL - WEIGHT: " + cWeight), "-1"};
                }
            }
        }

        return new String[] {cWeight, wIndex+""};
    }
    
    public String[] pastPerf_pgm(String[] arr)
    {
        if(arr[0].equals("---"))
        {
            return new String[] {"FR", "FR", "FR", arr[1]};
        }else{
            String[] w = arr[1].split("");
            String past_race ="";
            String past_track ="";
            String past_pos ="";
            boolean letters = false;
            for(int i = 0; i < w.length; i++)
            {
                int asciiVal = w[i].charAt(0);
                if(!letters)//Isolates track number
                {
                    if(asciiVal >= 48 && asciiVal <= 57)
                    {
                        past_race+=w[i];
                    }else{
                        past_track += w[i];
                        letters = true;
                    }
                }else{
                    if(asciiVal >= 48 && asciiVal <= 57)//numbers after letters have been found (past_Pos)
                    {
                        past_pos+=w[i];
                    }else{
                        if(asciiVal >= 65 && asciiVal <= 90 || asciiVal >= 97 && asciiVal <= 122) past_track += w[i];
                    }
                }
            }
            //System.out.println(past_race + " " + past_track + " " + past_pos);
            return new String[] {past_race, past_track, past_pos, arr[2]};
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
        if(trackType.equals("Dirt") || trackType.equals("Turf") || trackType.equals("Outer") || trackType.equals("Inner") || trackType.equals("Hurdle"))
        {
            return trackType;
        }

        return showError("ERROR: Track type not recognized: " + trackType);
    }
    
    public String[] distance_trackRecord(String[] lines)
    {
        int index = -1;
        String track_record = "";
        String[] dataLinePieces, track_record_pieces;
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
        track_record_pieces = lines[index].split("Record");
        String[] allChars = track_record_pieces[1].split("");
        
        boolean found_per = false;
        for(int i = 0; i < allChars.length; i++)
        {
            if(allChars[i].equals(".")) found_per = true;
        }
        //TRACK RECORD
        if(found_per)
        {
            track_record_pieces = track_record_pieces[1].split("-");
            track_record_pieces = track_record_pieces[1].split(" ");
            track_record = track_record_pieces[1];
        }else{//Too long string, track record booted to next line (AQU 11/14/20 Race 3)
            System.out.println(lines[index+1]);
            track_record_pieces = lines[index+1].split("- ");
            track_record_pieces = track_record_pieces[1].split("");
            track_record = track_record_pieces[1];
            System.out.println("date: " + date + "  TR:" + track_record);
        }
        
        //System.out.println("TR:" + track_record);
        
        //DISTANCE
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
        if(distance.equals("Four And One Half Furlongs")) return new String[] {"990", track_record};
        if(distance.equals("Five And One Half Furlongs")) return new String[] {"1210", track_record};
        if(distance.equals("Six Furlongs")) return new String[] {"1310", track_record};
        if(distance.equals("Six And One Half Furlongs")) return new String[] {"1430", track_record};
        if(distance.equals("Seven Furlongs")) return new String[] {"1540", track_record};
        if(distance.equals("One Mile")) return new String[] {"1760", track_record};
        if(distance.equals("One Mile And Seventy Yards")) return new String[] {"1830", track_record};
        if(distance.equals("One And One Sixteenth Miles")) return new String[] {"1870", track_record};
        if(distance.equals("One And One Eighth Miles")) return new String[] {"1980", track_record};
        if(distance.equals("One And Three Sixteenth Miles")) return new String[] {"2090", track_record};
        if(distance.equals("One And One Fourth Miles")) return new String[] {"2200", track_record};
        if(distance.equals("One And Three Eighth Miles")) return new String[] {"2420", track_record};
        if(distance.equals("One And Five Sixteenth Miles")) return new String[] {"2310", track_record};
        if(distance.equals("One And One Half Miles")) return new String[] {"2640", track_record};

        System.out.println("ERROR: Non registered distance of: '" + distance +"'");
        return new String[] {"ERR", "ERR"};
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
        String[] dataLinePieces;
        int index = find_index(lines, "Weather:");
        
        if(index == -1)
        {
            return showError("ERROR: weather() - Could not find weather");
        }
        dataLinePieces = lines[index].split(" ");
        return dataLinePieces[1];
    }
    
    public String trackCondition(String[] lines)
    {
        String[] dataLinePieces;
        int index = find_index(lines, "Weather:");
        
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
    
    public String finish(String var_line, String[] data_pieces, String horse_num, String horse_name)
    {
        //System.out.println(var_line);
        var_line = var_line.split("PP ")[1];
        var_line = var_line.split(" Odds")[0];
        int num_of_pieces = var_line.split(" ").length;
        //System.out.println(num_of_pieces);
        //System.out.println(var_line);
        
        int start_index = 0, finish_index = 0;
        
        start_index:
        for(int i = 0; i < data_pieces.length; i++)
        {
            String[] chars = ((data_pieces[i]).split(""));
            for(int j = 0; j < chars.length; j++)
            {
                if((chars[j]).equals(")") )
                {
                    start_index = i+2;
                }
            }
        }
        
        finish_index:
        for(int i = start_index; i < data_pieces.length; i++)
        {
            String[] chars = ((data_pieces[i]).split(""));
            for(int j = 0; j < chars.length; j++)
            {
                if((chars[j]).equals(".") )
                {
                    finish_index = i;
                    break finish_index;
                }
            }
        }
        
        //Isolating Measurments from Pole Pos and M/E
        ArrayList<String> pieces = new ArrayList<String>();
        boolean is_data = false;
        boolean start_data = false;
        final_start_index:
        for(int i = start_index; i < finish_index; i++)
        {
            int asciiVal = data_pieces[i].charAt(0);
            
            if(asciiVal >= 48 && asciiVal <= 57)
            {
                start_index = i + 1;
                break final_start_index;
            }
        }
        
        for(int i = start_index; i < finish_index; i++)
        {
            if(i >= finish_index-2)
            {
                pieces.add(data_pieces[i]);
                //System.out.print(data_pieces[i] + ",");
            }
        }
        
        for(int i = 0; i < pieces.size(); i++)
        {
            //System.out.print(pieces.get(i) + ",");
            if(i == pieces.size()-1 && pieces.get(i).equals("---")) return "DNF";
            
            if(i == pieces.size()-1 && pieces.get(i).split("").length == 1)
            {
                return pieces.get(i);
            }else{//Anyone other than the last horse to finish
                //Anyone other than the last horse to finish
                if(i == pieces.size()-1)
                {
                    String[] last_word = pieces.get(i).split("");
                    boolean has_slash = false;
                    for(int j = 0; j < last_word.length; j++)
                    { 
                        if(last_word[j].equals("/")) has_slash = true;
                    }
                    
                    if(has_slash)//in last piece of info
                    {
                        //System.out.println("Slash:" + pieces.get(i-1) + " " + pieces.get(i));
                        
                        //EX: 6th Place, 15/03/2020: 63,1/2 -> 6^(3+1/2)
                        if(pieces.get(i).split("").length == 3 && pieces.get(i-1).split("").length == 2 && pieces.get(i-1).split("")[0].equals(horse_num))
                        {
                            return (pieces.get(i-1).split("")[0] + "_" + pieces.get(i-1).split("")[1] + "+" + pieces.get(i));
                        }
                        
                        //EX 4th place, 15/03/2020: 41/2,41/2, -> 4^(1/2)
                        if(pieces.get(i).split("").length == 4 && pieces.get(i).substring(0,1).equals(horse_num))
                        {
                            return (pieces.get(i).substring(0,1) + "_" + pieces.get(i).substring(1,pieces.get(i).split("").length));
                        }
                        
                        //EX 10th place, 15/03/2020: 101/2, -> 4^(1/2)
                        if(pieces.get(i).split("").length == 5 && pieces.get(i).substring(0,2).equals(horse_num))
                        {
                            return (pieces.get(i).substring(0,2) + "_" + pieces.get(i).substring(2,pieces.get(i).split("").length));
                        }
                        
                        //EX: 5th place, 515,1/2,
                        if(pieces.get(i-1).substring(0,1).equals(horse_num))
                        {
                            return (pieces.get(i-1).substring(0,1) + "_" + pieces.get(i-1).substring(1,pieces.get(i).split("").length) + "+" +  pieces.get(i));
                        }
                        
                        //EX: 11th place, 1015,1/2
                        if(pieces.get(i-1).substring(0,2).equals(horse_num))
                        {
                            return (pieces.get(i-1).substring(0,2) + "_" + pieces.get(i-1).substring(2,pieces.get(i).split("").length) + "+" +  pieces.get(i));
                        }
                        
                        //MUST GO LAST, TIE (ERR: Will Did It, 3, 31/12/2016 -> 21,1/2,)
                        int h_num_tie = Integer.valueOf(horse_num)-1;
                        int DATA = Integer.valueOf(pieces.get(i-1).split("")[0]);
                        if(pieces.get(i).split("").length == 3 && pieces.get(i-1).split("").length == 2 && h_num_tie==DATA)//
                        {
                            //System.out.println("TIE: " + date + " - " +horse_num +" - "+ horse_name + "-" + pieces.get(i));
                            return (pieces.get(i-1).split("")[0] + "_" + pieces.get(i-1).split("")[1] + "+" + pieces.get(i));
                        }
                        
                        //EX.Sir Ballantine, 5, 02/12/2016,41/2,
                        DATA = Integer.valueOf(pieces.get(i).substring(0,1));
                        if(pieces.get(i).split("").length == 4 && h_num_tie==DATA)//
                        {
                            //System.out.println("TIE: " + date + " - " +horse_num +" - "+ horse_name + "-" + pieces.get(i));
                            return (pieces.get(i).substring(0,1) + "_" + pieces.get(i).substring(1,pieces.get(i).split("").length));
                        }
                        
                        //EX.ERR: Oathkeeper, 11, 09/11/2016: 101/2,
                        if(pieces.get(i).split("").length > 4)
                        {
                            //System.out.println(pieces.get(i));
                            DATA = Integer.valueOf(pieces.get(i).substring(0,2));
                            if(pieces.get(i).split("").length == 5 && h_num_tie==DATA)//
                            {
                                //System.out.println("TIE: " + date + " - " +horse_num +" - "+ horse_name + "-" + pieces.get(i));
                                return (pieces.get(i).substring(0,2) + "_" + pieces.get(i).substring(2,pieces.get(i).split("").length));
                            }
                        }
                        
                    }else{
                        //System.out.println("No Slash:" + pieces.get(i));
                        
                        //EX: 3rd place, 15/03/2020: 3Nose
                        if(pieces.get(i).substring(0,1).equals(horse_num))
                        {
                            return (pieces.get(i).substring(0,1) + "_" + pieces.get(i).substring(1,pieces.get(i).split("").length));
                        }
                        
                        //EX: 11th place, 15/03/2020: 11Head
                        if(pieces.get(i).substring(0,2).equals(horse_num))
                        {
                            return (pieces.get(i).substring(0,2) + "_" + pieces.get(i).substring(2,pieces.get(i).split("").length));
                        }
                        
                        //MUST GO LAST, TIE (ERR: Will Did It, 3, 31/12/2016 -> 21,1/2,)
                        int h_num_tie = Integer.valueOf(horse_num)-1;
                        int DATA = Integer.valueOf(pieces.get(i).substring(0,1));
                        //EX: ERR: Whitegate, 6, 01/12/2016 -> 51,52,
                        if(pieces.get(i).split("").length == 2 && h_num_tie==DATA)//
                        {
                            //System.out.println("TIE: " + date + " - " +horse_num +" - "+ horse_name + "-" + pieces.get(i));
                            return (pieces.get(i).substring(0,1) + "_" + pieces.get(i).substring(1,2));
                        }
                        
                        //EX: 3rd place, 15/03/2020: 2Nose ->ERR: Glacken Too, 3, 26/03/2015 - 31/2,2Neck,
                        if(DATA==h_num_tie)
                        {
                            return (pieces.get(i).substring(0,1) + "_" + pieces.get(i).substring(1,pieces.get(i).split("").length));
                        }
                    }
                }
                
                
                
            }

        }
        
        System.out.println("ERR: "+ horse_name + ", " + horse_num + ", " + date);
        for(int i = 0; i < pieces.size(); i++)
        {
            //System.out.print(pieces.get(i) + ",");
        }
        
        for(int i = 0; i < finish_index; i++)
        {
            System.out.println(data_pieces[i]);
        }
        
        System.out.println("");
        
        return "ERR"; //showError("ERROR: Did not find finish position");
    }
    
    public String final_time(String[] lines)
    {
        int index = find_index(lines, "Fractional");
        String[] final_time_pieces = lines[index].split("Final Time:");
        String final_time = final_time_pieces[1].split(" ")[1];
        final_time_pieces = final_time.split("");
        
        final_time = "";
        for(int i = 0; i < final_time_pieces.length; i++)
        {
            int asciiVal = final_time_pieces[i].charAt(0);
            if(asciiVal >= 48 && asciiVal <= 58  || asciiVal == 46)  final_time += final_time_pieces[i];
        }
        
        return final_time;
    }
    
    public int find_index(String[] lines, String keyword)
    {
        int index = -1;
        String[] dataLinePieces;
        for(int i = 0; i < lines.length-1; i++)
        {
            dataLinePieces = lines[i].split(" ");
            //System.out.println(dataLinePieces[0]);
            if(dataLinePieces[0].equals(keyword))
            {
                return i;
            }
        }
        
        return index;
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
        return "ERR!";
    }
}
