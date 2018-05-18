package ch.infofauna.excel.geo;
import com.fasterxml.jackson.annotation.JsonIgnore;
public class Location {


    private String countryCode;


    private String departmentCode;


    private String precisionCode;


    private String localite;


    private String lieudit;

    public Location() {
    }

    public Location(Double swissCoordinatesX, Double swissCoordinatesY) {
        this.swissCoordinatesX = swissCoordinatesX;
        this.swissCoordinatesY = swissCoordinatesY;
    }

    private Double swissCoordinatesX;
    private Double swissCoordinatesY;
    private Double altitude;

    @JsonIgnore
    public Coordinates getCoordinates(){
        if(this.swissCoordinatesX != null && this.swissCoordinatesY != null) {
            return new Coordinates(swissCoordinatesX, swissCoordinatesY);
        }
        return null;
    }

    @Override
    public String toString() {
        return "Location{" +
                "countryCode='" + countryCode + '\'' +
                ", departmentCode='" + departmentCode + '\'' +
                ", precisionCode='" + precisionCode + '\'' +
                ", localite='" + localite + '\'' +
                ", lieudit='" + lieudit + '\'' +
                ", swissCoordinatesX=" + swissCoordinatesX +
                ", swissCoordinatesY=" + swissCoordinatesY +
                ", altitude=" + altitude +
                '}';
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getDepartmentCode() {
        return departmentCode;
    }

    public void setDepartmentCode(String departmentCode) {
        this.departmentCode = departmentCode;
    }

    public String getPrecisionCode() {
        return precisionCode;
    }

    public void setPrecisionCode(String precisionCode) {
        this.precisionCode = precisionCode;
    }

    public String getLocalite() {
        return localite;
    }

    public void setLocalite(String localite) {
        this.localite = localite;
    }

    public String getLieudit() {
        return lieudit;
    }

    public void setLieudit(String lieudit) {
        this.lieudit = lieudit;
    }

    public Double getSwissCoordinatesX() {
        return swissCoordinatesX;
    }

    public void setSwissCoordinatesX(Double swissCoordinatesX) {
        this.swissCoordinatesX = swissCoordinatesX;
    }

    public Double getSwissCoordinatesY() {
        return swissCoordinatesY;
    }

    public void setSwissCoordinatesY(Double swissCoordinatesY) {
        this.swissCoordinatesY = swissCoordinatesY;
    }

    public Double getAltitude() {
        return altitude;
    }

    public void setAltitude(Double altitude) {
        this.altitude = altitude;
    }
}
