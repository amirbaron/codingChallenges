package webcrawler;

import java.util.Set;

public interface UrlFetcher {
    public Set<String> fetchUrl(String url);
}
