package br.com.pense.devtools.plugins.executor.command.maven;

import java.util.Map;

import br.com.pense.devtools.model.ParameterDef;
import br.com.pense.devtools.model.ParameterList;
import br.com.pense.devtools.plugins.executor.exceptions.ExecutionException;
import br.com.pense.devtools.plugins.executor.exceptions.ParameterNotSetException;
import br.com.pense.devtools.plugins.executor.listener.ExecutionLoggerListener;

public class MavenSetVersionExecutor extends AbstractMavenCommandExecutor {

    private static final ParameterDef PARAMETER_VERSAO       = new ParameterDef("versao", "Versão", String.class);

    private static final String HOLDER_VERSAO     = "{PARAMETER_VERSAO}";

    private String              commandLineFormat = HOLDER_MAVEN_CMD + " versions:set -DnewVersion=" + HOLDER_VERSAO;

    /*
     * (non-Javadoc)
     * 
     * @see
     * br.com.pense.devtools.plugins.executor.CommandExecutor#execute(br.com.pense
     * .devtools.model.MavenCommandConfig)
     */
    public void execute(Map<String, ?> parameters, ExecutionLoggerListener listener) throws ParameterNotSetException, ExecutionException {
        Map<String, Object> params = createParameterMap();
        params.put(HOLDER_VERSAO, this.getValidValue(PARAMETER_VERSAO, parameters, String.class));
        executeCommandLineImpl( replaceParamters(commandLineFormat, params), listener);
    }

    public ParameterList getParametersTypes() {
        return new ParameterList().addParameter(PARAMETER_VERSAO);
    }
    
    public String getName() {
        return "Alterar a Versão do Maven";
    }

}
