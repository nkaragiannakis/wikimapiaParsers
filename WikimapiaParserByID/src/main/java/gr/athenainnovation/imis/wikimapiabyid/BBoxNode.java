
package gr.athenainnovation.imis.wikimapiabyid;

import java.util.List;

/**
 *
 * @author imis-nkaragiannakis
 */   

public class BBoxNode {
        
    private boolean isLeaf;
    private boolean isParent;
    private String data;
    private BBoxNode parent;
    private List<BBoxNode> children;
    private final double minlon;
    private final double minlat;
    private final double maxlon;
    private final double maxlat;
    private int numberOfPlaces;
    private int numberOfPages;
    private int bookmarkPage;

    BBoxNode(double minlon, double minlat, double maxlon, double maxlat) {
        this.minlon = minlon;
        this.minlat = minlat;
        this.maxlon = maxlon;
        this.maxlat = maxlat;
    }

    public Double getMinLon(){
        return minlon;
    }
    public Double getMinLat(){
        return minlat;
    }
    public Double getMaxLon(){
        return maxlon;
    }
    public Double getMaxLat(){
        return maxlat;
    }    
    
    public void addChild(BBoxNode child){
        children.add(child);
    }

    public void convertToLeaf(){
        isLeaf = true;
    }

    public void convertToParent(){
        isParent = true;
    }
    public boolean isLeaf(){
        return isLeaf;
    }
    
    public boolean isParent(){
        return isParent;
    }
    
    public List<BBoxNode> getChildren(){
        return children;
    }
    
    public void setNumberOfPlaces(int numberOfPlaces){
        this.numberOfPlaces = numberOfPlaces;
    }
    
    public Integer getNumberOfPlaces(){
        return numberOfPlaces;
    }
    
    public void setNumberOfPages(int numberOfPages){
        this.numberOfPages = numberOfPages;
    }
    
    public Integer getNumberOfPages(){
        return numberOfPages;
    }
    
    public void setBookmarkPage(int bookmarkPage){
        this.bookmarkPage = bookmarkPage;
    }
    
    public Integer getBookmarkPage(){
        return bookmarkPage;
    }
}
