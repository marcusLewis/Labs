package br.com.penseimoveis.util.jasper.retail;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRDataSourceProvider;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperReport;
import br.com.penseimoveis.util.jasper.MongodbHelper;

public class ContatoRetailDataSourceProvider implements JRDataSourceProvider {

    public boolean supportsGetFieldsOperation() {
        return true;
    }

    public JRField[] getFields(JasperReport report) throws JRException, UnsupportedOperationException {
        return ContatoRetailField.getFields();
    }

    public JRDataSource create(JasperReport report) throws JRException {
        
        for (JRParameter p: report.getParameters()) {
            System.out.println(">>>>>>>>>>>>> " + p);
            System.out.println("     >>>>>>>> " + p.getName());
            System.out.println("     >>>>>>>> " + p.getDescription());
            System.out.println("     >>>>>>>> " + p.getPropertiesMap());
            System.out.println("     >>>>>>>> " + p.getValueClassName());
        }

        return new ContatoRetailDataSource(MongodbHelper.getMongoDB());
    }

    public void dispose(JRDataSource dataSource) throws JRException {

    }
    
}

