/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package C100Tools;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import javax.swing.JOptionPane;
import org.jopendocument.dom.OOUtils;
import org.jopendocument.dom.spreadsheet.Sheet;
import org.jopendocument.dom.spreadsheet.SpreadSheet;

/**
 *
 * @author mats
 */
public class OdsToolList {

    ArrayList<OdsTool> toolList = null;
    File odsToolListFile;
    private C100Preferences c100Preferences = C100Preferences.getInstance();
    
    OdsToolList(File selectedFile) {
        odsToolListFile = selectedFile;
        
    }

    
    private void saveToOds() throws IOException {
        String templateFileName = c100Preferences.getTemplateFileName();
        File odsTemplateFile = new File(templateFileName);
        Sheet sheet = SpreadSheet.createFromFile(odsTemplateFile).getFirstSheet();
        
        // Lägg in datum högst upp till höger
        sheet.getCellAt( 7, 0 ).setValue(new Date() );
        
        // Lägg in kund, benämning och artikelnummer i kolumn 0 och raderna 3,4 och 5
        // Först kund
        Path path = odsToolListFile.getAbsoluteFile().toPath();
        int level = path.getNameCount();
        String customerName = path.getName(level - 5 ).toString();
        sheet.getCellAt(0,3).setValue(customerName);

        // och så artikelnummer och benämning. Artikelnummer är det först "ordet" i pathnivå level - 4
        String pathLevelm4 = path.getName(level -4 ).toString();
        int posOfFirstSpace = pathLevelm4.indexOf(" ");
        String artNo = "";
        String name = "";
        if ( posOfFirstSpace > 0 ) {
            artNo = pathLevelm4.substring( 0, posOfFirstSpace );
            name = pathLevelm4.substring(posOfFirstSpace + 1 , pathLevelm4.length() );
        } else {
            name = pathLevelm4;
        }
        sheet.getCellAt( 0, 4 ).setValue(name);
        sheet.getCellAt( 0, 5 ).setValue(artNo);

        // Fyll i verktygslistan men sortera den först.
        Collections.sort(toolList );
        int currentOdsLine = 15;
        Iterator<OdsTool> iterator = toolList.iterator();
        while ( iterator.hasNext() ) {
            OdsTool tool = iterator.next();
            sheet.getCellAt( 0, currentOdsLine ).setValue(tool.getToolNo());
            sheet.getCellAt( 1, currentOdsLine ).setValue(tool.getPlaceNo());
            sheet.getCellAt( 2, currentOdsLine ).setValue(tool.getComment());
            sheet.getCellAt( 5, currentOdsLine ).setValue((String)tool.getRadius());
            sheet.getCellAt( 7, currentOdsLine ).setValue(tool.getHolderName());
            sheet.getCellAt( 2, currentOdsLine + 1 ).setValue( tool.getLength());
            currentOdsLine += 2;
        }
        sheet.getSpreadSheet().saveAs(odsToolListFile);
        OOUtils.open(odsToolListFile);
    }

    void createOdsToolList(ToolCollection usedTools) throws IOException {
        
        String templateFileName = c100Preferences.getTemplateFileName();
        File odsTemplateFile = new File(templateFileName);
        Sheet sheet = SpreadSheet.createFromFile(odsTemplateFile).getFirstSheet();
        
        for ( int turretNo = 1 ; turretNo <= 3 ; turretNo++ ) {
            
            int columnNo = 1 + ( turretNo - 1 ) * 6;
            
            for ( int placeNo = 1 ; placeNo <= 10 ; placeNo++ ) {
                int rowNo;
                if ( turretNo < 3 ) {
                    rowNo = placeNo * 2;
                } else {
                    rowNo = 1 + placeNo ;
                }
                ArrayList<Tool> placeList = usedTools.getToolsByPlace(turretNo, placeNo);
                if ( placeList.size() > 2 ) { 
                    JOptionPane.showConfirmDialog( null, "För många verktyg för verktygslistan. Endast två kan hanteras", "Fel!", JOptionPane.OK_OPTION );
                }
                Iterator<Tool> toolAtPlaceIterator = placeList.iterator();
                int counter = 0;
                while ( toolAtPlaceIterator.hasNext() ) {
                    Tool toolAtPlace = toolAtPlaceIterator.next();
                    int dNo = toolAtPlace.getdNo();
                    System.out.println("Row " + rowNo + counter + " Col " + columnNo );
                    sheet.getCellAt( columnNo, rowNo + counter ).setValue(dNo);
                    sheet.getCellAt( columnNo + 1, rowNo + counter).setValue( removeDots( toolAtPlace.getId() ) );
                    counter++;
                }
            }
        }
        sheet.getSpreadSheet().saveAs(odsToolListFile);
        OOUtils.open(odsToolListFile);

    }

    // Replaces all 
    private String removeDots(String id) {
        if ( id.startsWith(".")) id = " " + id.substring(1);
        if ( id.endsWith(".")) id = id.substring(0,id.length());

        boolean ready = false;
        int posOfDot = 0;
        while (!ready) {
            
            posOfDot = id. indexOf(".", posOfDot+1 );
            
            if ( posOfDot  < 0 ) ready = true;
            else {
            
                char chBefore = id.charAt(posOfDot - 1);
                char chAfter = id.charAt(posOfDot + 1);
                if ( Character.isDigit(chBefore) && Character.isDigit(chAfter)) {
                    // Both are digits so it probably is a decimal point. Keep it.
                } else {
                    id = id.substring(0, posOfDot ) + " " + id.substring(posOfDot+1,id.length());
                }
            }
        }
        return id;
    }
}
