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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

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
    private String toolListProgram;
    String entireProgram;
    private ToolCollection usedTools;
    private JTextArea jTAProgramArea;
    private Path currentFilePath;

    public C100Program(String entireProgram) {
        this.entireProgram = entireProgram;
        extractParts();
    }

    private void extractParts() {
        programRev[0] = getProgramPart( REV1HEADER);
        programRev[1] = getProgramPart( REV2HEADER);
        programRev[2] = getProgramPart( REV3HEADER);
        toolListProgram = getProgramPartAsString(TOOLLISTPROGHEADER);
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

    private String getProgramPartAsString( String header) {
        String regexp = "^" + header + "(.*?M30)";
        Pattern p = Pattern.compile(regexp, Pattern.MULTILINE + Pattern.DOTALL);
        Matcher m = p.matcher(entireProgram);
        if (m.find()) {
            String thisProgram = m.group(1);
            return thisProgram;
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

    private void storeTool(Tool tool, ToolCollection toolCollection ) {
        if ( tool != null ) {
            if ( toolCollection != null ) {
                toolCollection.addTool(tool);
            }
        }
    }

    void storeToolTreeInArc() {
        Pattern p = Pattern.compile( "(.*)MAG=1.*" , Pattern.DOTALL + Pattern.MULTILINE );
        Matcher m = p.matcher(toolListProgram);
        String newToolListProgram = "";
        if ( m.matches() ) newToolListProgram = m.group(1);
        
        for ( int turretNo = 1 ; turretNo <= 3 ; turretNo++ ) {
            if ( turretNo > 1 ) newToolListProgram += "\n; Revolver " + turretNo + "\n";
            newToolListProgram += "MAG=" + turretNo + "\n";
            for ( int placeNo = 1 ; placeNo <= 10 ; placeNo++ ){
                newToolListProgram += "  PL=" + placeNo;
                ArrayList<Tool> toolList = usedTools.getToolsByPlace(turretNo, placeNo);
                if ( toolList.isEmpty() ) {
                    newToolListProgram += " ID= \"\"\n";
                } else {
                    Iterator<Tool> toolListIterator = toolList.iterator();
                    Tool tool = toolListIterator.next();
                    newToolListProgram += " ID=\"" + tool.getId() + "\" TYP=" + tool.getType() + "\n";
                    boolean ready = false;
                    do {
                        newToolListProgram += "    SN=" + tool.getStationNo() + " D=" + tool.getdNo() + "\n";
                        newToolListProgram += "     " + getOptionalString(" Q=",tool.getqValue() ) + 
                                getOptionalString(" L=", tool.getlValue() ) +
                                getOptionalString(" H=", tool.gethValue() ) +
                                getOptionalString(" R=", tool.getrValue() ) + "\n";
                        int sl = tool.getSlValue();
                        if ( sl !=0 ) {
                            newToolListProgram += "      SL=" + sl + "\n";
                        }
                        String ofsString = getOptionalString(" V_Q=", tool.getQ_ofs() ) + 
                                getOptionalString(" V_L=", tool.getL_ofs() ) +
                                getOptionalString(" V_H=", tool.getH_ofs() ) +
                                getOptionalString(" V_R=", tool.getR_ofs() );
                        if ( !ofsString.isEmpty() ) newToolListProgram  += "     " + ofsString + "\n";
                        if ( toolListIterator.hasNext() ) {
                            tool = toolListIterator.next();
                        } else {
                            ready = true;
                        }
                    } while (!ready);
                }
            }
        }
        
        newToolListProgram += "\n_J:\nM30\n";
        
        String regexp = "^" + TOOLLISTPROGHEADER + ".*?M30";
        p = Pattern.compile(regexp, Pattern.MULTILINE + Pattern.DOTALL);
        m = p.matcher(entireProgram);
        if ( m.find() ) {
            entireProgram = entireProgram.substring(0, m.start() ) +
                    TOOLLISTPROGHEADER + "\n" +
                    newToolListProgram + entireProgram.substring(m.end());
            System.out.println("Matches!");
        }
        //System.out.println(entireProgram);
        jTAProgramArea.setText(entireProgram);
        
    }

    private String getOptionalString(String s, String valueString ) {
        if ( valueString != null  ) {
            if ( valueString.isEmpty() ) return "";
            return s + valueString;
        }
        return "";
    }

    
    public class FormatException extends Exception {
        public FormatException( String message ){
            super( message );
        }
    }
    
    private void buildUsedToolCollectionFromToolListProgram() {
        // Create a new empty tool collection
        usedTools = new ToolCollection();
        
        Scanner scanner = new Scanner(toolListProgram);

        int turretNo = 0;
        int stationNo;
        String id = "";
        int placeNo = 0;
        int dNo;
        int toolType = 0;
        
        boolean toolIsStarted = false;
        
        Tool tool = null ;

        final String FLOAT_REGEX = "[-+]?[0-9]*\\.?[0-9]+";
        
        while (scanner.hasNext()) {
            String s = scanner.next();
            
            
            try {
                if ( s.startsWith(";") ) { 
                    // A comment. Skip to next line.
                    scanner.nextLine();
                } else {
                    
                    if ( s.matches( "MAG=\\d+" ) ) {
                        // Set turretNo
                        turretNo = Integer.parseInt( s.split("=")[1] );
                    }
                    if ( s.matches( "^PL=\\d+" ) ) {
                        // Read placeNo, id and toolType
                        placeNo = Integer.parseInt( s.split( "=" )[1] );

                        // We have id and toolType no in the rest of the line. Read it. 
                        s = scanner.nextLine().trim();
                        int pos;
                        if ( s.startsWith( "ID=") ) {
                            pos = s.indexOf( "\"" );
                            s = s.substring( pos + 1 );
                            pos= s.indexOf( "\"" );
                            id = s.substring( 0, pos );
                            System.out.println(id);
                        } else throw new FormatException("Felaktigt format i verktygsfilen");

                        // Now look for the toolType.
                        try {
                            s = s.substring( pos + 1 ).trim();
                            if ( s.startsWith( "TYP=") ) s = s.substring(4);
                            toolType = Integer.parseInt(s);
                        } catch ( NumberFormatException E ) {
                            toolType = 0;
                        }
                        
                    }

                    // Station no
                    if ( s.matches( "^SN=\\d+" ) ) {
                        stationNo = Integer.parseInt( s.split( "=" )[1] );
                        storeTool( tool, usedTools );
                        tool = new Tool(id, turretNo, placeNo, stationNo, 0 );
                        tool.setType(toolType);
                        toolIsStarted = true;
                    }
                    
                    // D no
                    if ( s.matches( "^D=\\d+" ) ) {
                        dNo = Integer.parseInt( s.split( "=" )[1] );
                        if ( tool != null ) tool.setdNo(dNo);
                    }
                    
                    // Q value
                    if ( s.matches( "^Q=" + FLOAT_REGEX  ) ) {
                        String value =  s.split( "=" )[1];
                        if ( tool != null ) tool.setqValue( value );
                    }
                    
                    // L value
                    if ( s.matches( "^L=" + FLOAT_REGEX ) ) {
                        String value =  s.split( "=" )[1];
                        if ( tool != null ) tool.setlValue( value );
                    }
                    
                    // H value
                    if ( s.matches( "^H=" + FLOAT_REGEX  ) ) {
                        String value =  s.split( "=" )[1];
                        if ( tool != null ) tool.sethValue( value );
                    }
                    
                    // R value
                    if ( s.matches( "^R=" + FLOAT_REGEX  ) ) {
                        String value =  s.split( "=" )[1];
                        if ( tool != null ) tool.setrValue( value );
                    }
                    
                    // SL value
                    if ( s.matches( "^SL=\\d+" ) ) {
                        int value =  Integer.parseInt( s.split( "=" )[1] );
                        if ( tool != null ) tool.setSlValue( value );
                    }
                    
                    // V_Q value
                    if ( s.matches( "^V_Q=" + FLOAT_REGEX  ) ) {
                        String value =  s.split( "=" )[1];
                        if ( tool != null ) tool.setQ_ofs( value );
                    }
                    
                    // V_L value
                    if ( s.matches( "^V_L=" + FLOAT_REGEX  ) ) {
                        String value =  s.split( "=" )[1];
                        if ( tool != null ) tool.setL_ofs( value );
                    }
                    
                    // V_H value
                    if ( s.matches( "^V_H=" + FLOAT_REGEX  ) ) {
                        String value =  s.split( "=" )[1];
                        if ( tool != null ) tool.setH_ofs( value );
                    }
                    
                    // V_R value
                    if ( s.matches( "^V_R=" + FLOAT_REGEX  ) ) {
                        String value =  s.split( "=" )[1];
                        if ( tool != null ) tool.setR_ofs( value );
                    }
                    
                }
            } catch ( FormatException e ){
                System.out.println("FEL: " + e.getMessage() );
            }
        }
        // Store last tool if we found any.
        if ( toolIsStarted ) storeTool(tool, usedTools);
    }

//    private Tool getToolFromPlacePart(String thisPlacePart, int turretNo, int placeNo ) {
//        String toolId = getIdFromPlacePart( thisPlacePart );
//        
//        if ( ! toolId.equals("") ) {
//            String regexSN = "(SN=\\d+)(.*?)(SN=)|(PL=)|(^ *$)";
//            Pattern snPattern = Pattern.compile( regexSN, Pattern.DOTALL + Pattern.MULTILINE );
//            Matcher m = snPattern.matcher(thisPlacePart);
//            while ( m.find() ) {
//                System.out.println("Regex match " + m.group() );
//            }
//            //return newTool;
//        } 
//        return null;
//    }
//
//    private int getIntValueFromPlacePart( String placePart, String nameToLookFor ) {
//        String regexp = nameToLookFor + "(\\d+)";
//        Pattern p = Pattern.compile( regexp );
//        Matcher m = p.matcher( placePart );
//        if ( m.find() ) {
//            String s = m.group(1);
//            int num = Integer.parseInt(s);
//            return num;
//        }
//        else return -Integer.MAX_VALUE;
//    }
//    
//    
//    private String getIdFromPlacePart( String placePart ) {
//        String regexp = "ID=\"(.+)\"";
//        Pattern p = Pattern.compile( regexp );
//        Matcher m = p.matcher(placePart );
//        if ( m.find() ) return m.group(1);
//        else return "";
//    }
    
    private void buildUsedToolCollectionFromMainProgram() throws NumberFormatException {
        
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
                    currentToolId = m.group(1).trim();
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
                currentFilePath = Paths.get(fileName);
                System.out.println("Path " + currentFilePath);
            } catch (IOException ex) {
                Logger.getLogger(C100ToolsMainWindow.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    void saveFile() {
        entireProgram = jTAProgramArea.getText();
        try {
            Files.write(currentFilePath, entireProgram.getBytes() );
        } catch (IOException ex) {
            JOptionPane.showConfirmDialog( null , "Kunde inte spara filen " + ex.getMessage() , "FEL!", JOptionPane.OK_OPTION);
        }
    }

    void analyseMainProgram() {
        buildUsedToolCollectionFromMainProgram();
    }

    void setTextArea(JTextArea jTAProgramArea) {
        this.jTAProgramArea = jTAProgramArea;
    }

//    public void buildC100ToolTree(JScrollPane jSPC100TreePane) {
//        
//        DefaultMutableTreeNode root = new DefaultMutableTreeNode("C100");
//      
//        for ( int turretNo = 1 ; turretNo <= Tool.MAX_TURRET_NUMBER ; turretNo++ ) {
//            DefaultMutableTreeNode turretTree = new DefaultMutableTreeNode("Revolver " + turretNo);
//            root.add(turretTree);
//            for ( int placeNo = 1 ; placeNo <= Tool.MAX_PLACE_NUMBER ; placeNo++ ) {
//                ArrayList<Tool> toolListByPlace = usedTools.getToolsByPlace(turretNo, placeNo);
//                if ( !toolListByPlace.isEmpty() ) {
//                    DefaultMutableTreeNode placeNode = new DefaultMutableTreeNode("Plats " + placeNo );
//                    turretTree.add(placeNode);
//                    Iterator<Tool> toolIterator = toolListByPlace.iterator();
//                    while (toolIterator.hasNext() ) {
//                        DefaultMutableTreeNode tool = new DefaultMutableTreeNode( toolIterator.next() );
//                        placeNode.add(tool);
//                    }
//                }
//            }
//        }
//        JTree jTreeC100 = new JTree(root);
//        jSPC100TreePane.setViewportView(jTreeC100);
//        for (int i = 0; i < jTreeC100.getRowCount(); i++) {
//            jTreeC100.expandRow(i);
//        }
//    }

    public void buildC100ToolTree(JScrollPane jSPC100TreePane, JTree jTreeC100 ) {
        
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) jTreeC100.getModel().getRoot();
        root.removeAllChildren();
      
        for ( int turretNo = 1 ; turretNo <= Tool.MAX_TURRET_NUMBER ; turretNo++ ) {
            DefaultMutableTreeNode turretTree = new DefaultMutableTreeNode("Revolver " + turretNo);
            root.add(turretTree);
            for ( int placeNo = 1 ; placeNo <= Tool.MAX_PLACE_NUMBER ; placeNo++ ) {
                ArrayList<Tool> toolListByPlace = usedTools.getToolsByPlace(turretNo, placeNo);
                if ( !toolListByPlace.isEmpty() ) {
                    DefaultMutableTreeNode placeNode = new DefaultMutableTreeNode("Plats " + placeNo );
                    turretTree.add(placeNode);
                    Iterator<Tool> toolIterator = toolListByPlace.iterator();
                    while (toolIterator.hasNext() ) {
                        DefaultMutableTreeNode tool = new DefaultMutableTreeNode( toolIterator.next() );
                        placeNode.add(tool);
                    }
                }
            }
        }
    }

    void analyseToolListProgram() {
        buildUsedToolCollectionFromToolListProgram();
    }

}
