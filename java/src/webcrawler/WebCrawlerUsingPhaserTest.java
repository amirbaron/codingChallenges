package webcrawler;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import java.util.Set;

public class WebCrawlerUsingPhaserTest {
    private LinkFetcher linkFetcher;
    private WebCrawlerUsingPhaser webCrawlerUsingPhaser;

    @BeforeEach
    public void setUp() {
        linkFetcher = Mockito.mock(LinkFetcher.class);
        webCrawlerUsingPhaser = new WebCrawlerUsingPhaser(2, linkFetcher);
    }

    @Test
    public void testCrawler() {
        setupMockLinkFetcher();

        Set<String> links = webCrawlerUsingPhaser.startCrawling("http://www.google.com");

        Assertions.assertEquals(Set.of(
                "http://www.google.com",
                "http://www.google.com/1",
                "http://www.google.com/1/1",
                "http://www.google.com/1/2",
                "http://www.google.com/2",
                "http://www.google.com/2/1",
                "http://www.google.com/2/2"
        ), links);
    }

    @Test
    public void testCrawlerWhenLinkFetcherReturnsEmptySet() {
        Mockito.when(linkFetcher.fetchUrl(Mockito.anyString())).thenReturn(Set.of());

        Set<String> links = webCrawlerUsingPhaser.startCrawling("http://www.google.com");

        Assertions.assertEquals(Set.of("http://www.google.com"), links);
    }

    @Test
    public void testCrawlerWhenLinkFetcherReturnsNull() {
        Mockito.when(linkFetcher.fetchUrl(Mockito.anyString())).thenReturn(null);

        Set<String> links = webCrawlerUsingPhaser.startCrawling("http://www.google.com");

        Assertions.assertEquals(Set.of("http://www.google.com"), links);
    }

    private void setupMockLinkFetcher() {
        Mockito.when(linkFetcher.fetchUrl("http://www.google.com")).thenReturn(Set.of("http://www.google.com/1", "http://www.google.com/2"));
        Mockito.when(linkFetcher.fetchUrl("http://www.google.com/1")).thenReturn(Set.of("http://www.google.com/1/1", "http://www.google.com/1/2"));
        Mockito.when(linkFetcher.fetchUrl("http://www.google.com/2")).thenReturn(Set.of("http://www.google.com/2/1", "http://www.google.com/2/2"));
    }
}
