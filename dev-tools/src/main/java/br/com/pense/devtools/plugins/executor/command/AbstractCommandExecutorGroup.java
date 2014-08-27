package br.com.pense.devtools.plugins.executor.command;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import br.com.pense.devtools.model.ParameterList;

public abstract class AbstractCommandExecutorGroup implements CommandExecutorGroup {

    private static final String CLASS_SULFIX = ".class";
    private static final String VALUE_SULFIX = ".value";
    
    private CommandExecutor[] executors;

    private String            name;
    
    private String            configFileName;

    private ParameterList     config;

    public AbstractCommandExecutorGroup(String name, String configFileName, ParameterList config, CommandExecutor... executors) {
        this.name = name;
        this.configFileName = configFileName;
        this.config = config;
        this.executors = executors;
        
        for (CommandExecutor commandExecutor : executors) {
            commandExecutor.setCommandExecutorGroup(this);
        }
    }
    
    @Override
    public Map<String, Object> loadParameters() {
        Properties properties = new Properties();
        Map<String, Object> ret = new HashMap<String, Object>();
        
        FileInputStream in = null;
        try {
            if ((new File(this.configFileName)).exists()) {
                in = new FileInputStream(this.configFileName);
                properties.load(in);
                
                Map<String, String> cacheClass = new HashMap<String, String>();
                Map<String, String> cacheValue = new HashMap<String, String>();
                
                for (Object k : properties.keySet()) {
                    String key = k.toString();
                    String paramName = getParameterName(key);
                    
                    if (key.endsWith(CLASS_SULFIX)) {
                        cacheClass.put(paramName, properties.getProperty(key, ""));
                    } else if (key.endsWith(VALUE_SULFIX)) {
                        cacheValue.put(paramName, properties.getProperty(key, ""));
                    }
                }
                
                for (String key : cacheClass.keySet()) {
                    String klass = cacheClass.get(key);
                    String value = cacheValue.get(key);
                    
                    if (String.class.getName().equalsIgnoreCase(klass)) {
                        ret.put(key, value);
                    } else if (Boolean.class.getName().equalsIgnoreCase(klass)) {
                        ret.put(key, Boolean.valueOf(value));
                    }
                }
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return ret;
    }
    
    private String getParameterName(String key) {
        if (key.endsWith(CLASS_SULFIX)) {
            return key.substring(0, key.indexOf(CLASS_SULFIX));
        } else if (key.endsWith(VALUE_SULFIX)) {
            return key.substring(0, key.indexOf(VALUE_SULFIX));
        } else {
            return key;
        }
    }
    
    @Override
    public void saveParameters(Map<String, Object> parameters) {
        Properties properties = new Properties();
        
        for (String key: parameters.keySet()) {
            Object k = parameters.get(key);
            
            properties.put(key + CLASS_SULFIX, k.getClass().getName());
            properties.put(key + VALUE_SULFIX, k.toString());
        }
        
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(this.configFileName);
            properties.store(out, "Configurações da classe " + this.getClass().getName() + "....");
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /* (non-Javadoc)
     * @see br.com.pense.devtools.plugins.executor.command.CommandExecutorGroup#getExecutors()
     */
    @Override
    public CommandExecutor[] getExecutors() {
        return executors;
    }

    /* (non-Javadoc)
     * @see br.com.pense.devtools.plugins.executor.command.CommandExecutorGroup#getName()
     */
    @Override
    public String getName() {
        return name;
    }

    /* (non-Javadoc)
     * @see br.com.pense.devtools.plugins.executor.command.CommandExecutorGroup#getConfig()
     */
    @Override
    public ParameterList getConfig() {
        return config;
    }

    @Override
    public String toString() {
        return getName();
    }

    public String getConfigFileName() {
        return configFileName;
    }

    public void setConfigFileName(String configFileName) {
        this.configFileName = configFileName;
    }

    public void setName(String name) {
        this.name = name;
    }

}