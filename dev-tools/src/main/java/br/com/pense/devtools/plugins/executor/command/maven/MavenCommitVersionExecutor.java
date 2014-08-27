package br.com.pense.devtools.plugins.executor.command.maven;

import java.util.Map;

import br.com.pense.devtools.model.ParameterList;
import br.com.pense.devtools.plugins.executor.exceptions.ExecutionException;
import br.com.pense.devtools.plugins.executor.exceptions.ParameterNotSetException;
import br.com.pense.devtools.plugins.executor.listener.ExecutionLoggerListener;

/**
 * Comando para executar um commit da troca de versão dos poms do Maven
 * 
 * @author Marcus_Martins
 *
 */
public class MavenCommitVersionExecutor extends AbstractMavenCommandExecutor {
    private String              commandLineFormat = HOLDER_MAVEN_CMD + " versions:commit";

    /**
     * Executa o commit do plugin versions do maven.
     */
    public void execute(Map<String, ?> parameters, ExecutionLoggerListener listener) throws ParameterNotSetException, ExecutionException {
        executeCommandLineImpl(replaceParamters(commandLineFormat, createParameterMap()), listener);
    }

    /**
     * Comando sem parametros, retorna null
     */
    public ParameterList getParametersTypes() {
        return null;
    }
    
    /***
     * Retorna o nome do comando.
     */
    public String getName() {
        return "Commit da Versão Alterada do Maven";
    }

}
