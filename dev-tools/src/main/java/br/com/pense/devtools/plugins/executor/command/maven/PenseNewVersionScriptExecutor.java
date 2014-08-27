package br.com.pense.devtools.plugins.executor.command.maven;

import br.com.pense.devtools.plugins.executor.command.AbstractScriptExecutor;

public class PenseNewVersionScriptExecutor extends AbstractScriptExecutor {

    public PenseNewVersionScriptExecutor() {
        super(new MavenSetVersionExecutor(), new MavenCommitVersionExecutor(), new MavenGoalExecutor());
    }
    
    
    public String getName() {
        return "Full Build para uma nova Branch do Pense";
    }
}
