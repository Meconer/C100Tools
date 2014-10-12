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
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;

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
        ArrayList<String> retToolList = usedTools.getToolList(revolverNo);
        return retToolList;
    }

    private void buildUsedToolCollection() throws NumberFormatException {
        
        // Create a new empty tool collection
        usedTools = new ToolCollection();
        
        // Go through the program for each turret (channel)
        for (int turretNo = 1; turretNo <= Tool.MAX_TURRET_NUMBER ; turretNo++) {
            String[] program = programRev[turretNo-1];

            int currentToolNo = 0;
            String currentToolId = "";
            
            String toolRegexp = ".*(T\\d+).*";
            String dNoRegexp = ".*(D\\d+).*";
            String toolIdRegexp = ".*T\\d+.*D\\d+.*;(.*)";
            Pattern toolPattern = Pattern.compile(toolRegexp);
            Pattern dNoPattern = Pattern.compile(dNoRegexp);
            Pattern toolIdPattern = Pattern.compile(toolIdRegexp);
            
            // Check each line
            for (String line : program ) {
                
                // First check if this is a line of the form T# D# ... ; Tool Id
                // If it is, then extract the tool id.
                Matcher m = toolIdPattern.matcher(line);
                if (m.matches()) {
                    currentToolId = m.group(1);
                }
                
                // Now remove all comments
                line = line.replaceAll(";.*", "");
                // and also remove everything in parenthesis
                line = line.replaceAll("\\(.*?\\)", "");
                // make it in uppercase
                line = line.toUpperCase();
                
                // Check if the line has a T number. If so, set the current tool number
                m = toolPattern.matcher(line);
                if (m.matches()) {
                    String toolString = m.group(1);
                    int toolNo = Integer.parseInt(toolString.substring(1));
                    currentToolNo = toolNo;
                }
                
                // now check if the line has a D number. If so, add the current tool
                m = dNoPattern.matcher(line);
                if ( m.matches() ) {
                    String dString = m.group(1);
                    int dNo = Integer.parseInt( dString.substring(1) );
                    if ( ( currentToolNo != 0 ) && ( dNo != 0 ) ) {
                        usedTools.addTool( currentToolId, turretNo, currentToolNo, dNo );
                        
                        // Reset the tool id string so the next tool doesn't get the same id.
                        currentToolId ="";
                    }
                }
            }
            
            // If the same tool place has more than one d number then we should calculate a
            // new station number for each dNo that tool place has.
            usedTools.calculateStationNumbers();
            usedTools.toolPrint();
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
        
    }

    void setTextArea(JTextArea jTAProgramArea) {
        this.jTAProgramArea = jTAProgramArea;
    }

    public JTree buildC100ToolTree() {
        
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("C100");
      
        for ( int turretNo = 1 ; turretNo <= Tool.MAX_TURRET_NUMBER ; turretNo++ ) {
            DefaultMutableTreeNode turretTree = new DefaultMutableTreeNode("Revolver " + turretNo);
            root.add(turretTree);
            for ( int placeNo = 1 ; placeNo <= Tool.MAX_PLACE_NUMBER ; placeNo++ ) {
                DefaultMutableTreeNode placeNode = new DefaultMutableTreeNode("Plats " + placeNo );
                turretTree.add(placeNode);
                ArrayList<Tool> toolListByPlace = usedTools.getToolsByPlace(turretNo, placeNo);
                Iterator<Tool> toolIterator = toolListByPlace.iterator();
                while (toolIterator.hasNext() ) {
                    DefaultMutableTreeNode tool = new DefaultMutableTreeNode( toolIterator.next() );
                    placeNode.add(tool);
                }
            }
        }
        JTree jTreeC100 = new JTree(root);

        return jTreeC100;
    }
}
