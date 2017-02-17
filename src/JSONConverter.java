import org.json.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Created by lewis on 2/16/17.
 */

public class JSONConverter {
    private static final String[] KEYS = {"recursive", "synthetic", "include", "exclude", "patterns", "access",
            "generate", "exporter"};

    public static void parseJSON(String jsonPath, String propPath) throws IOException {
        byte[] file = Files.readAllBytes(Paths.get(jsonPath));
        String jsonString = new String(file);
        JSONObject jobj = new JSONObject(jsonString);
        PrintWriter propFile = new PrintWriter(propPath);

        for (String key : KEYS) {
            if (jobj.has(key)) {
                propFile.println(key + " = " + jobj.getString(key));
            }
        }

        propFile.close();
    }
}
