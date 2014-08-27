package br.com.pense.devtools.model;

import java.util.ArrayList;

/**
 * Classe utilitaria que define uma lista ordenada de parametros.
 * @author Marcus_Martins
 *
 */
public class ParameterList extends ArrayList<ParameterDef> {
    private static final long serialVersionUID = 1L;

    /**
     * Adiciona um nome parametro a partir de um nome e tipo
     * @param name
     * @param type
     * @return
     */
    public ParameterList addParameter(String key, String name, Class<?> type) {
        ParameterDef def = new ParameterDef(key, name, type);
        return this.addParameter(def);
    }
    
    /**
     * Adiciona um novo parametro na lista
     * @param p
     * @return
     */
    public ParameterList addParameter(ParameterDef p) {
        this.add(p);
        return this;
    }
    
}
