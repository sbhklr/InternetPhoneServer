import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class HTTPReader {
	
	private static final String PROTOCOL = "http://";

	public String getWebPageBody(String htmlContent) {
        Document doc = Jsoup.parseBodyFragment(htmlContent);
        return doc.body().text();
    }
	
	public String getArticleContent(String htmlContent){
		StringBuffer articleContent = new StringBuffer();
		Document doc = Jsoup.parseBodyFragment(htmlContent);
		String silence = ".           . ";

		Elements titles = doc.getElementsByTag("title");
		articleContent.append(titles.first().text());
		articleContent.append(silence);
		
		//Consider using different attributes e.g. div[class=text_detail]
		Elements articleElements = doc.select("article");
		
		for (Element articleElement : articleElements) {
			articleContent.append(articleElement.text());
			
			for (Element paragraphElement : articleElement.select("p")) {
				articleContent.append(paragraphElement.text());
			}
			
			articleContent.append(silence);
		}
		
		if(articleElements.isEmpty()) articleContent.append("This website doesn't seem to contain any articles.");
		
		return articleContent.toString();
	}
	
	public String getWebPageHTML(String ipAddress){
		Document webpage = null;
        try {
            webpage = Jsoup.connect(getURLFromIP(ipAddress)).get();
        } catch (Exception e) {
            return null;
        }
        return webpage.html();
	}
	
    private String getURLFromIP(String ipAddress) {

        String partOne = ipAddress.substring(0, 3);
        String partTwo = ipAddress.substring(3, 6);
        String partThree = ipAddress.substring(6, 9);
        String partFour = ipAddress.substring(9, 12);

        partOne = Integer.valueOf(partOne).toString();
        partTwo = Integer.valueOf(partTwo).toString();
        partThree = Integer.valueOf(partThree).toString();
        partFour = Integer.valueOf(partFour).toString();

        return PROTOCOL + partOne + "." + partTwo + "." + partThree + "." + partFour;
    }

	public String getLanguage(String html) {
		Document doc = Jsoup.parse(html);
		Element htmlTag = doc.getElementsByTag("html").first();
		String language = htmlTag.attr("lang");
		return language;
	}
}
