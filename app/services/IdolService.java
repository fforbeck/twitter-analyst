package services;

/**
 * Created by fforbeck on 24/01/15.
 */
public interface IdolService {

    String[] executeQueryFindSimilar(String word);

    String executeSentimentAnalysis(String addr);

    String executeQueryOcr(String linkToImage);

}
