package br.com.rbs.pense.solr.querytools;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 */
public class App implements ActionListener {
    private static final String                 QUERY_FILE       = "query.txt";
    private static final String                 SULFIX_PARAMETRO = "_parameter";

    // private static final String SOLR_URL =
    // "http://10.240.64.104:8080/solr/pense-imoveis-venda/select";
    private static final String                 SOLR_URL         = "http://10.240.64.105:8080/solr/pense-imoveis-venda_shard1_replica1/select";
    
    
    //private static final String                 SOLR_URL         = "http://localhost:18080/solr/pense-imoveis-venda/select";
    
    //private static final String                 SOLR_URL         = "http://10.240.64.105:8080/solr/pense-imoveis-retail_shard1_replica2/select";
    

    
    
    //private static final String                 SOLR_URL         = "http://localhost:18080/solr/pense-carros-carros/select";
    //private static final String                 SOLR_URL         = "http://localhost:18080/solr/pense-solr-teste/select";

    //private static final String                 ANUNCIO_URL      = "http://delorean:28080/desenv/pense-carros/anuncio/carros/";

    // homolo
    //private static final String                 SOLR_URL         = "http://10.243.0.94:8080/solr/pense-imoveis-aluguel_shard1_replica2/select";
    //private static final String                 ANUNCIO_URL      = "http://veloster:28080/homo/pense-imoveis/anuncio/aluguel/";

    //private static final String                 SOLR_URL         = "http://10.243.0.94:8080/solr/pense-imoveis-aluguel_shard1_replica2/select";
    //private static final String                 ANUNCIO_URL      = "http://veloster:28080/homo/pense-imoveis/anuncio/aluguel/";

    
    
    
    // prod
    private static final String                 ANUNCIO_URL      =  "http://veloster:28080/pense-imoveis/anuncio/venda/";
    //private static final String                 ANUNCIO_URL      =  "http://veloster:28080/pense-imoveis/anuncio/aluguel/";
    //private static final String                 SOLR_URL         = "http://10.240.64.105:8080/solr/pense-imoveis-aluguel_shard1_replica3/select";

    // local
    //private static final String                 ANUNCIO_URL      = "http://veloster:28080/desenv/pense-imoveis/anuncio/venda/";
    //private static final String                 SOLR_URL         = "http://localhost:18080/solr/pense-imoveis-venda/select";
    
    //private static final String                 ANUNCIO_URL      = "http://veloster:28080/desenv/pense-imoveis/anuncio/aluguel/";
    //private static final String                 SOLR_URL         = "http://localhost:18080/solr/pense-imoveis-aluguel/select";



    private JLabel                              lblUrl           = new JLabel("   URL:");
    private JTextField                          txtUrl           = new JTextField();
    private JTextArea                           txtJson          = new JTextArea("");
    private JButton                             bttQuery         = new JButton("Gerar");
    private JTabbedPane                         tabPanel         = new JTabbedPane();
    private DefaultMutableTreeNode              root             = new DefaultMutableTreeNode("root");
    private JTree                               jtree            = new JTree(root);

    private static NumberFormat                 nFormatter       = NumberFormat.getNumberInstance();
    
    private static NumberFormat                 nScoreFormatter  = NumberFormat.getNumberInstance();
    
    private Map<String, DefaultMutableTreeNode> cacheNodos       = new HashMap<String, DefaultMutableTreeNode>();

    private static final Map<String, Object>    VALORES_DEFAULT  = new HashMap<String, Object>();

    static {
        nScoreFormatter.setMinimumFractionDigits(1);
        nScoreFormatter.setMaximumFractionDigits(4);
        
        VALORES_DEFAULT.put("wt", "json");
        VALORES_DEFAULT.put("indent", "true");
        VALORES_DEFAULT.put("debugQuery", "true");
        VALORES_DEFAULT.put("debug.explain.structured", "true");
        VALORES_DEFAULT.put("version", "");
        //VALORES_DEFAULT.put("fl", "tituloFinalidade, textSearch, codigoAnuncio, score, nomeFantasia");
        VALORES_DEFAULT.put("fl", "qtdFotosContratadas, percentualDados, percentualDescricao, percentualContato, percentualCaracteristicas, percentualLocalizacao, percentualFotos, percentualVideo, ano, tituloFinalidade, textSearch, opcionais, categorias, tituloFinalidade, subtitulo, codigoAnuncio, nomeFantasia, descricao, dataPublicacao, score, midiaPossueFotos, possuiPreco, lancamento, possueFotos, precoValor, percentualPreechimento");
        //VALORES_DEFAULT.put("fl", "tituloFinalidade, bairro, subtitulo, codigoAnuncio, descricao, dataPublicacao, score, midiaPossueFotos, possuiPreco, lancamento, possueFotos, precoValor");

        // VALORES_DEFAULT.put("bf",
        // "recip(ms(NOW,dataPublicacao),3.16e-11,1,1)^30");
        // VALORES_DEFAULT.put("bf",
        // "product(possuiPreco,10) ms(NOW, dataPublicacao) ");

        // VALORES_DEFAULT.put("bf", "recip(ano,1,-2000,2000)^-30 ");

        // VALORES_DEFAULT.put("bf",
        // "product(midiaPossueFotos,10) product(possuiPreco,20) linear(ano,1,-2000)^50");

        // VALORES_DEFAULT.put("bf",
        // "linear(ano,1,-2000)^100 product(possuiPreco,70) recip(precoValor,1,1000,-1000)^50");

        // VALORES_DEFAULT.put("bf",
        // "linear(ano,1,-2000)^40 recip(product(possuiPreco,precoValor),1,1000,1000)^30 ");

        //QQQQQQ
        //VALORES_DEFAULT.put("bf", "product(linear(ano,1,-1900),10000) product(possuiPreco,5000) recip(precoValor,1,1000,1000)");
        
        ///VALORES_DEFAULT.put("bf", "product(linear(ano,1,-1900),10000000) product(possuiPreco,5000000) product(ceil(product(recip(precoValor,1,1000,1000),1000)),10000) ");
        
        
        // QUASE
        //VALORES_DEFAULT.put("bf", "linear(ano,1,-1900)^10000 product(possuiPreco,7000) recip(precoValor,1,1000,1000)^5000 product(midiaPossueFotos,2)");
        // QUSE V2
        //VALORES_DEFAULT.put("bf", "linear(ano,1,-1900)^10000 product(possuiPreco,5000) recip(precoValor,1,1000,1000)^1000 ");

        //VALORES_DEFAULT.put("bf", "product(div(ceil(product(abs(sub(recip(ano,1,1000,1000),1)),10000)),10000),1000000000)");
        
        // preço
        // VALORES_DEFAULT.put("bf",
        // "product(possuiPreco,30) recip(precoValor,1,1000,1000)^10");

    }

    public App() {
        File input = new File(QUERY_FILE);
        FileInputStream fIn = null;

        nFormatter.setMinimumFractionDigits(0);
        nFormatter.setMaximumFractionDigits(10);

        try {
            fIn = new FileInputStream(input);
            StringBuffer sb = new StringBuffer();

            while (fIn.available() > 0) {
                byte[] b = new byte[fIn.available()];
                fIn.read(b);
                sb.append(new String(b));
            }

            txtJson.setText(sb.toString());
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (fIn != null) {
                try {
                    fIn.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }

        tabPanel.addTab("Input", createInputPanel());
        tabPanel.addTab("Resultados", createResultsPanel());

        JFrame frmMain = new JFrame("Pense: Solr Query Tools");
        frmMain.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frmMain.setSize(800, 600);
        frmMain.setContentPane(tabPanel);
        frmMain.setVisible(true);

    }

    private JPanel createResultsPanel() {
        JPanel ret = new JPanel(new BorderLayout());
        jtree.setShowsRootHandles(false);

        ret.add(new JScrollPane(jtree), BorderLayout.CENTER);

        return ret;
    }

    private JPanel createInputPanel() {
        lblUrl.setPreferredSize(new Dimension(150, 18));
        bttQuery.addActionListener(this);

        JPanel pnlUrlLayout = new JPanel(new BorderLayout());
        pnlUrlLayout.add(lblUrl, BorderLayout.WEST);
        pnlUrlLayout.add(txtUrl, BorderLayout.CENTER);

        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlButtons.add(bttQuery);

        JPanel pnlQuery = new JPanel(new BorderLayout());
        pnlQuery.add(new JScrollPane(txtJson));
        pnlQuery.add(pnlButtons, BorderLayout.SOUTH);

        JPanel pnlInput = new JPanel(new BorderLayout());
        pnlInput.add(pnlQuery, BorderLayout.CENTER);
        pnlInput.add(pnlUrlLayout, BorderLayout.SOUTH);
        return pnlInput;
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == bttQuery) {
            parseJson();
        }
    }

    private void parseJson() {
        JSONParser parser = new JSONParser();
        root.removeAllChildren();
        try {
            JSONObject jsonObject = (JSONObject) parser.parse(txtJson.getText());
            Map<String, Object> map = new HashMap<String, Object>();

            @SuppressWarnings("unchecked")
            Iterator<Object> iter = jsonObject.keySet().iterator();
            while (iter.hasNext()) {
                Object item = iter.next();

                if (item != null && item.toString().endsWith(SULFIX_PARAMETRO)) {
                    String k = item.toString();
                    String chave = k.substring(0, k.length() - SULFIX_PARAMETRO.length());
                    Object valor = jsonObject.get(k);

                    if (valor != null) {
                        map.put(chave, valor);
                    }
                }
            }

            // sobrescreve os parametros default
            Iterator<String> iterStrings = VALORES_DEFAULT.keySet().iterator();
            while (iterStrings.hasNext()) {
                String k = iterStrings.next();
                Object v = VALORES_DEFAULT.get(k);

                if (v != null && !"".equalsIgnoreCase(v.toString())) {
                    map.put(k, v);
                } else {
                    map.remove(k);
                }
            }

            // monta a query para execucao
            List<NameValuePair> itens = new ArrayList<NameValuePair>();
            Iterator<String> tmpKeys = map.keySet().iterator();
            while (tmpKeys.hasNext()) {
                String tmpItemKey = tmpKeys.next();
                Object obj = map.get(tmpItemKey);

                if (obj instanceof Collection) {
                    @SuppressWarnings("unchecked")
                    Collection<Object> c = (Collection<Object>) obj;

                    for (Iterator<Object> it = c.iterator(); it.hasNext();) {
                        Object object = (Object) it.next();

                        if (object != null) {
                            if ("collection".equalsIgnoreCase(tmpItemKey)) {
                                if (!object.toString().toLowerCase().contains("_shard")) {
                                    itens.add(new BasicNameValuePair(tmpItemKey, object.toString()));
                                }
                            } else {
                                itens.add(new BasicNameValuePair(tmpItemKey, object.toString()));
                            }
                        }
                    }
                } else {
                    itens.add(new BasicNameValuePair(tmpItemKey, obj.toString()));
                }
            }

            txtUrl.setText(SOLR_URL + "?" + URLEncodedUtils.format(itens, "UTF-8"));

            JSONObject json = executeQuery(SOLR_URL, itens.toArray(new NameValuePair[itens.size()]));
            processNode(json, "", root);

            jtree.updateUI();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void processArray(JSONArray arr, Object nome, String ident, DefaultMutableTreeNode parent) {
        for (int i = 0; i < arr.size(); i++) {
            Object o = arr.get(i);
            // System.out.println(ident + ">>> [" + i + "][" + (o == null ?
            // "null" : o.getClass().getName()) + "]");
            if (o instanceof JSONObject) {
                JSONObject obj = (JSONObject) o;

                DefaultMutableTreeNode novo = null;

                if (parent != null) {
                    StringBuffer sb = new StringBuffer();

                    if (obj.containsKey("ano")) {
                        sb.append("  Ano [" + obj.get("ano") + "]");
                    }

                    if (obj.containsKey("precoValor")) {
                        sb.append("    Valor [" + obj.get("precoValor") + "]");
                    }

                    if (obj.containsKey("lancamento")) {
                        sb.append("    lancamento [" + obj.get("lancamento") + "]");
                    }

                    String cdAnuncio = null;
                    if (obj.containsKey("codigoAnuncio")) {
                        sb.append("    codigoAnuncio [" + obj.get("codigoAnuncio") + "]");
                        cdAnuncio = obj.get("codigoAnuncio").toString();
                        
                        if ("6683989".equals(cdAnuncio)) {
                            sb.append("    ******** RECANTO ***********");
                        }
                        
                        if ("6736066".equals(cdAnuncio)) {
                            sb.append("    ******** V PRIME ***********");
                        }
                        
                        if ("6861964".equals(cdAnuncio)) {
                            sb.append("    ******** FOXTER ***********");
                        }

                        if ("6943889".equals(cdAnuncio)) {
                            sb.append("    ******** NATAL ***********");
                        }
                        
                        if ("3731608".equals(cdAnuncio)) {
                            sb.append("    ******** TESTE DIEGUITO ***********");
                        }
                        if ("3577515".equals(cdAnuncio)) {
                            sb.append("    ******** TESTE DIEGUITO ***********");
                        }
                        
                        if ("3798930".equals(cdAnuncio)) {
                            sb.append("    ******** TESTE CINTIA ***********");
                        }
                        if ("1876428".equals(cdAnuncio)) {
                            sb.append("    ******** TESTE CINTIA ***********");
                        }
                    }
                    
                    
                    if (obj.containsKey("nomeFantasia")) {
                        String tmp = obj.get("nomeFantasia").toString().toUpperCase();
                        /*if (tmp.startsWith("V PRIME")) {
                            sb.append("    ******** V PRIME - TESTE ***********");
                        }
                        
                        if (tmp.startsWith("RECANTO")) {
                            sb.append("    ******** RECANTO - TESTE ***********");
                        }*/
                        
                        if (tmp.startsWith("PRIVATE")) {
                            sb.append("    ******** PRIVATE - TESTE ***********");
                        }
                    }
                    
                    if (obj.containsKey("midiaPossueFotos")) {
                        sb.append("    Fotos [" + obj.get("midiaPossueFotos") + "]");
                    }
                    
                    if (obj.containsKey("dataPublicacao")) {
                        sb.append("    DataPub [" + obj.get("dataPublicacao") + "]");
                    }
                    
                    if (obj.containsKey("score")) {
                        sb.append("    Score [" +  nScoreFormatter.format((Number)obj.get("score")) + "]");
                    }
                    
                    if (obj.containsKey("subtitulo")) {
                        sb.append("    subtitulo [" +  obj.get("subtitulo") + "]");
                    }

                    novo = new DefaultMutableTreeNode(nome + " [" + i + "] " + sb.toString());
                    parent.add(novo);
                    
                    if (cdAnuncio != null) {
                        cacheNodos.put(cdAnuncio, novo);
                    }
                }

                processNode((JSONObject) o, ident + "   ", novo);
            } else if (o instanceof JSONArray) {
                processArray((JSONArray) o, nome, ident + "   ", null);
            } else {
                DefaultMutableTreeNode novo = null;
                if (parent != null) {
                    novo = new DefaultMutableTreeNode(o.toString());
                    parent.add(novo);
                }
            }
        }
    }

    private void processNode(JSONObject json, String ident, DefaultMutableTreeNode parent) {
        @SuppressWarnings("unchecked")
        Iterator<Object> iterJson = json.keySet().iterator();
        while (iterJson.hasNext()) {
            Object k = iterJson.next();
            Object v = json.get(k);

            // System.out.println(ident + ">>> [" + k + "] [" + (v == null ?
            // "null" : v.getClass().getName()) + "]");
            

            if (v != null) {
                if (v instanceof JSONObject) {
                    DefaultMutableTreeNode novo = null;
                    
                    String url = k.toString();
                    if (url.startsWith(ANUNCIO_URL)) {
                        String cdAnuncio = url.substring(ANUNCIO_URL.length());
                        
                        if (cacheNodos.get(cdAnuncio) != null) {
                            parent = cacheNodos.get(cdAnuncio);
                        }
                    }
                    
                    
                    if (parent != null) {
                        novo = new DefaultMutableTreeNode(k);
                        parent.add(novo);
                    }
                    processNode((JSONObject) v, ident + "   ", novo);
                } else if (v instanceof JSONArray) {
                    DefaultMutableTreeNode novo = null;
                    if (parent != null) {
                        novo = new DefaultMutableTreeNode(k);
                        parent.add(novo);
                    }

                    JSONArray arr = (JSONArray) v;
                    processArray(arr, k, ident + "   ", novo);
                } else {
                    if (parent != null) {
                        String tmp = k + ": " + (v instanceof Number ? nFormatter.format((Number) v) : v);
                        DefaultMutableTreeNode novo = new DefaultMutableTreeNode(tmp);
                        parent.add(novo);
                    }
                }
            }

        }
    }

    public static void main(String[] args) {
        new App();
    }

    private JSONObject executeQuery(String uri, NameValuePair[] parameters) {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        try {
            RequestBuilder builder = RequestBuilder.post().setUri(new URI(uri));
            builder.addParameters(parameters);
            HttpUriRequest httpPost = builder.build();

            System.out.println("Executing request " + httpPost.getRequestLine());

            // Create a custom response handler
            ResponseHandler<String> responseHandler = new ResponseHandler<String>() {

                public String handleResponse(final HttpResponse response) throws ClientProtocolException, IOException {
                    int status = response.getStatusLine().getStatusCode();
                    if (status >= 200 && status < 300) {
                        HttpEntity entity = response.getEntity();
                        return entity != null ? EntityUtils.toString(entity) : null;
                    } else {
                        throw new ClientProtocolException("Unexpected response status: " + status);
                    }
                }

            };
            String responseBody = httpclient.execute(httpPost, responseHandler);

            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(responseBody);
            return jsonObject;
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (httpclient != null) {
                    httpclient.close();
                }
            } catch (Exception ex) {

            }
        }
        return null;
    }

}
