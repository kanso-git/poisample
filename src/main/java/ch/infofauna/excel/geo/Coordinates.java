package ch.infofauna.excel.geo;

public class Coordinates {

    public final double coordX, coordY;

    public Coordinates(double coordX, double coordY) {
        this.coordX = coordX;
        this.coordY = coordY;
    }

    @Override
    public String toString() {
        return "Coordinates{" +
                "coordX=" + coordX +
                ", coordY=" + coordY +
                '}';
    }
}
