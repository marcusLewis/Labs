package br.com.rbs.pense.teste;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.net.URL;

public class SolrDownload {

    private static final String ANO_MES = "201407";
    
    
    /* PROD */
    //private static final String URL_IMOVEIS = "http://10.240.64.104:8080/solr/pense-imoveis-log_shard1_replica4/select?q=monthTS%3A" + ANO_MES + "&rows=1&wt=xml&indent=true&facet=true&facet.field=q_facet&facet.limit=1000000&facet.mincount=1";
    //private static final String URL_CARROS = "http://10.240.64.104:8080/solr/pense-carros-log_shard-1_replica1/select?q=monthTS%3A" + ANO_MES + "&rows=1&wt=xml&indent=true&facet=true&facet.field=q_facet&facet.limit=1000000&facet.mincount=1";
    
    /* LOCAL */
    private static final String URL_IMOVEIS = "http://localhost:18080/solr/pense-imoveis-log-bkp/select?q=monthTS%3A" + ANO_MES + "&rows=1&wt=xml&indent=true&facet=true&facet.field=q_facet&facet.limit=1000000&facet.mincount=1";
    private static final String URL_CARROS = "http://localhost:18080/solr/pense-carros-log-bkp/select?q=monthTS%3A" + ANO_MES + "&rows=1&wt=xml&indent=true&facet=true&facet.field=q_facet&facet.limit=1000000&facet.mincount=1";
    
    
    private static final String FILE_IMOVEIS = "consultas_imoveis.xml";
    private static final String FILE_CARROS = "consultas_carros.xml";
    

    
    
    public static void main(String[] args) throws Exception {
        processar(URL_CARROS, FILE_CARROS);
                
        processar(URL_IMOVEIS, FILE_IMOVEIS);
    }
    
       
    private static final void processar(String url, String file) throws Exception {

        long t1 = System.currentTimeMillis();
        
        BufferedInputStream in = null;
        FileOutputStream fout = null;
        try {
            in = new BufferedInputStream(new URL(url).openStream());
            fout = new FileOutputStream(file);

            
            
            final byte data[] = new byte[1024];
            int count;
            while ((count = in.read(data, 0, 1024)) != -1) {
                fout.write(data, 0, count);
            }
        } finally {
            if (in != null) {
                in.close();
            }
            if (fout != null) {
                fout.close();
            }
        }
        
        System.out.println(">>> excutou o arquivo [" + file + "] em [" + (System.currentTimeMillis()-t1) +"] ms");
    }
    

}
