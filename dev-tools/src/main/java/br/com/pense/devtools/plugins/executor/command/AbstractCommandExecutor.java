package br.com.pense.devtools.plugins.executor.command;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import br.com.pense.devtools.model.ParameterDef;
import br.com.pense.devtools.plugins.executor.command.maven.MavenPenseCommandExecutorGroup;
import br.com.pense.devtools.plugins.executor.exceptions.ExecutionException;
import br.com.pense.devtools.plugins.executor.exceptions.ParameterNotSetException;
import br.com.pense.devtools.plugins.executor.listener.ExecutionLoggerListener;

/**
 * Classe base para todos os executors
 * @author Marcus_Martins
 *
 */
public abstract class AbstractCommandExecutor implements CommandExecutor {

    private CommandExecutorGroup group;
    
    
    @Override
    public void setCommandExecutorGroup(CommandExecutorGroup group) {
        this.group = group;        
    }
    
    @Override
    public CommandExecutorGroup getCommandExecutorGroup() {
        return group;
    }
    /**
     * Garante que o parametro tenha valor
     * 
     * @param name
     * @param parameters
     * @param klass
     * @return
     * @throws ParameterNotSetException
     */
    protected <K> K getValidValue(ParameterDef param, Map<String, ?> parameters, Class<K> klass) throws ParameterNotSetException {
        K ret = null;
        if (parameters.containsKey(param.getKey())) {
            try {
                Object obj = parameters.get(param.getKey());
                ret = klass.cast(obj);
            } catch (ClassCastException ex) {
                throw new ParameterNotSetException(param);
            }
        }
    
        if (ret == null || StringUtils.isEmpty(ret.toString())) {
            if (klass.equals(Boolean.class)) {
                return klass.cast(false);
            } else {
                throw new ParameterNotSetException(param);
            }
        }
    
        return ret;
    }

    /**
     * Executa a linha de comando do maven e mostra o retorno atraves do listener
     * @param commandLine
     * @param listener
     * @throws ExecutionException
     */
    protected void executeCommandLineImpl(String commandLine, ExecutionLoggerListener listener) throws ExecutionException {
        long start = System.currentTimeMillis();
        
        try {
            Map<String, Object> config = this.getCommandExecutorGroup().loadParameters();

            Runtime rt = Runtime.getRuntime();
            Process pr = rt.exec(commandLine, null, new File(config.get(MavenPenseCommandExecutorGroup.WORK_DIR_CONFIG_KEY).toString()));
            
            if (listener != null) {
                BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));
                
                String line = null;
                while ((line = input.readLine()) != null) {
                    listener.log(line);
                }
            }
            
            int exitVal = pr.waitFor();
            
            if (listener != null) {
                listener.log("\n\n Processo da class [" + this.getClass().getName() + "] executado em [" + (System.currentTimeMillis() - start) + "] ms com o retorno [" + exitVal + "]");
            }
            
        } catch (Exception e) {
            throw new ExecutionException(e);
        }
    }

    /**
     * Troca os keys do map por seus valores, onde aparecerem na string.
     * @param commandLine
     * @param vals
     * @return
     */
    protected String replaceParamters(String commandLine, Map<String, Object> vals) {
        String ret = commandLine;
        if (vals != null && !vals.isEmpty()) {
            Iterator<String> keys = vals.keySet().iterator();
            while (keys.hasNext()) {
                String k = keys.next();
                String obj = vals.get(k) + "";
                
                int ctrlLoop = 0;
                while (ret.contains(k)) {
                    ret = ret.replace(k, obj);
                    
                    ctrlLoop++;
                    if (ctrlLoop > 100) {
                        throw new RuntimeException("Tentou trocar a string [" + k + "] em [" + ret + "] mais de 100 vezes. Possível loop infinito.");
                    }
                }
                
            }
        }
        return ret;
    }

    /**
     * Cria um novo mapa de parametros previamente configurado
     * @return
     */
    protected Map<String, Object> createParameterMap() {
        return new HashMap<String, Object>();
    }

    @Override
    public String toString() {
        return this.getName();
    }

}