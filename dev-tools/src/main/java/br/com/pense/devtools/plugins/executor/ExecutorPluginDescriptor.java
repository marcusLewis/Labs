package br.com.pense.devtools.plugins.executor;

import br.com.pense.devtools.model.ParameterList;
import br.com.pense.devtools.plugins.PluginDescriptor;
import br.com.pense.devtools.plugins.executor.view.ExecutorsPanel;

/**
 * Descritor do plugin de execução de comandos.
 * 
 * @author Marcus_Martins
 *
 */
public class ExecutorPluginDescriptor implements PluginDescriptor<ExecutorsPanel> {

    /**
     * Cache da interface do plugin
     */
    private ExecutorsPanel panelInstance;
    
    
    /**
     * Retorna o nome do plugin
     */
    public String getName() {
        return "Executors";
    }
    
    /**
     * Monta a viw padrao do plugin
     */
    public ExecutorsPanel getMainView() {
        if (panelInstance == null) {
            panelInstance = new ExecutorsPanel();
        }
        return panelInstance;
    }
    
    /**
     * Retorna os parametros de configuração dos executors
     */
    public ParameterList getParametersTypes() {
        return new ParameterList();
    }
    
}
