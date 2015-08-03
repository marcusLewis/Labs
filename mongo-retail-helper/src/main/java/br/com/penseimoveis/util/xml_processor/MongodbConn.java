package br.com.penseimoveis.util.xml_processor;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;

public class MongodbConn {
	
	public static DB getMongoDB() throws Exception { 
		MongoClient mongo = new MongoClient( "54.207.82.169" , 27017 );
		DB db = mongo.getDB("pense-imoveis");
		db.authenticate("app", "pense@2013".toCharArray());
		return db;
	}
	
	public static void despublicarAnuncio(DBCollection table, long anuncioId) {
		BasicDBObject query = new BasicDBObject();
		query.put("codigoAnuncio", anuncioId);

		BasicDBObject newDocument = new BasicDBObject();
		newDocument.put("statusAnuncioRetail", "I");
	 
		BasicDBObject updateObj = new BasicDBObject();
		updateObj.put("$set", newDocument);

		table.update(query, updateObj);
		
		System.out.println(">>> Despublica [" + anuncioId +"]");
	}
}
