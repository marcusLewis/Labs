package br.com.penseimoveis.util.xml_processor;

import com.mongodb.AggregationOutput;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

public class DespublicarDuplicados {

	public static void main(String[] args) throws Exception {
		boolean ehParaDespublicar = false ;
		Long idCliente = 49279L;
		Long t1 = System.currentTimeMillis();
		
		
		// Cria os filtros
		DBObject matchParameters = new BasicDBObject();
		matchParameters.put("idCliente", idCliente);
		matchParameters.put("statusAnuncioRetail", "A");
		
		DBObject match = new BasicDBObject("$match", matchParameters);

		// monta o group by
		DBObject _idObject = new BasicDBObject();
		_idObject.put("codigoImobiliaria", "$codigoImobiliaria");
		_idObject.put("idContrato", "$publicacao.idContrato");

		DBObject groupFields = new BasicDBObject( "_id", _idObject);
		groupFields.put("count", new BasicDBObject( "$sum", 1));
		
		DBObject group = new BasicDBObject("$group", groupFields);

		// monta o projection
		DBObject fields = new BasicDBObject();
		fields.put("_id", 0);
		fields.put("codigoImobiliaria", "$_id.codigoImobiliaria");
		fields.put("idContrato", "$_id.idContrato");
		fields.put("count", 1);

		DBObject project = new BasicDBObject("$project", fields );


		// run aggregation
		DB db = MongodbConn.getMongoDB();
		DBCollection table = db.getCollection("anuncioMinhaAreaCompletoDocument");
		
		AggregationOutput output = table.aggregate(match, group, project);

		int idx = 0;
		for (DBObject result : output.results()) {
			if (((Number)result.get("count")).intValue() > 1) {
				Integer idContrato = ((Number)result.get("idContrato")).intValue();
				String codigoImobiliaria = result.get("codigoImobiliaria").toString();
				
				System.out.println(idx++ + " - " + idContrato +":"+codigoImobiliaria);
				
				// pega os anuncios por status
				BasicDBObject searchQuery = new BasicDBObject();
				searchQuery.put("codigoImobiliaria", codigoImobiliaria);
				searchQuery.put("publicacao.idContrato", idContrato);
				
				BasicDBObject sortQuery = new BasicDBObject();
				sortQuery.put("codigoAnuncio", -1);
			 
				DBCursor cursor = table.find(searchQuery).sort(sortQuery);
				int idxCursor = 0;
				while (cursor.hasNext()) {
					DBObject obj = cursor.next();
					
					if (obj.get("codigoAnuncio") != null) {
						Long codigoAnuncio = ((Number)obj.get("codigoAnuncio")).longValue();
						if (ehParaDespublicar && idxCursor > 0) {
							System.out.println("    >>>>>>>>> " + codigoAnuncio + " - Marcando para despublicacao");
							MongodbConn.despublicarAnuncio(table, codigoAnuncio);
						} else {
							System.out.println("    >>>>>>>>> " + codigoAnuncio);
						}
					}
					idxCursor++;
				}
			}
		}
		
		System.out.println(">::: Processou em [" + (System.currentTimeMillis()-t1) + "] ms");
	}
	
}
