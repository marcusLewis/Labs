package br.com.pense.devtools.plugins.executor.command.maven;

import java.util.Map;

import br.com.pense.devtools.plugins.executor.command.AbstractCommandExecutor;

/**
 * Classe abstrata para ser usada em todos os executors baseados em maven 
 * 
 * @author Marcus_Martins
 *
 */
public abstract class AbstractMavenCommandExecutor extends AbstractCommandExecutor {

    /**
     * String para marcar  
     */
    protected final String HOLDER_MAVEN_CMD  = "{MAVEN_CMD}";

    /**
     * Cria um novo map de parametros com as informações do maven pre selecionadas
     */
    protected Map<String, Object> createParameterMap() {
        Map<String, Object> config = this.getCommandExecutorGroup().loadParameters();
        
        Map<String, Object> ret = super.createParameterMap();
        ret.put(HOLDER_MAVEN_CMD, config.get(MavenPenseCommandExecutorGroup.MVN_CMD_CONFIG_KEY).toString());
        return ret;
    }

    @Override
    public String toString() {
        return this.getName();
    }
}
