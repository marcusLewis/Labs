package br.com.pense.devtools.plugins.executor.command.maven;

import br.com.pense.devtools.model.ParameterList;
import br.com.pense.devtools.plugins.executor.command.AbstractCommandExecutorGroup;

public class MavenPenseCommandExecutorGroup extends AbstractCommandExecutorGroup {
    
    public static final String WORK_DIR_CONFIG_KEY = "workdir";
    public static final String MVN_CMD_CONFIG_KEY = "mavencmd";
    
    
    public MavenPenseCommandExecutorGroup() {
        //@formatter:off
        super("Maven Pense"
            , "config-maven-pense-executors.properties"
            , new ParameterList()
                .addParameter(WORK_DIR_CONFIG_KEY, "Diretório da Workspace", String.class)
                .addParameter(MVN_CMD_CONFIG_KEY, "Caminho do Maven (mvn.bat)", String.class)
            , new PenseNewVersionScriptExecutor()
            , new MavenSetVersionExecutor()
            , new MavenCommitVersionExecutor()
            , new MavenGoalExecutor()
        );
        //@formatter:on
    }
    
}
