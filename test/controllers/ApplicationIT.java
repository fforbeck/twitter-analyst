package controllers;

import models.Tweet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import play.GlobalSettings;
import play.mvc.Result;
import play.test.WithApplication;
import repositories.TweetRepository;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static play.test.Helpers.*;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationIT extends WithApplication {

    private Application app;

    @Mock
    private TweetRepository repo;

    @Before
    public void setUp() throws Exception {
        app = new Application(repo);

        final GlobalSettings global = new GlobalSettings() {
            @Override
            public <A> A getControllerInstance(Class<A> aClass) {
                return (A) app;
            }
        };

        start(fakeApplication(global));
    }

    @Test
    public void indexSavesDataAndReturnsId() {
        final Tweet tweet = new Tweet();
        tweet.id = 1L;
        when(repo.save(any(Tweet.class))).thenReturn(tweet);
        when(repo.findOne(1L)).thenReturn(tweet);

        final Result result = route(fakeRequest(GET, "/"));

        assertEquals(OK, status(result));
        assertTrue(contentAsString(result).contains("Found one tweet from homer"));
    }

}