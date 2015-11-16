
package gr.athenainnovation.imis.wikimapiabyid;

import java.util.ArrayList;
import java.util.List;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;


/**
 *
 * @author imis-nkaragiannakis
 */

public class EntityTriples {
    
    private String subject1;
    private String predicate1;
    private String object1;
    private String predicate2;
    private String object2;
    private List<String> subjects;
    private List<String> predicates;
    private List<String> objects;
    private List<String> coordinates;
    //private Multimap<> subs;
    private LinkedListMultimap<String, Integer> subs;
    private String entityID;
    private ArrayList<String> tags;

    
    public EntityTriples(){
        subject1 = "";
        predicate1 = "";
        object1 = "";
        predicate2 = "";
        object2 = "";
        subjects = new ArrayList<>();
        predicates = new ArrayList<>();
        objects = new ArrayList<>();
        coordinates = new ArrayList<>();
        subs = LinkedListMultimap.create();
        entityID = "";
        tags = new ArrayList<>();
        //subs = new HashMultimap.create();
    }

    /**
     * @return the subjects
     */
    public List<String> getSubjects() {
        return subjects;
    }

    /**
     * @param subjects the subjects to set
     */
    public void setSubjects(List<String> subjects) {
        this.subjects = subjects;
    }

    /**
     * @return the predicates
     */
    public List<String> getPredicates() {
        return predicates;
    }

    /**
     * @param predicates the predicates to set
     */
    public void setPredicates(List<String> predicates) {
        this.predicates = predicates;
    }

    /**
     * @return the objects
     */
    public List<String> getObjects() {
        return objects;
    }

    /**
     * @param objects the objects to set
     */
    public void setObjects(List<String> objects) {
        this.objects = objects;
    }

    /**
     * @param coordinates the coordinates to set
     */
    public void setCoordinates(List<String> coordinates) {
        this.coordinates = coordinates;
    }

    /**
     * @return the coordinates
     */
    public List<String> getCoordinates() {
        return coordinates;
    }

    /**
     * @return the subs
     */
    public Multimap<String, Integer> getSubs() {
        return subs;
    }

    /**
     * @param subs the subs to set
     */
    public void setSubs(LinkedListMultimap<String, Integer> subs) {
        this.subs = subs;
    }

    /**
     * @return the entityID
     */
    public String getEntityID() {
        return entityID;
    }

    /**
     * @param entityID the entityID to set
     */
    public void setEntityID(String entityID) {
        this.entityID = entityID;
    }

    /**
     * @return the triples
     */
    public ArrayList<String> getTags() {
        return tags;
    }

    /**
     * @param triples the triples to set
     */
    public void setTags(ArrayList<String> triples) {
        this.tags = triples;
    }
    
//    public String getChainTriple(){
//        return getSubject1() + getPredicate1() + getObject1() + ". \n" 
//             + getObject1() + getPredicate2() + getObject2();
//    }
}
