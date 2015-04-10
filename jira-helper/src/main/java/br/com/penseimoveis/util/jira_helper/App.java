package br.com.penseimoveis.util.jira_helper;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Hello world!
 *
 */
public class App {
    
    private static int PAGE_SIZE = 10;
    
    public static void main(String[] args) {
        try {

            String jql = "project = Plataforma and sprint = 51";
            String name = "marcus.martins";
            String password = "pense@2015";

            JSONObject pagina1 = new JSONObject(JiraConnection.executeSearchRequest(name, password, 0, PAGE_SIZE, jql));
            long total = pagina1.getLong("total");
            
            long qtyPages = total / PAGE_SIZE;
            if (total % PAGE_SIZE > 0) {
                qtyPages++;
            }
            
            System.out.println("Page size: [" + PAGE_SIZE + "]");
            System.out.println("Total: [" + total + "]");
            System.out.println("Pages: [" + qtyPages  + "]");
            
            if (qtyPages > 1) {
                long remainingIssues = total - PAGE_SIZE;
                int currentPage = 1;
                
                while (remainingIssues > 0L) {
                    System.out.println(">>> [" + currentPage + "] [" + (currentPage*PAGE_SIZE) + "] ");
                    
                    remainingIssues -= PAGE_SIZE;
                    currentPage++;
                }
                
            }

            

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static List<IssueTimeTracking> extractIssues(JSONObject objResult) {
        List<IssueTimeTracking> list = new ArrayList<IssueTimeTracking>();

        if (objResult.get("issues") != null) {
            JSONArray issues = objResult.getJSONArray("issues");
            System.out.println("Issues " + issues.length());

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
        if (obj.get(name) != null) {
            return obj.getLong(name);
        } else {
            return 0L;
        }
    }
}
