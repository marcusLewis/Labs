package br.com.pense.devtools.plugins.executor.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

import br.com.pense.devtools.plugins.executor.command.CommandExecutor;
import br.com.pense.devtools.plugins.executor.command.CommandExecutorBuilder;
import br.com.pense.devtools.plugins.executor.command.CommandExecutorGroup;
import br.com.pense.devtools.plugins.executor.exceptions.ExecutionException;
import br.com.pense.devtools.plugins.executor.exceptions.ParameterNotSetException;
import br.com.pense.devtools.plugins.executor.listener.ExecutionLoggerListener;
import br.com.pense.devtools.view.ParametersPanel;

public class ExecutorsPanel extends JPanel implements ActionListener, ExecutionLoggerListener {
    private static final long                          serialVersionUID     = 1L;

    private JComboBox<CommandExecutorGroup>            cmbGroups;
    private JComboBox<CommandExecutor>                 cmbActions;

    private JButton                                    bttExecutar;
    private JButton                                    bttSalvarConfig;
    private JButton                                    bttResetConfig;

    private JTextArea                                  txtConsole;
    private JCheckBox                                  chkAutoscroll;

    private JPanel                                     parametersHolder;
    private JPanel                                     configHolder;

    private ParametersPanel                            nullParameterPanel   = new ParametersPanel(null, 80, 25);
    private ParametersPanel                            nullGroupPanel       = new ParametersPanel(null, 200, 25);

    private Map<CommandExecutor, ParametersPanel>      cacheParametersPanel = new HashMap<CommandExecutor, ParametersPanel>();
    private Map<CommandExecutorGroup, ParametersPanel> cacheConfigPanel     = new HashMap<CommandExecutorGroup, ParametersPanel>();

    public ExecutorsPanel() {
        super(new BorderLayout());

        init();
    }

    public void actionPerformed(ActionEvent e) {
        try {
            if (e.getSource() == cmbGroups) {
                setActiveGroup();
            } else if (e.getSource() == cmbActions) {
                setActiveExecutor();
            } else if (e.getSource() == bttSalvarConfig) {
                CommandExecutorGroup group = getCurrentExecutorGroup(); 
                if (group != null) {
                    group.saveParameters(getCurrentConfigPanel().getParameterValues());
                }
            } else if (e.getSource() == bttResetConfig) {
                CommandExecutorGroup group = getCurrentExecutorGroup(); 
                if (group != null) {
                    getCurrentConfigPanel().setParametersValues(group.loadParameters());
                }
            } else if (e.getSource() == bttExecutar) {
                bttExecutar.setEnabled(false);
                
                txtConsole.setText("");
                new Thread(new Runnable() {
                    public void run() {
                        CommandExecutor currExecutor = getCurrentExecutor();
    
                        if (currExecutor != null) {
                            Map<String, Object> params = getCurrentPanelValues();
                            try {
                                currExecutor.execute(params, ExecutorsPanel.this);
                            } catch (ParameterNotSetException e1) {
                                JOptionPane.showMessageDialog(ExecutorsPanel.this, e1.getMessage());
                            } catch (ExecutionException e1) {
                                JOptionPane.showMessageDialog(ExecutorsPanel.this, e1.getMessage());
                            }
                        } else {
                            JOptionPane.showMessageDialog(ExecutorsPanel.this, "Sem Ação para executar", "Erro", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }).start();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Exception", JOptionPane.ERROR_MESSAGE);
        } finally {
            if (!bttExecutar.isEnabled()) {
                bttExecutar.setEnabled(true);
            }
        }
    }

    public void log(String message) {
        txtConsole.append(message + "\n");

        if (chkAutoscroll.isSelected()) {
            if (txtConsole.getText().length() > message.length()) {
                txtConsole.setCaretPosition(txtConsole.getText().length() - (message.length() + 1));
            }
        }
    }

    public ParametersPanel getParamterPanel(CommandExecutor executor) {
        if (executor == null) {
            return nullParameterPanel;
        }

        ParametersPanel pnl = cacheParametersPanel.get(executor);

        if (pnl == null) {
            pnl = new ParametersPanel(executor.getParametersTypes(), 80, 25);
            cacheParametersPanel.put(executor, pnl);
        }
        return pnl;
    }

    public ParametersPanel getConfigPanel(CommandExecutorGroup group) {
        if (group == null) {
            return nullGroupPanel;
        }

        ParametersPanel pnl = cacheConfigPanel.get(group);

        if (pnl == null) {
            pnl = new ParametersPanel(group.getConfig(), 200, 25);
            pnl.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "  Configuração do grupo " + group.getName() + ":     "));

            pnl.setParametersValues(group.loadParameters());
            cacheConfigPanel.put(group, pnl);
        }
        return pnl;
    }

    public ParametersPanel getCurrentParametersPanel() {
        return getParamterPanel(getCurrentExecutor());
    }

    public ParametersPanel getCurrentConfigPanel() {
        return getConfigPanel(getCurrentExecutorGroup());
    }

    public CommandExecutor getCurrentExecutor() {
        return (CommandExecutor) cmbActions.getSelectedItem();
    }

    public CommandExecutorGroup getCurrentExecutorGroup() {
        return (CommandExecutorGroup) cmbGroups.getSelectedItem();
    }

    public void setActiveExecutor() {
        ParametersPanel pnl = getCurrentParametersPanel();

        parametersHolder.removeAll();
        parametersHolder.add(pnl);
        parametersHolder.updateUI();
    }

    public void setActiveGroup() {
        configHolder.removeAll();
        configHolder.add(getCurrentConfigPanel());
        configHolder.updateUI();

        CommandExecutorGroup current = getCurrentExecutorGroup();
        boolean hasConfig = (current != null && current.getConfig() != null && !current.getConfig().isEmpty());

        bttSalvarConfig.setEnabled(hasConfig);
        bttResetConfig.setEnabled(hasConfig);

        cmbActions.setModel(new DefaultComboBoxModel<CommandExecutor>(getCurrentExecutorGroup().getExecutors()));
        setActiveExecutor();
    }

    public Map<String, Object> getCurrentPanelValues() {
        return getCurrentParametersPanel().getParameterValues();
    }

    private void init() {
        // inicializa os componentes de tela
        cmbGroups = new JComboBox<CommandExecutorGroup>(CommandExecutorBuilder.getExecutorGroups());
        cmbGroups.addActionListener(this);

        cmbActions = new JComboBox<CommandExecutor>();
        cmbActions.addActionListener(this);

        txtConsole = new JTextArea();
        txtConsole.setFont(new Font("Courier New", Font.PLAIN, 12));

        chkAutoscroll = new JCheckBox("Rolagem automática");
        chkAutoscroll.setSelected(true);

        bttExecutar = new JButton("Executar");
        bttExecutar.addActionListener(this);

        bttSalvarConfig = new JButton("Salvar");
        bttSalvarConfig.addActionListener(this);

        bttResetConfig = new JButton("Reload");
        bttResetConfig.addActionListener(this);

        // cria o layout da aplicação
        JPanel pnlExecutor = new JPanel(new BorderLayout());
        pnlExecutor.add(createActionPanel(), BorderLayout.WEST);
        pnlExecutor.add(createConsolePanel(), BorderLayout.CENTER);

        JTabbedPane pnlLayout = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
        pnlLayout.addTab("Dados", pnlExecutor);
        pnlLayout.addTab("Configuração", createConfigPanel());

        add(pnlLayout, BorderLayout.CENTER);
        setActiveGroup();
    }

    private JPanel createConfigPanel() {
        configHolder = new JPanel(new BorderLayout());

        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        pnlButtons.add(bttSalvarConfig);
        pnlButtons.add(bttResetConfig);

        JPanel ret = new JPanel(new BorderLayout());
        ret.add(configHolder, BorderLayout.CENTER);
        ret.add(pnlButtons, BorderLayout.SOUTH);

        return ret;
    }

    private JPanel createActionPanel() {
        // cria o panel com as actions
        JPanel ret = new JPanel(new BorderLayout());

        ret.add(createCombosPanel(), BorderLayout.NORTH);
        ret.add(createParametersPanel(), BorderLayout.CENTER);
        ret.setPreferredSize(new Dimension(300, 400));
        return ret;
    }

    private JPanel createCombosPanel() {
        JPanel ret = new JPanel(new GridLayout(2, 1));

        ret.add(createCombosPanel("  Grupo:    ", cmbGroups));
        ret.add(createCombosPanel("  Ação:     ", cmbActions));

        ret.setPreferredSize(new Dimension(300, 100));
        return ret;
    }

    private JPanel createCombosPanel(String title, JComboBox<?> combo) {
        JPanel ret = new JPanel(new GridLayout(1, 1));

        ret.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), title));
        ret.add(combo);

        ret.setPreferredSize(new Dimension(300, 50));
        return ret;
    }

    private JPanel createParametersPanel() {
        parametersHolder = new JPanel(new BorderLayout());
        parametersHolder.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "  Parametros:     "));

        if (getCurrentExecutor() != null) {
            parametersHolder.add(getParamterPanel(getCurrentExecutor()));
        }
        parametersHolder.setPreferredSize(new Dimension(300, 50));

        JPanel ret = new JPanel(new BorderLayout(0, 0));
        ret.add(parametersHolder, BorderLayout.CENTER);
        ret.add(bttExecutar, BorderLayout.SOUTH);

        return ret;
    }

    private JPanel createConsolePanel() {
        JPanel ret = new JPanel(new BorderLayout());

        ret.add(chkAutoscroll, BorderLayout.NORTH);
        ret.add(new JScrollPane(txtConsole), BorderLayout.CENTER);

        return ret;
    }

}
