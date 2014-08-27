package br.com.pense.devtools.plugins.executor.command;

import java.util.Map;

import br.com.pense.devtools.model.ParameterList;
import br.com.pense.devtools.plugins.executor.exceptions.ExecutionException;
import br.com.pense.devtools.plugins.executor.exceptions.ParameterNotSetException;
import br.com.pense.devtools.plugins.executor.listener.ExecutionLoggerListener;

/**
 * Interface base que define as operações dos executores
 * 
 * @author Marcus_Martins
 *
 */
public interface CommandExecutor {

    /**
     * Executa o comando 
     * @param parameters Parametros do comando
     * @param listener listener para log
     * @throws ParameterNotSetException Caso um parametro nao existe
     * @throws ExecutionException Erro na execução do comando
     */
    void execute(Map<String, ?> parameters, ExecutionLoggerListener listener) throws ParameterNotSetException, ExecutionException;
    
    /**
     * Define os parametros e os tipos necessários para executar o comando.
     * @return
     */
    ParameterList getParametersTypes();
    
    /**
     * Identificação do executor.
     * @return
     */
    String getName();
    
    void setCommandExecutorGroup(CommandExecutorGroup group);
    
    CommandExecutorGroup getCommandExecutorGroup();

}