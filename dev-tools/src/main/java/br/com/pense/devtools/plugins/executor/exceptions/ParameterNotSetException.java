package br.com.pense.devtools.plugins.executor.exceptions;

import br.com.pense.devtools.model.ParameterDef;

/**
 * @author Marcus_Martins
 *
 */
public class ParameterNotSetException extends Exception {
    private static final long serialVersionUID = 1L;

    public ParameterNotSetException(ParameterDef _cause) {
        super("Parametro [" + _cause.getTitle() +"] do tipo [" + _cause.getType().getName() + "] sem valor válido");
    }
    
}
