package br.com.pense.devtools.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Define um parametro do sistema atraves de um tipo (class) e um nome
 * 
 * @author Marcus_Martins
 *
 */
public class ParameterDef {


    /**
     * Mnemonico de acesso
     */
    private String   key;

    /**
     * Nome do Parametro
     */
    private String   title;
    /**
     * Tipo do Parametro
     */
    private Class<?> type;

    /**
     * Instancia um novo parametro com nome e tipo
     * @param _name
     * @param _type
     */
    public ParameterDef(String _key, String _title, Class<?> _type) {
        super();

        this.key = _key;
        this.title = _title;
        this.type = _type;
    }

    
    /**
     * Retorna o nome do parametro
     * @return
     */
    public String getKey() {
        return key;
    }
    
    /**
     * Retorna o nome do parametro
     * @return
     */
    public String getTitle() {
        return title;
    }
    
    /**
     * Retorna o tipo do parametro
     * @return
     */
    public Class<?> getType() {
        return type;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof ParameterDef)) return false;
        
        ParameterDef other = (ParameterDef)obj;
        
        EqualsBuilder builder = new EqualsBuilder();
        builder.append(getKey(), other.getKey());
        builder.append(getTitle(), other.getTitle());
        builder.append(getType(), other.getType());
        return builder.build();
    }

    @Override
    public int hashCode() {
        HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(getKey());
        builder.append(getTitle());
        builder.append(getType());
        return super.hashCode();
    }

}
