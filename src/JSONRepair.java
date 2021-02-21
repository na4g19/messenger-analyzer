import java.io.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.List;

/**
 * Repairs incorrect encoding of Facebook message files
 */
public class JSONRepair {

    /**
     * Repairs the encoding
     * @param files the JSON files to be repaired
     */
    public static void repairJSON(List<File> files) {

        for(File file : files) {

            String line;
            StringBuilder contentBuilder = new StringBuilder();

            try ( BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"))) {

                while( (line = in.readLine()) != null ) {
                    contentBuilder.append(line).append("\n");
                }

            } catch(Exception e) {
                e.printStackTrace();
            }

            // Change unicode sequences to appropriate symbols
            String string = UnicodeParser.unescapeString(contentBuilder.toString());

            // Decode as utf8 -> encode as latin1 -> decode as utf8
            try {

                final Charset utf8Charset = Charset.forName("UTF-8");
                final Charset iso88591Charset = Charset.forName("latin1");

                ByteBuffer inputBuffer = ByteBuffer.wrap(string.getBytes(utf8Charset));
                CharBuffer data = utf8Charset.decode(inputBuffer);

                ByteBuffer isoBuffer = iso88591Charset.encode(data);
                CharBuffer outputData = utf8Charset.decode(isoBuffer);

                try ( BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"))) {
                    out.write(outputData.toString().trim().toCharArray());
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
