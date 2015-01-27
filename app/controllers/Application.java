package controllers;

import org.springframework.beans.factory.annotation.Autowired;
import play.libs.F;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.WebSocket;
import services.TwitterAnalysisService;

@org.springframework.stereotype.Controller
public class Application extends Controller {

    @Autowired
    private TwitterAnalysisService twitterAnalysisService;

    public static Result index() {
        return  ok(views.html.index.render("Ready."));
    }

    public static Result tweets() {
        double y = Math.random() / Math.random() > 0.5 ? 10 : -10;
        return  ok("{x: Date.UTC(2015, 1, 24), y:"+y+", tweet:\"Yay!  Living Play is out for Early Release!\"}");
    }

    public static WebSocket<String> wsTweetsSocket() {
        return new WebSocket<String>() {

            // Called when the Websocket Handshake is done.
            public void onReady(WebSocket.In<String> in, WebSocket.Out<String> out) {

                // For each event received on the socket,
                in.onMessage(new F.Callback<String>() {
                    public void invoke(String event) {
                        System.out.println(event);
                    }
                });

                // When the socket is closed.
                in.onClose(new F.Callback0() {
                    public void invoke() {
                        System.out.println("Disconnected");
                    }
                });
                // Send a single 'Hello!' message
                double y = Math.random() / Math.random() > 0.5 ? 10 : -10;
                out.write("{x: Date.UTC(2015, 1, 24), y:"+y+", tweet:\"Yay!  Living Play is out for Early Release!\"}");
            }
        };
    }


}