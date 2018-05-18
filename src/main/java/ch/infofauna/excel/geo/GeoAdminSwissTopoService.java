package ch.infofauna.excel.geo;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.apache.http.Header;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Service
public class GeoAdminSwissTopoService implements GeodataService {
    @Autowired
    private ObjectMapper mapper;
    /*
    externaluri.geoadminAltitudesURI=https://api3.geo.admin.ch/rest/services/height
    externaluri.geoadminIdentifyURI=https://api3.geo.admin.ch/rest/services/api/MapServer/identify
    externaluri.lv03toWgs84URI=http://tc-geodesy.bgdi.admin.ch/reframe/lv03towgs84
    externaluri.wgs84toLv03URI=http://tc-geodesy.bgdi.admin.ch/reframe/wgs84tolv03
     */

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Value("${externaluri.geoadminIdentifyURI}")
    private String identifyServiceUriStr;

    @Value("${externaluri.geoadminAltitudesURI}")
    private String altitudeServiceUriStr;

    @Value("${externaluri.lv03toWgs84URI}")
    private String lv03toWgs84URIStr;

    @Value("${externaluri.wgs84toLv03URI}")
    private String wgs84toLv03URIStr;

    private URI identifyServiceUri, altitudeServiceUri, lv03toWgs84URI, wgs84toLv03URI;

    private final String referer = "https://webfauna.cscf.ch/";

    private CloseableHttpClient httpClient;
    public static final int HTTP_TIMEOUT = 5;

    @PostConstruct
    public void init() {

        logger.info("Initializing Geo Admin and Swiss Topo Geographic Data Service. Building web services URIs.");

        acceptAllCertificates();

        try {
            identifyServiceUri = new URI(identifyServiceUriStr);
            logger.info("Valid URI for the geoadmin identify service : {}", identifyServiceUri.toString() );
        }catch(URISyntaxException e){
            throw new RuntimeException("The URI for the GeoAdmin altitude service is invalid :"+altitudeServiceUriStr+". Check the configuration in Tomcat's context.xml.");
        }

        try {
            altitudeServiceUri = new URI(altitudeServiceUriStr);
            logger.info("Valid URI for the geoadmin altitude service : {}", altitudeServiceUri.toString() );
        }catch(URISyntaxException e){
            throw new RuntimeException("The URI for the GeoAdmin altitude service is invalid :"+altitudeServiceUriStr+". Check the configuration in Tomcat's context.xml.");
        }

        try {
            lv03toWgs84URI = new URI(lv03toWgs84URIStr);
            logger.info("URI for the  identify service : {}", identifyServiceUri.toString() );
        }catch(URISyntaxException e){
            throw new RuntimeException("The URI for the Swisstopo coordinate conversion service is invalid :"+lv03toWgs84URIStr+". Check the configuration in Tomcat's context.xml.");
        }

        try {
            wgs84toLv03URI = new URI(wgs84toLv03URIStr);
        }catch(URISyntaxException e){
            throw new RuntimeException("The URI for the Swisstopo coordinate conversion service is invalid :"+wgs84toLv03URIStr+". Check the configuration in Tomcat's context.xml.");
        }
    }

    private void acceptAllCertificates() {
        TrustStrategy acceptingTrustStrategy = new TrustStrategy() {
            @Override
            public boolean isTrusted(X509Certificate[] certificate, String authType) {
                return true;
            }
        };

        try {
            SSLContextBuilder builder = new SSLContextBuilder();
            builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                    builder.build());
            CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(
                    sslsf).build();


        }catch(Exception e){
            throw new RuntimeException(e);
        }

    }

    @Override
    public Location getAdministrativeLocation(Coordinates coordinates) {

        logger.info("Now trying to get location data from the geoadmin identify web service...");

        Map<String,String> parameters = new HashMap<String,String>();
        parameters.put("geometryType", "esriGeometryPoint");
        parameters.put("geometry", coordinates.coordX+","+coordinates.coordY);
        parameters.put("mapExtent", "0,0,0,0");
        parameters.put("tolerance", Integer.toString(0));
        parameters.put("layers", "all:ch.swisstopo.swissboundaries3d-kanton-flaeche.fill,ch.swisstopo.swissboundaries3d-gemeinde-flaeche.fill,ch.swisstopo-vd.ortschaftenverzeichnis_plz");
        parameters.put("imageDisplay", "0,0,0");
        parameters.put("returnGeometry", "false");

        JsonNode root = getJson(identifyServiceUri, parameters);

        final ArrayNode results;
        final Location location = new Location(coordinates.coordX, coordinates.coordY);

        try {
            results = (ArrayNode) root.get("results");
        }catch(ClassCastException e){
            throw new RuntimeException("Could not extract node \"results\" from JSON answer.");
        }

        if (results.size() == 3) {
            try {
                location.setCountryCode("SZ");

                for (Iterator<JsonNode> i = results.elements(); i.hasNext(); ) {
                    JsonNode node = i.next();
                    switch (node.get("layerBodId").textValue()) {

                        case ("ch.swisstopo.swissboundaries3d-kanton-flaeche.fill"):
                            location.setDepartmentCode("SZ-"+node.get("attributes").get("ak").textValue().trim());
                            break;

                        case ("ch.swisstopo.swissboundaries3d-gemeinde-flaeche.fill"):
                            location.setLocalite(node.get("attributes").get("gemname").textValue().trim());
                            break;

                        case ("ch.swisstopo-vd.ortschaftenverzeichnis_plz"):
                            //not using it for now
                            break;
                        default:
                            logger.warn("Unexpected layer ID found in response from geoadmin identify service : " + node.get("layerBodId").textValue());
                    }
                }
                logger.info("Received location data : {}",location.toString());
                return location;
            } catch (NullPointerException e) {
                throw new RuntimeException("The location requested didn't return the expected JSON structure.", e);
            }
        }else {
            logger.info("The location requested didn't return the expected result elements. This likely means that the coordinate point was outside of Switzerland.");
            return location;
        }
    }

    @Override
    public double getAltitudeAt(Coordinates coordinates){

        logger.info("Now trying to get altitude data from the geoadmin web service...");

        Map<String,String> parameters = new HashMap<String,String>();
        parameters.put("easting", Double.toString(coordinates.coordX));
        parameters.put("northing", Double.toString(coordinates.coordY));

        JsonNode rootNode = getJson(altitudeServiceUri, parameters);

        JsonNode heightNode = rootNode.get("height");
        if(heightNode == null){
            throw new RuntimeException("Unexpected JSON format for altitude request. Property \"height\" not found. JSON :"+rootNode.asText());
        }

        try {
            double altitude = Double.parseDouble(heightNode.textValue());
            logger.info("Found altitude : {}", altitude);
            return altitude;
        }catch(NumberFormatException e){
            throw new RuntimeException("Invalid number found in JSON for altitude : "+heightNode.textValue());
        }
    }

    @Override
    public Coordinates wgs84toLv03(Coordinates wgs84Coordinates) {

        logger.info("Now trying to convert WGS 84 cooordinates to LV03 coordinates using the Swisstopo web service. Coordinates : {}", wgs84Coordinates.toString());

        Map<String,String> parameters = new HashMap<String,String>();
        parameters.put("easting", Double.toString(wgs84Coordinates.coordX));
        parameters.put("northing", Double.toString(wgs84Coordinates.coordY));
        parameters.put("format", "json");

        JsonNode root = getJson(wgs84toLv03URI, parameters);

        try{
            Coordinates lv03Coordinates = new Coordinates(
                    Double.parseDouble(root.get("easting").textValue()),
                    Double.parseDouble(root.get("northing").textValue())
            );
            logger.info("LV03 coordinates : {}", lv03Coordinates.toString());
            return lv03Coordinates;
        }catch(Exception e){
            throw new RuntimeException("Failed to parse JSON response from coordinates conversion service at Swisstopo.");
        }

    }

    @Override
    public Coordinates lv03toWgs84(Coordinates lv03Coordinates) {

        logger.info("Now trying to convert LV03 cooordinates to WGS 84 coordinates using the Swisstopo web service. Coordinates : {}", lv03Coordinates.toString());

        Map<String,String> parameters = new HashMap<String,String>();
        parameters.put("easting", Double.toString(lv03Coordinates.coordX));
        parameters.put("northing", Double.toString(lv03Coordinates.coordY));
        parameters.put("format", "json");

        JsonNode root = getJson(lv03toWgs84URI, parameters);

        try{
            Coordinates wgs84Coordinates = new Coordinates(
                    Double.parseDouble(root.get("easting").textValue()),
                    Double.parseDouble(root.get("northing").textValue())
            );
            logger.info("WGS 84 coordinates : {}", wgs84Coordinates.toString());
            return wgs84Coordinates;

        }catch(Exception e){
            throw new RuntimeException("Failed to parse JSON response from coordinates conversion service at Swisstopo.");
        }
    }

    @Override
    public Coordinates wgs84toLv03Approx(Coordinates coordinates) {
        double [] conv = ApproxSwissProjection.WGS84toLV03(coordinates.coordY, coordinates.coordX, 0.0);
        return new Coordinates(conv[0], conv[1]);
    }

    @Override
    public Coordinates lv03toWgs84Approx(Coordinates coordinates) {
        double [] conv = ApproxSwissProjection.LV03toWGS84(coordinates.coordX, coordinates.coordY, 0.0);
        return new Coordinates(conv[1], conv[0]);
    }

    /**
     * Lazily builds and returns the HTTP client instance
     * @return
     */
    private CloseableHttpClient getClient() {

        if(httpClient == null) {

            logger.info("Building HTTP client...");

            RequestConfig config = RequestConfig.custom()
                    .setConnectTimeout(HTTP_TIMEOUT * 1000)
                    .setConnectionRequestTimeout(HTTP_TIMEOUT * 1000)
                    .setSocketTimeout(HTTP_TIMEOUT * 1000).build();

            // After problem with certificates by StartCom that are not included in Java trusted certificates, we just accept eny certificate
            // So this strategy allows to accept any certificate.
            TrustStrategy acceptingTrustStrategy = new TrustStrategy() {
                @Override
                public boolean isTrusted(X509Certificate[] certificate, String authType) {
                    return true;
                }
            };

            try {
                SSLContextBuilder builder = new SSLContextBuilder();
                builder.loadTrustMaterial(null, acceptingTrustStrategy);
                SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(builder.build());

                httpClient = HttpClients.custom().setSSLSocketFactory(sslsf).setDefaultRequestConfig(config).build();
            }catch(NoSuchAlgorithmException | KeyStoreException | KeyManagementException e){
                throw new RuntimeException("SSL connection problem : "+e.getMessage(), e);
            }

            logger.info("HTTP client built successfully.");
        }

        return httpClient;
    }

    private JsonNode getJson(URI baseUri, Map<String, String> parameters){

        logger.info("Building HTTP GET request to URI {}", baseUri);

        final URI uri;
        final CloseableHttpResponse response;
        final String json;

        try {
            URIBuilder builder = new URIBuilder(baseUri);

            for(Map.Entry<String,String> entry : parameters.entrySet()){
                builder.addParameter(entry.getKey(), entry.getValue());
            }

            uri = builder.build();

            logger.info("Final URI with parameters : {}", uri);

        }catch(URISyntaxException use) {
            throw new RuntimeException("Failed to build the URI for the geo admin service :" + baseUri, use);
        }

        HttpGet request = new HttpGet(uri);
        request.addHeader("Referer", referer);

        try {
            logger.info("Now sending the HTTP request to {}", uri);
            response = getClient().execute(request);
        }catch(IOException e){
            throw new RuntimeException("The request to the geo admin API failed : " + uri, e);
        }

        try{
            logger.debug("HTTP request executed, now processing the response.");

            if(response.getStatusLine().getStatusCode()!= 200){
                throw new RuntimeException("The request to the geo admin API returned an error code : "+ uri + response.getStatusLine().toString());
            }

            logger.debug("The request has a 200 OK status code.");

            Header contentType = response.getEntity().getContentType();

            if(contentType == null){
                logger.warn("The response content-type of a request to {} is not specified. Will attempt to process it anyway.", uri);
            } else if(contentType.getValue().indexOf("json") == -1){
                logger.warn("The response content-type of a request to {} seems not to be JSON. Will attempt to process it anyway. Content-type : {}", uri, contentType.getValue());
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();

            logger.debug("Now reading the response entity.");
            response.getEntity().writeTo(out);

            json = out.toString();

            logger.debug("Response body :"+json);
            logger.info("Request to {} successful.", uri);

        }catch(IOException e){
            throw new RuntimeException("Problem reading the response for the geo admin API request to URI "+uri, e);
        }
        finally{
            try{
                response.close();
            }catch(Exception e){
                logger.warn("Failed to close response for URI "+uri);
            }
        }

        try {
            return mapper.readTree(json);
        }catch(IOException e){
            throw new RuntimeException(MessageFormat.format("Failed to parse the JSON received from {0}. The string received was : {1}.", uri.toString(), json), e);
        }
    }
}

