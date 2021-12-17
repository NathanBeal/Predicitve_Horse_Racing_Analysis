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
    int page_number;
    String last_distance;
    
    //Control Variables
    int START_PAGE = 1; //Actual Start is 1
    int END_PAGE = 3;
    boolean WRTITE_TO_CSV = true;
    
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
        String data_dir = System.getProperty("user.dir");
        data_dir = (data_dir.split("2. Java PDF-Data Conditioner")[0].replace("\\", "/") + "Racetrack Data/");
        data_dir += "AQU - Aqueduct Data/AQU 2017-2020-EDITED.pdf"; //Select which folder/data woulkd like to be used
        File myFile = new File(data_dir);
        System.out.println(data_dir);
        
        String text;
        String[] pages;
        int num_pages = 0;
        try (PDDocument doc = PDDocument.load(myFile)) 
        {
            PDFTextStripper stripper = new PDFTextStripper();
            text = stripper.getText(doc);
            num_pages = doc.getNumberOfPages();
            pages = new String[(num_pages+1)];
            
            for(int i = 0; i < num_pages; i++)
            {
                stripper.setStartPage(i);
                stripper.setEndPage(i);
                text = stripper.getText(doc);
                pages[i] = text;
            }
            
            //Adds the Last Page of Data to the Pages Array
            stripper.setStartPage(num_pages);
            stripper.setEndPage(num_pages);
            text = stripper.getText(doc);
            pages[num_pages] = text;

            System.out.println("Text size: " + text.length() + " Pages: " + num_pages);
        }
        
        //Evaluates Each Document Seperately
        ArrayList<ArrayList<String>> values = new ArrayList<ArrayList<String>>();
        String currentRace;
        int min = 0;
        int lim = num_pages+1;
        String last_date = "";
        for (int i = START_PAGE; i < lim; i++)//END_PAGE; i++)
        {
            //Temp ArrayList to collect Values of each race/page
            page_number = i;//Page numbber flag
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
                if(WRTITE_TO_CSV) writeToCSV(DATA);
            }
        } 
        if(WRTITE_TO_CSV) System.out.println("Finished Writing to CSV");
        else System.out.println("You have opted not to write to CSV file");
    }
    
    public void writeToCSV(ArrayList<ArrayList<String>> DATA)
    {
        try (PrintWriter writer = new PrintWriter(new File("DATA.csv"))) 
        {
            sb.append("Last_Race,"); sb.append("Past_Race,"); sb.append("Past_Track,");  sb.append("Past_Pos,"); sb.append("Pgm,");
            sb.append("Horse,"); sb.append("Jockey,");  sb.append("Weight,"); sb.append("M/E,"); sb.append("PP,"); sb.append("Finish (Place^Dist),");
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
            finish          = finish(lines[lIndex-1], dataLinePieces, horse_num+"", horseAndJockey[0]); //System.out.println("Distance Ahead: " + finish);
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
            indivHorse.add(weight_arr[0]); indivHorse.add(polePos_ME[1]); indivHorse.add(polePos_ME[0]); indivHorse.add(finish); //indivHorse.add("FIN"); 
            indivHorse.add(odds_fav_com[0]); indivHorse.add(odds_fav_com[1]); indivHorse.add(odds_fav_com[2]);
            
            horses.add(indivHorse);
            horse_num++;
            
            //System.out.println("PAGE NUM : " + page_number);
            //System.out.println(finish);
            last_distance = finish.split(" & ")[1];
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
        
        String odds = dataPieces[oddsIndex];
        String[] odds_pieces = odds.split("");
        odds = "";
        for(int i = 0; i < odds_pieces.length; i++)
        {
            int asciiChar = (int) odds_pieces[i].charAt(0);
            if(asciiChar >= 46 && asciiChar <= 57) odds += odds_pieces[i];
            if(odds_pieces[i].equals("*")) isFavorited = "Y";  
        }
        //System.out.println("Horse Odds: " + dataPieces[oddsIndex]);

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
        
        /*
        int S = 100;
        if(sOdds.equals(ODDS)) S = 2;
        else 
        { 
            System.out.println("Odds Mismatch: " + sOdds + "  " + ODDS);
            showError("-");
        }
        */
        
        odds_fav_com[0] = odds;
        odds_fav_com[1] = isFavorited;
        odds_fav_com[2] = comments;
        if(Float.valueOf(odds) == -1 || oddsIndex == -1)
        {
            showError("ERROR: retreiveOdds(), odds' decimal possibly not located");
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
            if(asciiVal >= 65 && asciiVal <= 90 || asciiVal >= 97 && asciiVal <= 122 || asciiVal == 45)
            {
                pole_pos_index = i;
                me += dataPieces[i];
            }
        }
        if(me.equals(""))
        {   
            me = "ERR"; 
            showError("Pole Position not found");
        }
        
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
        
        jockey_indexing:
        for(int i = 2; i < dataPieces.length-3; i++)//Added "-3" because of one case where algo was tripping on comment of '(widest)'
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
            
            //Added in the  "> 5" provision becuase in 'Nymphesa (GER) (Bravo, Joe)' "GER" was being parsed as Jockey
            if(jIndex1 != -1 && jIndex2 != -1 && (jIndex2-jIndex1) > 5) break jockey_indexing;
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
        //System.out.println(hName);
        
        if(hName == "" && dataPieces[0].equals("---"))
        {
            hName = dataPieces[2];
        }
        
        if(jName.equals("") || hName.equals(""))
        {
            showError("ERROR 404 - Horse/Jockey Name Not found");
            for(int i = 2; i < dataPieces.length; i++)
            {
                System.out.print(dataPieces[i] + "  ");
            }
            System.out.println();
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
        boolean found_per_two_lines = false;
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
            //Combines two lines to allow program to find track record
            String merged_lines = lines[index].replaceAll("[\n\r]", "") + lines[index+1].replaceAll("[\n\r]", "");
            allChars = merged_lines.split("Record")[1].split("");
            for(int i = 0; i < allChars.length; i++)
            {
                if(allChars[i].equals(".")) found_per_two_lines = true;
            }
            if(found_per_two_lines)
            {
                track_record = merged_lines.split("Track Record:")[1]; //Ex. "... Track Record: (Battle of Blenheim- 1:39.91 - April 14, 2018)"
                track_record = track_record.split("-")[1]; //Ex. "(Battle of Blenheim- 1:39.91 - April 14, 2018)"
                track_record = track_record.split(" ")[1]; //Ex. " 1:39.91 "
                //System.out.println("ML Track Record: " + track_record + "  Page Number: " + page_number);
            }else{
                showError("Issue finding the track record");
                track_record = "ERR";
            }
        }
        
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
        /* How far ahead did the horse finish in fron of the horese behind it*/
        //System.out.println(var_line);
        
        String distance_ahead = "";
        int num_of_pieces = var_line.split(" ").length;
        String glue =" & ";
        //System.out.println(num_of_pieces);
        //System.out.println(var_line);
        
        boolean distance_fraction = false;
        boolean distance_horse_part = false;
        
        
        int finish_index = -1;
        for(int i = 2; i < data_pieces.length; i++)
        {
            String[] tempVal = ((data_pieces[i]).split(""));
            for(int j = 0; j < tempVal.length-1; j++)
            {
                if( (tempVal[j]).equals(".") )
                {
                    finish_index = i;
                }
            }
        }
        
        //System.out.print("("+ horse_num +"): ");
        //Work backwards to see if the last sequence in the now first position has a / or their place number in uit
        String finish_distance_1 = data_pieces[finish_index-2];
        String finish_distance_2 = data_pieces[finish_index-1];
        //System.out.println(finish_distance_1 + ", " + finish_distance_2);
        
        // First check if there are words (Neck Head) or a /
        String [] fd1_pieces = finish_distance_1.split("");
        String [] fd2_pieces = finish_distance_2.split("");
        for(int i = 0; i < fd2_pieces.length; i++)
        {
            if(fd2_pieces[i].equals("/")) distance_fraction = true;
            if(is_letter(fd2_pieces[i])) distance_horse_part = true;
        }
        
        if(finish_distance_2.equals("---")) return "-1" + glue + "DNF";
        if(Integer.valueOf(horse_num) < 10)
        {
            //HANDLED IN THE 2ND PART OF THE PHRASE
            //Last place horse
            if(fd2_pieces[0].equals(horse_num) && fd2_pieces.length == 1) 
            return (horse_num + glue + "Last");
            //Has neck, heqad, nose in distance
            if(distance_horse_part && fd2_pieces[0].equals(horse_num))
            {
                return horse_num +glue+finish_distance_2.substring(1);
            }
            
            //Horse beat horse behind it by single integer value
            if(!distance_horse_part && !distance_fraction && fd2_pieces.length == 2 && fd2_pieces[0].equals(horse_num)) 
            return (horse_num+glue+fd2_pieces[1]);
            
            //Ex. 43/4 = Came in fourth beat by 3/4
            if(distance_fraction && fd2_pieces.length == 4 && fd2_pieces[0].equals(horse_num))
            return (horse_num + glue +"0" + fraction_to_decimal(finish_distance_2.substring(1,4)));
            
            //HANDLED IN THE 1ST PART OF THE PHRASE
            //Ex.  61, 1/4 = Came in 6th, 1.25
            if(distance_fraction && fd1_pieces[0].equals(horse_num) && fd1_pieces.length == 2)
            return horse_num+glue+(fd1_pieces[1] + fraction_to_decimal(finish_distance_2));
            
            //Multiple length loss with fraction
            if(distance_fraction && fd1_pieces[0].equals(horse_num) && fd1_pieces.length == 3) 
            return horse_num+glue+(finish_distance_1.substring(1,3) + fraction_to_decimal(finish_distance_2));
            
            //Integer distance ahead of length > 10
            //Ex.  612 = Came in 6th, 12 lengths
            if(!distance_horse_part && !distance_fraction && fd2_pieces[0].equals(horse_num) && fd2_pieces.length == 3) 
            return horse_num+glue+(finish_distance_2.substring(1,3));
            
            //TIES
            //Ex. WIP: 36, 1/2 = Tie for 3rd, 6.5 lengths (Page Number: 2911)
            if(distance_fraction && (Integer.valueOf(fd1_pieces[0])+1)==Integer.valueOf(horse_num) && fd1_pieces.length == 2 &&
            last_distance.equals(fd1_pieces[1] + fraction_to_decimal(finish_distance_2)))
            {
                //System.out.println("Tie on page: " + page_number);
                return fd1_pieces[0]+glue+(fd1_pieces[1] + fraction_to_decimal(finish_distance_2));
            }
            //Ex. WIP: 21 = Tie for 2nd, 1 length (Page Number: 712)
            if(!distance_fraction && fd2_pieces.length == 2 && (Integer.valueOf(fd2_pieces[0])+1)==Integer.valueOf(horse_num) &&
            last_distance.equals(fd2_pieces[1]))
            {
                //System.out.println("Tie on page: " + page_number);
                return fd2_pieces[0]+glue+(fd2_pieces[1]);
            }
            //Ex. WIP: 71/2 = Tie for 7TH, 1/2 length (Page Number: 753)
            if(distance_fraction && fd2_pieces.length == 4 && (Integer.valueOf(fd2_pieces[0])+1)==Integer.valueOf(horse_num))
            {
                //System.out.println(last_distance);
                String d = "0" +  fraction_to_decimal(finish_distance_2.substring(1));
                if(last_distance.equals(d))
                {
                    //System.out.println("Tie on page: " + page_number);
                    return fd2_pieces[0]+glue+"0" + fraction_to_decimal(finish_distance_2.substring(1));
                }
            }
            //Ex. 4Neck = Tied for 4th w/ neck distance
            if(distance_horse_part && fd2_pieces.length == 5 &&  (Integer.valueOf(fd2_pieces[0])+1)==Integer.valueOf(horse_num)
            && last_distance.equals(finish_distance_2.substring(1)))
            {
                //System.out.println(last_distance);
                return fd2_pieces[0]+glue+finish_distance_2.substring(1);
            }
            //Ex.  813, 1/2 = Tied for 7th, 23 1/2 lengths
            if(distance_fraction && fd2_pieces.length == 3 && (Integer.valueOf(fd1_pieces[0])+1)==Integer.valueOf(horse_num))
            {
                String curr_distance = finish_distance_1.substring(1) +  fraction_to_decimal(finish_distance_2);
                //System.out.println("Tie on page: " + page_number);
                return fd1_pieces[0]+glue+curr_distance;
            }
            
            //Tied for last
            if( (Integer.valueOf(fd2_pieces[0])+1)==Integer.valueOf(horse_num) && fd2_pieces.length == 1 && last_distance.equals("Last")) 
            return fd2_pieces[0]+glue+"Last";
            
            System.out.print("("+ horse_num +"): ");
            System.out.println("Last Distance: " + last_distance);
            showError("WIP:  " + finish_distance_1 + ", " + finish_distance_2);
            return "WIP";
        }else{
            //Last place horse
            if(finish_distance_2.equals(horse_num) && fd2_pieces.length == 2) 
            return horse_num +glue+"Last";
            
            //Has neck, head, nose in distance
            if(distance_horse_part && finish_distance_2.substring(0,2).equals(horse_num))
            {
                return horse_num +glue+finish_distance_2.substring(2); 
            }
            
            //Ex. 101/2 = 10th place, 1/2 (2961)
            if(distance_fraction && fd2_pieces.length == 5 && finish_distance_2.substring(0,2).equals(horse_num)) 
            return horse_num+glue+(finish_distance_2.substring(2,5));
            
            //Ex. 109 = 10th place, 9 (2975)
            if(!distance_fraction && !distance_horse_part && fd2_pieces.length == 3 && finish_distance_2.substring(0,2).equals(horse_num)) 
            return horse_num+glue+(fd2_pieces[2]);
            
            //Ex. 1035 = 10th place, 35 lengths (2964)
            if(!distance_fraction && !distance_horse_part && fd2_pieces.length == 4 && finish_distance_2.substring(0,2).equals(horse_num)) 
            return horse_num+glue+(finish_distance_2.substring(2,4));
            
            //Ex. 1313, 1/4 = 13th place, 13.25 (2980)
            if(distance_fraction && finish_distance_1.substring(0,2).equals(horse_num) && fd1_pieces.length == 4 && fd2_pieces.length == 3) 
            return horse_num+glue+(finish_distance_1.substring(2,4) + fraction_to_decimal(finish_distance_2));
            
            //Ex.  126, 1/2 = 12th place, 6.5
            if(distance_fraction && finish_distance_1.substring(0,2).equals(horse_num) && fd1_pieces.length == 3 && fd2_pieces.length == 3)
            return horse_num+glue+(fd1_pieces[2] + fraction_to_decimal(finish_distance_2));
            
            //TIES
            // EX. 91, 3/4 = Tied for 9th, 1.75
            if(distance_fraction && fd2_pieces.length == 3 && (Integer.valueOf(fd1_pieces[0])+1)==Integer.valueOf(horse_num))
            {
                String curr_distance = finish_distance_1.substring(1) +  fraction_to_decimal(finish_distance_2);
                if(last_distance.equals(curr_distance))
                {
                    //System.out.println("Tie on page: " + page_number);
                    return fd1_pieces[0]+glue+curr_distance;
                }
            }
            
            System.out.print("("+ horse_num +"): ");
            showError("WIP:  " + finish_distance_1 + ", " + finish_distance_2);
            return "WIP";
        }
        
        
        /*
        int start_index = 0, //finish_index = 0;
        for(int i = 0; i < data_pieces.length; i++)
        {
            //System.out.print(data_pieces[i] + "+");
        }
        //System.out.println();
        
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
            if(i == pieces.size()-1 && pieces.get(i).equals("---")) return "-1:DNF";
            
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
        */
        //System.out.println("");
        
        //return "ERR"; //showError("ERROR: Did not find finish position");
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
    
    public boolean is_letter(String letter)
    {
        int ascii_value = (int) letter.charAt(0);
        if((ascii_value >= 65 && ascii_value <= 90) || (ascii_value >= 97 && ascii_value <= 122)) return true;
        else return false;
    }
    
    public String fraction_to_decimal(String fraction)
    {
        String [] fraction_pieces = fraction.split("");
        
        if(fraction_pieces[1].equals("/"))
        {
            if(fraction_pieces[0].equals("1") && fraction_pieces[2].equals("4")) return ".25";
            if(fraction_pieces[0].equals("3") && fraction_pieces[2].equals("4")) return ".75";
            if(fraction_pieces[0].equals("1") && fraction_pieces[2].equals("2")) return ".5";
            showError("Fraction not defined (" + fraction + ")");
        }else{
            showError("Not real fraction sent (" + fraction + ")");
        }
        return "ERR";
    }
    
    public String horse_length_to_decimal(String length)
    {   
        //Distances are w.r.t. a "length". length defined as "approximately 8 feet"
        if(length.equals("Nose")) return ".05";
        if(length.equals("Head")) return ".2";
        if(length.equals("Neck")) return ".3";
        return "ERR";
    }
  
    
    public String showError(String message)
    {
        System.out.println("Page Number: " + page_number);
        System.out.println(message);
        return "ERR!";
    }
}
