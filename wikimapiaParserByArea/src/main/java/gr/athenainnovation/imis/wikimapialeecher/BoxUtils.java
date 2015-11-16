
package gr.athenainnovation.imis.wikimapialeecher;

/**
 * Bounding Box dividing utils.
 * 
 * @author imis-nkaragiannakis
 */

public class BoxUtils {
    
    public static String getDownLeftBox(Double minlon, Double minlat, Double maxlon, Double maxlat){
 
//        if(centLon -180 > 0)
//        centLon -= 360; //that's 2 * 180
//        else if (centLon + 180 < 0)
//        centLon += 360;
        
        double centlon = (maxlon+minlon)/2;
        double centlat = (maxlat+minlat)/2;
        
        return minlon.toString() + "," + minlat.toString() + "," + centlon + "," + centlat;
 
    }
    
    public static String getUpLeftBox(Double minLon, Double minLat, Double maxLon, Double maxLat){
       
        double centLon = (maxLon+minLon)/2;
        double centLat = (maxLat+minLat)/2;
        
        return minLon.toString() + "," + centLat + "," + centLon + "," + maxLat;
 
    }
    
    public static String getUpRightBox(Double minLon, Double minLat, Double maxLon, Double maxLat){
       
        double centLon = (maxLon+minLon)/2;
        double centLat = (maxLat+minLat)/2;        
        return centLon + "," + centLat + "," + maxLon + "," + maxLat;
    }
    
    public static String getDownRightBox(Double minLon, Double minLat, Double maxLon, Double maxLat){
       
        double centLon = (maxLon+minLon)/2;
        double centLat = (maxLat+minLat)/2;       
        
        return centLon + "," + minLat + "," + maxLon + "," + centLat;
    }    
}
