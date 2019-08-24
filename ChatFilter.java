import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
/**
 * This is the ChatFilter class which filters the messages and sends back to the server.
 *
 * @author Anuj Mohanbabu Tukade, atukade@purdue.edu
 * @version 11/24/2018
 */

public class ChatFilter {
    public String badWordsFileName;

    public ChatFilter(String badWordsFileName) {
        this.badWordsFileName = badWordsFileName;
    }

    public String filter(String msg) {
        ArrayList<String> words = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(badWordsFileName));
            String line;
            while ((line = br.readLine()) != null) {
                words.add(line);
            }
        } catch (IOException e) {
        }
        String res = "";
        for (int i = 0; i < words.size(); i++) {


            if (msg.toLowerCase().contains(words.get(i).toLowerCase()) || msg.toUpperCase().contains(words.get(i).toUpperCase())) {
                for (int j = 0; j < words.get(i).length(); j++) {
                    res += "*";
                }
                msg = msg.replaceAll("(?i)" + words.get(i), res);
            }
        }

        return msg;
    }
}
