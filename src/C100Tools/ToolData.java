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
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;

/**
 *
 * @author mats
 */
class ToolData {

    private String toolText;
    private JTextArea toolTextArea;
    private ArrayList<MeasuredTool> measuredToolList = new ArrayList<>();
    
    private final static String FLOAT_REGEX = "[-+]?[0-9]*\\.?[0-9]+";


    public void setToolTextArea(JTextArea toolTextArea) {
        this.toolTextArea = toolTextArea;
    }

    public boolean analyseToolData() {
        if ( toolText == null ) return false;
        Scanner scanner = new Scanner(toolText);
        
        MeasuredTool mTool = null;
        while ( scanner.hasNext() ) {
            String s = scanner.next();

            // D no
            if ( s.matches("D=\\d+")) {
                // Lines start with D, so if there is an old mTool, now is the time to store it in the arraylist.
                if ( mTool != null ) measuredToolList.add(mTool);

                mTool = new MeasuredTool();
                String dNoString = s.split("=")[1];
                try {
                    int dNo = Integer.parseInt(dNoString );
                    mTool.setdNo(dNo);
                } catch ( NumberFormatException e ) {
                    showError( );
                }
            }
            
            // SL
            if ( s.matches( "SL=\\d+" )) {
                if ( mTool != null ) {
                    try {
                        mTool.setsL( Integer.parseInt(s.split("=")[1]) );
                    } catch ( NumberFormatException e ) {
                        showError( );
                    }
                }
            }
            
            // TYP
            if ( s.matches( "TYP=\\d+" )) {
                if ( mTool != null ) {
                    try {
                        mTool.setToolType( Integer.parseInt(s.split("=")[1]) );
                    } catch ( NumberFormatException e ) {
                        showError( );
                    }
                }
            }
            
            // Q
            if ( s.matches( "^Q=" + FLOAT_REGEX )) {
                if ( mTool != null ) {
                    try {
                        String value = s.split( "=" )[1];
                        Double.parseDouble( value );
                        mTool.setqVal( value );
                    } catch ( NumberFormatException e ) {
                        showError();
                    }
                }
            }

            // L
            if ( s.matches( "^L=" + FLOAT_REGEX )) {
                if ( mTool != null ) {
                    try {
                        String value = s.split( "=" )[1];
                        Double.parseDouble( value );
                        mTool.setlVal( value );
                    } catch ( NumberFormatException e ) {
                        showError();
                    }
                }
            }

            // R
            if ( s.matches( "^R=" + FLOAT_REGEX )) {
                if ( mTool != null ) {
                    try {
                        String value = s.split( "=" )[1];
                        Double.parseDouble( value );
                        mTool.setrVal( value );
                    } catch ( NumberFormatException e ) {
                        showError();
                    }
                }
            }
        }
        
        // When we get here there is one unstored tool. Store it now.
        measuredToolList.add(mTool);
        
        return true;
    }

    private void showError()  {
        JOptionPane.showConfirmDialog(null, "Fel i filen med mätta data", "FEL", JOptionPane.OK_OPTION );
    }
    public void readFile(String fileName){
        
        if ( fileName != null ) {

            byte[] encoded;
            try {
                encoded = Files.readAllBytes(Paths.get(fileName));
                Charset cs = Charset.forName("ISO_8859_1");
                toolText = cs.decode(ByteBuffer.wrap(encoded)).toString();
                toolTextArea.setText( toolText );
            } catch (IOException ex) {
                Logger.getLogger(C100ToolsMainWindow.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        analyseToolData();
    }

    public Iterator<MeasuredTool> getIterator() {
        return measuredToolList.iterator();
    }
    
    public ArrayList<MeasuredTool> getList() {
        return measuredToolList;
    }

    
}
