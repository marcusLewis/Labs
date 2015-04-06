package br.com.penseimoveis.util.xml_processor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

public class Comparador {
	
	public static void main1(String[] args) throws Exception {
		
		DB db = getMongoDB();
		DBCollection table = db.getCollection("anuncioMinhaAreaCompletoDocument");
		
		BasicDBObject query = new BasicDBObject();
		query.put("codigoAnuncio", 7364800L);

		BasicDBObject newDocument = new BasicDBObject();
		newDocument.put("statusAnuncioRetail", "I");
	 
		BasicDBObject updateObj = new BasicDBObject();
		updateObj.put("$set", newDocument);
	 
		table.update(query, updateObj);
	}
	
	
	private static void despublicarAnuncio(DB db, long anuncioId) {
		BasicDBObject query = new BasicDBObject();
		query.put("codigoAnuncio", anuncioId);

		BasicDBObject newDocument = new BasicDBObject();
		newDocument.put("statusAnuncioRetail", "I");
	 
		BasicDBObject updateObj = new BasicDBObject();
		updateObj.put("$set", newDocument);
	 
		DBCollection table = db.getCollection("anuncioMinhaAreaCompletoDocument");
		table.update(query, updateObj);
		
		System.out.println(">>> Despublica [" + anuncioId +"]");
	}
	
	private static DB getMongoDB() throws Exception { 
		MongoClient mongo = new MongoClient( "54.207.82.169" , 27017 );
		DB db = mongo.getDB("pense-imoveis");
		db.authenticate("app", "pense@2013".toCharArray());
		return db;
	}
	
	public static Map<Long, Map<String, Long>> getAnunciosPublicados(DB db, Long idCliente) throws Exception {
		Map<Long, Map<String, Long>> mapAnunciosDoContrato = new HashMap<Long, Map<String,Long>>();
		DBCollection table = db.getCollection("anuncioMinhaAreaCompletoDocument");
		 
		BasicDBObject searchQuery = new BasicDBObject();
		searchQuery.put("idCliente", idCliente);
		searchQuery.put("statusAnuncioRetail", "A");
	 
		DBCursor cursor = table.find(searchQuery);
		while (cursor.hasNext()) {
			DBObject obj = cursor.next();
			
			Long contratoId = null;
			String codigoRevenda = null;
			Long codigoAnuncio = null;
			
			if (obj.get("codigoImobiliaria") != null) {
				codigoRevenda = obj.get("codigoImobiliaria").toString(); 
			}

			if (obj.get("codigoAnuncio") != null) {
				codigoAnuncio = new Long(obj.get("codigoAnuncio").toString()); 
			}

			if (obj.get("publicacao") != null && obj.get("publicacao") instanceof DBObject) {
				DBObject tmp = (DBObject)obj.get("publicacao");
				
				if (tmp.get("idContrato") != null) {
					contratoId = new Long(tmp.get("idContrato").toString());
				}
			}

			if (codigoRevenda != null && codigoAnuncio != null && contratoId != null) {
				Map<String, Long> anunciosDoContrato = mapAnunciosDoContrato.get(contratoId);
				if (anunciosDoContrato == null) {
					anunciosDoContrato = new HashMap<String, Long>();
					mapAnunciosDoContrato.put(contratoId, anunciosDoContrato);
				}
				
				anunciosDoContrato.put(codigoRevenda, codigoAnuncio);
			}
			
		}
		
		return mapAnunciosDoContrato;		
	}

	public static void main(String[] args) throws Exception {
		Set<Long> contratosParaExcluir = new HashSet<Long>();
		boolean ehParaDespublicar = false;
		DB db = getMongoDB();
		
		System.out.println("Buscando anuncios do mongo....");
		long t1 = System.currentTimeMillis();
		
		// processa os dados do mongo ------------------------------------------------------------------------------------------------------------
		Map<Long, Map<String, Long>> mapAnunciosDoContrato = getAnunciosPublicados(db, 49279L);
		System.out.println("Buscou os anuncios em [" + (System.currentTimeMillis()-t1) + "] ms");
		
		
		// le os anucnios dos xml
		Iterator<Long> iterContratos = mapAnunciosDoContrato.keySet().iterator();
		while (iterContratos.hasNext()) {
			Long contratoId = iterContratos.next();
			System.out.println("  Contrato [" + contratoId + "]..........................................................................................");
			
			File f = new File("xmls\\credito-anuncios-" + contratoId + ".xml");
			Map<String, Long> anunciosDoContrato = mapAnunciosDoContrato.get(contratoId);
			
			if (f.exists()) {
				System.out.println("       Processando anuncios do do xml [" + f.getAbsolutePath() +"]....");
				t1 = System.currentTimeMillis();
				List<String> codigosXML = XMLProcessor.process(f.getPath());
				System.out.println("       Processou os [" + codigosXML.size() + "] anuncios do xml em [" + (System.currentTimeMillis()-t1) + "] ms.");
				
				if (codigosXML != null && !codigosXML.isEmpty()) {
					for (String strCodigo : codigosXML) {
						anunciosDoContrato.remove(strCodigo.trim().toUpperCase());
					}
				}
				
				System.out.println("  contrato [" + contratoId + "] deve despublicar [" + anunciosDoContrato.size() + "] anuncios");
			} else {
				contratosParaExcluir.add(contratoId);
				System.out.println("  Sem xml para o contrato [" + contratoId + "][" + anunciosDoContrato.size() + "]");
			}
			System.out.println("  _______________________________________________________________________________________________________________________");
		}
		
		// remove os contratos sem xml ---------------------------------------------------------------------------------------------------------------
		for (Long l : contratosParaExcluir) {
			mapAnunciosDoContrato.remove(l);
		}
		
		
		// mostra o resultado ---------------------------------------------------------------------------------------------------------------
		FileWriter out = new FileWriter("anuncios-para-despulicar.txt");
		BufferedWriter writer = new BufferedWriter(out);
		
		iterContratos = mapAnunciosDoContrato.keySet().iterator();
		while (iterContratos.hasNext()) {
			Long contratoId = iterContratos.next();
			Map<String, Long> anunciosDoContrato = mapAnunciosDoContrato.get(contratoId);
			
			if (!anunciosDoContrato.isEmpty()) {
				Iterator<String> keys = anunciosDoContrato.keySet().iterator();
				while (keys.hasNext()) {
					String k = keys.next();
					writer.append("  contrato [" + contratoId + "] codigo revenda [" + k + "] codigo no pense [" + anunciosDoContrato.get(k) + "]\n");
					if (ehParaDespublicar) {
						despublicarAnuncio(db, anunciosDoContrato.get(k));
					}
				}
			}
		}
		
		writer.close();

		System.out.println(">>> fim");
	}
	
}
