package br.com.pense.devtools.plugins.executor.exceptions;

/**
 * Classe para identificar as exeções vinculadas as execuções dos scripts
 * 
 * @author Marcus_Martins
 *
 */
public class ExecutionException extends Exception {
    private static final long serialVersionUID = 1L;

    public ExecutionException(Throwable cause) {
        super(cause);
    }
}
