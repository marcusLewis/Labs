package br.com.pense.devtools.plugins.executor.command.maven;

public class MavenPluginCommandExecutorGroup extends MavenPenseCommandExecutorGroup {

    
    public MavenPluginCommandExecutorGroup() {
        super();
        setName("Maven Plugins");
        setConfigFileName("config-maven-plugin-executors.properties");
    }
    
    
}
