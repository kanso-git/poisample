package ch.infofauna.excel.geo;

public interface GeodataService {
    /**
     * Returns administratrive subdivisions information for the given coordinates, encapsulated in a Location object.
     * The fields country, department and locality (commune) will be filled.
     * If the point is outside Switzerland, an IllegalArgumentException will be raised.
     * @param coordinates
     * @return
     */

    public Location getAdministrativeLocation(Coordinates coordinates);

    double getAltitudeAt(Coordinates coordinates);

    public Coordinates wgs84toLv03(Coordinates coordinates);

    public Coordinates lv03toWgs84(Coordinates coordinates);

    public Coordinates wgs84toLv03Approx(Coordinates coordinates);

    public Coordinates lv03toWgs84Approx(Coordinates coordinates);
}
