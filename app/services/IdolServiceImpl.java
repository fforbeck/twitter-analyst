package services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import org.apache.commons.lang3.StringEscapeUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * @author tcsdeveloper
 */
@Service
@Scope(value = "prototype")
public class IdolServiceImpl implements IdolService {

    private static String API_KEY = "ef5bff01-7eef-407a-9534-b5bfb00704ba";

    @Override
    public String[] executeQueryFindSimilar(String word) {
        try {
            String query = "https://api.idolondemand.com/1/api/sync/findsimilar/v1" +
                    "?text=" + URLEncoder.encode(word, "UTF-8") +
                    "&max_results=1" +
                    "&print=all" +
                    "&apikey=" + API_KEY;

            String output = callApi(query);

            JSONObject obj = (JSONObject)JSONValue.parse(output);
            JSONArray documents = (JSONArray)obj.get("documents");

            JSONObject doc = (JSONObject)documents.get(0);

            String txt = (String)doc.get("content");
            String address = (String)doc.get("reference");
            return new String[] { txt, address };
        } catch(Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    public String executeSentimentAnalysis(String addr) {
        try {
            String query = "https://api.idolondemand.com/1/api/sync/analyzesentiment/v1" +
                    "?url=" + URLEncoder.encode(addr, "UTF-8") +
                    "&apikey=" + API_KEY;

            String output = callApi(query);

            JSONObject obj = (JSONObject)JSONValue.parse(output);
            JSONObject aggregate = (JSONObject)obj.get("aggregate");
            String sentiment = (String)aggregate.get("sentiment");
            Object score = aggregate.get("score");
            return sentiment + ", value: " + score;
        } catch(Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    public String executeQueryOcr(String linkToImage) {
        try {
            String query = "https://api.idolondemand.com/1/api/sync/ocrdocument/v1" +
                    "?url=" + URLEncoder.encode(linkToImage, "UTF-8") +
                    "&apikey=" + API_KEY;
            String output = callApi(query);

            JSONObject obj = (JSONObject)JSONValue.parse(output);
            JSONArray text_block = (JSONArray)obj.get("text_block");
            JSONObject text = (JSONObject)text_block.get(0);
            return (String) text.get("text");
        } catch(Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private static String readToString(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        char[] data = new char[1024];
        int xf = 0;
        while((xf = rd.read(data, 0, 1024)) > 0) {
            sb.append(data, 0, xf);
        }
        return sb.toString();
    }

    private static String callApi(String query) {
        try {
            System.out.println(query);
            URL url = new URL(query);
            InputStream is = url.openStream();
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(is, Charset.forName("UTF-8")));
           return readToString(br);
        } catch(Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }


//    public static void main(String[] args) {
//        String[] entry = executeQueryFindSimilar("Hello, World!");
//        System.out.println(entry[1] + "\n\n" + entry[0] + "\n\n");
//
//        String sentiment = executeSentimentAnalysis(entry[1]);
//        System.out.println(sentiment);
//
//        String ocr = executeQueryOcr("http://www.java-made-easy.com/images/hello-world.jpg");
//        String ocr2 = StringEscapeUtils.unescapeXml(ocr);
//        System.out.println(ocr2);
//    }

}