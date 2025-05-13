package com.example.EarthquakeCheck.DTO;





public class EvaluationResult {

    private String safetyGrade;
    private String nearestAssemblyArea;


    public EvaluationResult(String safetyGrade, String nearestAssemblyArea){

        this.safetyGrade=safetyGrade;
        this.nearestAssemblyArea=nearestAssemblyArea;

}
    public String getSafetyGrade(){
        return safetyGrade;
    }
    public void setSafetyGrade(String safetyGrade){
        this.safetyGrade=safetyGrade;
    }
    public String getNearestAssemblyArea(){
        return nearestAssemblyArea;
    }
public void setNearestAssemblyArea(){
    this.nearestAssemblyArea=nearestAssemblyArea;
}


}