package br.com.pense.devtools.view;

import java.util.Arrays;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import br.com.pense.devtools.plugins.PluginDescriptor;

/**
 * Frame principal responsavel por carregar todos os plugins registrados
 * 
 * @author Marcus_Martins
 *
 */
public class MainFrame extends JFrame {
    private static final long serialVersionUID = 1L;
    
    private List<PluginDescriptor<?>> listOfPlugins;

    public MainFrame(PluginDescriptor<?>... plugins){
        super("Pense: Dev Tools");

        this.listOfPlugins = Arrays.asList(plugins);
        
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(800, 600);
        this.setContentPane(createContentPane());
    }

    private JTabbedPane createContentPane() {
        JTabbedPane ret = new JTabbedPane();
        
        for (PluginDescriptor<?> pluginDescriptor : listOfPlugins) {
            ret.add(pluginDescriptor.getName(), pluginDescriptor.getMainView());
        }
        return ret;
    }
}
