package br.com.penseimoveis.util.jira_helper;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import org.apache.commons.codec.binary.Base64;

public class JiraConnection {
    
    private static final String URL_DOMAIN = "https://devpense.atlassian.net";
    
    public static String executeSearchRequest(String user, String password, int start, int rows, String jql) throws Exception {
        String query = URLEncoder.encode(jql, "UTF-8");
        String webPage = URL_DOMAIN + "/rest/api/2/search?fields=timetracking,labels&maxResults=" + rows + "&startAt=" + start + "&jql=" + query;

        String authString = user + ":" + password;
        byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
        String authStringEnc = new String(authEncBytes);

        URL url = new URL(webPage);
        URLConnection urlConnection = url.openConnection();
        urlConnection.setRequestProperty("Authorization", "Basic " + authStringEnc);
        InputStream is = urlConnection.getInputStream();
        InputStreamReader isr = new InputStreamReader(is);

        int numCharsRead;
        char[] charArray = new char[1024];
        StringBuffer sb = new StringBuffer();
        while ((numCharsRead = isr.read(charArray)) > 0) {
            sb.append(charArray, 0, numCharsRead);
        }
        
        return sb.toString();
    }
}
