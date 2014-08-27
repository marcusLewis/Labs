package br.com.pense.devtools;

import br.com.pense.devtools.plugins.executor.ExecutorPluginDescriptor;
import br.com.pense.devtools.view.MainFrame;


/**
 * Abre o frame principal do Pense DevTools
 *
 */
public class MainApp {
    
    public static void main(String[] args) throws Exception {
        MainFrame frmMain = new MainFrame(new ExecutorPluginDescriptor());
        frmMain.setVisible(true);
    }
    
}
