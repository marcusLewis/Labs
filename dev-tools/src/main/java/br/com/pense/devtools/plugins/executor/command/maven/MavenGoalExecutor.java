package br.com.pense.devtools.plugins.executor.command.maven;

import java.util.Map;

import br.com.pense.devtools.model.ParameterDef;
import br.com.pense.devtools.model.ParameterList;
import br.com.pense.devtools.plugins.executor.exceptions.ExecutionException;
import br.com.pense.devtools.plugins.executor.exceptions.ParameterNotSetException;
import br.com.pense.devtools.plugins.executor.listener.ExecutionLoggerListener;

/**
 * Executa os Goals no maven.
 * 
 * @author Marcus_Martins
 *
 */
public class MavenGoalExecutor extends AbstractMavenCommandExecutor {

    private static final ParameterDef PARAMETER_GOALS       = new ParameterDef("goals", "Goals", String.class);
    private static final ParameterDef PARAMETER_EXEC_TESTES = new ParameterDef("execTests", "Executar Testes", Boolean.class);

    private static final String       HOLDER_GOALS          = "{PARAMETER_GOALS}";
    private static final String       HOLDER_EXEC_TESTES    = "{PARAMETER_EXEC_TESTES}";

    private String                    commandLineFormat     = HOLDER_MAVEN_CMD + " " + HOLDER_EXEC_TESTES + " " + HOLDER_GOALS;

    /*
     * (non-Javadoc)
     * 
     * @see
     * br.com.pense.devtools.plugins.executor.CommandExecutor#execute(br.com
     * .pense .devtools.model.MavenCommandConfig)
     */
    public void execute(Map<String, ?> parameters, ExecutionLoggerListener listener) throws ParameterNotSetException, ExecutionException {
        String execTestsParamter = "";

        if (!this.getValidValue(PARAMETER_EXEC_TESTES, parameters, Boolean.class)) {
            execTestsParamter = "-DskipTests";
        }

        Map<String, Object> params = createParameterMap();
        params.put(HOLDER_GOALS, this.getValidValue(PARAMETER_GOALS, parameters, String.class));
        params.put(HOLDER_EXEC_TESTES, execTestsParamter);

        executeCommandLineImpl(replaceParamters(commandLineFormat, params), listener);
    }

    public ParameterList getParametersTypes() {
        // @formatter:off
        return new ParameterList()
            .addParameter(PARAMETER_GOALS)
            .addParameter(PARAMETER_EXEC_TESTES);
        // @formatter:on
    }

    public String getName() {
        return "Executar Goals do Maven";
    }

}
