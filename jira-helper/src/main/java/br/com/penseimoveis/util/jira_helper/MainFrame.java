package br.com.penseimoveis.util.jira_helper;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.DefaultCaret;

import org.json.JSONObject;

public class MainFrame extends JFrame implements ActionListener {

    private static final long serialVersionUID = 1L;
    
    private static int PAGE_SIZE = 20;
    
    private static final String URL_DOMAIN = "https://devpense.atlassian.net";


    
    private JTextField txtPagesize = new JTextField(PAGE_SIZE + "");
    private JTextField txtUrl = new JTextField(URL_DOMAIN);
    private JTextField txtJql = new JTextField("project = Plataforma and sprint = 51");

    private JTextField txtUsername = new JTextField("marcus.martins");
    private JPasswordField pwdPassword = new JPasswordField();
    
    private JTextArea txtResult = new JTextArea();
    
    private JProgressBar progressbar = new JProgressBar();
    
    private JButton bttOk = new JButton("Ok");
    
    public MainFrame() {
        super("Pense Agile Timetracking counter for Jira Cloud");
        
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setBounds(new Rectangle(450, 600));
        
        JPanel pnlContents = new JPanel(new BorderLayout(5, 5));
        pnlContents.add(createFieldsPanel(), BorderLayout.NORTH);
        pnlContents.add(new JScrollPane(txtResult), BorderLayout.CENTER);
        
        DefaultCaret caret = (DefaultCaret)txtResult.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        
        setContentPane(pnlContents);
        setVisible(true);

    }
 
    
    private JPanel createFieldsPanel() {
        JPanel pnlFields = new JPanel(new GridLayout(5, 1, 5, 5));
        
        pnlFields.add(createFieldValuePanel("Jira Url:", txtUrl));
        pnlFields.add(createFieldValuePanel("JQL:", txtJql));
        pnlFields.add(createFieldValuePanel("Page size:", txtPagesize));
        
        pnlFields.add(createFieldValuePanel("Username:", txtUsername));
        pnlFields.add(createFieldValuePanel("Password:", pwdPassword));

        
        JPanel pnlBotoes = new JPanel(new BorderLayout());
        pnlBotoes.add(bttOk, BorderLayout.EAST);
        pnlBotoes.add(progressbar, BorderLayout.CENTER);
        
        bttOk.addActionListener(this);
        
        
        JPanel ret = new JPanel(new BorderLayout(5, 5));
        
        ret.add(pnlFields, BorderLayout.CENTER);
        ret.add(pnlBotoes, BorderLayout.SOUTH);
        
        progressbar.setVisible(false);
        
        ret.setPreferredSize(new Dimension(400, 180));
        return ret;
    }
    
    private JPanel createFieldValuePanel(String name, JComponent cmp) {
        JPanel ret = new JPanel(new BorderLayout());
        
        JLabel lbl = new JLabel(name);
        lbl.setPreferredSize(new Dimension(80, 18));
        
        ret.add(lbl, BorderLayout.WEST);
        ret.add(cmp, BorderLayout.CENTER);
        
        return ret;
    }
    
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == bttOk) {
            txtResult.setText("");

            Thread t = new Thread(new Runnable() {
                public void run() {
                    try {
                        
                        String urlJira = txtUrl.getText();
                        String username = txtUsername.getText();
                        String password =  new String(pwdPassword.getPassword());
                        String jql = txtJql.getText();
                                        
                        if ("".equals(urlJira)) throw new RuntimeException("URL do jira é obrigatório");
                        if ("".equals(username.trim())) throw new RuntimeException("username é obrigatório");
                        if ("".equals(password.trim())) throw new RuntimeException("password jira é obrigatório");
                        if ("".equals(jql)) throw new RuntimeException("JQL é obrigatório");
                        
                        addLog(txtResult, "Conectando com [" + txtUrl.getText() + "]...");                        
                        int pageSize = Integer.parseInt(txtPagesize.getText());
                        
                        
                        JSONObject jiraIssuePage = new JSONObject(JiraConnection.executeSearchRequest(urlJira, username, password, 0, pageSize, jql));
                        int total = jiraIssuePage.getInt("total");
                        
                        int qtyPages = total / pageSize;
                        if (total % pageSize > 0) {
                            qtyPages++;
                        }
                        
                        progressbar.setMinimum(0);
                        progressbar.setMaximum(qtyPages+1);
                        progressbar.setValue(1);
                        progressbar.setVisible(true);
                        
                        
                        List<IssueTimeTracking> issues = JiraConnection.extractIssues(jiraIssuePage);
                        List<IssueTimeTracking> allIssues = new ArrayList<IssueTimeTracking>((int)total);
                        allIssues.addAll(issues);
                        
                        addLog(txtResult, "Page size: [" + pageSize + "]");
                        addLog(txtResult, "Total: [" + total + "]");
                        addLog(txtResult, "Pages: [" + qtyPages  + "]");
                        addLog(txtResult, "size: [" + issues.size()  + "]");
                        
                        Thread.yield();
                        if (qtyPages > 1) {
                            long remainingIssues = total - issues.size();
                            int currentPage = 1;
                            
                            while (remainingIssues > 0L) {
                                int start = currentPage*pageSize;
                                addLog(txtResult, ">>> Buscando pagina [" + currentPage + "]");
                                
                                // pega a proxima pagina
                                jiraIssuePage = new JSONObject(JiraConnection.executeSearchRequest(urlJira, username, password, start, pageSize, jql));
                                issues = JiraConnection.extractIssues(jiraIssuePage);
                                allIssues.addAll(issues);
                                
                                remainingIssues -= issues.size();
                                currentPage++;
                                
                                progressbar.setValue(currentPage);
                                Thread.yield();
                            }
                        }

                        addLog(txtResult, "");
                        addLog(txtResult, allIssues.size()+ " issues loaded... ");
                        addLog(txtResult, "");
                        progressbar.setValue(progressbar.getMaximum());
                        
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
                        
                        addLog(txtResult, "::: [estimated]\t[" + estimated + "]\t[" + timeConversion((int)estimated) + "]");
                        addLog(txtResult, "::: [remaining]\t[" + remaining + "]\t[" + timeConversion((int)remaining) + "]");
                        addLog(txtResult, "::: [burndown]\t[" + (estimated - remaining) + "]\t[" + timeConversion((int)(estimated - remaining)) + "]");
                        addLog(txtResult, "");
                        addLog(txtResult, "::: [spent]\t[" + spent + "]\t[" + timeConversion((int)spent) + "]");
                        addLog(txtResult, "::: [burnup]\t[" + burnup + "]\t[" + timeConversion((int)burnup) + "]");
                        

                        NumberFormat formatter = NumberFormat.getPercentInstance();
                        formatter.setMinimumFractionDigits(2);
                        formatter.setMaximumFractionDigits(2);
                        
                        addLog(txtResult, "");
                        addLog(txtResult, "");
                        addLog(txtResult, "A sprint reduziu o burndown em [" + formatter.format((double)(estimated - remaining) / (double)estimated) + "]");
                        addLog(txtResult, "Do tempo trabalhado [" + formatter.format((double)burnup / (double)spent) + "] foi gasto em tarefas não estimadas");
                        
                    } catch (Exception ex) {
                        StringWriter str = new StringWriter();
                        PrintWriter p = new PrintWriter(str);
                        
                        ex.printStackTrace(p);
                        addLog(txtResult, str.toString());                
                    }
                }
            });
            t.start();
        }
    }
    
    private static void addLog(JTextArea txt, String tmp) {
        txt.append(tmp);
        txt.append("\n");
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
