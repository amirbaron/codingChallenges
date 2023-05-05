package webcrawler;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WebCrawlerUsingCompletableFuture implements WebCrawler {

    private final UrlFetcher urlFetcher;
    private final int maxThreads;

    public WebCrawlerUsingCompletableFuture(int maxThreads, UrlFetcher urlFetcher) {
        this.maxThreads = maxThreads;
        this.urlFetcher = urlFetcher;
    }

    @Override
    public Set<String> startCrawling(String rootUrl) {
        Set<String> visitedLinks = ConcurrentHashMap.newKeySet();
        ExecutorService service  = Executors.newFixedThreadPool(this.maxThreads);
        visitedLinks.add(rootUrl);
        try {
            CompletableFuture<Void> future = crawlLink(service, visitedLinks, rootUrl);
            future.join();
        }
        finally {
            service.shutdown();
        }
        return visitedLinks;
    }

    private CompletableFuture<Void> crawlLink(ExecutorService service, Set<String> visitedLinks, String rootUrl) {

        CompletableFuture<Void> future = CompletableFuture.supplyAsync(() -> this.urlFetcher.fetchUrl(rootUrl), service)
                .thenCompose(links -> {
                    if (Objects.nonNull(links)) {
                        CompletableFuture<Void>[] subTasks = links.stream().filter(visitedLinks::add).map(
                                nextLink -> crawlLink(service, visitedLinks, nextLink)
                        ).toArray(CompletableFuture[]::new);
                        return CompletableFuture.allOf(subTasks);
                    }
                    return CompletableFuture.completedFuture(null);
                });
        return future;
    }
}
