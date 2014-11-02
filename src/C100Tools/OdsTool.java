/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package C100Tools;

/**
 *
 * @author mats
 */
class OdsTool implements Comparable<OdsTool>{
    private int toolNo = 0;
    private int placeNo = 0;
    private String length = "";
    private String radius = "";
    private String comment = "";
    private String holderName = "";

    public int getToolNo() {
        return toolNo;
    }

    public void setToolNo(int toolNo) {
        this.toolNo = toolNo;
    }

    public int getPlaceNo() {
        return placeNo;
    }

    public void setPlaceNo(int placeNo) {
        this.placeNo = placeNo;
    }

    public String getLength() {
        return length;
    }

    public void setLength(String length) {
        this.length = length;
        if ( length == null ) this.length = "";
    }

    public String getRadius() {
        return radius;
    }

    public void setRadius(String radius) {
        this.radius = radius;
        if ( radius == null ) this.radius = "";
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
        if ( comment == null ) this.comment = "";
    }

    void setHolderName(String holderName ) {
        this.holderName = holderName;
        if ( holderName == null ) this.holderName = "";
    }
    
    public String getHolderName() {
        return holderName;
    }

    @Override
    public int compareTo(OdsTool toolToCompare) {
        int compToolNo = toolToCompare.getToolNo();
        return ( compToolNo > toolNo ) ? -1 : 
               ( compToolNo == toolNo ) ? 0 : 1;
    }


}
