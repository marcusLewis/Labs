package br.com.pense.devtools.plugins.executor.command;

import br.com.pense.devtools.plugins.executor.command.maven.MavenPenseAuthCommandExecutorGroup;
import br.com.pense.devtools.plugins.executor.command.maven.MavenPenseCommandExecutorGroup;
import br.com.pense.devtools.plugins.executor.command.maven.MavenPluginCommandExecutorGroup;


/**
 * Classe responsável por gerenciar os executors registrados no sistema. 
 * 
 * @author Marcus_Martins
 *
 */
public class CommandExecutorBuilder {

    /**
     * Retorna a lista de executors disponiveis
     * @return
     */
    public static CommandExecutorGroup[] getExecutorGroups() {
        //@formatter:off
        return new CommandExecutorGroup[] {
            new MavenPenseCommandExecutorGroup()
            , new MavenPluginCommandExecutorGroup()
            , new MavenPenseAuthCommandExecutorGroup()
        };
        //@formatter:on
    }
    
}
