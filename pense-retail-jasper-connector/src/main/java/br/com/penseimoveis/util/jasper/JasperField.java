package br.com.penseimoveis.util.jasper;

import net.sf.jasperreports.engine.base.JRBaseField;

public class JasperField extends JRBaseField {
	private static final long serialVersionUID = 5466433364361927239L;
	
	
	public JasperField(String name, String description, Class<?> type) {
		this.name = name;
		this.description = description;
		this.valueClass = type;
		this.valueClassName = type.getName();
	}
	
	public JasperField(String name, String description) {
		this(name, description, String.class);
	}

}
