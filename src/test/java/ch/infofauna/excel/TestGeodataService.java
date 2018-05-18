package ch.infofauna.excel;

import ch.infofauna.excel.geo.Coordinates;
import ch.infofauna.excel.geo.GeodataService;

import ch.infofauna.excel.geo.Location;
import ch.infofauna.excel.integration.IntegrationTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertEquals;

/**
 * @author: kanso
 */
public class TestGeodataService extends IntegrationTest {

    @Autowired
    private GeodataService service;

    //known point in CH Neuchâtel
    private final Coordinates coordinates1 = new Coordinates(562902, 205518);

    //same place in wgs84
    private final Coordinates coordinates1wgs84 = new Coordinates(6.9508604170216195, 46.99967977123971);

    //known point in France, dép. 25 Doubs
    private final Coordinates coordinates2 = new Coordinates(536716.0, 212354.0);

    @Test
    public void testGetAltitude(){
        double altitude = service.getAltitudeAt(coordinates1);
        assertEquals(altitude, 483.0, 0.001);
    }

    @Test
    public void testAdministrativeLocation(){
        Location location = service.getAdministrativeLocation(coordinates1);
        assertEquals("SZ-NE", location.getDepartmentCode());
        assertEquals("SZ", location.getCountryCode());
        assertEquals("Neuchâtel", location.getLocalite());
    }

    @Test()
    public void testAdministrativeLocationOutsideCH(){
        Location location = service.getAdministrativeLocation(coordinates2);
        assert(location.getDepartmentCode() == null);
        assert(location.getCountryCode() == null);
        assert(location.getLocalite() == null);
        assert(location.getLieudit() == null);
    }

    /*
    @Test
    public void testWgs84toLv03(){

        Coordinates lv03 = service.wgs84toLv03(coordinates1wgs84);

        assertEquals(coordinates1.coordX, lv03.coordX, 0.01);
        assertEquals(coordinates1.coordY, lv03.coordY, 0.01);
    }

    @Test
    public void testLv03toWgs84(){

        Coordinates wgs84 = service.lv03toWgs84(coordinates1);

        assertEquals(coordinates1wgs84.coordX, wgs84.coordX, 0.00000001);
        assertEquals(coordinates1wgs84.coordY, wgs84.coordY, 0.00000001);
    }
    */

    @Test
    public void testWgs84toLv03Approx(){

        Coordinates lv03 = service.wgs84toLv03Approx(coordinates1wgs84);

        assertEquals(coordinates1.coordX, lv03.coordX, 1);
        assertEquals(coordinates1.coordY, lv03.coordY, 1);
    }

    @Test
    public void testLv03toWgs84Approx(){

        Coordinates wgs84 = service.lv03toWgs84Approx(coordinates1);

        assertEquals(coordinates1wgs84.coordX, wgs84.coordX, 0.0001);
        assertEquals(coordinates1wgs84.coordY, wgs84.coordY, 0.0001);
    }


}
