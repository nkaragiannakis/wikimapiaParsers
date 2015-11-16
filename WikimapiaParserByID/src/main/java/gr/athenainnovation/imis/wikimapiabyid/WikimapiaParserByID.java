
package gr.athenainnovation.imis.wikimapiabyid;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Extracts unique IDs of wikimapia places and constructs RDF triples. 
 * 
 * These triples are based on info provided by the getID method of wikimapia API.
 * The getID method provides more detailed data for each place, contrary to the getByArea method. 
 * This approach is much more time consuming (due to requests per ID + requests restriction)
 * 
 * Check http://wikimapia.org/api for more details.
 * 
 * @author imis-nkaragiannakis
 */

public class WikimapiaParserByID {
    
    private static final String PLACE_LINE = new String(new char[100]).replace("\0", "#");
    private static final String SUBJECT_PREFIX_PLACE = "<http://geoknow.eu/geodata#place_";
    private static final String SUBJECT_PREFIX = "<http://geoknow.eu/geodata#";
    private static final String PREDICATE_PREFIX = "<http://geoknow.eu/geodata#has_";
    private static final String OBJECT_PREFIX = "<http://geoknow.eu/geodata#";
    private static final String WGS84_PREFIX = "<http://www.w3.org/2003/01/geo/wgs84_pos#"; 
    //private static final String SPATIAL_REL = "<http://data.ordnancesurvey.co.uk/ontology/spatialrelations";
    //private static final String GML = "http://www.opengis.net/gml"; //for gml envelope
    private static final String OWS_BBOX_TYPE = "<http://www.opengis.net/ows#BoundingBoxType>";
    private static final String WGS84_BBOX = "<http://www.opengis.net/ows#WGS84BoundingBox";
    private static final String NORTH = "<http://www.opengis.net/ows#northbc>";
    private static final String EAST = "<http://www.opengis.net/ows#eastbc>";
    private static final String SOUTH = "<http://www.opengis.net/ows#southbc>";
    private static final String WEST = "<http://www.opengis.net/ows#westbc>";   
    private static final String RDF = "<http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    private static final String OWL_CLASS = "<http://www.w3.org/2002/07/owl#Class>";
    private static final String CLASS_LABEL = "<http://www.w3.org/2000/01/rdf-schema#label>";
    
    private static final String XSD_DOUBLE = "^^<http://www.w3.org/2001/XMLSchema#double>";
    //private static final String XSD_FLOAT = "^^<http://www.w3.org/2001/XMLSchema#float>";
    private static final String XSD_INT = "^^<http://www.w3.org/2001/XMLSchema#integer>";                                           
    private static final String XSD_DECIMAL = "^^<http://www.w3.org/2001/XMLSchema#decimal>";        
    
    //using virtrdf#Geometry instead of geosparql#wktLiteral to be compatible with virtuoso.
    private static final String virtDatatype = "^^<http://www.openlinksw.com/schemas/virtrdf#Geometry>";
    
    
    private static final String virtHasGeometry = "<http://geovocab.org/geometry#geometry>";
    private static final String asWKT = "<http://www.opengis.net/ont/geosparql#asWKT>";
    //private static final boolean polygon_dataset = true; //choose dataset mode
    //private static String datasetName = "test";    
    
    //<entity> http://www.opengis.net/ows#WGS84BoundingBoxType <WGS84BoundingBox>
    //<WGS84BoundingBox> <WGS84BoundingBox#northbc> "north value" 
    //<WGS84BoundingBox> <WGS84BoundingBox#eastbc> "east value"
    //<WGS84BoundingBox> <WGS84BoundingBox#southbc> "south value
    //<WGS84BoundingBox> <WGS84BoundingBox#westbc> "west value"
    
    private static int depth = 0;
    private static int totalPages = 1;
    private static int page = 1;
    private static boolean newPlace = false;
    private static int countEntities = 0;
    private static String numOfEntities;
    private static int allEntities=0;
    private static HashSet<String> classSet = new HashSet<>();
    private static final List<BBoxNode> leafBoxes = new ArrayList<>();
    private static HashSet<String> placeIDs = new HashSet<>();
  
    //west london
//    private static final String datasetName = "west_london";
//    private static final double minlat = 50.83682;
//    private static final double minlon = -1.516113;
//    private static final double maxlat = 52.052153;
//    private static final double maxlon = -0.189514;
    
//    private static final String datasetName = "athens";
//    private static final double minlat = 38.011339;
//    private static final double minlon = 23.741627;
//    private static final double maxlat = 38.069474;
//    private static final double maxlon = 23.834667;
 
//    private static final String datasetName = "testath";
//    private static final double minlat = 38.011339;
//    private static final double minlon = 23.741627;
//    private static final double maxlat = 38.069474;
//    private static final double maxlon = 23.764667;        
    
    //all europe
//    private static final String datasetName = "europe";
//    private static final double minlat = 34.914766 ;
//    private static final double minlon = -5.449219;
//    private static final double maxlat = 71.255657;
//    private static final double maxlon = 34.277344; 
       
//    private static final String datasetName = "west_europe";
//    private static final double minlat = 35.202542;
//    private static final double minlon = -24.257812;
//    private static final double maxlat = 67.217651;
//    private static final double maxlon = 3.339844;  
    
//    private static final String datasetName = "mid_europe";
//    private static final double minlat = 37.188331;
//    private static final double minlon = 3.867188;
//    private static final double maxlat = 71.591638;
//    private static final double maxlon = 21.445313;     
    
    //BERLIN
    //13.17901773138858 52.385780263039265,13.6322037664818 52.385780263039265,13.6322037664818 52.609840562729126,13.17901773138858 52.609840562729126,13.17901773138858 52.385780263039265
   //13.6322037664818 52.609840562729126
    private static final String datasetName = "berlin";
    private static final double minlat = 52.385780;
    private static final double minlon = 13.179017;
    private static final double maxlat = 52.609840;
    private static final double maxlon = 13.632203;     
    
//    private static final String datasetName = "central_london";
//    private static final double minlat = 51.291467;
//    private static final double minlon = -0.464172;
//    private static final double maxlat = 51.676302;
//    private static final double maxlon = 0.178528;    
   
    private static boolean interrupted = false;
    private static EntityTriples entityTripleList;
    private static int totalRequests = 0;
    
    
    /*
        NUMBER_OF_PLACES_RESTRICTION
        Max 10000. 
        Smaller numbers are slower, but maximize the chance of getting more IDs by dividing the boundingBoxes further.
        Small numbers (smaller bounding boxes produced) may fail to return large geometries.
    */
    
    private static final int THREAD_SLEEP = 300000; //interval to wait for wikimapia api request restriction
    private static final int THREAD_SLEEP_MAX = 370000; 
    
    private static final int NUMBER_OF_PLACES_RESTRICTION = 5000; 
    
    //Each wikimapia account gets a unique alphanumeric key of the form: 
    //********-********-********-********-********-********-********-********
    //put yours to wikimapiaKey string variable
    
    private static final String wikimapiaKey = "********-********-********-********-********-********-********-********";
    
    //define the path of the produced files.
    private static final String rdfTriplesDestinationPath = "/home/user/path/to/folder/";
    private static final String wikimapiaIDsDestinationPath = "/home/user/path/to/folder/";
    
    public static void main(String[] args) throws IOException, InterruptedException {
         
        Charset charset = StandardCharsets.UTF_8;       
        //wikiRDF = "/home/imis-nkarag/software/wikimapiaLeecher/" + datasetName + "polygons_test.nt";            
        Path output = Paths.get(rdfTriplesDestinationPath + datasetName + ".nt");     
        Path outputIDs = Paths.get(wikimapiaIDsDestinationPath + datasetName + "IDs.txt");        
        
        //bf = new BufferedWriter(new FileWriter(wikiRDF));     
        BBoxNode bboxNode = new BBoxNode(minlon, minlat, maxlon, maxlat);
        int rootPlaces = bboxPlaces(bboxNode);
        
        //if(rootPlaces > NUMBER_OF_PLACES_RESTRICTION){            
            recursiveTetrachotomize(minlon, minlat, maxlon, maxlat);
        //}

        System.out.println("produced bboxes: " + leafBoxes.size());
        int u = 1;
        int placesFromAllBboxes = 0;
        for(BBoxNode leafBox : leafBoxes){
            System.out.println(u +" bbox places: " + leafBox.getNumberOfPlaces());
            placesFromAllBboxes += leafBox.getNumberOfPlaces();
            System.out.println("bbox coordinates: " + leafBox.getMinLon() + " "+ leafBox.getMinLat() 
                    + " " + leafBox.getMaxLon() + " "+ leafBox.getMaxLat());
            
            u++;
        }
        
        System.out.println("unique ids: " + placeIDs.size());        
        Files.write(outputIDs, placeIDs, Charset.defaultCharset());               
        System.out.println("initial bbox places: " + rootPlaces + "\n places after division: " + placesFromAllBboxes);
        
        try {
            System.out.println("waiting..");
            Thread.sleep(THREAD_SLEEP); 
        } catch (InterruptedException ex1) {
            Logger.getLogger(WikimapiaParserByID.class.getName()).log(Level.SEVERE, null, ex1);
        }
        
        int limit = 1;
        long startTimer = System.nanoTime();
        long endTimer;
        long miliSecondsToWait;
        
        for(String id : placeIDs){
            entityTripleList = new EntityTriples(); 
            if( limit%100 == 0){

                endTimer = System.nanoTime();
                miliSecondsToWait = THREAD_SLEEP_MAX - TimeUnit.NANOSECONDS.toMillis(endTimer-startTimer);
                if(miliSecondsToWait>0){
                    System.out.println("requests till now: " + limit);
                    System.out.println("time passed: " + TimeUnit.NANOSECONDS.toMillis(endTimer-startTimer) + " seconds");
                    System.out.print("Waiting ("+ TimeUnit.MILLISECONDS.toSeconds(miliSecondsToWait)+" secs) to avoid wikimapia limit reach..");                   
                    try {
                        Thread.sleep(miliSecondsToWait);
                    } catch (InterruptedException ex1) {
                        Logger.getLogger(WikimapiaParserByID.class.getName()).log(Level.SEVERE, null, ex1);
                    }
                    System.out.print(".done!\n");
                }
                startTimer = System.nanoTime();
            }
            
            parseWikimapiaByID(id);
            produceGeometry(id);
            System.out.println("tags size: " + entityTripleList.getTags().size());
            Files.write(output, entityTripleList.getTags(), charset, StandardOpenOption.CREATE,
                                                StandardOpenOption.APPEND);
            limit++;
        }
        
        //bf.close();
    }

    private static void parseWikimapiaByID(String placeID)  {
            
            try {
                String url2;               
                //if(polygon_dataset){
                    url2 = "http://api.wikimapia.org/"
                        + "?key=" + wikimapiaKey
                        + "&function=place.getbyid"
                        + "&&id=" + placeID    
                            
                        //configure the data to be returned from wikimapia. Check http://wikimapia.org/api#placegetbyid
                        + "&data_blocks=main,edit,location,translate,nearest_streets,geometry"; 
                        //+ "&data_blocks=main,similar_places,nearest_hotels,geometry"; 
                //}
                System.out.println("requesting..\n"+url2);
                URL url = new URL(url2);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Accept", "application/xml");

                InputStream xml = connection.getInputStream();

                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                Document doc = db.parse(xml); 
                
                //Node firstChild = doc.getFirstChild();
                //NodeList nodeList = firstChild.getChildNodes();
                //Node firstChild = doc.getFirstChild();
                //System.out.println("find page count" + doc.getElementsByTagName("found").item(0).getTextContent());
                //exec = true;
                NodeList childsOfPlace = doc.getChildNodes().item(0).getChildNodes();    
                //System.out.println("processing page: " + page + "...");
                //EntityTriples entityTriples = new EntityTriples();                
                iterateChilds(childsOfPlace, placeID); 
                //System.out.println("tags size: " + entityTriples2.getTags().size());
//                for(String entityTag : entityTriples2.getTags()){    
//                
//                    System.out.println(entityTag);
//                    //bf.write(la);
//                    //bf.newLine();
//                }
            
        } catch (ParserConfigurationException | SAXException ex) {
            System.out.println("ParserConfigurationException or SAXException" );
            Logger.getLogger(WikimapiaParserByID.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MalformedURLException ex) {
            System.out.println("MalformedURLException" );
            Logger.getLogger(WikimapiaParserByID.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            System.out.println("IOException" );
            Logger.getLogger(WikimapiaParserByID.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("recovering...");                    
            //System.out.println("retrying.. with page: " + page);
            recover(placeID);  
        }  
    }    
       
    private static void produceGeometry(String placeID){
        List<String> ge;
        String fullGeom;
        fullGeom="POLYGON((";
        ge = entityTripleList.getCoordinates();
        for(int g = 0; g<ge.size()-1; g += 2){
            String x = ge.get(g);
            String y = ge.get(g+1);
            fullGeom = fullGeom + x + " "+ y + ", ";
        }

        if(ge.size()>1){

            fullGeom = fullGeom + ge.get(0) + " " + ge.get(1) + "))"; //add first point to close polygon 
                                                            //will delete the ", " addition with substring below
 
            entityTripleList.getTags().add(SUBJECT_PREFIX_PLACE + placeID + "> " + virtHasGeometry + " " 
                    + OBJECT_PREFIX + "geom_" + placeID + "> . ");
            entityTripleList.getTags().add(OBJECT_PREFIX + "geom_" + placeID + "> " + asWKT + " "
                    + "\"" + fullGeom + "\"" + virtDatatype + " . ");
        }            
    }     
    
    private static void iterateChilds(NodeList nodeList, String placeID) throws IOException{        
        //int depth; 
        Node node; 
        //System.out.println("test");
        for(int i=0; i < nodeList.getLength(); i++){     
            //System.out.println("iter i " + i);
            node = nodeList.item(i);
            
            depth = 0;               
            //entityTriples2 = new EntityTriples();
            entityTripleList.setEntityID(placeID);
            //countEntities++;
            //System.out.println("nana\n" + node.getChildNodes().getLength());
            //System.out.println("nana2\n" + node.getChildNodes().item(0).getNodeName());
            //System.out.println("nana3\n" + node.getNodeName());
            
            if(node.getNodeName().equals("id")){
                //System.out.println("success");
            }
            else if(node.getNodeName().equals("object_type")){
                //System.out.println("suc2");
            }
            else if(node.getNodeName().equals("tags")){

                if(node.hasChildNodes()){
                    NodeList tagChilds = node.getChildNodes();
                    for(int h = 0; h < tagChilds.getLength(); h++){
                        Node child = tagChilds.item(h);                      
                        
                        String label = child.getLastChild().getTextContent();
                        String className = label.replaceAll("\\s", "_");
                        
                        String triple1 = SUBJECT_PREFIX_PLACE + entityTripleList.getEntityID() + "> " 
                                + RDF + "type> " + SUBJECT_PREFIX + className + "> . "; 
                        
                        entityTripleList.getTags().add(triple1);
                        if(!classSet.contains(className)){
                            String triple2 = SUBJECT_PREFIX + className + "> " 
                                + RDF + "type> " + OWL_CLASS + " . "; 
                            
                            String labelTriple = SUBJECT_PREFIX + className + "> " + CLASS_LABEL + " \"" + label + "\" . ";
                            entityTripleList.getTags().add(labelTriple);
                            classSet.add(className);
                            
                            entityTripleList.getTags().add(triple2);
                        }       
                    }
                }               
            }
            else if(node.getNodeName().equals("language_id")){
                
                if(node.hasChildNodes()){
                    entityTripleList.getTags().add(SUBJECT_PREFIX_PLACE+entityTripleList.getEntityID() + "> " 
                            + PREDICATE_PREFIX+"language_id>" + " " + "\"" + node.getFirstChild().getTextContent() + "\"" + XSD_INT + " . ");
                }
            }
            else if(node.getNodeName().equals("language_iso")){
                if(node.hasChildNodes()){
                    entityTripleList.getTags().add(SUBJECT_PREFIX_PLACE+entityTripleList.getEntityID() + "> " 
                            + PREDICATE_PREFIX + "language_iso>" + " " + "\"" + node.getFirstChild().getTextContent() + "\" . ");
                }    
            }
            else if(node.getNodeName().equals("language_name")){
                    entityTripleList.getTags().add(SUBJECT_PREFIX_PLACE+entityTripleList.getEntityID() + "> " 
                            + PREDICATE_PREFIX + "language_name>" + " " + "\"" + node.getFirstChild().getTextContent() + "\" . ");
            }
            else if(node.getNodeName().equals("wikipedia")){
                if(node.hasChildNodes()){
                    String ob = node.getFirstChild().getTextContent();
                    deleteQuotes(ob);
                    entityTripleList.getTags().add(SUBJECT_PREFIX_PLACE+entityTripleList.getEntityID() + "> " 
                            + PREDICATE_PREFIX + "wikipedia_link>" + " " + "\"" + ob + "\" . ");
                }
            }
            else if(node.getNodeName().equals("urlhtml")){
                if(node.hasChildNodes()){
                    //text = text.replace(System.getProperty("line.separator"), "");
                    //[\\t\\n\\r]
                    //str = str.replaceAll("\\r\\n|\\r|\\n", " ");
                    
                    String ob = deleteQuotes(node.getFirstChild().getTextContent());
                    ob = ob.substring(ob.indexOf("http"), ob.length());
                    ob = ob.substring(0, ob.indexOf(">"));
                    entityTripleList.getTags().add(SUBJECT_PREFIX_PLACE+entityTripleList.getEntityID() + "> " 
                                                    + PREDICATE_PREFIX + "urlhtml>" + " " + "\"" + ob + "\" . ");
                }                
            }            
            else if(node.getNodeName().equals("title") && !node.getParentNode().getNodeName().contains("tags")){
                if(node.hasChildNodes()){
                    //entityTriples.getTags().add("parent node " +node.getParentNode().getNodeName()); 
                    
                    //remove quotes from object, not escape. virtuoso deletes em 
                    String ob = deleteQuotes(node.getFirstChild().getTextContent());
                    entityTripleList.getTags().add(SUBJECT_PREFIX_PLACE + entityTripleList.getEntityID() + "> " 
                                                        + PREDICATE_PREFIX+"title>" + " " + "\"" + ob + "\" . ");
                }                
            }
            else if(node.getNodeName().equals("description")){
                if(node.hasChildNodes()){
                    String ob = deleteQuotes(node.getFirstChild().getTextContent());
                    ob = ob.replaceAll("\\r\\n|\\r|\\n", " ");
                    ob = ob.replaceAll("\\\\", "/");
                    //entityTriples.getTags().add("parent node " +node.getParentNode().getNodeName()); 
                    entityTripleList.getTags().add(SUBJECT_PREFIX_PLACE + entityTripleList.getEntityID() + "> " 
                            + PREDICATE_PREFIX + "description>" + " " + "\"" + ob + "\" . ");
                }                
            }
            else if(node.getNodeName().equals("is_building")){
                if(node.hasChildNodes()){
                    //System.out.println("found is building");
                    entityTripleList.getTags().add(SUBJECT_PREFIX_PLACE+entityTripleList.getEntityID() + "> " 
                            + RDF + "type> " + OBJECT_PREFIX + "building> . ");
                    if(!classSet.contains("building")){
                        classSet.add("building");
                        entityTripleList.getTags().add(OBJECT_PREFIX + "building> "  
                            + RDF + "type> " + OWL_CLASS + " . ");
                    }
                }
            }
            else if(node.getNodeName().equals("is_region")){
                if(node.hasChildNodes()){
                    //System.out.println("found is region");
                    entityTripleList.getTags().add(SUBJECT_PREFIX_PLACE+entityTripleList.getEntityID() + "> " 
                            + RDF +"type> " +  OBJECT_PREFIX + "region> . ");
                    if(!classSet.contains("region")){
                        classSet.add("region");
                        entityTripleList.getTags().add(OBJECT_PREFIX + "region> "  
                            + RDF + "type> " + OWL_CLASS + " . ");
                    }                   
                }
            }
            else if(node.getNodeName().equals("parent_id")){
                if(node.hasChildNodes()){
                    //System.out.println("found parent_id");
                    entityTripleList.getTags().add(SUBJECT_PREFIX_PLACE+entityTripleList.getEntityID() + "> " 
                            + PREDICATE_PREFIX + "parent_id>" + " \"" + node.getFirstChild().getTextContent() + "\"" + XSD_INT + " . ");
                }
            }  
            else if(node.getNodeName().equals("is_deleted")){
                if(node.hasChildNodes()){
                    //System.out.println("found parent_id");
                    entityTripleList.getTags().add(SUBJECT_PREFIX_PLACE+entityTripleList.getEntityID() + "> " 
                            + PREDICATE_PREFIX + "is_deleted> " + " \"" + node.getFirstChild().getTextContent() + "\"" + XSD_INT + " . ");
                }
            }
            else if(node.getNodeName().equals("edit_info")){
                if(node.hasChildNodes()){
                    String triple = SUBJECT_PREFIX_PLACE + entityTripleList.getEntityID() + "> " 
                            + PREDICATE_PREFIX + "type>" + " " + OBJECT_PREFIX + "edit_info_" + entityTripleList.getEntityID() + "> . ";
                    entityTripleList.getTags().add(triple);
                    
                    //String tripleEdit="";
                    NodeList edits = node.getChildNodes();
                    for(int h = 0; h < edits.getLength(); h++){
                        Node child = edits.item(h);                      

                        if(child.hasChildNodes() ){//&& child.getFirstChild().hasChildNodes()                            
                            String tripleEdit = SUBJECT_PREFIX + "edit_info_" + entityTripleList.getEntityID() + ">" + " " 
                                + PREDICATE_PREFIX + child.getNodeName() + "> " + "\"" 
                                + child.getFirstChild().getTextContent() + "\" . "; 
                            entityTripleList.getTags().add(tripleEdit);
                        }                                               
                    }
                }
            }
            else if(node.getNodeName().equals("nearestHotels")){
                //System.out.println("found nearest");
                //entityTriples2.getTags().add(SUBJECT_PREFIX_PLACE+placeID + "> " + PREDICATE_PREFIX + "nearestHotels> " 
                       // + OBJECT_PREFIX + "nearestHotels_"  + "> . ");
                for(int h = 0; h < node.getChildNodes().getLength(); h++){
                    Node child = node.getChildNodes().item(h);
                    //add triple to point to hotel node in order to produce further chains
                    String thisNearHotel = child.getNodeName();
                    entityTripleList.getTags().add(SUBJECT_PREFIX_PLACE + placeID + "> " + PREDICATE_PREFIX + "nearestHotels> " 
                            + OBJECT_PREFIX + thisNearHotel + "> . ");
                    
                    for(int g = 0; g < node.getChildNodes().item(h).getChildNodes().getLength(); g++ ){
                        Node ch = node.getChildNodes().item(h).getChildNodes().item(g);
                        //System.out.println(ch.getNodeName());
                       
                        if(ch.getNodeName().equals("title")){
                            entityTripleList.getTags().add(OBJECT_PREFIX + thisNearHotel + "> "
                                + PREDICATE_PREFIX + "title> \"" + deleteQuotes(ch.getTextContent()) + "\" . "); 
                        }                        
                        else if(ch.getNodeName().equals("lat")){
                            entityTripleList.getTags().add(OBJECT_PREFIX + thisNearHotel + "> "
                                    + WGS84_PREFIX + "lat> \"" + ch.getTextContent() + "\"" + XSD_DOUBLE + " . ");                            

                        }
                        else if(ch.getNodeName().equals("lon")){
                            entityTripleList.getTags().add(OBJECT_PREFIX + thisNearHotel + "> "
                                    + WGS84_PREFIX + "long> \"" + ch.getTextContent() + "\"" + XSD_DOUBLE + " . "); 
                        }
                        else if(ch.getNodeName().equals("url")){
                            entityTripleList.getTags().add(OBJECT_PREFIX + thisNearHotel + "> "
                                    + PREDICATE_PREFIX + "url> \"" + ch.getTextContent() + "\" . ");
                        }
                        else if(ch.getNodeName().equals("minrate")){
                            if(!ch.getTextContent().equals("")){
                                entityTripleList.getTags().add(OBJECT_PREFIX + thisNearHotel + "> "
                                    + PREDICATE_PREFIX + "minrate> \"" + ch.getTextContent() + "\"" + XSD_DECIMAL + " . ");
                            }
                        }
                        else if(ch.getNodeName().equals("currencycode")){
                            entityTripleList.getTags().add(OBJECT_PREFIX + thisNearHotel + "> "
                                    + PREDICATE_PREFIX + "currencycode> \"" + ch.getTextContent() + "\" . ");
                        }
                        else if(ch.getNodeName().equals("photo_url")){
                            entityTripleList.getTags().add(OBJECT_PREFIX + thisNearHotel + "> "
                                    + PREDICATE_PREFIX + "photo_url> \"" + ch.getTextContent() + "\" . ");
                        }
                        else if(ch.getNodeName().equals("class")){
                            entityTripleList.getTags().add(OBJECT_PREFIX + thisNearHotel + "> "
                                    + PREDICATE_PREFIX + "class> \"" + ch.getTextContent() + "\"" + XSD_DECIMAL + " . ");
                        }
                        else if(ch.getNodeName().equals("name")){
                            entityTripleList.getTags().add(OBJECT_PREFIX + thisNearHotel + "> "
                                    + PREDICATE_PREFIX + "name> \"" + deleteQuotes(ch.getTextContent()) + "\" . ");
                        }      
                        else if(ch.getNodeName().equals("distance")){
                            entityTripleList.getTags().add(OBJECT_PREFIX + thisNearHotel + "_from_"+ placeID+"> " + 
                                    PREDICATE_PREFIX + "near_hotel> " + OBJECT_PREFIX + thisNearHotel + "> . ");
                            
                            entityTripleList.getTags().add(SUBJECT_PREFIX_PLACE + placeID + "> "
                                    + PREDICATE_PREFIX + "distanceFrom> " 
                                    + OBJECT_PREFIX + thisNearHotel + "_from_"+ placeID+"> . ");
                            
                            entityTripleList.getTags().add(OBJECT_PREFIX + thisNearHotel + "_from_"+ placeID+"> "
                                    + PREDICATE_PREFIX + "distance> \"" + ch.getTextContent() + "\"" + XSD_DECIMAL + " . ");
                        }   
                    }
                }                
            }
            else if(node.getNodeName().equals("similarPlaces")){
                //System.out.println("found similar places");
                for(int h = 0; h < node.getChildNodes().getLength(); h++){
                    Node child = node.getChildNodes().item(h);
                    //add triple to point to hotel node in order to produce further chains
                    String thisSimilarPlace = child.getNodeName();
                    entityTripleList.getTags().add(SUBJECT_PREFIX_PLACE + placeID + "> " + PREDICATE_PREFIX + "similarPlaces> " 
                            + OBJECT_PREFIX + thisSimilarPlace + "> . ");
                    
                    for(int g = 0; g < node.getChildNodes().item(h).getChildNodes().getLength(); g++ ){
                        Node ch = node.getChildNodes().item(h).getChildNodes().item(g);
                        //System.out.println(ch.getNodeName());
                       
                        if(ch.getNodeName().equals("title")){                            
                            entityTripleList.getTags().add(OBJECT_PREFIX + thisSimilarPlace + "> "
                                + PREDICATE_PREFIX + "title> \"" + deleteQuotes(ch.getTextContent()) + "\" . "); 
                        }                        
                        else if(ch.getNodeName().equals("lat")){
                            entityTripleList.getTags().add(OBJECT_PREFIX + thisSimilarPlace + "> "
                                    + WGS84_PREFIX + "lat> \"" + ch.getTextContent() + "\"" + XSD_DOUBLE + " . ");                            

                        }
                        else if(ch.getNodeName().equals("lon")){
                            entityTripleList.getTags().add(OBJECT_PREFIX + thisSimilarPlace + "> "
                                    + WGS84_PREFIX + "long> \"" + ch.getTextContent() + "\"" + XSD_DOUBLE + " . "); 
                        }
                        else if(ch.getNodeName().equals("url")){
                            entityTripleList.getTags().add(OBJECT_PREFIX + thisSimilarPlace + "> "
                                    + PREDICATE_PREFIX + "url> \"" + ch.getTextContent() + "\" . ");
                        }
                        else if(ch.getNodeName().equals("minrate")){
                            entityTripleList.getTags().add(OBJECT_PREFIX + thisSimilarPlace + "> "
                                    + PREDICATE_PREFIX + "minrate> \"" + ch.getTextContent() + "\"" + XSD_DECIMAL + " . ");
                        }
                        else if(ch.getNodeName().equals("currencycode")){
                            entityTripleList.getTags().add(OBJECT_PREFIX + thisSimilarPlace + "> "
                                    + PREDICATE_PREFIX + "currencycode> \"" + ch.getTextContent() + "\" . ");
                        }
                        else if(ch.getNodeName().equals("photo_url")){
                            entityTripleList.getTags().add(OBJECT_PREFIX + thisSimilarPlace + "> "
                                    + PREDICATE_PREFIX + "photo_url> \"" + ch.getTextContent() + "\" . ");
                        }
                        else if(ch.getNodeName().equals("class")){
                            entityTripleList.getTags().add(OBJECT_PREFIX + thisSimilarPlace + "> "
                                    + PREDICATE_PREFIX + "class> \"" + ch.getTextContent() + "\"" + XSD_DECIMAL + " . ");
                        }
                        else if(ch.getNodeName().equals("name")){
                            entityTripleList.getTags().add(OBJECT_PREFIX + thisSimilarPlace + "> "
                                    + PREDICATE_PREFIX + "name> \"" + deleteQuotes(ch.getTextContent()) + "\" . ");
                        }      
                        else if(ch.getNodeName().equals("distance")){
                            
                            entityTripleList.getTags().add(SUBJECT_PREFIX_PLACE + placeID + "> "
                                    + PREDICATE_PREFIX + "distanceFrom> " + OBJECT_PREFIX + thisSimilarPlace + "_from_"+ placeID+"> . ");
                            
                            entityTripleList.getTags().add(OBJECT_PREFIX + thisSimilarPlace + "_"+ placeID+"> "
                                    + PREDICATE_PREFIX + "distance> \"" + ch.getTextContent() + "\"" + XSD_DECIMAL + " . ");
                            
                            entityTripleList.getTags().add(OBJECT_PREFIX + thisSimilarPlace + "_from_"+ placeID+"> " + 
                                    PREDICATE_PREFIX + "similar> " + OBJECT_PREFIX + thisSimilarPlace + "> . ");
                        } 
                    }                    
                }                
            }
            else if(node.getNodeName().equals("location")){
                if(node.hasChildNodes()){
                    String triple = SUBJECT_PREFIX_PLACE + entityTripleList.getEntityID()+">" + " " 
                            + PREDICATE_PREFIX + "type>" + " "+ OBJECT_PREFIX + "location> . ";
                    entityTripleList.getTags().add(triple);
                    String tripleLocation;
                    NodeList location = node.getChildNodes();
                    for(int h = 0; h < location.getLength(); h++){
                        Node child = location.item(h);                      
                                
                        if(child.getNodeName().equals("gadm")){
                            //String tripleGadms1 = SUBJECT_PREFIX_PLACE+entityTriples.getEntityID()+">" + " " 
                                                        //+ PREDICATE_PREFIX + "#type" +" "+OBJECT_PREFIX + "gadms>";  
                            //entityTriples.getTags().add(tripleGadms1);
                            
                            NodeList gadms = child.getChildNodes();
                            for(int l = 0; l < gadms.getLength(); l++){
                                gadms.item(l).getNodeName();
                                String tripleGadms2 = SUBJECT_PREFIX_PLACE + entityTripleList.getEntityID() + ">" + " " 
                                        + PREDICATE_PREFIX + "gadm>" + " " 
                                        + OBJECT_PREFIX + gadms.item(l).getNodeName() + "_" 
                                        + entityTripleList.getEntityID() + "> . "; //+" " + gadms.item(l).getTextContent();
                                
                                entityTripleList.getTags().add(tripleGadms2);
                                if(gadms.item(l).hasChildNodes()){
                                    NodeList gadmChilds = gadms.item(l).getChildNodes();
                                    
                                    for(int q = 0; q < gadmChilds.getLength(); q++){
                                        
                                        if(!gadmChilds.item(q).getTextContent().equals("")){
                                            String tripleGadms3 = SUBJECT_PREFIX+gadms.item(l).getNodeName() + "_" 
                                                    + entityTripleList.getEntityID() + "> " 
                                                    + PREDICATE_PREFIX + gadmChilds.item(q).getNodeName() + "> " 
                                                    + "\"" + gadmChilds.item(q).getTextContent() + "\" . ";
                                            entityTripleList.getTags().add(tripleGadms3);
                                        }
                                    }                                  
                                }
                                //String tripleGadms3 = SUBJECT_PREFIX+gadms.item(l).getNodeName() + "#" 
                                //+ gadms.item(l).getChildNodes();                   
                            }                           
                        }
                        //if it is location and it is not gadm
                        if(child.hasChildNodes() && !child.getNodeName().equals("gadm")){   
                            if(child.getNodeName().equals("lon")){
                                tripleLocation = SUBJECT_PREFIX_PLACE+entityTripleList.getEntityID() + "> " 
                                        + WGS84_PREFIX + "long>" + " " +"\"" 
                                        + child.getFirstChild().getTextContent() + "\"" + XSD_DOUBLE + " . "; 
                                entityTripleList.getTags().add(tripleLocation);
                            }
                            else if(child.getNodeName().equals("lat")){
                                tripleLocation = SUBJECT_PREFIX_PLACE+entityTripleList.getEntityID() + "> " 
                                        + WGS84_PREFIX + "lat>" + " " + "\"" 
                                        + child.getFirstChild().getTextContent() + "\"" + XSD_DOUBLE + " . "; 
                                entityTripleList.getTags().add(tripleLocation);                                
                            }
                            else if(child.getNodeName().equals("north")){
                                String bbox = SUBJECT_PREFIX_PLACE+entityTripleList.getEntityID() + "> " 
                                        + OWS_BBOX_TYPE + " " + WGS84_BBOX + "_" + entityTripleList.getEntityID() + "> . "; 
                                entityTripleList.getTags().add(bbox);
                                tripleLocation = WGS84_BBOX + "_" + entityTripleList.getEntityID() + "> " 
                                        + NORTH + " " +"\""+child.getFirstChild().getTextContent()+"\""+ XSD_DOUBLE +" . "; 
                                entityTripleList.getTags().add(tripleLocation); 
                            }
                            else if(child.getNodeName().equals("south")){
                                tripleLocation = WGS84_BBOX + "_" + entityTripleList.getEntityID() + "> " 
                                        + SOUTH + " " +"\""+child.getFirstChild().getTextContent()+"\""+ XSD_DOUBLE +" . "; 
                                entityTripleList.getTags().add(tripleLocation);
                            }
                            else if(child.getNodeName().equals("east")){
                                tripleLocation = WGS84_BBOX + "_" + entityTripleList.getEntityID() + "> " 
                                        + EAST + " " + "\"" + child.getFirstChild().getTextContent() + "\""+ XSD_DOUBLE +" . "; 
                                entityTripleList.getTags().add(tripleLocation);
                            }
                            else if(child.getNodeName().equals("west")){
                                tripleLocation = WGS84_BBOX +"_" + entityTripleList.getEntityID() +  "> " 
                                        + WEST + " " +"\""+child.getFirstChild().getTextContent()+"\""+ XSD_DOUBLE +" . "; 
                                entityTripleList.getTags().add(tripleLocation);
                            }                            
                            else{
                            tripleLocation = SUBJECT_PREFIX_PLACE+entityTripleList.getEntityID() + "> " 
                                    + PREDICATE_PREFIX + child.getNodeName() + ">" + " " + "\"" 
                                    + child.getFirstChild().getTextContent() + "\" . "; 
                            entityTripleList.getTags().add(tripleLocation);
                            }
                        }                        
                    }
                }
            }
            else if(node.getNodeName().equals("availableLanguages")){
                if(node.hasChildNodes()){
                    NodeList languages = node.getChildNodes();
                    for(int k=0; k<languages.getLength(); k++){
                        Node lang = languages.item(k);
                        String triple = SUBJECT_PREFIX_PLACE+entityTripleList.getEntityID() + "> " 
                                + PREDICATE_PREFIX + "availableLanguages>" + " " + OBJECT_PREFIX + lang.getNodeName() + "_"+entityTripleList.getEntityID() + "> . ";
                        entityTripleList.getTags().add(triple);

                        if(lang.hasChildNodes()){
                            NodeList langs = lang.getChildNodes();
                            for(int l = 0; l< langs.getLength(); l++){
                                String tripleLang = SUBJECT_PREFIX+lang.getNodeName() + "_"+entityTripleList.getEntityID()+"> " 
                                        + PREDICATE_PREFIX+langs.item(l).getNodeName() + "> " 
                                        + "\""+ langs.item(l).getTextContent() + "\" . ";
                                entityTripleList.getTags().add(tripleLang);
                            }
                        }
                        //String triple2 = languages.item(k)
                        //String tripleLanguage;
                        //String lang = languages.item(k).getNodeName() + " " + " ";
                        //entityTriples.getTags().add(lang);
                    }
                }
            }
            //else if(node.getNodeName().equals("nearestHotels")){
               // System.out.println("found nearest");
           // }
            
            if(node.getNodeName().equalsIgnoreCase("polygon")){                
                //polygon_0, polygon_1 .. etc               
                NodeList polygons = node.getChildNodes();
                List<String> coordinates = new ArrayList<>();
                for(int j=0; j < polygons.getLength(); j++){
                    Node polygon = polygons.item(j);
                    coordinates.add(polygon.getFirstChild().getTextContent()); //x coordinate tag
                    coordinates.add(polygon.getLastChild().getTextContent()); //y coordinate tag
                }               
                //System.out.println(getTab(depth) +"Coordinates: " + coordinates + "\n");
                entityTripleList.setCoordinates(coordinates);
                entityTripleList.getObjects().add(coordinates.toString()); 
                
                //multimap geom
                entityTripleList.getSubs().put(coordinates.toString(), depth);
            }   
        }        
    } 

    private static String deleteQuotes(String string) {       
        return  string.replaceAll("\"", "");
    }
    
    private static void recursiveTetrachotomize(double minlon, double minlat, double maxlon, double maxlat){
        System.out.println("recursive dive..");
        BBoxNode downLeft = BBoxUtils.getDownLeftNode(minlon, minlat, maxlon, maxlat);
        BBoxNode upLeft = BBoxUtils.getUpLeftNode(minlon, minlat, maxlon, maxlat);
        BBoxNode upRight = BBoxUtils.getUpRightNode(minlon, minlat, maxlon, maxlat);
        BBoxNode downRight = BBoxUtils.getDownRightNode(minlon, minlat, maxlon, maxlat);
        
        if(bboxPlaces(downLeft)>NUMBER_OF_PLACES_RESTRICTION){
            recursiveTetrachotomize(downLeft.getMinLon(),downLeft.getMinLat(),downLeft.getMaxLon(),downLeft.getMaxLat());
        }
        else{
            leafBoxes.add(downLeft);
            extractPlaceIDs(downLeft);           
        }
        
        if(bboxPlaces(upLeft)>NUMBER_OF_PLACES_RESTRICTION){
            recursiveTetrachotomize(upLeft.getMinLon(),upLeft.getMinLat(),upLeft.getMaxLon(),upLeft.getMaxLat());
        }
        else{
            leafBoxes.add(upLeft);
            extractPlaceIDs(upLeft);
        }
        
        if(bboxPlaces(upRight)>NUMBER_OF_PLACES_RESTRICTION){
            recursiveTetrachotomize(upRight.getMinLon(),upRight.getMinLat(),upRight.getMaxLon(),upRight.getMaxLat());
        }
        else{
            leafBoxes.add(upRight);
            extractPlaceIDs(upRight);
        } 
        
        if(bboxPlaces(downRight)>NUMBER_OF_PLACES_RESTRICTION){
            recursiveTetrachotomize(downRight.getMinLon(),downRight.getMinLat(),downRight.getMaxLon(),downRight.getMaxLat());
        }       
        else{
            leafBoxes.add(downRight);
            extractPlaceIDs(downRight);
        }          
    }

    private static int bboxPlaces(BBoxNode bboxNode) {
        
        try {
            String box = bboxNode.getMinLon() + "," + bboxNode.getMinLat() +"," + bboxNode.getMaxLon() + ","+ bboxNode.getMaxLat();

            String url1 = "http://api.wikimapia.org/"
                    + "?key=" + wikimapiaKey
                    + "&function=place.getbyarea"
                    + "&coordsby=bbox"
                    + "&bbox="+box
                    + "&count=100";
                    //+ "&data_blocks=main,edit,location,translate,geometry";

            System.out.println("extracting number of places inside bbox " 
            + bboxNode.getMinLon() + " "+ bboxNode.getMinLat() + " "+ bboxNode.getMaxLon() + " "+ bboxNode.getMaxLat() 
                    + "\n"+ url1);                     
            
            URL urla = new URL(url1);
            HttpURLConnection connection1 = (HttpURLConnection) urla.openConnection();
            connection1.setRequestMethod("GET");
            connection1.setRequestProperty("Accept", "application/xml");
            InputStream xml1 = connection1.getInputStream();

            DocumentBuilderFactory dbf1 = DocumentBuilderFactory.newInstance();
            DocumentBuilder db1 = dbf1.newDocumentBuilder();
            Document doc1 = db1.parse(xml1);
            numOfEntities = doc1.getElementsByTagName("found").item(0).getTextContent();
            //Document doc2 = db1.parse(xml1);
            int numberOfPlaces = Integer.parseInt(numOfEntities);
            int pagesCount = numberOfPlaces/100 + 1;
            bboxNode.setNumberOfPages(pagesCount);
            bboxNode.setNumberOfPlaces(numberOfPlaces);
            System.out.println("number of places: " + numOfEntities);
            //System.out.println("number of pages to parse: " + pagesCount);
            totalRequests++;
            
            if(totalRequests%99 == 0){
                try {
                    System.out.println("waiting..");
                    Thread.sleep(THREAD_SLEEP);
                } catch (InterruptedException ex1) {
                    Logger.getLogger(WikimapiaParserByID.class.getName()).log(Level.SEVERE, null, ex1);
                }
            }            
        }
        catch (MalformedURLException ex){
            Logger.getLogger(WikimapiaParserByID.class.getName()).log(Level.SEVERE, null, ex); 
        } 
        catch (IOException ex) {
            Logger.getLogger(WikimapiaParserByID.class.getName()).log(Level.SEVERE, null, ex);
        } 
        catch (ParserConfigurationException | SAXException ex) {
            Logger.getLogger(WikimapiaParserByID.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (NullPointerException ex) {
            System.out.println("null from wiki");
            //bboxPlaces(bboxNode);
        }        
        
        return Integer.parseInt(numOfEntities);
    }

    private static void extractPlaceIDs(BBoxNode bbox) {
        //check if bbox has more than 0 places first
        String box = bbox.getMinLon() + "," + bbox.getMinLat() +"," + bbox.getMaxLon() + ","+ bbox.getMaxLat();
        
        int pagesCount;
        if(interrupted){
            pagesCount = bbox.getBookmarkPage();
            interrupted = false;
        }
        else{
            pagesCount = bbox.getNumberOfPages();
        }
        while(pagesCount >= 1){
            
            String pagesURL = "";                   
            try {
                
                pagesURL = "http://api.wikimapia.org/"
                        + "?key=" + wikimapiaKey
                        + "&function=place.getbyarea"
                        + "&coordsby=bbox"
                        + "&bbox=" + box
                        + "&count=100"
                        + "&page=" + pagesCount;
                
                System.out.println("pages request\n" + pagesURL);
                
                URL urlPages = new URL(pagesURL);
                HttpURLConnection connection3 = (HttpURLConnection) urlPages.openConnection();
                connection3.setRequestMethod("GET");
                connection3.setRequestProperty("Accept", "application/xml");
                InputStream xmlPages = connection3.getInputStream();
                
                DocumentBuilderFactory dbf3 = DocumentBuilderFactory.newInstance();
                DocumentBuilder db3 = dbf3.newDocumentBuilder();
                Document pagesDoc = db3.parse(xmlPages);
                //String pageEntitiesString = pagesDoc.getElementsByTagName("places").item(0).getTextContent();
                //iterate list, get ids                
                //System.out.println("pageEntities\n" + pageEntitiesString);
                
                NodeList places = pagesDoc.getElementsByTagName("places").item(0).getChildNodes();
                System.out.println("nodelist places size: " + places.getLength());
                Node node;
                
                for(int i=0; i < places.getLength(); i++){      
                    node = places.item(i);
                    if(node.getNodeName().startsWith("places_") && node.getParentNode().getNodeName().equalsIgnoreCase("places")){
                        String id = node.getFirstChild().getFirstChild().getTextContent();
                        placeIDs.add(id);
                        //System.out.println("id: " + id);
                    } 
                }                

                totalRequests++;
                if(totalRequests%99 == 0){
                    try {
                        System.out.println("waiting..");
                        Thread.sleep(THREAD_SLEEP);
                    } catch (InterruptedException ex1) {
                        Logger.getLogger(WikimapiaParserByID.class.getName()).log(Level.SEVERE, null, ex1);
                    }
                }                
                
                pagesCount--;
            } catch (MalformedURLException ex) {
                Logger.getLogger(WikimapiaParserByID.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(WikimapiaParserByID.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ParserConfigurationException | SAXException ex) {
                Logger.getLogger(WikimapiaParserByID.class.getName()).log(Level.SEVERE, null, ex);
            }
            catch (NullPointerException ex) {
                //null occurs from wiki api restrictions
                System.out.println("wikimapia key limit reach at page " + pagesCount + ", waiting...\n\n");
                System.out.println("URL:\n" + pagesURL);
                System.out.println("trying to recover..");
                bbox.setBookmarkPage(pagesCount);
                interrupted = true;
                try {
                    System.out.println("waiting..");
                    Thread.sleep(THREAD_SLEEP);
                } catch (InterruptedException ex1) {
                    Logger.getLogger(WikimapiaParserByID.class.getName()).log(Level.SEVERE, null, ex1);
                }
                               
                extractPlaceIDs(bbox);
                System.out.println("recovery successfull!");
                //get next bbox
            }
        }
    }
    
    private static void recover(String id){
        parseWikimapiaByID(id);
        System.out.println("\n\n Recovery succesfull!");
    }            
}

/*
////        deprecated methods     /////////

    private static void recover(String box){
        parseWikimapia(box);
        System.out.println("\n\n Recovery succesfull!");
    }

    private static void parseWikimapiaByBox(String box)  {
            
        try {            
        determineTotalPages(box);    
        while(page >= 1){ //reversing

            String url2;                
            url2 = "http://api.wikimapia.org/"
                + "?key=********-********-********-********-********-********-********-********"   //wikimapia key
                + "&function=place.getbyarea"
                + "&coordsby=bbox"
                + "&bbox="+box
                //+ "&categories_or=50,109,46533,164,44865,7,17,4,203,45716,74,163,182"
                + "&count=100"
                + "&data_blocks=main,edit,location,translate,geometry" //get all metadata in polygon dataset
                + "&page=" + page;

            URL url = new URL(url2);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/xml");
            InputStream xml = connection.getInputStream();
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(xml); 
            NodeList places = doc.getElementsByTagName("places");    
            System.out.println("processing page: " + page + "...");
            EntityTriples entityTriples = new EntityTriples();
            //iterateChilds(places, entityTriples);
            //page++;
            page--; //reversing results  
            totalRequests++;

            if(totalRequests%99 == 0){
                try {
                    Thread.sleep(THREAD_SLEEP);
                } catch (InterruptedException ex1) {
                    Logger.getLogger(WikimapiaParser.class.getName()).log(Level.SEVERE, null, ex1);
                }
            }    
        }

        System.out.println(countEntities + " must be equals to " + numOfEntities);

        } catch (ParserConfigurationException | SAXException ex) {
            System.out.println("ParserConfigurationException or SAXException" );
            Logger.getLogger(WikimapiaParser.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MalformedURLException ex) {
            System.out.println("MalformedURLException" );
            Logger.getLogger(WikimapiaParser.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            System.out.println("IOException" );
            Logger.getLogger(WikimapiaParser.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("recovering...");                    
            System.out.println("retrying.. with page: " + page);
            recover(box);  
        }  
    }

    private static void iterateChilds2(NodeList nodeList, EntityTriples entityTriples) throws IOException{
        
        //int depth; 
        Node node; 
        //System.out.println("test");
        for(int i=0; i < nodeList.getLength(); i++){            
            node = nodeList.item(i);
            //System.out.println("parent:" + node.getParentNode().getNodeName());
            if(node.getNodeName().startsWith("places_") && node.getParentNode().getNodeName().equalsIgnoreCase("places")){
                //new place found, print previous entity triples

                for(String la : entityTriples.getTags()){
                    System.out.println(la);
                    bf.write(la);
                    bf.newLine();
                }
                depth = 0;
                
                entityTriples = new EntityTriples();
                String id = node.getFirstChild().getFirstChild().getNodeValue();
                entityTriples.setEntityID(id);
                //entityTripleList.setEntityID(id);
                countEntities++;
                
                //System.out.println(getTab(depth) + PLACE_LINE + "\n\n Entity: "+ node.getNodeName() 
                //        + ", id: " + node.getFirstChild().getFirstChild().getNodeValue()+ "\n\n" + PLACE_LINE);
                
                //System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
                entityTriples.getSubjects().add(node.getFirstChild().getFirstChild().getNodeValue());
                entityTriples.getSubs().put(node.getFirstChild().getFirstChild().getNodeValue(), depth);
            } 
            else if(node.getNodeName().equalsIgnoreCase("tags")){

                if(node.hasChildNodes()){
                    NodeList tagChilds = node.getChildNodes();
                    for(int h = 0; h < tagChilds.getLength(); h++){
                        Node child = tagChilds.item(h);                      
                        
                        String label = child.getLastChild().getTextContent();
                        String className = label.replaceAll("\\s", "_");
                        
                        String triple1 = SUBJECT_PREFIX_PLACE + entityTriples.getEntityID() + "> " 
                                + RDF + "type> " + SUBJECT_PREFIX + className + "> . "; 
                        
                        entityTriples.getTags().add(triple1);
                        if(!classSet.contains(className)){
                            String triple2 = SUBJECT_PREFIX + className + "> " 
                                + RDF + "type> " + "<http://www.w3.org/2002/07/owl#Class> . "; 
                            
                            String labelTriple = SUBJECT_PREFIX + className + ">" + " <http://www.w3.org/2000/01/rdf-schema#label> " + "\"" + label + "\" . ";
                            entityTriples.getTags().add(labelTriple);
                            classSet.add(className);
                            
                            entityTriples.getTags().add(triple2);
                        }       
                    }
                }               
            }
            else if(node.getNodeName().equalsIgnoreCase("language_id")){
                if(node.hasChildNodes()){
                    entityTriples.getTags().add(SUBJECT_PREFIX_PLACE+entityTriples.getEntityID() + "> " 
                            + PREDICATE_PREFIX+"language_id>" + " " + "\"" + node.getFirstChild().getTextContent() + "\"" + XSD_INT + " . ");
                }
            }
            else if(node.getNodeName().equalsIgnoreCase("language_iso")){
                if(node.hasChildNodes()){
                    entityTriples.getTags().add(SUBJECT_PREFIX_PLACE+entityTriples.getEntityID() + "> " 
                            + PREDICATE_PREFIX + "language_iso>" + " " + "\"" + node.getFirstChild().getTextContent() + "\" . ");
                }    
            }  
            else if(node.getNodeName().equalsIgnoreCase("wikipedia")){
                if(node.hasChildNodes()){
                    String ob = node.getFirstChild().getTextContent();
                    deleteQuotes(ob);
                    entityTriples.getTags().add(SUBJECT_PREFIX_PLACE+entityTriples.getEntityID() + "> " 
                            + PREDICATE_PREFIX + "wikipedia_link>" + " " + "\"" + ob + "\" . ");
                }
            }
            else if(node.getNodeName().equalsIgnoreCase("urlhtml")){
                if(node.hasChildNodes()){
                    String ob = deleteQuotes(node.getFirstChild().getTextContent());
                    ob = ob.substring(ob.indexOf("http"), ob.length());
                    ob = ob.substring(0, ob.indexOf(">"));
                    entityTriples.getTags().add(SUBJECT_PREFIX_PLACE+entityTriples.getEntityID() + "> " 
                                                    + PREDICATE_PREFIX + "urlhtml>" + " " + "\"" + ob + "\" . ");
                }                
            }            
            else if(node.getNodeName().equalsIgnoreCase("title") && !node.getParentNode().getNodeName().contains("tags")){
                if(node.hasChildNodes()){
                    //entityTriples.getTags().add("parent node " +node.getParentNode().getNodeName()); 
                    
                    //remove quotes from object, not escape. virtuoso deletes em 
                    String ob = deleteQuotes(node.getFirstChild().getTextContent());
                    entityTriples.getTags().add(SUBJECT_PREFIX_PLACE + entityTriples.getEntityID() + "> " 
                                                        + PREDICATE_PREFIX+"title>" + " " + "\"" + ob + "\" . ");
                }                
            }
            else if(node.getNodeName().equalsIgnoreCase("description")){
                if(node.hasChildNodes()){
                    String ob = deleteQuotes(node.getFirstChild().getTextContent());
                    ob = ob.replaceAll("\\r\\n|\\r|\\n", " ");
                    ob = ob.replaceAll("\\\\", "/");
                    //entityTriples.getTags().add("parent node " +node.getParentNode().getNodeName()); 
                    entityTriples.getTags().add(SUBJECT_PREFIX_PLACE + entityTriples.getEntityID() + "> " 
                            + PREDICATE_PREFIX + "description>" + " " + "\"" + ob + "\" . ");
                }                
            }
            else if(node.getNodeName().equalsIgnoreCase("is_building")){
                if(node.hasChildNodes()){
                    //System.out.println("found is building");
                    entityTriples.getTags().add(SUBJECT_PREFIX_PLACE+entityTriples.getEntityID() + "> " 
                            + RDF + "type> " + OBJECT_PREFIX + "building> . ");
                    if(!classSet.contains("building")){
                        classSet.add("building");
                        entityTriples.getTags().add(OBJECT_PREFIX + "building> "  
                            + RDF + "type> <http://www.w3.org/2002/07/owl#Class> . ");
                    }
                }
            }
            else if(node.getNodeName().equalsIgnoreCase("is_region")){
                if(node.hasChildNodes()){
                    //System.out.println("found is region");
                    entityTriples.getTags().add(SUBJECT_PREFIX_PLACE+entityTriples.getEntityID() + "> " 
                            + RDF +"type> " +  OBJECT_PREFIX + "region> . ");
                    if(!classSet.contains("region")){
                        classSet.add("region");
                        entityTriples.getTags().add(OBJECT_PREFIX + "region> "  
                            + RDF + "type> <http://www.w3.org/2002/07/owl#Class> . ");
                    }
                    
                }
            }
            else if(node.getNodeName().equalsIgnoreCase("parent_id")){
                if(node.hasChildNodes()){
                    //System.out.println("found parent_id");
                    entityTriples.getTags().add(SUBJECT_PREFIX_PLACE+entityTriples.getEntityID() + "> " 
                            + PREDICATE_PREFIX + "parent_id>" + " \"" + node.getFirstChild().getTextContent() + "\" . ");
                }
            }  
            else if(node.getNodeName().equalsIgnoreCase("is_deleted")){
                if(node.hasChildNodes()){
                    //System.out.println("found parent_id");
                    entityTriples.getTags().add(SUBJECT_PREFIX_PLACE+entityTriples.getEntityID() + "> " 
                            + PREDICATE_PREFIX + "type>" + " " + OBJECT_PREFIX + "is_deleted> . ");
                }
            }
            else if(node.getNodeName().equalsIgnoreCase("edit_info")){
                if(node.hasChildNodes()){
                    String triple = SUBJECT_PREFIX_PLACE + entityTriples.getEntityID() + "> " 
                            + PREDICATE_PREFIX + "type>" + " " + OBJECT_PREFIX + "edit_info_" + entityTriples.getEntityID() + "> . ";
                    entityTriples.getTags().add(triple);
                    
                    //String tripleEdit="";
                    NodeList edits = node.getChildNodes();
                    for(int h = 0; h < edits.getLength(); h++){
                        Node child = edits.item(h);                      

                        if(child.hasChildNodes() ){//&& child.getFirstChild().hasChildNodes()                            
                            String tripleEdit = SUBJECT_PREFIX + "edit_info_" + entityTriples.getEntityID() + ">" + " " 
                                + PREDICATE_PREFIX + child.getNodeName() + "> " + "\"" 
                                + child.getFirstChild().getTextContent() + "\" . "; 
                            entityTriples.getTags().add(tripleEdit);
                        }                                               
                    }
                }
            }
            else if(node.getNodeName().equalsIgnoreCase("location")){
                if(node.hasChildNodes()){
                    String triple = SUBJECT_PREFIX_PLACE + entityTriples.getEntityID()+">" + " " 
                            + PREDICATE_PREFIX + "type>" + " "+ OBJECT_PREFIX + "location> . ";
                    entityTriples.getTags().add(triple);
                    String tripleLocation;
                    NodeList location = node.getChildNodes();
                    for(int h = 0; h < location.getLength(); h++){
                        Node child = location.item(h);                      
                                
                        if(child.getNodeName().equalsIgnoreCase("gadm")){
                            //String tripleGadms1 = SUBJECT_PREFIX_PLACE+entityTriples.getEntityID()+">" + " " 
                                                        //+ PREDICATE_PREFIX + "#type" +" "+OBJECT_PREFIX + "gadms>";  
                            //entityTriples.getTags().add(tripleGadms1);
                            
                            NodeList gadms = child.getChildNodes();
                            for(int l = 0; l < gadms.getLength(); l++){
                                gadms.item(l).getNodeName();
                                String tripleGadms2 = SUBJECT_PREFIX_PLACE + entityTriples.getEntityID() + ">" + " " 
                                        + PREDICATE_PREFIX + "gadm>" + " " 
                                        + OBJECT_PREFIX + gadms.item(l).getNodeName() + "_" 
                                        + entityTriples.getEntityID() + "> . "; //+" " + gadms.item(l).getTextContent();
                                
                                entityTriples.getTags().add(tripleGadms2);
                                if(gadms.item(l).hasChildNodes()){
                                    NodeList gadmChilds = gadms.item(l).getChildNodes();
                                    
                                    for(int q = 0; q < gadmChilds.getLength(); q++){
                                        
                                        if(!gadmChilds.item(q).getTextContent().equals("")){
                                            String tripleGadms3 = SUBJECT_PREFIX+gadms.item(l).getNodeName() + "_" 
                                                    + entityTriples.getEntityID() + "> " 
                                                    + PREDICATE_PREFIX + gadmChilds.item(q).getNodeName() + "> " 
                                                    + "\"" + gadmChilds.item(q).getTextContent() + "\" . ";
                                            entityTriples.getTags().add(tripleGadms3);
                                        }
                                    }                                  
                                }
                                //String tripleGadms3 = SUBJECT_PREFIX+gadms.item(l).getNodeName() + "#" 
                                //+ gadms.item(l).getChildNodes();                   
                            }                           
                        }
                        //if it is location and it is not gadm
                        if(child.hasChildNodes() && !child.getNodeName().equalsIgnoreCase("gadm")){   
                            if(child.getNodeName().equalsIgnoreCase("lon")){
                                tripleLocation = SUBJECT_PREFIX_PLACE+entityTriples.getEntityID() + "> " 
                                        + WGS84_PREFIX+child.getNodeName()+"g>" + " " +"\"" 
                                        + child.getFirstChild().getTextContent() + "\"" + XSD_DOUBLE + " . "; 
                                entityTriples.getTags().add(tripleLocation);
                            }
                            else if(child.getNodeName().equalsIgnoreCase("lat")){
                                tripleLocation = SUBJECT_PREFIX_PLACE+entityTriples.getEntityID() + "> " 
                                        + WGS84_PREFIX+child.getNodeName()+">" + " " + "\"" 
                                        + child.getFirstChild().getTextContent() + "\"" + XSD_DOUBLE + " . "; 
                                entityTriples.getTags().add(tripleLocation);                                
                            }
                            else if(child.getNodeName().equalsIgnoreCase("north")){
                                String bbox = SUBJECT_PREFIX_PLACE+entityTriples.getEntityID() + "> " 
                                        + OWS_BBOX_TYPE + " " + WGS84_BBOX + "_" + entityTriples.getEntityID() + "> . "; 
                                entityTriples.getTags().add(bbox);
                                tripleLocation = WGS84_BBOX + "_" + entityTriples.getEntityID() + "> " 
                                        + NORTH + " " +"\""+child.getFirstChild().getTextContent()+"\"" + XSD_DOUBLE + " . "; 
                                entityTriples.getTags().add(tripleLocation); 
                            }
                            else if(child.getNodeName().equalsIgnoreCase("south")){
                                tripleLocation = WGS84_BBOX + "_" + entityTriples.getEntityID() + "> " 
                                        + SOUTH + " " +"\""+child.getFirstChild().getTextContent()+"\"" + XSD_DOUBLE + " . "; 
                                entityTriples.getTags().add(tripleLocation);
                            }
                            else if(child.getNodeName().equalsIgnoreCase("east")){
                                tripleLocation = WGS84_BBOX + "_" + entityTriples.getEntityID() + "> " 
                                        + EAST + " " + "\"" + child.getFirstChild().getTextContent() + "\"" + XSD_DOUBLE + " . "; 
                                entityTriples.getTags().add(tripleLocation);
                            }
                            else if(child.getNodeName().equalsIgnoreCase("west")){
                                tripleLocation = WGS84_BBOX +"_" + entityTriples.getEntityID() +  "> " 
                                        + WEST + " " +"\""+child.getFirstChild().getTextContent()+"\"" + XSD_DOUBLE + " . "; 
                                entityTriples.getTags().add(tripleLocation);
                            }                            
                            else{
                            tripleLocation = SUBJECT_PREFIX_PLACE+entityTriples.getEntityID() + "> " 
                                    + PREDICATE_PREFIX + child.getNodeName() + ">" + " " + "\"" 
                                    + child.getFirstChild().getTextContent() + "\" . "; 
                            entityTriples.getTags().add(tripleLocation);
                            }
                        }                        
                    }
                }
            }
            else if(node.getNodeName().equalsIgnoreCase("availableLanguages")){
                if(node.hasChildNodes()){
                    NodeList languages = node.getChildNodes();
                    for(int k=0; k<languages.getLength(); k++){
                        Node lang = languages.item(k);
                        String triple = SUBJECT_PREFIX_PLACE+entityTriples.getEntityID() + "> " 
                                + PREDICATE_PREFIX + "availableLanguages>" + " " + OBJECT_PREFIX + lang.getNodeName() + "_"+entityTriples.getEntityID() + "> . ";
                        entityTriples.getTags().add(triple);

                        if(lang.hasChildNodes()){
                            NodeList langs = lang.getChildNodes();
                            for(int l = 0; l< langs.getLength(); l++){
                                String tripleLang = SUBJECT_PREFIX+lang.getNodeName() + "_"+entityTriples.getEntityID()+"> " 
                                        + PREDICATE_PREFIX+langs.item(l).getNodeName() + "> " 
                                        + "\""+ langs.item(l).getTextContent() + "\" . ";
                                entityTriples.getTags().add(tripleLang);
                            }                                
                        }
                    }
                }
            }

            if(node.getNodeName().equalsIgnoreCase("polygon")){                
                //polygon_0, polygon_1 .. etc               
                NodeList polygons = node.getChildNodes();
                List<String> coordinates = new ArrayList<>();
                for(int j=0; j < polygons.getLength(); j++){
                    Node polygon = polygons.item(j);
                    coordinates.add(polygon.getFirstChild().getTextContent()); //x coordinate tag
                    coordinates.add(polygon.getLastChild().getTextContent()); //y coordinate tag
                }               
                //System.out.println(getTab(depth) +"Coordinates: " + coordinates + "\n");
                entityTriples.setCoordinates(coordinates);
                //entityTriples.getObjects().add(coordinates.toString()); 
                
                //multimap geom
                entityTriples.getSubs().put(coordinates.toString(), depth);
                
                
                String sub = entityTriples.getEntityID();
                List<String> ge;
                String fullGeom;
                fullGeom="POLYGON((";
                ge = entityTriples.getCoordinates();
                for(int g = 0; g<ge.size()-1; g=g+2){
                    String x = ge.get(g);
                    String y = ge.get(g+1);
                    //System.out.println(x + " " + y);
                    fullGeom = fullGeom + x + " "+ y + ", ";
                }

                if(ge.size()>1){

                    fullGeom = fullGeom + ge.get(0) + " " + ge.get(1) + "))"; //add first point to close polygon 
                                                                    //will delete the ", " addition with substring below

                    entityTriples.getTags().add(SUBJECT_PREFIX_PLACE+sub+"> " + "<http://www.opengis.net/ont/geosparql#hasGeometry> " 
                            + OBJECT_PREFIX + "geom_" + sub+"> .");
                    //bf.newLine();
                    entityTriples.getTags().add(OBJECT_PREFIX + "geom_" + sub+"> " + "<http://www.opengis.net/ont/geosparql#asWKT> "
                            + "\"" + fullGeom + "\"^^<http://www.opengis.net/ont/geosparql#wktLiteral> . ");
                    //bf.newLine();
                }                                               
            }
            else{  
                if(node.getNodeValue() != null){
                    //System.out.println(getTab(depth) +"value:" + node.getNodeValue());
                    entityTriples.getObjects().add(node.getNodeValue());
                    entityTriples.getSubs().put(node.getNodeValue(), depth);
                }
                if(node.hasAttributes()){
                   // System.out.println(getTab(depth) + node.getAttributes().getLength() 
                     //       + " total attributes:" + node.getAttributes());
                }
                if(node.hasChildNodes()){       
                    //make sub uris
                    depth++;
                    //System.out.println("depth " + depth);
                    iterateChilds(node.getChildNodes(), entityTriples);
                    depth--;
                }
            }    
        }       
    } 

    private static void determineTotalPages(String box) 
            throws MalformedURLException, IOException, ParserConfigurationException, SAXException {
            String url1 = "http://api.wikimapia.org/"
                        + "?key=" + wikimapiaKey
                        + "&function=place.getbyarea"
                        + "&coordsby=bbox"
                        + "&bbox="+box
                        //+ "&categories_or=50,109,46533,164,44865,7,17,4,203,45716,74,163,182"
                        + "&count=100"
                        + "&data_blocks=main,edit,location,translate,geometry";
            
            URL urla = new URL(url1);
            HttpURLConnection connection1 = (HttpURLConnection) urla.openConnection();
            connection1.setRequestMethod("GET");
            connection1.setRequestProperty("Accept", "application/xml");
            InputStream xml1 = connection1.getInputStream();

            DocumentBuilderFactory dbf1 = DocumentBuilderFactory.newInstance();
            DocumentBuilder db1 = dbf1.newDocumentBuilder();
            Document doc1 = db1.parse(xml1);
            numOfEntities = doc1.getElementsByTagName("found").item(0).getTextContent();

            try {
                   totalPages = Integer.parseInt(numOfEntities)/100 + 1;
                }
            catch (NumberFormatException nfe){
                   System.out.println("Something went wrong counting the entities.. try again"); 
                   totalPages = 1;
            }
            System.out.println("total pages are: " + totalPages);
            System.out.println("total entities: " + numOfEntities);        
            page = totalPages;
    }
*/