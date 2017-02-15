import java.util.HashSet;
import java.util.Set;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import graph.AccessLevel;
import graph.ClassCell;
import graph.Edge;
import graph.Graph;
import patterns.Pattern;

public class DemeterViolationPattern extends Pattern {
    private boolean logging = false;
    
    @Override
    public Graph detect(Graph graphToSearch) {
        Graph ret = new Graph();
        
        
        for (ClassCell c : graphToSearch.getCells()) {
            Set<String> badClasses = new HashSet<>();

            Class<?> cellClass = null;
            try {
                cellClass = Class.forName(c.getName().replace('/', '.'));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            
            Set<Class<?>> fieldClasses = new HashSet<>();
            
            for (FieldNode fieldNode : c.getFieldNodes(AccessLevel.PRIVATE)) {
                if (fieldNode.desc.startsWith("L")) {
                    try {
                        String typeName = fieldNode.desc.substring(1, fieldNode.desc.length() - 1).replace('/', '.');
                        fieldClasses.add(Class.forName(typeName));
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
            
            for (MethodNode methodNode : c.getMethods(AccessLevel.PRIVATE)) {
                Set<Class<?>> goodClasses = new HashSet<>();
                Set<Class<?>> subclassGoodClasses = new HashSet<>();
                
                subclassGoodClasses.add(cellClass);
                subclassGoodClasses.addAll(fieldClasses);
                
                String args = methodNode.desc.substring(methodNode.desc.indexOf('(') + 1, methodNode.desc.indexOf(')'));
                for (String argType : args.split(";")) {
                    if (argType.startsWith("L")) {
                        try {
                            Class<?> klass = Class.forName(argType.substring(1).replace('/',  '.'));
                            subclassGoodClasses.add(klass);
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
                
                for (int i = 0; i < methodNode.instructions.size(); i++) {
                    AbstractInsnNode insnNode = methodNode.instructions.get(i);
                    switch (insnNode.getType()) {
                        case AbstractInsnNode.METHOD_INSN :
                            MethodInsnNode insn = (MethodInsnNode)insnNode;
                            if (insn.name.equals("<init>")) {
                                try {
                                    Class<?> klass = Class.forName(insn.owner.replace('/',  '.'));
                                    goodClasses.add(klass);
                                } catch (ClassNotFoundException e) {
                                    e.printStackTrace();
                                }

                            }
                    }
                }
                
                for (int i = 0; i < methodNode.instructions.size(); i++) {
                    AbstractInsnNode insnNode = methodNode.instructions.get(i);
                    switch (insnNode.getType()) {
                        case AbstractInsnNode.METHOD_INSN :
                            MethodInsnNode insn = (MethodInsnNode)insnNode;
                            if (insn.getOpcode() == Opcodes.INVOKESTATIC) {
                                continue;
                            }
                            
                            try {
                                Class<?> insnClass = Class.forName(insn.owner.replace('/', '.'));
                                if (goodClasses.contains(insnClass)) {
                                    continue;
                                }
                                
                                boolean safeCall = false;
                                for (Class<?> klass : subclassGoodClasses) {
                                    if (klass.isAssignableFrom(insnClass)
                                            || insnClass.isAssignableFrom(cellClass)) {
                                        safeCall = true;
                                        break;
                                    }
                                }
                                
                                if (safeCall) {
                                    continue;
                                }
                                
                                
                                badClasses.add(insnClass.getName());
                                if (logging) {
                                    System.out.println("Violation in class " + c.getName() + ", method " + methodNode.name + " calling " + insn.name + " on " + insn.owner);
                                }
                            } catch (ClassNotFoundException e) {
                                e.printStackTrace();
                            }

                           
                    }
                }
            }
            
            for (Edge e : c.getEdges()) {
                if (badClasses.contains(e.getDestination().getName().replace('/', '.'))) {
                    ret.addEdge(e);
                }
            }
        }
        
        
        return ret;
    }
    
    @Override
    public void setArgs(String[] args){
        if (args.length > 0){
            logging = args[0].equals("true");            
        }
    }

}
