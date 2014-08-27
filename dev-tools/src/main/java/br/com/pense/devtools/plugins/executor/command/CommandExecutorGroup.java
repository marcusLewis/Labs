package br.com.pense.devtools.plugins.executor.command;

import java.util.Map;

import br.com.pense.devtools.model.ParameterList;

public interface CommandExecutorGroup {

    CommandExecutor[] getExecutors();

    String getName();

    ParameterList getConfig();
    
    Map<String, Object> loadParameters();
    
    void saveParameters(Map<String, Object> parameters);
    
    

}