import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class HTTPReader {
	
	private static final String PROTOCOL = "http://";

	public String getWebPageBody(String ipAddress) {
        Document webpage = null;
        try {
            webpage = Jsoup.connect(getURLFromIP(ipAddress)).get();
        } catch (Exception e) {
            return null;
        }
        String webpageHtml = webpage.html();
        Document doc = Jsoup.parseBodyFragment(webpageHtml);
        String webpageText = doc.body().text();
        return webpageText;
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
}
