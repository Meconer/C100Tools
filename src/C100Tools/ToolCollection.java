/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package C100Tools;

import java.util.ArrayList;
import java.util.Iterator;

/**
 *
 * @author mats
 */
public class ToolCollection {
    ArrayList<Tool> collection = new ArrayList<>();

    public void addTool(String id, int turretNo, int placeNo, int dNo ) {
        
        if ( !toolExist( dNo ) ) {
            Tool tool = new Tool( id, turretNo, placeNo, 1, dNo);
            collection.add(tool);
        }
    }

    public void addTool( Tool tool ) {
        collection.add(tool);
    }
    
    public boolean toolExist(int dNo) {
        Iterator<Tool> toolIterator = collection.iterator();
        while ( toolIterator.hasNext() ) {
            Tool tool = toolIterator.next();
            if ( tool.getdNo() == dNo) {
                return true;
            }
        }
        return false;
    }

    public void toolPrint() {
        Iterator<Tool> toolIterator = collection.iterator();
        while ( toolIterator.hasNext() ) {
            Tool tool = toolIterator.next();
            System.out.println(tool);
        }
    }

    
    
    public ArrayList<String> getToolList() {
        ArrayList<String> toolList = new ArrayList<>();
        Iterator<Tool> toolIterator = collection.iterator();
        while ( toolIterator.hasNext() ) {
            Tool tool = toolIterator.next();
            toolList.add(tool.toString());
        }
        return toolList;
    }

    public ArrayList<String> getToolList(int turretNo) {
        ArrayList<String> toolList = new ArrayList<>();
        Iterator<Tool> toolIterator = collection.iterator();
        while ( toolIterator.hasNext() ) {
            Tool tool = toolIterator.next();
            if ( turretNo == tool.getTurretNo() ) {
                toolList.add(tool.toString());
            }
        }
        return toolList;
        
    }
    
    public ArrayList<Tool> getToolsByPlace(int turretNo, int placeNo ) {
        ArrayList<Tool> toolListByPlace = new ArrayList<>();
        Iterator<Tool> toolIterator = collection.iterator();
        while (toolIterator.hasNext() ) {
            Tool tool = toolIterator.next();
            if (tool.turretNo == turretNo ) {
                if (tool.placeNo == placeNo ) {
                    toolListByPlace.add(tool);
                }
            }
        }
        return toolListByPlace;
    }
    
    public Tool getToolByPlaceAndStation(int turretNo, int placeNo, int stationNo ) {
        
        Iterator<Tool> toolIterator = collection.iterator();
        while ( toolIterator.hasNext() ) {
            Tool tool = toolIterator.next();
            if ( tool.getTurretNo() == turretNo ) {
                if ( tool.getPlaceNo() == placeNo ) {
                    if (tool.getStationNo() == stationNo ) {
                        return tool;
                    }
                }
            }
        }
        return null;
    }

    public void calculateStationNumbers() {
        
        ArrayList<Tool> toolForThisPlace;
        if ( collection != null ) {
            for (int turretNo = 1 ; turretNo <= Tool.MAX_TURRET_NUMBER ; turretNo++ ) {
                for ( int placeNo = 1; placeNo <= Tool.MAX_PLACE_NUMBER ; placeNo++ ) {
                    int stationNo = 1;
                    toolForThisPlace = getToolsByPlace(turretNo, placeNo);
                    Iterator<Tool> tIter = toolForThisPlace.iterator();
                    while ( tIter.hasNext() ) {
                        tIter.next().stationNo = stationNo;
                        stationNo++;
                    }
                }
            }
        }
    }

}
