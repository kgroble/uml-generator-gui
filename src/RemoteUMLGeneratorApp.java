import client.ConfigSettings;
import exporters.Exporter;
import graph.Graph;
import graph.GraphGenerator;
import graphviz.GraphvizElement;
import patterns.Parser;
import patterns.Pattern;

import java.io.IOException;
import java.util.List;

/**
 * Created by lewis on 2/16/17.
 */
public class RemoteUMLGeneratorApp {
    public static void main(String[] args) throws IOException {
        String[] passedArgs = new String[1];
        if (args.length == 0 || !args[0].matches(".*json$")) {
            System.err.println("Pass a JSON settings file as a command line argument.");
            return;
        }

        passedArgs[0] = args[0].replaceAll("json$", "txt");
        JSONConverter.parseJSON(args[0], passedArgs[0]);
        passedArgs[0] = "--settings=" + passedArgs[0];

        try {
            ConfigSettings.setupConfig(passedArgs);
        } catch (IOException e) {
            System.err.println("Failed to parse temporary properties file.");
            return;
        }

        // CALL GUI
        SettingsGUI gui = new SettingsGUI(passedArgs[0].substring(11));
        gui.launch(() -> {
            GraphGenerator generator = ConfigSettings.getGenerator();

            Graph g = generator.execute(ConfigSettings.getWhiteList());

            Parser parser = new Parser();
            List<Pattern> patterns = ConfigSettings.getPatterns();
            for (int i = 0; i < patterns.size(); i++) {
                parser.addPattern(patterns.get(i), i);
            }

            List<GraphvizElement> elements = parser.parseGraph(g);
            Exporter exporter = ConfigSettings.getExporter();
            exporter.export(elements);
        });
    }
}
