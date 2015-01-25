package actors;

import actors.messages.Read;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import play.Configuration;
import play.Play;
import play.api.libs.ws.WS;
import play.libs.F;
import play.mvc.Http;
import twitter4j.Status;

import java.io.*;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.List;

/**
 * Created by fforbeck on 24/01/15.
 */
public class TweetAnalyser extends UntypedActor {

    private final LoggingAdapter log = Logging.getLogger(context().system(), this);

    private String requestURL;
    private String apiKey;

    @Override
    public void preStart() throws Exception {
        super.preStart();
        Configuration configuration = Play.application().configuration();
        requestURL = configuration.getString("hp.idol.analyze.sentiment.uri");
        apiKey = configuration.getString("hp.idol.analyze.sentiment.uri");
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof Read) {
            List<Status> tweets = read((Read) message);
            analyseSentimentFrom(tweets);
        } else {
            unhandled(message);
        }
    }

    private List<Status> read(Read msg) {
        log.info("Reading tweet from queue for tag " + msg.getHashTag());

        return null;
    }

    private void analyseSentimentFrom(List<Status> tweets) {
        log.info("Analysing sentiments from tweets");

    }

    public String executeSentimentAnalysis(String tweet) {
        try {
            String query = Play.application().configuration().getString("idol.analyze.sentiment.uri");
            query = query
                    .replaceAll("%TWEET%", URLEncoder.encode(tweet, "UTF-8"))
                    .replaceAll("%API_KEY%", apiKey);

            String result = callApi(query);

            JSONObject obj = (JSONObject) JSONValue.parse(result);
            JSONObject aggregate = (JSONObject) obj.get("aggregate");
            String sentiment = (String) aggregate.get("sentiment");
            return sentiment + ", value: " + aggregate.get("score");
        } catch(Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private String callApi(String query) {
        try {
           // F.Promise<Http.Response> result = WS.url("http://localhost:9001").post("content");

            log.info("Calling sentiment analysis: " + query);

            InputStream is = new URL(query).openStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            return readToString(br);
        } catch (Exception ex) {
            log.error(ex.getMessage());
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

}
