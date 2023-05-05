package webcrawler;

import java.util.Set;

public interface LinkFetcher {
    public Set<String> fetchUrl(String url);
}
