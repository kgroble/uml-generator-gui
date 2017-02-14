import java.util.List;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.FieldNode;

import graph.ClassCell;
import graph.Graph;
import patterns.Pattern;

public class UnderscoreFieldPattern extends Pattern {
    private int hello_world;
    
    @Override
    public Graph detect(Graph graphToSearch) {
        Graph ret = new Graph();
        
        for (ClassCell c : graphToSearch.getCells()) {
            for (FieldNode f : c.getFieldNodes()) {
                if ((f.access & Opcodes.ACC_FINAL) == 0
                        && f.name.contains("_")) {
                    ret.addClass(c);
                    break;
                }
            }
        }
        return ret;
    }
}
