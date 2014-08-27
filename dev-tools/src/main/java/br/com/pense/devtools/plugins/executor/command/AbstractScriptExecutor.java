package br.com.pense.devtools.plugins.executor.command;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import br.com.pense.devtools.model.ParameterDef;
import br.com.pense.devtools.model.ParameterList;
import br.com.pense.devtools.plugins.executor.exceptions.ExecutionException;
import br.com.pense.devtools.plugins.executor.exceptions.ParameterNotSetException;
import br.com.pense.devtools.plugins.executor.listener.ExecutionLoggerListener;


/**
 * Classe base para a execução de scripts, que são o encadeamento de uma lista pre definida de executors.
 * @author Marcus_Martins
 *
 */
public abstract class AbstractScriptExecutor implements CommandExecutor {

    protected CommandExecutorGroup group;
    
    /**
     * Lista de comandos que serão executados pelo script
     */
    protected List<CommandExecutor> commands;
    
    /**
     * Cria um novo script a partir da lista de comandos.    
     * @param commands
     */
    protected AbstractScriptExecutor(CommandExecutor... commands) {
        this.commands = Arrays.asList(commands);
        
        for (CommandExecutor commandExecutor : commands) {
            commandExecutor.setCommandExecutorGroup(getCommandExecutorGroup());
        }
    }
    
    /**
     * Unifica os paramtros de todos os comandos, evitando repetições.
     */
    public ParameterList getParametersTypes() {
        ParameterList ret = new ParameterList();
        
        for (CommandExecutor cmd: commands) {
            ParameterList cmdParameters = cmd.getParametersTypes();
            
            if (cmdParameters != null && !cmdParameters.isEmpty()) {
                for (ParameterDef parameterDef : cmdParameters) {
                    if (!ret.contains(parameterDef)) {
                        ret.add(parameterDef);
                    }
                }
            }
        }
        return ret;
    }
    
    /**
     * Execujta os comandos na sequencia definida pela lista.
     */
    public void execute(Map<String, ?> parameters, ExecutionLoggerListener listener) throws ParameterNotSetException, ExecutionException {
        long t1 = System.currentTimeMillis();
        
        if (listener != null) {
            listener.log("+--------------------------------------------------------------------");
            listener.log("| Começando a execução dos scripts");
            listener.log("+--------------------------------------------------------------------");
        }
        for (CommandExecutor cmd: commands) {
            cmd.execute(parameters, listener);
        }
        
        if (listener != null) {
            listener.log("+--------------------------------------------------------------------");
            listener.log("| Finalizou os scripts em [" + ((System.currentTimeMillis() - t1)/(float)(1000*60)) +"] minutos ");
            listener.log("+--------------------------------------------------------------------");
        }
    }
    
    @Override
    public String toString() {
        return this.getName();
    }
    
    @Override
    public void setCommandExecutorGroup(CommandExecutorGroup group) {
        this.group = group;
        
        for (CommandExecutor commandExecutor : commands) {
            commandExecutor.setCommandExecutorGroup(group);
        }
    }

    @Override
    public CommandExecutorGroup getCommandExecutorGroup() {
        return this.group;
    }

}
