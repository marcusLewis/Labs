package br.com.penseimoveis.util.jira_helper;

import java.util.ArrayList;
import java.util.List;

public class IssueTimeTracking {

    private String issueKey;
    private long originalEstimated;
    private long remainingEstimated;
    private long timeSpent;
    private List<String> labels = new ArrayList<String>();

    
    public String getIssueKey() {
        return issueKey;
    }
    public void setIssueKey(String issueKey) {
        this.issueKey = issueKey;
    }
    public long getOriginalEstimated() {
        return originalEstimated;
    }
    public void setOriginalEstimated(long originalEstimated) {
        this.originalEstimated = originalEstimated;
    }
    public long getRemainingEstimated() {
        return remainingEstimated;
    }
    public void setRemainingEstimated(long remainingEstimated) {
        this.remainingEstimated = remainingEstimated;
    }
    public long getTimeSpent() {
        return timeSpent;
    }
    public void setTimeSpent(long timeSpent) {
        this.timeSpent = timeSpent;
    }
    public List<String> getLabels() {
        return labels;
    }
    public void setLabels(List<String> labels) {
        this.labels = labels;
    }
    
    

    
}
