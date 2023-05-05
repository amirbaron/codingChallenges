package webcrawler;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

public class WebCrawlerUsingCompletionService implements WebCrawler {

    private final UrlFetcher urlFetcher;
    private final int maxThreads;

    public WebCrawlerUsingCompletionService(int maxThreads, UrlFetcher urlFetcher) {
        this.maxThreads = maxThreads;
        this.urlFetcher = urlFetcher;
    }

    @Override
    public Set<String> startCrawling(String rootUrl) {
        Set<String> visitedLinks = ConcurrentHashMap.newKeySet();
        ExecutorCompletionService<Set<String>> completionService = new ExecutorCompletionService(Executors.newFixedThreadPool(this.maxThreads));
        visitedLinks.add(rootUrl);
        completionService.submit(() -> crawlLink(rootUrl));
        AtomicInteger numberOfJobs = new AtomicInteger(1);
        while (numberOfJobs.get() > 0) {
            try {
                Future<Set<String>> future = completionService.take();
                numberOfJobs.decrementAndGet();
                Set<String> currentLinks = future.get();
                currentLinks.stream()
                        .filter(visitedLinks::add)
                        .forEach(nextLink -> {
                            numberOfJobs.getAndIncrement();
                            completionService.submit(() -> crawlLink(nextLink));
                        });
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return visitedLinks;
    }

    private Set<String> crawlLink(String rootUrl) {
        Set<String> res = this.urlFetcher.fetchUrl(rootUrl);
        if (Objects.nonNull(res)) {
            return res;
        }
        return Set.of();
    }
}
