package webcrawler;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import java.util.Set;
import java.util.stream.Stream;

public class WebCrawlerUsingPhaserTest {
    public static Stream<Arguments> crawlerInstance() {
        UrlFetcher urlFetcherForPhaser = Mockito.mock(UrlFetcher.class);
        UrlFetcher urlFetcherForCompletionService = Mockito.mock(UrlFetcher.class);
        return Stream.of(Arguments.of(new WebCrawlerUsingPhaser(2, urlFetcherForPhaser), urlFetcherForPhaser),
                Arguments.of(new WebCrawlerUsingCompletionService(2, urlFetcherForPhaser), urlFetcherForCompletionService)
        );
    }

    @ParameterizedTest
    @MethodSource("crawlerInstance")
    public void testCrawler(WebCrawler crawler, UrlFetcher urlFetcher) {
        setupMockLinkFetcher(urlFetcher);

        Set<String> links = crawler.startCrawling("http://www.google.com");

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

    @ParameterizedTest
    @MethodSource("crawlerInstance")
    public void testCrawlerWhenLinkFetcherReturnsEmptySet(WebCrawler crawler, UrlFetcher urlFetcher) {
        Mockito.when(urlFetcher.fetchUrl(Mockito.anyString())).thenReturn(Set.of());

        Set<String> links = crawler.startCrawling("http://www.google.com");

        Assertions.assertEquals(Set.of("http://www.google.com"), links);
    }

    @ParameterizedTest
    @MethodSource("crawlerInstance")
    public void testCrawlerWhenLinkFetcherReturnsNull(WebCrawler crawler, UrlFetcher urlFetcher) {
        Mockito.when(urlFetcher.fetchUrl(Mockito.anyString())).thenReturn(null);

        Set<String> links = crawler.startCrawling("http://www.google.com");

        Assertions.assertEquals(Set.of("http://www.google.com"), links);
    }

    private void setupMockLinkFetcher(UrlFetcher urlFetcher) {
        Mockito.when(urlFetcher.fetchUrl("http://www.google.com")).thenReturn(Set.of("http://www.google.com/1", "http://www.google.com/2"));
        Mockito.when(urlFetcher.fetchUrl("http://www.google.com/1")).thenReturn(Set.of("http://www.google.com/1/1", "http://www.google.com/1/2"));
        Mockito.when(urlFetcher.fetchUrl("http://www.google.com/2")).thenReturn(Set.of("http://www.google.com/2/1", "http://www.google.com/2/2"));
    }
}
