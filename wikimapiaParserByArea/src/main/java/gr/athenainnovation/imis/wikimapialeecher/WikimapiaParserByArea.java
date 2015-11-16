package gr.athenainnovation.imis.wikimapialeecher;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import java.util.List;
import org.xml.sax.SAXException;


/**
 * Extracts places of wikimapia by area and constructs RDF triples. 
 * 
 * These triples are based on info provided by the getByArea method of wikimapia API.
 * The getByArea method provides less information than the getByID method, but this approach is very quick
 * for constructing RDF data for a large number of wikimapia places. 
 * 
 * WKT literal datatype is defined only for the geometry. Might need to add more for virtuoso.
 * 
 * @author imis-nkaragiannakis
 * 
 */

public class WikimapiaParserByArea {
    
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
    
    private static final String HAS_GEOMETRY = "<http://www.opengis.net/ont/geosparql#hasGeometry>";
    private static final String AS_WKT = "<http://www.opengis.net/ont/geosparql#asWKT>";
    private static final String WKT_LITERAL = "^^<http://www.opengis.net/ont/geosparql#wktLiteral>";
    private static final String XSD_INT = "^^<http://www.w3.org/2001/XMLSchema#integer>";
    
        //<entity> http://www.opengis.net/ows#WGS84BoundingBoxType <WGS84BoundingBox>
        //<WGS84BoundingBox> <WGS84BoundingBox#northbc> "north value" 
        //<WGS84BoundingBox> <WGS84BoundingBox#eastbc> "east value"
        //<WGS84BoundingBox> <WGS84BoundingBox#southbc> "south value
        //<WGS84BoundingBox> <WGS84BoundingBox#westbc> "west value"
    
    private static int depth = 0;
    private static int totalPages = 1;
    private static int page = 1;
    //private static String subject, predicate, object;
    private static boolean newPlace = false;
    private static int countEntities = 0;
    private static String numOfEntities;
    private static BufferedWriter bf;
    //private int failedPage;
    private static boolean exec = false;
    private static int allEntities=0;
    private static final boolean polygon_dataset = true; //choose dataset mode. If false, dataset containing Points will get cosntructed.  
    
    
    //Each wikimapia account gets a unique alphanumeric key of the form: 
    //********-********-********-********-********-********-********-********
    //put yours to wikimapiaKey string variable
    private static final String wikimapiaKey = "********-********-********-********-********-********-********-********";
    private static final String rdfTriplesDestinationPath = "/home/user/path/to/folder";
    
//    private static double lat = 51.189328;
//    private static double lon = -0.802002;
//    private static double mlat = 51.784154;
//    private static double mlon = 0.50537109;    
    
    //west london
    private static final String datasetName = "west_london";
    private static final double minlat = 50.83682;
    private static final double minlon = -1.516113;
    private static final double maxlat = 52.052153;
    private static final double maxlon = -0.189514;
    
    //east london
//    private static final String datasetName = "east_london";
//    private static final double minlat = 50.83682;
//    private static final double minlon = -0.189514;
//    private static final double maxlat = 52.052153;
//    private static final double maxlon = 1.340332;

    //attiki
//    private static final String datasetName = "athens";
//    private static final double minlat = 37.818029;
//    private static final double minlon = 23.455811;
//    private static final double maxlat = 38.188114;
//    private static final double maxlon = 24.00238;    
    
    //Leipzig
//    private static final String datasetName = "leipzig";
//    private static final double minlat = 51.211873;
//    private static final double minlon = 12.13028;
//    private static final double maxlat = 51.427299;
//    private static final double maxlon = 12.531281;  
    
    //Berlin 
//    private static final String datasetName = "berlin";
//    private static final double minlat = 52.405603;
//    private static final double minlon = 13.184967;
//    private static final double maxlat = 52.608719;
//    private static final double maxlon = 13.612061;  
    
    //New York     
//    private static final double minlat = 40.507117;
//    private static final double minlon = -74.520264;
//    private static final double maxlat = 41.085148;
//    private static final double maxlon = -73.534241; 
    
    //New York west
//    private static final String datasetName = "west_new_york";
//    private static final double minlat = 40.507117;
//    private static final double minlon = -74.520264;
//    //Latitude : 41.256336 | Longitude : -73.998413
//    private static final double maxlat = 41.256336;
//    private static final double maxlon = -73.998413; 
        
    //New York east  //use below further divided boubnding boxes
//    private static final String datasetName = "east_new_york3";
//    private static final double minlat = 40.507117;
//    private static final double minlon = -73.998413;
//    //
//    private static final double maxlat = 41.256336;
//    private static final double maxlon = -73.289794; 
    
//east new york further divided
    
    //original division. will skip the first bbox and we will run again with the first bbox in order to divide it
//[-73.998413,40.507117,-73.6441035,40.8817265, -73.998413,40.8817265,-73.6441035,41.256336, -73.6441035,40.8817265,-73.289794,41.256336, -73.6441035,40.507117,-73.289794,40.8817265]
//    private static final String datasetName = "east_new_york1";
//    private static final double minlat = 40.507117;
//    private static final double minlon = -73.998413;
//    //
//    private static final double maxlat = 40.8817265;
//    private static final double maxlon = -73.6441035;  

    
    
//  bbox0 = BBox(minLon, centLon, minLat, centLat);
//  bbox1 = BBox(centLon, maxLon, minLat, centLat);
//  bbox2 = BBox(minLon, centLon, centLat, maxLat);
//  bbox3 = BBox(centLon, maxLon, centLat, maxLat); 
    
    public static void main(String[] args) throws MalformedURLException, IOException, InterruptedException  {
        String wikiRDF;
        if(polygon_dataset){
            System.out.println("producing POLYGON dataset");
            wikiRDF = rdfTriplesDestinationPath + datasetName + "polygons_test.nt";
        }
        else{
            System.out.println("producing POINT dataset");
            wikiRDF = rdfTriplesDestinationPath + datasetName + "points_test.nt";
        }
        
        bf = new BufferedWriter(new FileWriter(wikiRDF));

        ArrayList<String> bboxes = new ArrayList<>();
        bboxes.add(BoxUtils.getDownLeftBox(minlon, minlat, maxlon, maxlat));
        bboxes.add(BoxUtils.getUpLeftBox(minlon, minlat, maxlon, maxlat));
        bboxes.add(BoxUtils.getUpRightBox(minlon, minlat, maxlon, maxlat));
        bboxes.add(BoxUtils.getDownRightBox(minlon, minlat, maxlon, maxlat));
        System.out.println(bboxes);
        
        //this is a hack for larger and not equall density distribution bounding boxes, that wikimapia fails to return the actual contained places.
        //used for New york. After the further division of the bounding box, all the entities are returned.
        boolean first = false; //if false will execute for the 3 bbxoes
        for(String box : bboxes){
            if(first){
            exec = false;
            System.out.println("bbox: " + box);
            page = 1;
            totalPages =1;
            parseWikimapia(box); 
            System.out.println("waiting to avoid limit reach..");
            Thread.sleep(30000);
            System.out.println("..running again!");
            allEntities += Integer.parseInt(numOfEntities);
            }
            else{
                first = true;
            }
        }
        System.out.println("~total entities: "+ allEntities);
        bf.close();
    }     
    
    private static void iterateChilds(NodeList nodeList, EntityTriples entityTriples) throws IOException{
        
        Node node; 
        for(int i=0; i < nodeList.getLength(); i++){            
            node = nodeList.item(i);
            //System.out.println("parent:" + node.getParentNode().getNodeName());
            if(node.getNodeName().startsWith("places_") && node.getParentNode().getNodeName().equals("places")){
                //new place found, output previous entity triples
                
                String sub = entityTriples.getEntityID();
                //print main until tags found
//                for(int k = 3; k <entityTriples.getObjects().size()-2; k= k+3){
//                    //if(entityTriples.getObjects().get(k).contains("tags")){break;}
//                //for(String ka : entityTriples.getObjects()){
//                    if(entityTriples.getObjects().get(k).contains("tags")){break;}
//                    String pred = entityTriples.getObjects().get(k);
//                    String obj = entityTriples.getObjects().get(k+2);
//                    System.out.println(" 1st loop " + SUBJECT_PREFIX_PLACE+sub+">"   + "  " 
//                            + PREDICATE_PREFIX+pred+">" + "  " + "\""+obj+"\" . ");                    
//                }
                
                for(String la : entityTriples.getTags()){
                    //System.out.println(la);
                    bf.write(la);
                    bf.newLine();
                }                
                
                List<String> geo;
                String fullGeom;
                if(polygon_dataset){
                    //polygon construction
                    fullGeom="POLYGON((";
                    geo = entityTriples.getCoordinates();
                    for(int g = 0; g < geo.size()-1; g += 2){ //step 2.
                        String x = geo.get(g);
                        String y = geo.get(g+1);
                        fullGeom = fullGeom + x + " "+ y + ", ";
                    }
                }
                else{
                //point construction 
                    fullGeom = "";
                    geo = entityTriples.getCoordinates();
                }
                
                if(geo.size() > 1){
                    
                    if(polygon_dataset){
                        //for polygon
                        fullGeom = fullGeom + geo.get(0) + " " + geo.get(1) + "))"; //add first point to close polygon 
                                                                    //will delete the ", " addition with substring below
                    }
                    else{
                        //for dummy points
                        fullGeom = "POINT(" + geo.get(0) + " " + geo.get(1) + ")";
                    }
                    
                    //fullGeom = fullGeom.substring(0, fullGeom.length()-2) + "))";
                    
                    //System.out.println(SUBJECT_PREFIX_PLACE+sub+"> " + "<http://www.opengis.net/ont/geosparql#hasGeometry> " 
                                                                                //+ OBJECT_PREFIX + "geom_" + sub+"> .");
                    //System.out.println(OBJECT_PREFIX + "geom_" + sub+"> " + "<http://www.opengis.net/ont/geosparql#asWKT> " 
                                            //+ "\"" + fullGeom + "\"^^<http://www.opengis.net/ont/geosparql#wktLiteral> . ");
               
                    bf.write(SUBJECT_PREFIX_PLACE+sub+"> " + HAS_GEOMETRY + " " 
                            + OBJECT_PREFIX + "geom_" + sub+"> .");
                    bf.newLine();
                    bf.write(OBJECT_PREFIX + "geom_" + sub+"> " + AS_WKT + " "
                            + "\"" + fullGeom + "\"" + WKT_LITERAL + " . ");
                    bf.newLine();
                }
                depth = 0;
                
                entityTriples = new EntityTriples();
                entityTriples.setEntityID(node.getFirstChild().getFirstChild().getNodeValue());
                countEntities++;
                
                //System.out.println(getTab(depth) + PLACE_LINE + "\n\n Entity: "+ node.getNodeName() 
                //        + ", id: " + node.getFirstChild().getFirstChild().getNodeValue()+ "\n\n" + PLACE_LINE);
                
                //System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
                entityTriples.getSubjects().add(node.getFirstChild().getFirstChild().getNodeValue());
                entityTriples.getSubs().put(node.getFirstChild().getFirstChild().getNodeValue(), depth);
            } 
            else if(node.getNodeName().equals("tags")){
                //this must change to produce proper classes
   
                String rdfType = SUBJECT_PREFIX_PLACE + entityTriples.getEntityID() + "> " + RDF + "type> " + OBJECT_PREFIX + "tags> . ";
                entityTriples.getTags().add(rdfType);
                if(node.hasChildNodes()){
                    NodeList tagChilds = node.getChildNodes();
                    for(int h = 0; h < tagChilds.getLength(); h++){
                        Node child = tagChilds.item(h);                      
                        //System.out.println(child.getNodeName());//CHAIN: make these subjects. 
                                                        //their childs predicates, and the values of their childs literals
                        String triple = SUBJECT_PREFIX_PLACE + entityTriples.getEntityID() + "> " 
                                + PREDICATE_PREFIX +"tag>" + " \"" + child.getLastChild().getTextContent() 
                                + "\" . "; 
                        entityTriples.getTags().add(triple);
                    }
                }               
            }
            else if(node.getNodeName().equals("language_id")){
                if(node.hasChildNodes()){
                    entityTriples.getTags().add(SUBJECT_PREFIX_PLACE+entityTriples.getEntityID() + "> " 
                            + PREDICATE_PREFIX+"language_id>" + " " + "\"" + node.getFirstChild().getTextContent() + "\"" + XSD_INT + " . ");
                }
            }
            else if(node.getNodeName().equals("language_iso")){
                if(node.hasChildNodes()){
                    entityTriples.getTags().add(SUBJECT_PREFIX_PLACE+entityTriples.getEntityID() + "> " 
                            + PREDICATE_PREFIX + "language_iso>" + " " + "\"" + node.getFirstChild().getTextContent() + "\" . ");
                }    
            }     
            else if(node.getNodeName().equals("wikipedia")){
                if(node.hasChildNodes()){
                    String ob = node.getFirstChild().getTextContent();
                    deleteQuotes(ob);
                    entityTriples.getTags().add(SUBJECT_PREFIX_PLACE+entityTriples.getEntityID() + "> " 
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
                    entityTriples.getTags().add(SUBJECT_PREFIX_PLACE+entityTriples.getEntityID() + "> " 
                                                    + PREDICATE_PREFIX + "urlhtml>" + " " + "\"" + ob + "\" . ");
                }                
            }            
            else if(node.getNodeName().equals("title") && !node.getParentNode().getNodeName().contains("tags")){
                if(node.hasChildNodes()){
                    //entityTriples.getTags().add("parent node " +node.getParentNode().getNodeName()); 
                    
                    //remove quotes from object, not escape. virtuoso deletes em 
                    String ob = deleteQuotes(node.getFirstChild().getTextContent());
                    entityTriples.getTags().add(SUBJECT_PREFIX_PLACE + entityTriples.getEntityID() + "> " 
                                                        + PREDICATE_PREFIX+"title>" + " " + "\"" + ob + "\" . ");
                }                
            }
            else if(node.getNodeName().equals("description")){
                if(node.hasChildNodes()){
                    String ob = deleteQuotes(node.getFirstChild().getTextContent());
                    ob = ob.replaceAll("\\r\\n|\\r|\\n", " ");
                    ob = ob.replaceAll("\\\\", "/");
                    //entityTriples.getTags().add("parent node " +node.getParentNode().getNodeName()); 
                    entityTriples.getTags().add(SUBJECT_PREFIX_PLACE + entityTriples.getEntityID() + "> " 
                            + PREDICATE_PREFIX + "description>" + " " + "\"" + ob + "\" . ");
                }                
            }
            else if(node.getNodeName().equals("is_building")){
                if(node.hasChildNodes()){
                    //System.out.println("found is building");
                    entityTriples.getTags().add(SUBJECT_PREFIX_PLACE+entityTriples.getEntityID() + "> " 
                            + PREDICATE_PREFIX + "type>" + " " + OBJECT_PREFIX + "is_building> . ");
                }
            }
            else if(node.getNodeName().equals("is_region")){
                if(node.hasChildNodes()){
                    //System.out.println("found is region");
                    entityTriples.getTags().add(SUBJECT_PREFIX_PLACE+entityTriples.getEntityID() + "> " 
                            + PREDICATE_PREFIX+"type>" + " " +  OBJECT_PREFIX + "is_region> . ");
                }
            }
            else if(node.getNodeName().equals("parent_id")){
                if(node.hasChildNodes()){
                    //System.out.println("found parent_id");
                    entityTriples.getTags().add(SUBJECT_PREFIX_PLACE+entityTriples.getEntityID() + "> " 
                            + PREDICATE_PREFIX + "parent_id>" + " \"" + node.getFirstChild().getTextContent() + "\" . ");
                }
            }  
            else if(node.getNodeName().equals("is_deleted")){
                if(node.hasChildNodes()){
                    //System.out.println("found parent_id");
                    entityTriples.getTags().add(SUBJECT_PREFIX_PLACE+entityTriples.getEntityID() + "> " 
                            + PREDICATE_PREFIX + "type>" + " " + OBJECT_PREFIX + "is_deleted> . ");
                }
            }
            else if(node.getNodeName().equals("edit_info")){
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
            else if(node.getNodeName().equals("location")){
                if(node.hasChildNodes()){
                    String triple = SUBJECT_PREFIX_PLACE + entityTriples.getEntityID()+">" + " " 
                            + PREDICATE_PREFIX + "type>" + " "+ OBJECT_PREFIX + "location> . ";
                    entityTriples.getTags().add(triple);
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
                        if(child.hasChildNodes() && !child.getNodeName().equals("gadm")){   
                            if(child.getNodeName().equals("lon")){
                                tripleLocation = SUBJECT_PREFIX_PLACE+entityTriples.getEntityID() + "> " 
                                        + WGS84_PREFIX+child.getNodeName()+"g>" + " " +"\"" 
                                        + child.getFirstChild().getTextContent() + "\" . "; 
                                entityTriples.getTags().add(tripleLocation);
                            }
                            else if(child.getNodeName().equals("lat")){
                                tripleLocation = SUBJECT_PREFIX_PLACE+entityTriples.getEntityID() + "> " 
                                        + WGS84_PREFIX+child.getNodeName()+">" + " " + "\"" 
                                        + child.getFirstChild().getTextContent() + "\" . "; 
                                entityTriples.getTags().add(tripleLocation);                                
                            }
                            else if(child.getNodeName().equals("north")){
                                String bbox = SUBJECT_PREFIX_PLACE+entityTriples.getEntityID() + "> " 
                                        + OWS_BBOX_TYPE + " " + WGS84_BBOX + "_" + entityTriples.getEntityID() + "> . "; 
                                entityTriples.getTags().add(bbox);
                                tripleLocation = WGS84_BBOX + "_" + entityTriples.getEntityID() + "> " 
                                        + NORTH + " " +"\""+child.getFirstChild().getTextContent()+"\" . "; 
                                entityTriples.getTags().add(tripleLocation); 
                            }
                            else if(child.getNodeName().equals("south")){
                                tripleLocation = WGS84_BBOX + "_" + entityTriples.getEntityID() + "> " 
                                        + SOUTH + " " +"\""+child.getFirstChild().getTextContent()+"\" . "; 
                                entityTriples.getTags().add(tripleLocation);
                            }
                            else if(child.getNodeName().equals("east")){
                                tripleLocation = WGS84_BBOX + "_" + entityTriples.getEntityID() + "> " 
                                        + EAST + " " + "\"" + child.getFirstChild().getTextContent() + "\" . "; 
                                entityTriples.getTags().add(tripleLocation);
                            }
                            else if(child.getNodeName().equals("west")){
                                tripleLocation = WGS84_BBOX +"_" + entityTriples.getEntityID() +  "> " 
                                        + WEST + " " +"\""+child.getFirstChild().getTextContent()+"\" . "; 
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
            else if(node.getNodeName().equals("availableLanguages")){
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
                        //String triple2 = languages.item(k)
                        //String tripleLanguage;
                        //String lang = languages.item(k).getNodeName() + " " + " ";
                        //entityTriples.getTags().add(lang);
                    }
                }
            }
//            else if(node.getNodeName().equalsIgnoreCase("polygon")){                
//                //polygon_0, polygon_1 .. etc               
//                NodeList polygons = node.getChildNodes();
//                List<String> coordinates = new ArrayList<>();
//                for(int j=0; j < polygons.getLength(); j++){
//                    Node polygon = polygons.item(j);
//                    coordinates.add(polygon.getFirstChild().getTextContent()); //x coordinate tag
//                    coordinates.add(polygon.getLastChild().getTextContent()); //y coordinate tag
//                }               
//                //System.out.println(getTab(depth) +"Coordinates: " + coordinates + "\n");
//                entityTriples.setCoordinates(coordinates);
//                //entityTriples.getObjects().add(coordinates.toString()); 
//                
//                //multimap geom
//                //entityTriples.getSubs().put(coordinates.toString(), depth);
//                entityTriples.getTags().add(coordinates.toString());
//            }            
            
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
                    iterateChilds(node.getChildNodes(), entityTriples);
                    depth--;
                }
            }    
        }       
    }    
    
    private static String getTab(int depth){
        String tab = "";
        if(depth == 0){
            tab = "";           
        }
        else if(depth == 1){
            tab = "\t";
        }
        else if(depth == 2){
            tab = "\t\t";
        }
        else if(depth == 3){
            tab = "\t\t\t";
        }
        else if(depth ==4){
            tab = "\t\t\t\t";
        }
        else if(depth ==5){
            tab = "\t\t\t\t\t";
        }
        else if(depth ==6){
            tab = "\t\t\t\t\t\t";
        }
        return tab;
    }
    
    
    private static String getTriple(String s, String p, String o){
        return "<" + s + ">" + " <" + p + "> " + "<" + o + ">";
    }
    
    private static void parseWikimapia(String box)  {
            
            try {
            
            //while(page <= totalPages){
            page = 13; //reversing   
            while(page >= 1){ //reversing
                //String uri =
                    //"http://api.wikimapia.org/?key=" + wikimapiaKey + "&function=place.getbyarea&coordsby=bbox&bbox=23.713346,37.957700,23.764501,37.984426&count=10&data_blocks=main,geometry,edit,location,photos,comments,translate"; //&category=50            
               
                //&bbox=bbox|lon_min=lon_min&lat_min=lat_min&lon_max=lon_max&lat_max=lat_max|x=x&y=y&z=z
                
                //"&bbox=-0.5103751,51.2867602,0.3340155,51.6918741"   //london
                // -0.5103751 0.3340155
                //boxes long //-0.5103751  vima 0.16887812 ews 0.3340155 5 fores
                //boxes lat  //51.2867602 vima 0.08102278 51.6918741 
//                String downleftBox = BoxUtils.getDownLeftBox(minlon, minlat, maxlon, maxlat);
//                String upleftBox = BoxUtils.getUpLeftBox(minlon, minlat, maxlon, maxlat);
//                String upRightBox = BoxUtils.getUpRightBox(minlon, minlat, maxlon, maxlat);
//                String downRightBox = BoxUtils.getDownRightBox(minlon, minlat, maxlon, maxlat);
                
                //String box = downLeftLon +","+downLeftLat+","+centerLon+","+centerLat;
                //System.out.println(box);
      
                String url3 =
                        "http://api.wikimapia.org/"
                        + "?key=" + wikimapiaKey
                        + "&function=place.getbyarea"
                        + "&coordsby=bbox"
                        //+ "&bbox=23.713346,37.957700,23.764501,37.984426" //athens
                        //+ "&bbox=-0.5103751,51.2867602,0.3340155,51.6918741"  //london
                        
                        //+ "&bbox=-0.5103751,51.2867602,-0.34149698,51.36778298"  //1st box london doen.
 
                        //+ "&bbox=-0.5103751,51.2867602,-0.2000000,51.5500000"  //greedy 1st working, returned 90 pages
                        //+ "&bbox=-0.2000000,51.5500000,0.1000001,51.66000001"  //greedy 2nd working, returned 55 pages
                        //+ "&bbox=-0.802002,51.189328,-0.12084960,51.4913026"  //greedy test
                        +"&bbox="+box
                        //+ "&bbox=-0.5103751,51.2867602,0.3340155,51.6918741"  //greedy 3rd working, returned ? pages         
                        
                        
                        
                        //+ "&bbox=-0.34149698,51.36778298,0.3340155,51.6918741"  //2nd box london changed step
                        + "&count=100" //+ count, change it to 100
                        //+ "&data_blocks=main,edit,location,translate,geometry" //removed photos, comments
                                               
                        + "&data_blocks=main,location,translate,geometry" //for point dataset
                        //+ "&data_blocks=main,location,geometry" //for polygon dataset, without language
                        //+ "&data_blocks=geometry" //for links
                        //+ "&data_blocks=main,edit,geometry" //for polygons dataset
                        //+ "&data_blocks=geometry"
                        + "&page=" + page; //+ page;
                String url2;
                
                if(polygon_dataset){
                    url2 = "http://api.wikimapia.org/"
                        + "?key=" + wikimapiaKey
                        + "&function=place.getbyarea"
                        + "&coordsby=bbox"
                        + "&bbox="+box
                        + "&categories_or=50,109,46533,164,44865,7,17,4,203,45716,74,163,182"
                        + "&count=100"
                        //+ "&data_blocks=main,edit,geometry" //for polygons dataset
                        + "&data_blocks=main,edit,location,translate,geometry" //get all metadata in polygon dataset
                        + "&page=" + page;
                }
                else{
                    url2 = "http://api.wikimapia.org/"
                        + "?key=" + wikimapiaKey
                        + "&function=place.getbyarea"
                        + "&coordsby=bbox"
                        +"&bbox="+box
                        + "&count=100"                        
                        + "&data_blocks=main,location,translate,geometry" //for point dataset
                        + "&page=" + page;
                }
                
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

                if(!exec){
                    System.out.println(url2);
                    
                    numOfEntities = doc.getElementsByTagName("found").item(0).getTextContent();
                    
                    try {
                           totalPages = Integer.parseInt(numOfEntities)/100 + 1;
                        }
                    catch (NumberFormatException nfe){
                           System.out.println("Something went wrong counting the entities.. try again"); 
                           totalPages = 1;
                    }
                    System.out.println("total pages are: " + totalPages);
                    System.out.println("total entities: " + numOfEntities);
                    exec = true;
                }
                NodeList places = doc.getElementsByTagName("places");    
                System.out.println("processing page: " + page + "...");
                EntityTriples entityTriples = new EntityTriples();
                iterateChilds(places,entityTriples);
                //page++;
                  page--; //reversing results  
                }
             
            
        //System.out.println(countEntities + " must be equals to " + numOfEntities);
            
        } catch (ParserConfigurationException | SAXException ex) {
            System.out.println("ParserConfigurationException or SAXException" );
            Logger.getLogger(WikimapiaParserByArea.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MalformedURLException ex) {
            System.out.println("MalformedURLException" );
            Logger.getLogger(WikimapiaParserByArea.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            System.out.println("IOException" );
            Logger.getLogger(WikimapiaParserByArea.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("recovering...");                    
            System.out.println("retrying.. with page: " + page);
            recover(box);  
        }  
    }

    private static void recover(String box){
        parseWikimapia(box);
        System.out.println("\n\n Recovery succesfull!");
    }
    
    private static String escapeQuotes(String string) {       
        String s5 = string.replaceAll("\"", "\\\\\"");
        return s5;
    }
    
    private static String deleteQuotes(String string) {       
        return  string.replaceAll("\"", "");
    }

}
//node.getNodeType() == Node.TEXT_NODE)