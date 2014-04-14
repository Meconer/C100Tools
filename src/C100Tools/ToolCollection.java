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

    void addTool(int toolNo, int dNo, int turretNo ) {
        if ( !toolExist( toolNo, dNo, turretNo ) ) {
            Tool tool = new Tool( "Rev" + turretNo + "T" + toolNo + "D" + dNo, turretNo, toolNo);
            tool.dNos.add(dNo);
            collection.add(tool);
        }
            
    }

    public boolean toolExist(int toolNo, int dNo, int turrentNo) {
        Iterator<Tool> toolIterator = collection.iterator();
        while ( toolIterator.hasNext() ) {
            Tool tool = toolIterator.next();
            if ( tool.getTurretNo() == turrentNo ) {
                if ( tool.getStationNo() == toolNo ) {
                    if ( tool.getdNo().contains(dNo)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public ArrayList<String> getToolList() {
        ArrayList<String> toolList = new ArrayList<>();
        Iterator<Tool> toolIterator = collection.iterator();
        while ( toolIterator.hasNext() ) {
            Tool tool = toolIterator.next();
            for ( int dNo : tool.getdNo() )
            toolList.add("Rev" + tool.getTurretNo() + " T" + tool.getStationNo() + " D" + dNo );
        }
        return toolList;
    }
}
