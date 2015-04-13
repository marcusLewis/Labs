package br.com.penseimoveis.util.jira_helper;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONObject;

public class JiraConnection {

    
    public static String executeSearchRequest(String urlJira, String user, String password, int start, int rows, String jql) throws Exception {
        String query = URLEncoder.encode(jql, "UTF-8");
        String webPage = urlJira + "/rest/api/2/search?fields=timetracking,labels&maxResults=" + rows + "&startAt=" + start + "&jql=" + query;

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
    
    public static List<IssueTimeTracking> extractIssues(JSONObject objResult) {
        List<IssueTimeTracking> list = new ArrayList<IssueTimeTracking>();

        if (objResult.get("issues") != null) {
            JSONArray issues = objResult.getJSONArray("issues");

            for (int i = 0; i < issues.length(); i++) {
                JSONObject issue = issues.getJSONObject(i);

                IssueTimeTracking tmp = new IssueTimeTracking();
                tmp.setIssueKey(issue.getString("key"));

                // extrai os dados
                // --------------------------------------------------------------------------------
                if (issue.getJSONObject("fields") != null) {
                    JSONObject fields = issue.getJSONObject("fields");
                    if (fields.getJSONObject("timetracking") != null) {
                        JSONObject timeTracking = fields.getJSONObject("timetracking");

                        tmp.setOriginalEstimated(getLongFromJSON(timeTracking, "originalEstimateSeconds"));
                        tmp.setRemainingEstimated(getLongFromJSON(timeTracking, "remainingEstimateSeconds"));
                        tmp.setTimeSpent(getLongFromJSON(timeTracking, "timeSpentSeconds"));
                    } 
                    if (fields.get("labels") != null) {
                        JSONArray arr = fields.getJSONArray("labels");

                        for (int a = 0; a < arr.length(); a++) {
                            tmp.getLabels().add(arr.getString(a));
                        }
                    }
                }
                list.add(tmp);
            }
        }

        return list;
    }
    
    

    private static Long getLongFromJSON(JSONObject obj, String name) {
        if (obj.has(name)) {
            return obj.getLong(name);
        } else {
            return 0L;
        }
    }
}
