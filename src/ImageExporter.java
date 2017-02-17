import java.io.IOException;
import java.util.List;

import exporters.Exporter;
import exporters.FileExporter;
import graphviz.GraphvizElement;

public class ImageExporter extends Exporter {
    private String rootName;
    
    @Override
    public void setArgs(String[] args) {
        if (args.length < 1) {
            rootName = "./output/out";
        } else {
            rootName = args[0];
        }
    }
    
    @Override
    public void export(List<GraphvizElement> elements) {
        FileExporter dotExporter = new FileExporter();
        dotExporter.setArgs(new String[] {(rootName + ".dot")});
        dotExporter.export(elements); 
        
        try {
            Process p = Runtime.getRuntime().exec("gvedit " + rootName + ".dot");
//            Process p = Runtime.getRuntime().exec("dot " + rootName + ".dot" + " -o " + rootName + ".png");
            p.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

}
