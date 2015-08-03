package br.com.penseimoveis.util.jira_helper;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

/**
 * Hello world!
 *
 */
public class App {
    
    private static int PAGE_SIZE = 3;
    
    private static final String URL_DOMAIN = "https://devpense.atlassian.net";
    
    
    public static void main(String[] args) {
        new MainFrame();
    }
    
    public static void main1(String[] args) {
        try {

            String jql = "project = Plataforma and sprint = 51";
            String name = "marcus.martins";
            String password = "pense@2015";

            JSONObject jiraIssuePage = new JSONObject(JiraConnection.executeSearchRequest(URL_DOMAIN, name, password, 0, PAGE_SIZE, jql));
            long total = jiraIssuePage.getLong("total");
            
            long qtyPages = total / PAGE_SIZE;
            if (total % PAGE_SIZE > 0) {
                qtyPages++;
            }
            

            List<IssueTimeTracking> issues = JiraConnection.extractIssues(jiraIssuePage);
            List<IssueTimeTracking> allIssues = new ArrayList<IssueTimeTracking>((int)total);
            allIssues.addAll(issues);
            
            System.out.println("Page size: [" + PAGE_SIZE + "]");
            System.out.println("Total: [" + total + "]");
            System.out.println("Pages: [" + qtyPages  + "]");
            System.out.println("size: [" + issues.size()  + "]");
            
            
            if (qtyPages > 1) {
                long remainingIssues = total - issues.size();
                int currentPage = 1;
                
                while (remainingIssues > 0L) {
                    int start = currentPage*PAGE_SIZE;
                    System.out.println(">>> [" + currentPage + "] [" + (start) + "] ");
                    
                    // pega a proxima pagina
                    jiraIssuePage = new JSONObject(JiraConnection.executeSearchRequest(URL_DOMAIN, name, password, start, PAGE_SIZE, jql));
                    issues = JiraConnection.extractIssues(jiraIssuePage);
                    allIssues.addAll(issues);
                    
                    remainingIssues -= issues.size();
                    currentPage++;
                }
                
            }

            System.out.println(">>> " + allIssues.size());
            
            long estimated = 0L;
            long spent = 0L;
            long remaining = 0L;
            long burnup = 0L;
            
            for (IssueTimeTracking t: allIssues) {
                estimated += t.getOriginalEstimated();
                remaining += t.getRemainingEstimated();
                spent += t.getTimeSpent();
                
                if (t.getOriginalEstimated() == 0L && t.getTimeSpent() > 0L) {
                    burnup += t.getTimeSpent();
                }
            }
            
            System.out.println("::: [estimated][" + estimated + "][" + timeConversion((int)estimated) + "]");
            System.out.println("::: [spent][" + spent + "][" + timeConversion((int)spent) + "]");
            System.out.println("::: [remaining][" + remaining + "][" + timeConversion((int)remaining) + "]");
            System.out.println("::: [burnup][" + burnup + "][" + timeConversion((int)burnup) + "][" + NumberFormat.getPercentInstance().format((double)burnup / (double)spent) + "]");
            System.out.println("::: [burndown][" + (estimated - remaining) + "][" + timeConversion((int)(estimated - remaining)) + "][" + NumberFormat.getPercentInstance().format((double)(estimated - remaining) / (double)estimated) + "]");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    
    private static String timeConversion(int totalSeconds) {
        int hours = totalSeconds / 60 / 60;
        int minutes = (totalSeconds - (hoursToSeconds(hours)))
                / 60;

        return hours + ":" + minutes;
    }

    private static int hoursToSeconds(int hours) {
        return hours * 60 * 60;
    }

}


