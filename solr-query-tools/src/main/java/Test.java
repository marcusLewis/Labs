import java.io.ByteArrayOutputStream;
import java.io.File;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;



public class Test {

    
    
    public static void main(String[] args) {
        String javaExecutor = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
        System.out.println(javaExecutor);
        
        String destDir = "C:\\Users\\MARCUS~1\\AppData\\Local\\Temp\\7715e709-eb26-41a4-ab68-e7eddb819f7d";
        String runtimePath = "C:\\Desenvolvimento\\Git\\pense-produto-legado\\backoffice\\bin\\src\\main\\webapp\\WEB-INF\\solr-runtime";
        String confName = "pense-imoveis";
        String zookeeperHost = "solr1.dev.pense.com.br:2181,solr2.dev.pense.com.br:2182";
        
        
        
        CommandLine cmdLine = new CommandLine(javaExecutor);
        cmdLine.addArgument("-Dlog4j.configuration=\"file:" + runtimePath + File.separator + "log4j.properties\"");
        cmdLine.addArgument("-classpath");
        cmdLine.addArgument("\"" + runtimePath + File.separator +  "*\"");
        
        cmdLine.addArgument("-zkhost");
        cmdLine.addArgument(zookeeperHost);
        cmdLine.addArgument("-cmd");
        cmdLine.addArgument("upconfig");
        cmdLine.addArgument("-confdir");
        cmdLine.addArgument(destDir);
        cmdLine.addArgument("-confname");
        cmdLine.addArgument(confName);
        
        
        /*
-Dlog4j.configuration="file:C:\Desenvolvimento\Git\pense-produto-legado\backoffice\bin\src\main\webapp\WEB-INF\solr-runtime\log4j.properties" 
-classpath "C:\Desenvolvimento\Git\pense-produto-legado\backoffice\bin\src\main\webapp\WEB-INF\solr-runtime\*" 
org.apache.solr.cloud.ZkCLI 
-zkhost solr1.dev.pense.com.br:2181,solr2.dev.pense.com.br:2182 
-cmd upconfig 
-confdir C:\Users\MARCUS~1\AppData\Local\Temp\7715e709-eb26-41a4-ab68-e7eddb819f7d 
-confname pense-imoveis
         * */

        System.out.println("vai executar zkCli.sh :: " + cmdLine.toString());

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        DefaultExecutor executor = new DefaultExecutor();
        executor.setStreamHandler(new PumpStreamHandler(stream));
        try {
            executor.execute(cmdLine);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        System.out.println(stream.toString());
        
        
    }
    
}
