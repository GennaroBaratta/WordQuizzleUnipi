package server;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class TranslateService {
    private static final String HOST = "api.mymemory.translated.net";
    private static final String PATH = "/get?q=";

    public ArrayList<String> translate(String word) {
        String query = PATH + word + "&langpair=it|en";
        ArrayList<String> traduzioni = new ArrayList<>();
        return traduzioni;
        /*
        try {
            URL url = new URL("https", HOST, query);
            URLConnection uc = url.openConnection();
            HttpURLConnection con = (HttpURLConnection) uc;
            int responseCode = con.getResponseCode();
            System.out.println("GET Response Code :: " + responseCode);
            if (responseCode == HttpURLConnection.HTTP_OK) { // success
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        con.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNode = objectMapper.readTree(response.toString());

                for (JsonNode node : jsonNode.path("matches")) {
                  //  if(node.path("quality").asInt() == 74)
                        traduzioni.add(node.path("translation").asText().trim().toLowerCase());
                }
            } else {
                traduzioni.add("GET request not worked");
            }
        } catch (IOException e) {
            e.printStackTrace();
            traduzioni.add("IOError");
        }
        return traduzioni;*/
    }

    public static void main(String[] args) {
        TranslateService ts = new TranslateService();
        System.out.println(ts.translate("ciao"));
    }
}
