package com.qametrics.portal.domain.model;

import java.util.ArrayList;
import java.util.List;

public class ClientTrackingSummary {

    private int totalEvolutivos;
    private int defectsEvolutivos;
    private Double qualityEvolutivos;

    private int totalSoportes;
    private int defectsSoportes;
    private Double qualitySoportes;

    private int totalStandardChange;
    private int defectsStandardChange;
    private Double qualityStandardChange;

    private int totalDeliveries;
    private int totalDefects;
    private Double consolidatedQuality;
    private double targetQuality = 95.0;

    private List<MonthlyQualityData> monthlyTrend = new ArrayList<>();

    public ClientTrackingSummary() {}

    public static class MonthlyQualityData {
        private String monthName; // ej. "Ene 2026", "Jun 2026"
        private int year;
        private int month;
        private Double evolutivosQuality;
        private Double soportesQuality;
        private Double standardChangeQuality;
        private Double consolidatedQuality;

        public MonthlyQualityData() {}

        public MonthlyQualityData(String monthName, int year, int month, Double evolutivosQuality,
                                  Double soportesQuality, Double standardChangeQuality, Double consolidatedQuality) {
            this.monthName = monthName;
            this.year = year;
            this.month = month;
            this.evolutivosQuality = evolutivosQuality;
            this.soportesQuality = soportesQuality;
            this.standardChangeQuality = standardChangeQuality;
            this.consolidatedQuality = consolidatedQuality;
        }

        public String getMonthName() { return monthName; }
        public void setMonthName(String monthName) { this.monthName = monthName; }
        public int getYear() { return year; }
        public void setYear(int year) { this.year = year; }
        public int getMonth() { return month; }
        public void setMonth(int month) { this.month = month; }
        public Double getEvolutivosQuality() { return evolutivosQuality; }
        public void setEvolutivosQuality(Double q) { this.evolutivosQuality = q; }
        public Double getSoportesQuality() { return soportesQuality; }
        public void setSoportesQuality(Double q) { this.soportesQuality = q; }
        public Double getStandardChangeQuality() { return standardChangeQuality; }
        public void setStandardChangeQuality(Double q) { this.standardChangeQuality = q; }
        public Double getConsolidatedQuality() { return consolidatedQuality; }
        public void setConsolidatedQuality(Double q) { this.consolidatedQuality = q; }
    }

    public int getTotalEvolutivos() { return totalEvolutivos; }
    public void setTotalEvolutivos(int totalEvolutivos) { this.totalEvolutivos = totalEvolutivos; }
    public int getDefectsEvolutivos() { return defectsEvolutivos; }
    public void setDefectsEvolutivos(int defectsEvolutivos) { this.defectsEvolutivos = defectsEvolutivos; }
    public Double getQualityEvolutivos() { return qualityEvolutivos; }
    public void setQualityEvolutivos(Double qualityEvolutivos) { this.qualityEvolutivos = qualityEvolutivos; }
    public int getTotalSoportes() { return totalSoportes; }
    public void setTotalSoportes(int totalSoportes) { this.totalSoportes = totalSoportes; }
    public int getDefectsSoportes() { return defectsSoportes; }
    public void setDefectsSoportes(int defectsSoportes) { this.defectsSoportes = defectsSoportes; }
    public Double getQualitySoportes() { return qualitySoportes; }
    public void setQualitySoportes(Double qualitySoportes) { this.qualitySoportes = qualitySoportes; }
    public int getTotalStandardChange() { return totalStandardChange; }
    public void setTotalStandardChange(int totalStandardChange) { this.totalStandardChange = totalStandardChange; }
    public int getDefectsStandardChange() { return defectsStandardChange; }
    public void setDefectsStandardChange(int defectsStandardChange) { this.defectsStandardChange = defectsStandardChange; }
    public Double getQualityStandardChange() { return qualityStandardChange; }
    public void setQualityStandardChange(Double qualityStandardChange) { this.qualityStandardChange = qualityStandardChange; }
    public int getTotalDeliveries() { return totalDeliveries; }
    public void setTotalDeliveries(int totalDeliveries) { this.totalDeliveries = totalDeliveries; }
    public int getTotalDefects() { return totalDefects; }
    public void setTotalDefects(int totalDefects) { this.totalDefects = totalDefects; }
    public Double getConsolidatedQuality() { return consolidatedQuality; }
    public void setConsolidatedQuality(Double consolidatedQuality) { this.consolidatedQuality = consolidatedQuality; }
    public double getTargetQuality() { return targetQuality; }
    public void setTargetQuality(double targetQuality) { this.targetQuality = targetQuality; }
    public List<MonthlyQualityData> getMonthlyTrend() { return monthlyTrend; }
    public void setMonthlyTrend(List<MonthlyQualityData> monthlyTrend) { this.monthlyTrend = monthlyTrend; }
}
