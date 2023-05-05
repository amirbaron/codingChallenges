package webcrawler;

import java.io.Closeable;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Phaser;
import java.util.logging.Logger;

public class WebCrawlerUsingPhaser implements WebCrawler {
    private static final Logger LOGGER = Logger.getLogger(WebCrawlerUsingPhaser.class.getName());

    private final int maxThreads;
    private final LinkFetcher linkFetcher;

    public WebCrawlerUsingPhaser(int maxThreads, LinkFetcher linkFetcher) {
        this.maxThreads = maxThreads;
        this.linkFetcher = linkFetcher;
    }

    @Override
    public Set<String> startCrawling(String rootUrl) {
        Set<String> visitedLinks = ConcurrentHashMap.newKeySet();
        ExecutorService executor = Executors.newFixedThreadPool(maxThreads);
        try (Closeable close = executor::shutdown) {
            crawlLinks(rootUrl, visitedLinks, executor);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        return visitedLinks;
    }

    private void crawlLinks(String rootUrl, Set<String> visitedLinks, ExecutorService executor) throws InterruptedException {
        Phaser phaser = new Phaser(1);
        LOGGER.info("Crawling started");

        visitedLinks.add(rootUrl);
        crawl(rootUrl, visitedLinks, executor, phaser);

        phaser.arriveAndAwaitAdvance();

        LOGGER.info("Crawling completed");
    }

    private void crawl(String currentUrl, Set<String> visitedLinks, ExecutorService executor, Phaser phaser) {
        phaser.register();
        executor.execute(() -> {
            try {
                Set<String> currentLinks = linkFetcher.fetchUrl(currentUrl);
                currentLinks.stream()
                        .filter(visitedLinks::add)
                        .forEach(nextLink -> crawl(nextLink, visitedLinks, executor, phaser));
            } catch (Exception e) {
                LOGGER.warning("Error while fetching links for " + currentUrl + ": " + e.getMessage());
            } finally {
                phaser.arrive();
            }
        });
    }
}




