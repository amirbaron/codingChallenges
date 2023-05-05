package webcrawler;

import java.util.Set;

public interface WebCrawler {
    Set<String> startCrawling(String rootUrl) ;
}
