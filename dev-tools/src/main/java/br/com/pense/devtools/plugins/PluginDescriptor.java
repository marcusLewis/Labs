package br.com.pense.devtools.plugins;

import javax.swing.JComponent;

import br.com.pense.devtools.model.ParameterList;

/**
 *  Classe que descreve as caracteristicas dos plugins
 * 
 * @author Marcus_Martins
 *
 * @param <K>
 */
public interface PluginDescriptor<K extends JComponent> {

    /**
     * Nome do plugin
     * @return
     */
    String getName();
    
    /**
     * Retorna a view principal de manipulação do plugin
     * @return
     */
    K getMainView();
    
    /**
     * Lista de parametros para configurar o plugin
     * @return
     */
    ParameterList getParametersTypes();

}
