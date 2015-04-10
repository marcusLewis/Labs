package br.com.penseimoveis.util.jasper.retail;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

import com.mongodb.DB;

public class ContatoRetailDataSource implements JRDataSource {

	private DB mongodb;
	private boolean inicializado = false;
	
	private int  i = 0;
	
	public ContatoRetailDataSource(DB conn) {
		this.mongodb = conn;
		System.out.println(">>>>> new " + conn);
	}
	
	public boolean next() throws JRException {
	    if (i < 5) {
	        i++;
	        return true;
	    } else {
	        return false;
	    }
	}

	public Object getFieldValue(JRField jrField) throws JRException {
	    System.out.println(">>> "+ jrField);
		return "TESTE";
	}
	
	
	public static ContatoRetailDataSource newInstance() {
	    System.out.println("><>>> factory");
	    return new ContatoRetailDataSource(null);
	}
}
