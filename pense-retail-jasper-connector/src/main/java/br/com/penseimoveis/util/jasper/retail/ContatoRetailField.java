package br.com.penseimoveis.util.jasper.retail;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import br.com.penseimoveis.util.jasper.JasperField;

public enum ContatoRetailField {
	DATA("DATA", new JasperField("DATA", "Data e hora da mensagem", Date.class)),
	CODIGOANUNCIO("CODIGOANUNCIO", new JasperField("CODIGOANUNCIO", "Código do Anuncio", Long.class)),
	FINALIDADE("FINALIDADE", new JasperField("FINALIDADE", "Finalidade do Anuncio")),
	NOME("NOME", new JasperField("NOME", "Rementente da mensagem")),
	EMAIL("EMAIL", new JasperField("EMAIL", "Email do Remetentee")),
	TELEFONE("TELEFONE", new JasperField("TELEFONE", "Telefone do Remetente")),
	MENSAGEM("MENSAGEM", new JasperField("MENSAGEM", "Mensagem"));
	
	
	private String key;
	private JasperField field;
	
	private ContatoRetailField(String name, JasperField field) {
		this.key = name;
		this.field = field;
	}

	public String getKey() {
		return key;
	}

	public JasperField getField() {
		return field;
	}
	
	
	public static JasperField[] getFields() {
		List<JasperField> ret = new ArrayList<JasperField>();
		
		for (ContatoRetailField f: values()) {
			ret.add(f.getField());
		}
		
		return ret.toArray(new JasperField[ret.size()]);
	}
	
}
