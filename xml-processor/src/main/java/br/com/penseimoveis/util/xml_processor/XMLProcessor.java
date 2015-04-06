package br.com.penseimoveis.util.xml_processor;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Hello world!
 *
 */
public class XMLProcessor extends DefaultHandler {
	
	private Stack<String> elementStack = new Stack<String>();

	private List<String> codigos = new ArrayList<String>();
	
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		this.elementStack.push(qName);
	}

	public void endElement(String uri, String localName, String qName) throws SAXException {
		this.elementStack.pop();
	}

	public void characters(char ch[], int start, int length) throws SAXException {
		if("codigo_anuncio_revenda".equals(currentElement())){
			String value = new String(ch, start, length).trim();
			
			codigos.add(value);
		}
	}
	
	
    private String currentElement() {
        return this.elementStack.peek();
    }

	public List<String> getCodigos() {
		return codigos;
	}
	
	public static List<String> process(String file) {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		try {
			InputStream xmlInput = new FileInputStream(file);

			SAXParser saxParser = factory.newSAXParser();
			XMLProcessor handler = new XMLProcessor();
			saxParser.parse(xmlInput, handler);
			
			return handler.getCodigos();
		} catch (Throwable err) {
			err.printStackTrace();
		}
		return null;
	}
}
