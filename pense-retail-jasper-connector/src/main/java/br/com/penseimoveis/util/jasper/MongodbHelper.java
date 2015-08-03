package br.com.penseimoveis.util.jasper;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import com.mongodb.DB;
import com.mongodb.MongoClient;

public class MongodbHelper {
	
	public static DB getMongoDB() { 
		Properties config = loadMongodbSettings();
		
		String host = config.getProperty("mongodb.host");
		String port = config.getProperty("mongodb.port");
		String database = config.getProperty("mongodb.database");
		String username = config.getProperty("mongodb.username");
		String password = config.getProperty("mongodb.password");	
		
		try {
			MongoClient mongo = new MongoClient( host , Integer.parseInt(port) );
			DB db = mongo.getDB(database);
			db.authenticate(username, password.toCharArray());
			return db;
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	
	private static Properties loadMongodbSettings() {
		Properties prop = new Properties();
		String fileName = "mongodb-settings.properties";
		
		InputStream in = MongodbHelper.class.getClassLoader().getResourceAsStream(fileName);
		
		if (in != null) {
			try {
				prop.load(in);
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}
			return prop;
		} else {
			File f = new File(fileName);
			
			if (f.exists()) {
				try {
					prop.load(new FileInputStream(f));
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
				return prop;
			} else {
				throw new RuntimeException("Arquivo [" + f.getAbsolutePath() + "] não encontrado!");
			}
		}
	}
	
}
