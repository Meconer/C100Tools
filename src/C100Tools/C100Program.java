/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package C100Tools;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JTextArea;

/**
 *
 * @author mats
 */
public class C100Program {
    private final static String REV1HEADER = "%_N_1_0_MPF";
    private final static String REV2HEADER = "%_N_2_0_MPF";
    private final static String REV3HEADER = "%_N_3_0_MPF";
    private final static String TOOLLISTPROGHEADER = "%_N_1_3_MPF";
    private String[][] programRev = new String[3][];
    private String[] toolListProgram;
    String entireProgram;
    private ArrayList<String>[] toolList = new ArrayList[3];
    private ToolCollection usedTools;
    private JTextArea jTAProgramArea;

    public C100Program(String entireProgram) {
        this.entireProgram = entireProgram;
        extractParts();
        for ( int i = 0 ; i < 3 ; i++ ) 
            toolList[i] = new ArrayList<>();
    }

    private void extractParts() {
        programRev[0] = getProgramPart( REV1HEADER);
        programRev[1] = getProgramPart( REV2HEADER);
        programRev[2] = getProgramPart( REV3HEADER);
        toolListProgram = getProgramPart( TOOLLISTPROGHEADER);
    }

    private String[] getProgramPart( String header) {
        String regexp = "^" + header + "(.*?M30)";
        Pattern p = Pattern.compile(regexp, Pattern.MULTILINE + Pattern.DOTALL);
        Matcher m = p.matcher(entireProgram);
        if (m.find()) {
            String thisProgram = m.group(1);
            String[] thisProgramAsArray = thisProgram.split("\r\n");
            return thisProgramAsArray;
        } else {
            return null;
        }
        
    }

    private void printOut(String[] program ) {
        for ( String line : program ) {
            System.out.println(line);
        }
    }
    
    public ArrayList<String> getToolsUsed(int revolverNo) {
        buildUsedToolCollection();
        ArrayList<String> retToolList = usedTools.getToolList(revolverNo);
        return retToolList;
    }

    private void buildUsedToolCollection() throws NumberFormatException {
        usedTools = new ToolCollection();
        for (int revNo = 0; revNo < 3 ; revNo++) {
            String[] program = programRev[revNo];

            int currentToolNo = 0;
            String toolRegexp = ".*(T\\d+).*";
            String dNoRegexp = ".*(D\\d+).*";
            Pattern toolPattern = Pattern.compile(toolRegexp);
            Pattern dNoPattern = Pattern.compile(dNoRegexp);
            for (String line : program ) {
                line = line.replaceAll(";.*", "");
                line = line.replaceAll("\\(.*?\\)", "");
                line = line.toUpperCase();
                Matcher m = toolPattern.matcher(line);
                if (m.matches()) {
                    String toolString = m.group(1);
                    int toolNo = Integer.parseInt(toolString.substring(1));
                    currentToolNo = toolNo;
                }
                m = dNoPattern.matcher(line);
                if ( m.matches() ) {
                    String dString = m.group(1);
                    int dNo = Integer.parseInt( dString.substring(1) );
                    if ( currentToolNo != 0 ) {
                        usedTools.addTool( currentToolNo, dNo, revNo );

                    }
                }

            }
        }
    }
    
    
    public void readFile(String fileName){
        
        if ( fileName != null ) {

            byte[] encoded;
            try {
                encoded = Files.readAllBytes(Paths.get(fileName));
                Charset cs = Charset.forName("ISO_8859_1");
                entireProgram = cs.decode(ByteBuffer.wrap(encoded)).toString();
                jTAProgramArea.setText(entireProgram);
                extractParts();
            } catch (IOException ex) {
                Logger.getLogger(C100ToolsMainWindow.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    void analyseProgram() {
        buildUsedToolCollection();
        for ( int i = 0 ; i < 3 ; i++ )
            toolList[i] = getToolsUsed(i);
        
    }

    void setTextArea(JTextArea jTAProgramArea) {
        this.jTAProgramArea = jTAProgramArea;
    }

     
    
}
