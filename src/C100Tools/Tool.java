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
public class Tool {

    public Tool(String id, int turretNo, int stationNo, int dNo, String qValue, String lValue, String hValue, String rValue, int slValue) {
        this.id = id;
        this.turretNo = turretNo;
        this.stationNo = stationNo;
        this.dNo = dNo;
        this.qValue = qValue;
        this.lValue = lValue;
        this.hValue = hValue;
        this.rValue = rValue;
        this.slValue = slValue;
    }

    public Tool(String id, int turretNo, int stationNo) {
        this.id = id;
        this.turretNo = turretNo;
        this.stationNo = stationNo;
    }
    protected String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getTurretNo() {
        return turretNo;
    }

    public void setTurretNo(int turretNo) {
        this.turretNo = turretNo;
    }

    public int getStationNo() {
        return stationNo;
    }

    public void setStationNo(int stationNo) {
        this.stationNo = stationNo;
    }

    public int getdNo() {
        return dNo;
    }

    public void setdNo(int dNo) {
        this.dNo = dNo;
    }

    public String getqValue() {
        return qValue;
    }

    public void setqValue(String qValue) {
        this.qValue = qValue;
    }

    public String getlValue() {
        return lValue;
    }

    public void setlValue(String lValue) {
        this.lValue = lValue;
    }

    public String gethValue() {
        return hValue;
    }

    public void sethValue(String hValue) {
        this.hValue = hValue;
    }

    public String getrValue() {
        return rValue;
    }

    public void setrValue(String rValue) {
        this.rValue = rValue;
    }

    public int getSlValue() {
        return slValue;
    }

    public void setSlValue(int slValue) {
        this.slValue = slValue;
    }

    public String getQ_ofs() {
        return q_ofs;
    }

    public void setQ_ofs(String q_ofs) {
        this.q_ofs = q_ofs;
    }

    public String getL_ofs() {
        return l_ofs;
    }

    public void setL_ofs(String l_ofs) {
        this.l_ofs = l_ofs;
    }

    public String getH_ofs() {
        return h_ofs;
    }

    public void setH_ofs(String h_ofs) {
        this.h_ofs = h_ofs;
    }

    public String getR_ofs() {
        return r_ofs;
    }

    public void setR_ofs(String r_ofs) {
        this.r_ofs = r_ofs;
    }
    protected int turretNo;
    protected int stationNo;
    protected int dNo;
    protected String qValue;
    protected String lValue;
    protected String hValue;
    protected String rValue;
    protected int slValue;
    protected String q_ofs;
    protected String l_ofs;
    protected String h_ofs;
    protected String r_ofs;
    
}
