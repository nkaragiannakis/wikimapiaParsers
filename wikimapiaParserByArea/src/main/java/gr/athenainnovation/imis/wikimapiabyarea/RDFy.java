package gr.athenainnovation.imis.wikimapiabyarea;

import java.util.List;

/**
 *
 * @author imis-nkaragiannakis
 */

public class RDFy {
    
    private final EntityTriples entityTriples;
    private static final String SUBJECT_PREFIX = "http://geoknow.eu/geodata#polygons_";
    private static final String PREDICATE_PREFIX = "http://geoknow.eu/geodata#";
    
    
    public RDFy(EntityTriples entityTriples){
        
        this.entityTriples = entityTriples;
        produceTriples();
        
    }

    private void produceTriples() {
        List<String> preds = entityTriples.getPredicates();
        List<String> objs = entityTriples.getObjects();
        List<String> subs = entityTriples.getSubjects();
        //System.out.println("preds size" + preds.size());
        //System.out.println("objs size" + objs.size());
        
        //predicates list to object list. Assign 2 to 3. First 2 preds refer to first 3 objects.
        //Every two records to the preds is #has_text. This will get omitted and will get straight to the literal value.
        String triple2 = "";
        if(objs.size() > 1){
        String sub = SUBJECT_PREFIX + objs.get(2);
        int k = 2;
        int l = 2;
            for (int i=0; i<objs.size()-1; i++){
                //String sub = SUBJECT_PREFIX + objs.get(2);
                triple2 = sub + "  " + preds.get(k) + "  " + objs.get(l); //predicate
                
                System.out.println("triple: " + triple2);
            k += 2;
            l += 3;
            }
        }
        //System.out.println("triple2" + triple2);
        //2 -> 3
    }    
}
