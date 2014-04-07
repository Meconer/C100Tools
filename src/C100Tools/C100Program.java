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
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public C100Program(String entireProgram) {
        this.entireProgram = entireProgram;
        extractParts();
    }

    private void extractParts() {
        programRev[0] = getProgramPart( REV1HEADER);
        programRev[1] = getProgramPart( REV2HEADER);
        programRev[2] = getProgramPart( REV3HEADER);
        toolListProgram = getProgramPart( TOOLLISTPROGHEADER);
        printOut(programRev[2]);
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
    
    public Set<Integer> getToolsUsed(int revolverNo) {
        Set<Integer> usedTools = new TreeSet<>();
        String[] program = programRev[revolverNo];
        
        String regexp = ".*(T\\d+).*";
        Pattern p = Pattern.compile(regexp);
        for (String line : program ) {
            line = line.replaceAll(";.*", "");
            line = line.replaceAll("\\(.*?\\)", "");
            line = line.toUpperCase();
            Matcher m = p.matcher(line);
            if (m.matches()) {
                String toolString = m.group(1);
                if ( !toolString.equals("T0")) {
                    toolString = toolString.replaceAll("T0+", "T");
                }
                int toolNo = Integer.parseInt(toolString.substring(1));
                usedTools.add(toolNo);
            }
        }
        
        return usedTools;
    }
    
    public void readFile(String fileName){
        
        if ( fileName != null ) {

            byte[] encoded;
            try {
                encoded = Files.readAllBytes(Paths.get(fileName));
                Charset cs = Charset.forName("ISO_8859_1");
                entireProgram = cs.decode(ByteBuffer.wrap(encoded)).toString();
                extractParts();
            } catch (IOException ex) {
                Logger.getLogger(C100ToolsMainWindow.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

     
    
}
