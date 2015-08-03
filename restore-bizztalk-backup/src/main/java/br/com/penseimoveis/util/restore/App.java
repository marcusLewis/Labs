package br.com.penseimoveis.util.restore;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * Hello world!
 *
 */
public class App {
    private static Map<Long, DadosFTP> mapDadosFTP = new HashMap<Long, DadosFTP>();
    
    static {
        loadClientes();
    }
    
    
    public static void main1(String[] args) throws Exception {
        BufferedWriter writer = new BufferedWriter(new FileWriter("verificar-out-" + System.currentTimeMillis() + ".txt"));

        List<Long> contratosSemBackup = new ArrayList<Long>();
        
        List<DadosFTP> ftps = new ArrayList<DadosFTP>(mapDadosFTP.values());
        printLog(writer, "Vai verificar [" + ftps.size() + "] ftps");
        
        for (DadosFTP ftp: ftps) {
            try {
                FTPClient cli2Verify = ftp.connect();
                printLog(writer, ">>> prepara para verificar o contrato [" + ftp.getContrato() + "]");
                
                // cria o diretorio do backp
                String backupFolderName = "backup";
                String folderBackup = ftp.getPathXml() + "/" + backupFolderName;
                
                FTPFile[] arquivos = cli2Verify.listFiles(folderBackup);
                
                String fileName = null;
                for (FTPFile arq: arquivos) {
                    if (arq != null && arq.getName() != null && arq.getName().toLowerCase().endsWith(".xml")) {
                        fileName = folderBackup + "/" + arq.getName();
                    }
                }
                
                if (fileName == null) {
                    contratosSemBackup.add(ftp.getContrato());
                    printLog(writer, "     >>> não achou o arquivo do contrato [" + ftp.getContrato() + "]");
                } else {
                    printLog(writer, "     >>> achou o arquivo [" + fileName + "]");
                }
                
                cli2Verify.logout();
                cli2Verify.disconnect();
            } catch (Exception ex) {
                printLog(writer, ">>> Erro no contrato [" + ftp.getContrato() + "][" + ex.getMessage() +"]");
            }
        }
        
        
        printLog(writer, "######################################################");
        printLog(writer, "Erros [" + contratosSemBackup.size() + "]");
        printLog(writer, contratosSemBackup + "");
        printLog(writer, "######################################################");
    }
    
    
    
    public static void main2(String[] args) {
        Long arr[] = new Long[]{10240L, 2049L,       2050L,        8197L,        2055L,        8199L,        2056L,        2057L};
        
        for (Long l: arr) {
            DadosFTP ftp = mapDadosFTP.get(l);
            
            System.out.println("[" + l +"] ftp://" + ftp.getUsuario() + ":" + ftp.getSenha() + "@" + ftp.getFtp());
        }
    }
    
    public static void main(String[] args) throws Exception {
        Long t1 = System.currentTimeMillis();
        
        BufferedWriter writer = new BufferedWriter(new FileWriter("out-" + System.currentTimeMillis() + ".txt"));
        
        Map<Long, String> pastaPorContrato = getMapPastaPorContrato(writer);

        File f = new File(Constants.TXT_FILE_NAME);
        List<Long> contratos2Restore = new ArrayList<Long>();
        List<Long> oks = new ArrayList<Long>();
        List<Long> errors = new ArrayList<Long>();

        
        if (f.exists()) {
            BufferedReader reader = new BufferedReader(new FileReader(f));
            String l = null;
            
            while ((l = reader.readLine()) != null) {
                if (l.trim().length() > 0) {
                    try {
                        Long idContrato = Long.parseLong(l);
                        contratos2Restore.add(idContrato);
                    } catch (NumberFormatException ex) {}
                }
            }
            reader.close();
            
            printLog(writer, ">>> Achou [" + contratos2Restore.size() + "] contratos para fazer o restore. Iniciando o processo....");
            int idx = 0;

            NumberFormat fmt = NumberFormat.getPercentInstance();
            fmt.setMinimumFractionDigits(2);
            fmt.setMaximumFractionDigits(2);
            
            for (Long idContrato: contratos2Restore) {
                printLog(writer, "#############################################################################");
                printLog(writer, "# [" + (idx++) + "][" +  fmt.format(((new Double(idx))/contratos2Restore.size())) +"] Rodando o contrato [" + idContrato + "]");
                printLog(writer, "#############################################################################");
                DadosFTP ftp2Restore = mapDadosFTP.get(idContrato);
                
                try {
                    if (ftp2Restore != null) {
                        restoreBackup(writer, pastaPorContrato, ftp2Restore);
                        oks.add(idContrato);
                    } else {
                        printLog(writer, "  .. contrato [" + idContrato + "] sem dados de ftp.");
                        errors.add(idContrato);
                    }
                } catch (Exception ex) {
                    printLog(writer, "Exception: [" + ex.getMessage() + "][ftp://" + ftp2Restore.getUsuario() + ":" + ftp2Restore.getSenha() + "@" + ftp2Restore.getFtp() + "][" + ftp2Restore.getPathXml() + "]");
                    errors.add(idContrato);
                }
            }
            
        } else {
            printLog(writer, ">>> O arquivo [" + f.getAbsolutePath() + "] não existe.");
        }

        
        printLog(writer, "");
        printLog(writer, "");
        printLog(writer, "#############################################################################");
        printLog(writer, "# RESULTADO FINAL ");
        printLog(writer, "#############################################################################");
        printLog(writer, ">>>> Rodou [" + oks.size() + "] oks e [" + errors.size() + "] com errors");
        printLog(writer, "Erros: " + errors);
        
        printLog(writer, ">>> Executou o processo todo em [" + ((System.currentTimeMillis()-t1)/(60d*1000d)) + "] min");
        
        writer.flush();
        writer.close();
    }    
    

    
    public static void printLog(BufferedWriter writer, String txt) throws IOException {
        writer.write(txt + "\n");
        System.out.println(txt);
    }
    
    private static void addFTPData(String ftp, String usuario, String senha, Long contrato, String pathXML) {
        DadosFTP data = new DadosFTP(ftp, usuario, senha, contrato, pathXML);
        mapDadosFTP.put(contrato, data);
    }
    
    
    private static void restoreBackup(BufferedWriter writer, Map<Long, String> pastaPorContrato, DadosFTP ftp2Restore) throws Exception {
        printLog(writer, "Restaurando backup do contrato [" + ftp2Restore.getContrato() + "]");
        
        // baixa o arquivo
        if (pastaPorContrato.containsKey(ftp2Restore.getContrato())) {
            String pasta = pastaPorContrato.get(ftp2Restore.getContrato());
            
            FTPClient cli2Download = Constants.FTP_BACKUP_BIZZTALK.connect();
            
            File savedFile = null;
            
            try {
                // pega o arquivo mais novo
                List<FTPFile> files = Arrays.asList(cli2Download.listFiles(pasta));
                
                if (files != null && !files.isEmpty()) {
                    if (files.size() > 1) {
                        Collections.sort(files, new Comparator<FTPFile>() {
                            public int compare(FTPFile o1, FTPFile o2) {
                                return o2.getTimestamp().compareTo(o1.getTimestamp());
                            }
                        });
                    }
                    
                    FTPFile file2Save = files.get(0);
                    String originalFileName = file2Save.getName();
                    
                    String destFileName = originalFileName.substring(0, originalFileName.indexOf(".")) + ".xml";
                    
                    File tmpDir = new File("work-files");
                    tmpDir.mkdirs();
                    savedFile = new File(tmpDir, destFileName);
                    
                    // so cria o arquivo se nao existir local
                    if (savedFile.exists()) {
                        printLog(writer, ">>>> Arquivo de backup ja existe local [" + savedFile.getAbsolutePath() +"]");
                    } else {
                        FileOutputStream out = new FileOutputStream(savedFile);
                        cli2Download.retrieveFile(pasta + "/" + file2Save.getName(), out);
                        out.close();
                        
                        printLog(writer, ">>>> Salvou o backup em [" + savedFile.getAbsolutePath() +"]");
                    }
                } else {
                    printLog(writer, ">> Não achou arquivo de backup para o contrato [" + ftp2Restore.getContrato() + "]");
                }
            } catch (Exception ex) {
                printLog(writer, "Exception: [" + ex.getMessage() + "][ftp://" + ftp2Restore.getUsuario() + ":" + ftp2Restore.getSenha() + "@" + ftp2Restore.getFtp() + "][" + ftp2Restore.getPathXml() + "]");
            }
            cli2Download.logout();
            cli2Download.disconnect();
            
            // prepara para subr o arquivo
            if (savedFile != null) {
                FTPClient cli2Restore = ftp2Restore.connect();
                
                printLog(writer, ">>> Preparando para subir o backup do contrato [" + ftp2Restore.getContrato() + "]");
                
                // cria o diretorio do backp
                
                FileInputStream fIn = new FileInputStream(savedFile);
                String backupFolderName = "backup";
                String folderBackup = ftp2Restore.getPathXml() + "/" + backupFolderName;
                
                // verifica se existe a pasta de backup
                boolean existBackupFolder = false;
                
                FTPFile[] contents = cli2Restore.listFiles(ftp2Restore.getPathXml());
                for (FTPFile f: contents) {
                    if (backupFolderName.equals(f.getName())) {
                        existBackupFolder = true;
                        break;
                    }
                }
                
                if (!existBackupFolder) {
                    boolean sucessCreateDir = cli2Restore.makeDirectory(folderBackup);
                    printLog(writer, ">>> Cria o diretorio de backup [" + folderBackup  +"]");
                    if (!sucessCreateDir) {
                        fIn.close();
                        throw new RuntimeException(">>>>> não foi possivel criar o diretorio [" + folderBackup + "]");
                    }
                }
                // salva o arquivo
                cli2Restore.storeFile(folderBackup + "/" + savedFile.getName(), fIn);
                fIn.close();
                
                printLog(writer, ">>> Upload do backup do contrato [" + ftp2Restore.getContrato() + "] concluido em [" + ftp2Restore.getFtp() + "][" + folderBackup  +"]");
                
                cli2Restore.logout();
                cli2Restore.disconnect();
            } else {
                printLog(writer, ">>>>> contrato [" + ftp2Restore.getContrato() + "] sem arquivo salvo");
                throw new RuntimeException(">>>>> contrato [" + ftp2Restore.getContrato() + "] sem arquivo salvo");
            }
        } else {
            printLog(writer, ">>>>> contrato [" + ftp2Restore.getContrato() + "] sem backup");
            throw new RuntimeException("contrato [" + ftp2Restore.getContrato() + "] sem backup");
        }
    }
    
    
    public static Map<Long, String> getMapPastaPorContrato(BufferedWriter writer) throws IOException {
        try {
            // se prepara para carregar os dados do backup
            File arquivoMapBizz = new File(Constants.EXCEL_FILE_NAME);
            
            // se nao existir o arquivo gera ele
            if (!arquivoMapBizz.exists()) {
                gerarDadosBiztalk(writer);
            }
            
            
            // carrega em memoria
            Workbook wbInput = new HSSFWorkbook(new FileInputStream(arquivoMapBizz));
            Sheet sheet = wbInput.getSheetAt(0);
            
            int idxRow = 0;
            Row row = null;
            
            Map<Long, String> pastaPorContrato = new HashMap<Long, String>();
            
            while ((row = sheet.getRow(idxRow++)) != null) {
                if (row.getCell(Constants.IDX_COLUNA_CONTRATO) != null) {
                    Double vlrContrato = null;
                    try {
                        vlrContrato = row.getCell(Constants.IDX_COLUNA_CONTRATO).getNumericCellValue();
                    } catch (Exception ex) {}
                    
                    if (vlrContrato != null) {
                        pastaPorContrato.put(vlrContrato.longValue(), row.getCell(Constants.IDX_COLUNA_PASTA).getStringCellValue());
                    }
                }
            }
            return pastaPorContrato;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
    
    public static void gerarDadosBiztalk(BufferedWriter writer) throws Exception {
        Long t1 = System.currentTimeMillis();

        FTPClient client = Constants.FTP_BACKUP_BIZZTALK.connect();

        Workbook workbook = new HSSFWorkbook();
        Sheet sheet = workbook.createSheet("Backups-Bizztalk");
        
        int idxRow = 0;
        
        createRow(sheet, idxRow++, "IDContrato", "Data", "Pasta", "Arquivo");

        String[] names = client.listNames();
        
        DateFormat dateFormater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        
        printLog(writer, "Preparando para criar mapeamentos ");
        for (String n: names) {
            List<FTPFile> files = Arrays.asList(client.listFiles(n));
            
            if (files != null && !files.isEmpty()) {
                if (files.size() > 1) {
                    Collections.sort(files, new Comparator<FTPFile>() {
                        public int compare(FTPFile o1, FTPFile o2) {
                            return o2.getTimestamp().compareTo(o1.getTimestamp());
                        }
                    });
                }
                
                FTPFile f = files.get(0);
                String fname = f.getName();
                Long idContrato = -1L;
                
                if (fname != null && fname.indexOf("_") > -1) {
                    idContrato = Long.parseLong(fname.substring(0, fname.indexOf("_"))); 
                }
                
                if (idContrato != -1L) {
                    //printLog(writer, "      ------------ TS [" + dateFormater.format(f.getTimestamp().getTime()) + "] idcontrato [" + idContrato + "] Filename [" + f.getName() + "]");
                    printLog(writer, "      >>>>>> [" + idContrato + "][" + n + "]");
                    createRow(sheet, idxRow++, idContrato, dateFormater.format(f.getTimestamp().getTime()), n, f.getName());
                }
            }
            
        }
        
        boolean logout = client.logout();
        if (logout) {
            printLog(writer, "Logout from FTP server...");
        }

        client.disconnect();
        
        workbook.write(new FileOutputStream(Constants.EXCEL_FILE_NAME));
        
        
        printLog(writer, ">>> Rodou em [" + ((System.currentTimeMillis()-t1)/(1000d*60d)) + "] min");
    }
    
    private static void createRow(Sheet sheet, int idxRow, Object val1, String val2, String val3, String val4) {
        Row row = sheet.createRow(idxRow++);
        
        {
            Cell cell = row.createCell(Constants.IDX_COLUNA_CONTRATO);
            
            if (val1 instanceof Number) {
                cell.setCellValue(((Number)val1).doubleValue());
            } else {
                cell.setCellValue(val1.toString());
            }
        }
        {
            Cell cell = row.createCell(Constants.IDX_COLUNA_DATA);
            cell.setCellValue(val2);
        }

        {
            Cell cell = row.createCell(Constants.IDX_COLUNA_PASTA);
            cell.setCellValue(val3);
        }
        {
            Cell cell = row.createCell(Constants.IDX_COLUNA_ARQUIVO);
            cell.setCellValue(val4);
        }
    }
    
    
    private static void loadClientes() {
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 1876L, "/3014");
        addFTPData("187.115.52.235", "administrador", "ybr98xk6", 1877L, "/Hagah");
        addFTPData("177.71.188.45", "Pense", "4h21ui4y198hrnjds", 6370L, "/2795");
        addFTPData("carga.rbs.com.br", "MSInformatica", "097b3e3f", 6371L, "/hansen/hagah");
        addFTPData("carga.rbs.com.br", "MSInformatica", "097b3e3f", 1885L, "/nobresimoveis/hagah");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 7190L, "/936XML");
        addFTPData("carga.rbs.com.br", "MSInformatica", "097b3e3f", 1890L, "/koncreta/hagah");
        addFTPData("carga.rbs.com.br", "MSInformatica", "097b3e3f", 1891L, "/fratelli/hagah");
        addFTPData("carga.rbs.com.br", "MSInformatica", "097b3e3f", 1892L, "/visao/pense");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 1895L, "/188XML");
        addFTPData("177.71.188.45", "Pense", "4h21ui4y198hrnjds", 14793L, "/3221");
        addFTPData("carga.rbs.com.br", "MSInformatica", "097b3e3f", 25148L, "/heart/pense");
        addFTPData("carga.rbs.com.br", "MSInformatica", "097b3e3f", 1896L, "/presente/hagah");
        addFTPData("carga.rbs.com.br", "vegasistemas", "ab2798d1", 1900L, "/gallon");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 1902L, "/246");
        addFTPData("masterimoveis.imb.br", "pense@masterimoveis.imb.br", "masterpi", 7263L, "/hagah");
        addFTPData("186.202.65.93", "duda_hagah", "a1461q8F437M9R88", 1912L, "/hagah");
        addFTPData("ftp.bancoi.com.br", "bancoi68", "penseirs", 1915L, "/canelaimoveis/hagah");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 1921L, "/2747");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 1925L, "/12159");
        addFTPData("carga.rbs.com.br", "casarao", "65df4b86", 1926L, "/casarao");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 14588L, "/1096XML");
        addFTPData("bpi.hagah.sami.suprisoft.com.br", "hagah", "Xert772", 6080L, "/suc/hagah");
        addFTPData("repo.lenova.com.br", "penseimoveis", "79HZxPXT", 14792L, "/332592/hagah");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 1941L, "/669XML");
        addFTPData("carga.rbs.com.br", "viasw", "grupo@rbs123", 7981L, "/pense/tempo");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 1965L, "/200XML");
        addFTPData("repo.lenova.com.br", "penseimoveis", "79HZxPXT", 6547L, "/128863/hagah");
        addFTPData("ftp.imobiliariamodelosm.com.br", "penseimoveis@imobiliariamodelosm.com.br", "pense10mode20ft30", 1966L, "/cliente_pense");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 1973L, "/2651");
        addFTPData("ftp.exemploimoveis.com.br", "exemploimoveis", "ftp13@exe87@", 5792L, "/public_html/cliente_pense");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 1978L, "/1234XML");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 1993L, "/2566/farroupilha");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 1991L, "/3074XML");
        addFTPData("177.71.188.45", "Pense", "4h21ui4y198hrnjds", 7650L, "/2434");
        addFTPData("penseimoveis.imovelpro.com.br", "penseimoveis", "rRbdrgfrg43535dbDr4", 17989L, "/www.g3imoveis.com");
        addFTPData("186.202.65.93", "desc_hagah", "peg48mah&23gru1h", 2557L, "/hagah/");
        addFTPData("177.71.188.45", "Pense", "4h21ui4y198hrnjds", 1997L, "/4312");
        addFTPData("penseimoveis.imovelpro.com.br", "penseimoveis", "rRbdrgfrg43535dbDr4", 9206L, "/www.portalimoveisitapema.com.br");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 25120L, "/3043XML");
        addFTPData("177.71.188.45", "Pense", "4h21ui4y198hrnjds", 2011L, "/1808");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 2015L, "/180XML");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 6094L, "/7345");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 2020L, "/175XML");
        addFTPData("ftp.imobiliariaourobranco.com.br", "imobiliariaourobranc", "OUROftp10", 25242L, "/Web/hagah");
        addFTPData("carga.rbs.com.br", "UNION", "fdffa368", 2021L, "/investfloripa");
        addFTPData("cli22131.dominioimovel.com.br", "portoseguro_hagah", "bK5gFoK2HnpQrJjX", 7089L, "/hagah");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 2030L, "/12939");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 25246L, "/3048XML");
        addFTPData("carga.rbs.com.br", "Tec-Imoveis", "0bdae98c", 2070L, "/conill");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 2032L, "/170XML");
        addFTPData("ftp.steinhaus.com.br", "steinhaus", "st4956us@ftp", 2034L, "/Web/hagah");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 2035L, "/7130");
        addFTPData("penseimoveis.imovelpro.com.br", "penseimoveis", "rRbdrgfrg43535dbDr4", 24895L, "/www.liderancaimobiliaria.com.br");
        addFTPData("ftp.paesimoveis1.provisorio.ws", "paesimoveis11", "ipa12139es", 9154L, "/Web/hagah");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 2037L, "/2662");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 6201L, "/276XML");
        addFTPData("penseimoveis.imovelpro.com.br", "penseimoveis", "rRbdrgfrg43535dbDr4", 2042L, "/www.icaimoveis.com.br");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 2049L, "/2907");
        addFTPData("ftp.bancoi.com.br", "bancoi68", "penseirs", 2050L, "/dimensaoimoveis/hagah");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 9322L, "/922XML");
        addFTPData("177.71.188.45", "Pense", "4h21ui4y198hrnjds", 2055L, "/3978");
        addFTPData("carga.rbs.com.br", "CODE49", "C0D349", 2056L, "/crimoveis/hagah");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 25297L, "/3076XML");
        addFTPData("carga.rbs.com.br", "area_digital", "grupo@rbs123", 2057L, "/floripahouses_pense");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 25241L, "/3068XML");
        addFTPData("repo.lenova.com.br", "penseimoveis", "79HZxPXT", 8149L, "/98213/hagah");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 2061L, "/9855");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 2063L, "/9571");
        addFTPData("carga.rbs.com.br", "maisonave", "e4bf67de", 2067L, "/maisonave/hagah");
        addFTPData("ftp.idealeimoveissc.com.br", "idealeimoveissc01", "integrapense", 2069L, "/");
        addFTPData("carga.rbs.com.br", "plug7", "eafd4ab0", 7226L, "/hagah/realprime_2");
        addFTPData("ftp.integracao.penseimoveis.com.br", "site1369150788", "vidanova09sc", 2073L, "/pense");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 6901L, "/440");
        addFTPData("177.71.188.45", "Pense", "4h21ui4y198hrnjds", 2074L, "/866");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 2083L, "/935XML");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 25086L, "/3038XML");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 2087L, "/6992");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 2089L, "/11819");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 7853L, "/968XML");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 12891L, "/1041XML");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 9363L, "/1058XML");
        addFTPData("ftp.serverdo.in", "pxml@serverdo.in", "VuS5O0riXrTO", 2889L, "/cliente_pense");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 7470L, "/11587");
        addFTPData("177.71.188.45", "Pense", "4h21ui4y198hrnjds", 9045L, "/3925");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 13188L, "/1045XML");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 7841L, "/646XML");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 7886L, "/6638");
        addFTPData("carga.rbs.com.br", "Tec-Imoveis", "0bdae98c", 2107L, "/eloimoveissc");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 2109L, "/10024");
        addFTPData("ftp.bancoi.com.br", "bancoi68", "penseirs", 24935L, "/bomlar/hagah");
        addFTPData("ftp.sobressai.com.br", "sobressai02", "penseimoveis", 25017L, "/188");
        addFTPData("177.71.188.45", "Pense", "4h21ui4y198hrnjds", 2615L, "/4600");
        addFTPData("ftp.sobressai.com.br", "sobressai02", "penseimoveis", 25312L, "/176");
        addFTPData("repo.lenova.com.br", "penseimoveis", "79HZxPXT", 25073L, "/319631/hagah");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 2127L, "/2685");
        addFTPData("177.71.188.45", "Pense", "4h21ui4y198hrnjds", 2130L, "/3339");
        addFTPData("177.71.188.45", "Pense", "4h21ui4y198hrnjds", 2133L, "/4602");
        addFTPData("BPI.hagah.sami.suprisoft.com.br", "hagah", "Xert772", 9347L, "/eni/hagah");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 3025L, "/1772/petropolis");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 5853L, "/147");
        addFTPData("ftp.bancoi.com.br", "bancoi68", "penseirs", 2290L, "/leandrocidade/hagah");
        addFTPData("177.71.188.45", "Pense", "4h21ui4y198hrnjds", 2145L, "/3455");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 2152L, "/2978");
        addFTPData("www.imobiliariasaogeraldo.com.br", "ct00062752-001", "ftpj2345", 2153L, "htdocs/hagah");
        addFTPData("repo.lenova.com.br", "penseimoveis", "79HZxPXT", 7859L, "/82413/hagah");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 6852L, "/ciaimob");
        addFTPData("ftp.inetsoft.com.br", "inetsoft01", "PenseRBS", 2163L, "/creditoreal/hagah");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 2209L, "/841");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 12238L, "/1003XML");
        addFTPData("repo.lenova.com.br", "penseimoveis", "79HZxPXT", 7917L, "/200573/hagah");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 2168L, "/1772/canoasdoutorbarcelos");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 2170L, "/1150XML");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 2174L, "/460");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 2175L, "/850");
        addFTPData("LCT.hagah.sami.suprisoft.com.br", "hagah", "Xert772", 2221L, "/lct/hagah");
        addFTPData("ftp.sobressai.com.br", "sobressai02", "penseimoveis", 7219L, "/289");
        addFTPData("carga.rbs.com.br", "CODE49", "C0D349", 2435L, "/natan/hagah");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 2180L, "/2581");
        addFTPData("cer.hagah.sami.suprisoft.com.br", "hagah", "Xert772", 7704L, "/cer/hagah");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 2189L, "/2756");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 7686L, "/2399");
        addFTPData("aei.hagah.sami.suprisoft.com.br", "hagah", "Xert772", 2200L, "/aei/hagah");
        addFTPData("POR.hagah.sami.suprisoft.com.br", "hagah", "Xert772", 2203L, "/por/hagah");
        addFTPData("ftp.inetsoft.com.br", "inetsoft01", "PenseRBS", 2769L, "/aluga/hagah");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 6524L, "/8935");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 25277L, "/13112");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 2210L, "/1922");
        addFTPData("repo.lenova.com.br", "penseimoveis", "79HZxPXT", 2214L, "/212113/hagah");
        addFTPData("carga.rbs.com.br", "CODE49", "C0D349", 8126L, "/auxaluguel/pense/zonasul");
        addFTPData("repo.lenova.com.br", "penseimoveis", "79HZxPXT", 25203L, "/230663/hagah");
        addFTPData("177.71.188.45", "Pense", "4h21ui4y198hrnjds", 16738L, "/4019");
        addFTPData("www.foxter.com.br", "penseimoveis", "fUY1BYKXWUOLq6g", 2220L, "/hagah");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 5712L, "/41");
        addFTPData("repo.lenova.com.br", "penseimoveis", "79HZxPXT", 2223L, "/6852/hagah");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 2227L, "/2859");
        addFTPData("repo.lenova.com.br", "penseimoveis", "79HZxPXT", 7277L, "/16241/hagah");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 2240L, "/2156");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 2242L, "/2882");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 2243L, "/2857/Venda");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 9325L, "/2857/Locacao");
        addFTPData("carga.rbs.com.br", "mdoinformatica", "2190ffdb1", 10636L, "/pieta/hagah");
        addFTPData("esx.hagah.sami.suprisoft.com.br", "hagah", "Xert772", 2251L, "/esx/hagah");
        addFTPData("ESX.hagah.suprisoft.com.br", "hagah", "Xert772", 2252L, "/ESX/samiweb/hagah");
        addFTPData("repo.lenova.com.br", "penseimoveis", "79HZxPXT", 2254L, "/5217/hagah");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 2258L, "/2743");
        addFTPData("carga.rbs.com.br", "Tec-Imoveis", "0bdae98c", 16838L, "/presidente");
        addFTPData("bar.hagah.sami.suprisoft.com.br", "hagah", "Xert772", 5139L, "/izn/hagah");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 6970L, "/10301");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 2268L, "/1772/santana");
        addFTPData("ftp.sobressai.com.br", "sobressai02", "penseimoveis", 25002L, "/225");
        addFTPData("repo.lenova.com.br", "penseimoveis", "79HZxPXT", 2271L, "/32473/hagah");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 2272L, "/2602");
        addFTPData("mor.hagah.suprisoft.com.br", "hagah", "Xert772", 2273L, "mor/hagah");
        addFTPData("ftp.inetsoft.com.br", "inetsoft01", "PenseRBS", 2275L, "/perfil/hagah");
        addFTPData("ftp.inetsoft.com.br", "inetsoft01", "PenseRBS", 2277L, "/leindecker/hagah");
        addFTPData("repo.lenova.com.br", "penseimoveis", "79HZxPXT", 2280L, "/305251/hagah");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 2282L, "/1156");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 6763L, "/485");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 2288L, "/6658");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 2311L, "/1440");
        addFTPData("ftp.sobressai.com.br", "sobressai02", "penseimoveis", 2297L, "/467");
        addFTPData("177.71.188.45", "Pense", "4h21ui4y198hrnjds", 10487L, "/4002");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 2305L, "/2580");
        addFTPData("ftp.inetsoft.com.br", "inetsoft01", "PenseRBS", 2306L, "bona/hagah");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 2308L, "/2089");
        addFTPData("carga.rbs.com.br", "BR_Brokers", "73fe0139", 24543L, "/noblesse/belavista");
        addFTPData("carga.rbs.com.br", "BR_Brokers", "73fe0139", 24544L, "/noblesse/nilo");
        addFTPData("177.71.188.45", "Pense", "4h21ui4y198hrnjds", 25183L, "/4534");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 15089L, "/1106XML");
        addFTPData("repo.lenova.com.br", "penseimoveis", "79HZxPXT", 2327L, "/6766/hagah");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 2328L, "/12437");
        addFTPData("ftp.inetsoft.com.br", "inetsoft01", "PenseRBS", 25291L, "/condotta/hagah");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 2330L, "/6572");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 2331L, "/6476");
        addFTPData("man.hagah.sami.suprisoft.com.br", "hagah", "Xert772", 2334L, "man/hagah");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 2338L, "/1182");
        addFTPData("carga.rbs.com.br", "kuickfast", "3f020a17", 2340L, "/toplineimoveis");
        addFTPData("177.71.188.45", "Pense", "4h21ui4y198hrnjds", 2342L, "/1835");
        addFTPData("ftp.dallasanta.com.br", "dallasanta", "21j68bsy", 2346L, "/site/exports");
        addFTPData("repo.lenova.com.br", "penseimoveis", "79HZxPXT", 2347L, "/32263/hagah");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 2349L, "/3075XML");
        addFTPData("ftp.inetsoft.com.br", "inetsoft01", "PenseRBS", 6369L, "/maffer/hagah");
        addFTPData("177.71.188.45", "Pense", "4h21ui4y198hrnjds", 24974L, "/3949");
        addFTPData("carga.rbs.com.br", "Diretriz", "d371715e", 7406L, "/diretriz");
        addFTPData("ftp.integracao.penseimoveis.com.br", "imob21deabril01", "P3Nse_&mp631", 2358L, "//");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 2529L, "/8156");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 2372L, "/6468");
        addFTPData("esx.hagah.sami.suprisoft.com.br", "hagah", "Xert772", 2378L, "/aia/hagah");
        addFTPData("carga.rbs.com.br", "Tec-Imoveis", "0bdae98c", 10836L, "/eucurtopoa");
        addFTPData("ftp.inetsoft.com.br", "inetsoft01", "PenseRBS", 2385L, "dinmar/hagah");
        addFTPData("ftp.inetsoft.com.br", "inetsoft01", "PenseRBS", 2392L, "/humanize/hagah");
        addFTPData("ftp.integracao.penseimoveis.com.br", "triene", "tir#1a0en0", 2410L, "/triene_pense");
        addFTPData("repo.lenova.com.br", "penseimoveis", "79HZxPXT", 2416L, "/1806/hagah");
        addFTPData("esx.hagah.sami.suprisoft.com.br", "hagah", "Xert772", 2396L, "/ivo/hagah");
        addFTPData("ftp.inetsoft.com.br", "inetsoft01", "PenseRBS", 2420L, "gaucha/hagah");
        addFTPData("ftp.inetsoft.com.br", "inetsoft01", "PenseRBS", 2423L, "portosol/hagah");
        addFTPData("177.71.188.45", "Pense", "4h21ui4y198hrnjds", 2427L, "/3770");
        addFTPData("ftp.inetsoft.com.br", "inetsoft01", "PenseRBS", 13639L, "/stefani/hagah");
        addFTPData("ar3.hagah.sami.suprisoft.com.br", "hagah", "Xert772", 2428L, "/AR3/hagah");
        addFTPData("ftp.integracao.penseimoveis.com.br", "vprimerede", "Vpr1me2015", 2438L, "/");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 2439L, "/2056");
        addFTPData("177.71.188.45", "Pense", "4h21ui4y198hrnjds", 2441L, "/4048");
        addFTPData("ftp.inetsoft.com.br", "inetsoft01", "PenseRBS", 2446L, "/azenha/hagah");
        addFTPData("ftp.bancoi.com.br", "bancoi68", "penseirs", 2457L, "/focoimobiliario/hagah");
        addFTPData("imobiliarialcd.com.br", "imobiliarialcd", "penseimoveis", 2462L, "/hagah");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 2471L, "/906");
        addFTPData("carga.rbs.com.br", "imovi_ftp", "058b3e04", 2474L, "/christini");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 25030L, "/3063XML");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 25282L, "/3073XML");
        addFTPData("ftp.bancoi.com.br", "bancoi68", "penseirs", 2489L, "/pugen/hagah");
        addFTPData("carga.rbs.com.br", "agimovel", "85450355", 2490L, "/a33");
        addFTPData("ftp.bancoi.com.br", "bancoi68", "penseirs", 2491L, "/novaamerica/hagah");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 2492L, "/1110XML");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 7561L, "/3069XML");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 2493L, "/1772/torres");
        addFTPData("ftp.sosseg.com", "sosseg", "Jnpg189*", 2496L, "/public_ftp/hagah");
        addFTPData("ftp.bancoi.com.br", "bancoi68", "penseirs", 2497L, "/penz/hagah");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 2498L, "/3023XML");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 9358L, "/881XML");
        addFTPData("ftp.bancoi.com.br", "bancoi68", "penseirs", 2501L, "/novatorres/hagah");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 2502L, "/212XML");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 2503L, "/232XML");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 25061L, "/3034XML");
        addFTPData("windows1.ftpmultiusuario.ws", "pense_dinamicasul@nativadesign", "pense0808", 8043L, "/347");
        addFTPData("windows1.ftpmultiusuario.ws", "pense_magda@nativadesign", "pense606", 9369L, "/606");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 7625L, "/939XML");
        addFTPData("bpi.hagah.sami.samisistemas.com.br", "hagah", "Xert772", 2511L, "/pef/samiweb/hagah");
        addFTPData("penseimoveis.imovelpro.com.br", "penseimoveis", "rRbdrgfrg43535dbDr4", 2513L, "/www.novolarimobiliaria.com.br");
        addFTPData("windows1.ftpmultiusuario.ws", "pense_schork@nativadesign", "pense0954", 2514L, "/327");
        addFTPData("windows1.ftpmultiusuario.ws", "pense_portal@nativadesign", "pense357", 16791L, "/357");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 2516L, "/650XML");
        addFTPData("ftp.wsrun.net", "bp218973@wsrun.net", "218973", 6397L, "/hagah/");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 6361L, "/196XML");
        addFTPData("windows1.ftpmultiusuario.ws", "pense_abelardo@nativadesign", "pense365", 19541L, "/365");
        addFTPData("177.71.188.45", "Pense", "4h21ui4y198hrnjds", 9058L, "/1686");
        addFTPData("ftp.bancoi.com.br", "bancoi68", "penseirs", 7031L, "/novolar/hagah");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 9120L, "/3084XML");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 9277L, "/2100");
        addFTPData("ftp.sobressai.com.br", "sobressai02", "penseimoveis", 6351L, "/208");
        addFTPData("ftp.bancoi.com.br", "bancoi68", "penseirs", 9387L, "/ferrari-sepe/hagah");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 2464L, "/2584");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 5687L, "/1772/jardimbotanico");
        addFTPData("186.237.28.230", "penseimoveis", "1!2@3#zxcadacon", 2526L, "/hagah");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 8030L, "/1772/belavista");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 2527L, "/1415");
        addFTPData("ftp.inetsoft.com.br", "inetsoft01", "PenseRBS", 2893L, "cim/hagah");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 6137L, "/3012");
        addFTPData("carga.rbs.com.br", "Lopes_Sul", "66dc961b", 7436L, "/lopes_sul");
        addFTPData("177.71.188.45", "Pense", "4h21ui4y198hrnjds", 11288L, "/3564");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 2531L, "/1772/higienopolis");
        addFTPData("177.71.188.45", "Pense", "4h21ui4y198hrnjds", 2384L, "/3952");
        addFTPData("177.71.188.45", "Pense", "4h21ui4y198hrnjds", 25213L, "/5629");
        addFTPData("hil.hagah.sami.suprisoft.com.br", "hagah", "Xert772", 18738L, "/hil/hagah");
        addFTPData("ftp.inetsoft.com.br", "inetsoft01", "PenseRBS", 2151L, "zimmer/hagah");
        addFTPData("rvh.hagah.sami.suprisoft.com.br", "hagah", "Xert772", 2159L, "/rvh/hagah");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 8712L, "/3017");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 9252L, "/ferreirapoa_rede");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 8031L, "/1772/borges");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 2539L, "/1772/floresta");
        addFTPData("glo.hagah.sami.suprisoft.com.br", "hagah", "Xert772", 2263L, "/glo/hagah");
        addFTPData("bar.hagah.sami.suprisoft.com.br", "hagah", "Xert772", 6296L, "/bar/hagah");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 6303L, "/7188");
        addFTPData("ftp.inetsoft.com.br", "inetsoft01", "PenseRBS", 2307L, "freire/hagah");
        addFTPData("177.71.188.45", "Pense", "4h21ui4y198hrnjds", 9224L, "/3489");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 2542L, "/1772/wenceslauescobar");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 5223L, "/29");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 2543L, "/1772/medianeira");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 6479L, "/1352");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 2544L, "/1772/gramado_lagonatalluz");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 2545L, "/1772/vinhedos");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 11788L, "/988XML");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 5951L, "/6527");
        addFTPData("ftp.inetsoft.com.br", "inetsoft01", "PenseRBS", 6715L, "/sperinde/hagah");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 20288L, "/1205XML");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 24956L, "/3022XML");
        addFTPData("BAH.hagah.sami.suprisoft.com.br", "hagah", "Xert772", 5140L, "/bah/hagah");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 25104L, "/464");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 2547L, "/1190XML");
        addFTPData("ftp.integracao.penseimoveis.com.br", "sub100", "e00679bd", 6362L, "/public_html/rk/");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 25016L, "/6835");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 6420L, "/2710");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 2550L, "/3006XML");
        addFTPData("177.71.188.45", "Pense", "4h21ui4y198hrnjds", 25060L, "/3545");
        addFTPData("177.71.188.45", "Pense", "4h21ui4y198hrnjds", 7471L, "/3714");
        addFTPData("177.71.188.45", "Pense", "4h21ui4y198hrnjds", 2551L, "/965");
        addFTPData("carga.rbs.com.br", "vegasistemas", "ab2798d1", 2553L, "/adimoveis");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 2554L, "/3045");
        addFTPData("carga.rbs.com.br", "virtuacomm", "9abf8688", 2869L, "/pense");
        addFTPData("ftp.estreladomaringleses.com.br", "estreladomaringleses", "scv@im*veisftp", 2079L, "/Web/hagah");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 7048L, "/8813");
        addFTPData("ftp.habimoveis.com.br", "penseimoveis@habimoveis.com.br", "30pen40", 9537L, "/hagah");
        addFTPData("177.71.188.45", "Pense", "4h21ui4y198hrnjds", 7381L, "/4491");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 7107L, "/3039XML");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 7430L, "/7418");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 2560L, "/1772/jardimeuropa");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 2561L, "/1772/caxiasdosul");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 5767L, "/3041XML");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 1988L, "/2566/caxias");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 25302L, "/3080XML");
        addFTPData("ftp.imperioimoveis-rs.com.br", "operador@imperioimoveis-rs.com.br", "imperio", 2563L, "/imperio/hagah");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 2564L, "/962XML");
        addFTPData("carga.rbs.com.br", "viasw", "grupo@rbs123", 7551L, "/pense/nichele");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 7603L, "/2873");
        addFTPData("carga.rbs.com.br", "viasw", "grupo@rbs123", 7282L, "/pense/idealcaxias");
        addFTPData("ftp.bancoi.com.br", "bancoi68", "penseirs", 1922L, "/casadagente/hagah");
        addFTPData("ftp.bancoi.com.br", "bancoi68", "penseirs", 25067L, "/fmz/hagah");
        addFTPData("carga.rbs.com.br", "imperio", "1mp3r10", 1909L, "/Pilar/Pense");
        addFTPData("carga.rbs.com.br", "imperio", "1mp3r10", 25206L, "/Imperio/Criciuma/Pense/");
        addFTPData("carga.rbs.com.br", "MSInformatica", "097b3e3f", 25004L, "/loyolalobo/pense");
        addFTPData("carga.rbs.com.br", "MSInformatica", "097b3e3f", 7311L, "/imovile/pense");
        addFTPData("carga.rbs.com.br", "UNION", "fdffa368", 1879L, "/buch");
        addFTPData("carga.rbs.com.br", "MSInformatica", "097b3e3f", 8340L, "/galeriaimoveis/hagah");
        addFTPData("177.71.188.45", "Pense", "4h21ui4y198hrnjds", 1880L, "/1756");
        addFTPData("carga.rbs.com.br", "MSInformatica", "097b3e3f", 9150L, "/zibell/hagah");
        addFTPData("carga.rbs.com.br", "UNION", "fdffa368", 9232L, "/borba");
        addFTPData("carga.rbs.com.br", "MSInformatica", "097b3e3f", 9184L, "/sobradoimoveis/hagah");
        addFTPData("carga.rbs.com.br", "MSInformatica", "097b3e3f", 9131L, "/sacada/hagah");
        addFTPData("carga.rbs.com.br", "MSInformatica", "097b3e3f", 9028L, "/olivaimoveis/hagah");
        addFTPData("ftp.sobressai.com.br", "sobressai02", "penseimoveis", 2583L, "/166");
        addFTPData("repo.lenova.com.br", "penseimoveis", "79HZxPXT", 2600L, "/8145/hagah");
        addFTPData("repo.lenova.com.br", "penseimoveis", "79HZxPXT", 19088L, "/239963/hagah");
        addFTPData("wdp.hagah.samiweb.suprisoft.com.br", "hagah", "Xert772", 9437L, "/WDP/samiweb/hagah");
        addFTPData("repo.lenova.com.br", "penseimoveis", "79HZxPXT", 2614L, "/6581/hagah");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 2621L, "/7527");
        addFTPData("177.71.188.45", "Pense", "4h21ui4y198hrnjds", 2631L, "/4044");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 2641L, "/2596");
        addFTPData("ftp.inetsoft.com.br", "inetsoft01", "PenseRBS", 2642L, "/centralimov/hagah");
        addFTPData("aau.hagah.sami.suprisoft.com.br", "hagah", "Xert772", 8430L, "/aau/hagah");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 21543L, "/3027XML");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 2690L, "/740XML");
        addFTPData("cpro4144.publiccloud.com.br", "terraz_hagah", "2v321184us5A243S", 9636L, "/hagah");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 7083L, "/10246");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 2729L, "/1141XML");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 9168L, "/3055");
        addFTPData("bpi.hagah.sami.suprisoft.com.br", "hagah", "Xert772", 25018L, "/ibx/samiweb/hagah");
        addFTPData("177.71.188.45", "Pense", "4h21ui4y198hrnjds", 2738L, "/2111");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 6099L, "/7581");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 8199L, "/7581/cilar_aluguel");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 2762L, "/227XML");
        addFTPData("penseimoveis.imovelpro.com.br", "penseimoveis", "rRbdrgfrg43535dbDr4", 2833L, "/www.markize.com.br");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 9161L, "/2152");
        addFTPData("repo.lenova.com.br", "penseimoveis", "79HZxPXT", 2781L, "/66913/hagah");
        addFTPData("ftp.inetsoft.com.br", "inetsoft01", "PenseRBS", 2796L, "/somma/hagah");
        addFTPData("ftp.fuhrosouto.com.br", "hagah", "Ohzie4ad", 2823L, "/hagah");
        addFTPData("ftp.takaiimoveis.com", "takaiimoveis", "atendimento@", 2853L, "/web/hagah");
        addFTPData("ftp.vitrineimobiliariasc.com.br", "vitrineimobiliariasc", "ftp2230Vitrine", 7586L, "/web/hagah");
        addFTPData("ftp.casanobreimoveis.com.br", "hagah@casanobreimoveis.com.br", "Z[J[XLn}v6xR", 2864L, "/hagah");
        addFTPData("177.71.188.45", "Pense", "4h21ui4y198hrnjds", 2874L, "/2626");
        addFTPData("www.poloimoveiscaxias.com.br", "pense@poloimoveiscaxias.com.br", "polopi", 2880L, "/hagah");
        addFTPData("jdd.hagah.sami.suprisoft.com.br", "hagah", "Xert772", 2890L, "/paa/samiweb/hagah");
        addFTPData("jdd.hagah.sami.suprisoft.com.br", "hagah", "Xert772", 14838L, "/jdd/hagah");
        addFTPData("carga.rbs.com.br", "mdoinformatica", "2190ffdb1", 2891L, "/RIAL/HAGAH");
        addFTPData("tabaimoveis.com", "pense@tabaimoveis.com", "tabapi", 2892L, "/hagah");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 2907L, "/183XML");
        addFTPData("177.71.188.45", "Pense", "4h21ui4y198hrnjds", 2928L, "/3945");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 5889L, "/3014XML");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 6100L, "/3008XML");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 24910L, "/1772/platinum");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 2979L, "/12409");
        addFTPData("54.232.202.186", "ftp_user", "ftp@1510", 2988L, "/public_html/hagah");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 3018L, "/1772/xangrila");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 3019L, "/1772/passofundo");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 3020L, "/1772/nh_juliodecastilhos");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 3021L, "/1772/saoleopoldo");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 3023L, "/1772/moinhosdevento");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 3024L, "/1772/saopedro");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 6649L, "/1772/aparicioborges");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 3026L, "/1772/meninodeus");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 3028L, "/1772/protasioalves");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 3046L, "/7526");
        addFTPData("177.71.188.45", "Pense", "4h21ui4y198hrnjds", 3098L, "/3950");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 3103L, "/1772/nilopecanha");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 3106L, "/1772/canoasjardimdolago");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 3107L, "/1772/assisbrasil");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 3111L, "/1772/cristovaocolombo");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 3119L, "/1772/baltazar");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 3122L, "/1772/bomfim");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 4912L, "/7528");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 6338L, "/667XML");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 5150L, "/1772/joaowallig");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 5235L, "/594XML");
        addFTPData("ftp.integracao.penseimoveis.com.br", "expoimob", "ex&1029#obi", 5672L, "/conquista");
        addFTPData("ftp.tudoo.com.br", "tudoo", "seco221004", 5874L, "/public_html/chiarelloimoveis/pense");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 5392L, "/8462");
        addFTPData("ftp.sobressai.com.br", "sobressai02", "penseimoveis", 5509L, "/116");
        addFTPData("ftp.sobressai.com.br", "sobressai02", "penseimoveis", 5667L, "/262");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 25144L, "/12829");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 5824L, "/8549");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 5884L, "/1772/alvorada");
        addFTPData("177.71.188.45", "Pense", "4h21ui4y198hrnjds", 6058L, "/3938");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 6049L, "/2893");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 6138L, "/1772/gravatai");
        addFTPData("177.71.188.45", "Pense", "4h21ui4y198hrnjds", 6060L, "/3936");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 6855L, "/3029XML");
        addFTPData("ftp.inetsoft.com.br", "inetsoft01", "PenseRBS", 6182L, "/acemil/hagah");
        addFTPData("repo.lenova.com.br", "penseimoveis", "79HZxPXT", 6191L, "/63103/hagah");
        addFTPData("177.71.188.45", "Pense", "4h21ui4y198hrnjds", 6942L, "/5374");
        addFTPData("ar3.hagah.sami.suprisoft.com.br", "hagah", "Xert772", 6310L, "/cmi/hagah");
        addFTPData("windows1.ftpmultiusuario.ws", "pense_primum@nativadesign", "pense315", 6398L, "/315");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 6418L, "/889XML");
        addFTPData("qtl.hagah.sami.suprisoft.com.br", "hagah", "Xert772", 6426L, "/qtl/hagah");
        addFTPData("ftp.bancoi.com.br", "bancoi68", "penseirs", 6485L, "/veramello/hagah");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 6494L, "/1221XML");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 13546L, "/719XML");
        addFTPData("repo.lenova.com.br", "penseimoveis", "79HZxPXT", 6504L, "/8635/hagah");
        addFTPData("ftp.sobressai.com.br", "sobressai02", "penseimoveis", 25240L, "/75");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 6542L, "/440XML");
        addFTPData("NEE.hagah.sami.suprisoft.com.br", "hagah", "Xert772", 13838L, "/NEE/hagah");
        addFTPData("repo.lenova.com.br", "penseimoveis", "79HZxPXT", 25271L, "/8076/hagah/");
        addFTPData("ftp.bancoi.com.br", "bancoi68", "penseirs", 6710L, "/marxter/hagah");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 6713L, "/1173XML");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 6720L, "/3077XML");
        addFTPData("carga.rbs.com.br", "agimovel", "85450355", 25121L, "/investeandira");
        addFTPData("penseimoveis.imovelpro.com.br", "penseimoveis", "rRbdrgfrg43535dbDr4", 7681L, "/www.primmeimoveis.com");
        addFTPData("carga.rbs.com.br", "area_digital", "grupo@rbs123", 25105L, "/cvd_pense");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 6771L, "/884XML");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 6803L, "/721XML");
        addFTPData("penseimoveis.imovelpro.com.br", "penseimoveis", "rRbdrgfrg43535dbDr4", 6815L, "/www.rybergimoveis.com.br");
        addFTPData("www.brognoli.com.br", "penseimoveis", "Pense50*", 6898L, "/hagah");
        addFTPData("177.71.188.45", "Pense", "4h21ui4y198hrnjds", 6924L, "/5105");
        addFTPData("ftp.bancoi.com.br", "bancoi68", "penseirs", 6929L, "/jaquesimoveis/hagah");
        addFTPData("177.71.188.45", "Pense", "4h21ui4y198hrnjds", 25310L, "/1561");
        addFTPData("penseimoveis.imovelpro.com.br", "penseimoveis", "rRbdrgfrg43535dbDr4", 6945L, "/www.julianoimoveis.com.br");
        addFTPData("ftp.inetsoft.com.br", "inetsoft01", "PenseRBS", 7855L, "/clovisfreitas/hagah");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 7036L, "/3083XML");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 7077L, "/3031XML");
        addFTPData("177.71.188.45", "Pense", "4h21ui4y198hrnjds", 8995L, "/6498");
        addFTPData("mauroimoveis.imb.br", "penseimoveis", "WySiwYG", 7081L, "/penseimoveis");
        addFTPData("carga.rbs.com.br", "CODE49", "C0D349", 12491L, "/bono/hagah");
        addFTPData("penseimoveis.imovelpro.com.br", "penseimoveis", "rRbdrgfrg43535dbDr4", 7096L, "/www.costaosul.com");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 7136L, "/1201XML");
        addFTPData("gestor.plannertec.com.br", "hagahftp", "penseftp", 7169L, "/pense");
        addFTPData("177.71.188.45", "Pense", "4h21ui4y198hrnjds", 7230L, "/1649");
        addFTPData("177.71.188.45", "Pense", "4h21ui4y198hrnjds", 7237L, "/2853");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 7252L, "/6495");
        addFTPData("carga.rbs.com.br", "Tec-Imoveis", "0bdae98c", 7325L, "/giliancorretor");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 8028L, "/6529");
        addFTPData("carga.rbs.com.br", "kotel", "48daa8c2", 24947L, "/kotel-rede/cliente_pense");
        addFTPData("ftp.prediallisboa.provisorio.ws", "prediallisboa1", "Plis12*", 7429L, "/Web/hagah");
        addFTPData("carga.rbs.com.br", "agimovel", "85450355", 7438L, "/a39");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 7530L, "/6881");
        addFTPData("ftp.integracao.penseimoveis.com.br", "b2m", "b2m2015215", 7664L, "/renasa");
        addFTPData("ORG.hagah.samiweb.suprisoft.com.br", "hagah", "Xert772", 7680L, "/org/samiweb/hagah");
        addFTPData("repo.lenova.com.br", "penseimoveis", "79HZxPXT", 7691L, "/274611/hagah");
        addFTPData("ftp.abvaleimoveis.com.br", "pense@abvaleimoveis.com.br", "00y6bw83hosz", 7697L, "/hagah");
        addFTPData("177.71.188.45", "Pense", "4h21ui4y198hrnjds", 7715L, "/3837");
        addFTPData("cli20341.dominioimovel.com.br", "as_hagah", "I276959ooiL73627", 7786L, "/hagah/");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 7844L, "/573XML");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 7845L, "/7219");
        addFTPData("repo.lenova.com.br", "penseimoveis", "79HZxPXT", 7914L, "/90803/hagah");
        addFTPData("repo.lenova.com.br", "penseimoveis", "79HZxPXT", 7946L, "/247/hagah");
        addFTPData("repo.lenova.com.br", "penseimoveis", "79HZxPXT", 7947L, "/112702/hagah");
        addFTPData("177.71.188.45", "Pense", "4h21ui4y198hrnjds", 7976L, "/2911");
        addFTPData("penseimoveis.imovelpro.com.br", "penseimoveis", "rRbdrgfrg43535dbDr4", 8027L, "/www.helenaalugueis.com");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 8076L, "/9772");
        addFTPData("ftp.sobressai.com.br", "sobressai02", "penseimoveis", 8077L, "/422");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 8081L, "/1772/viamao");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 8082L, "/1772/vintequatrooutubro");
        addFTPData("177.71.188.45", "Pense", "4h21ui4y198hrnjds", 8083L, "/4043");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 8150L, "/738XML");
        addFTPData("177.71.188.45", "Pense", "4h21ui4y198hrnjds", 8197L, "/3036");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 11388L, "/1164XML");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 8221L, "/676XML");
        addFTPData("carga.rbs.com.br", "CODE49", "C0D349", 8266L, "/ikaza/hagah");
        addFTPData("177.71.188.45", "Pense", "4h21ui4y198hrnjds", 8270L, "/3384");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 8291L, "/7049");
        addFTPData("carga.rbs.com.br", "UNION", "fdffa368", 8342L, "/irineuimoveis");
        addFTPData("penseimoveis.imovelpro.com.br", "penseimoveis", "rRbdrgfrg43535dbDr4", 8767L, "/www.imobiliarialumini.com.br");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 8944L, "/706XML");
        addFTPData("177.71.188.45", "Pense", "4h21ui4y198hrnjds", 8945L, "/1978");
        addFTPData("177.71.188.45", "Pense", "4h21ui4y198hrnjds", 8956L, "/2945");
        addFTPData("repo.lenova.com.br", "penseimoveis", "79HZxPXT", 8967L, "/6949/hagah");
        addFTPData("ftp.dhiimoveis.com.br", "dhiimoveis", "d20202020", 8981L, "/www/pense");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 9010L, "/12158");
        addFTPData("bar.hagah.sami.suprisoft.com.br", "hagah", "Xert772", 25059L, "/cns/hagah");
        addFTPData("177.71.188.45", "Pense", "4h21ui4y198hrnjds", 9017L, "/3095");
        addFTPData("ftp.sobressai.com.br", "sobressai02", "penseimoveis", 9029L, "/336");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 9126L, "/730XML");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 9037L, "/723XML");
        addFTPData("177.71.188.45", "Pense", "4h21ui4y198hrnjds", 9048L, "/2139");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 9063L, "/1009XML");
        addFTPData("ftp.sobressai.com.br", "sobressai02", "penseimoveis", 9068L, "/338");
        addFTPData("repo.lenova.com.br", "penseimoveis", "79HZxPXT", 9099L, "/186903/hagah");
        addFTPData("177.71.188.45", "Pense", "4h21ui4y198hrnjds", 9111L, "/3351");
        addFTPData("carga.rbs.com.br", "agimovel", "85450355", 9118L, "/agimovel432");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 9123L, "/10050");
        addFTPData("bpi.hagah.sami.suprisoft.com.br", "hagah", "Xert772", 9124L, "/exa/hagah");
        addFTPData("carga.rbs.com.br", "MSInformatica", "097b3e3f", 9125L, "/jcc/pense");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 9129L, "/1026XML");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 9132L, "/1185XML");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 23288L, "/1229XML");
        addFTPData("repo.lenova.com.br", "penseimoveis", "79HZxPXT", 9136L, "/1577/hagah");
        addFTPData("177.71.188.45", "Pense", "4h21ui4y198hrnjds", 21538L, "/3899");
        addFTPData("177.71.188.45", "Pense", "4h21ui4y198hrnjds", 9176L, "/1865");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 9178L, "/10973");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 9210L, "/1083XML");
        addFTPData("177.71.188.45", "Pense", "4h21ui4y198hrnjds", 9211L, "/3673");
        addFTPData("penseimoveis.imovelpro.com.br", "penseimoveis", "rRbdrgfrg43535dbDr4", 9221L, "/www.lpimoveissc.com.br");
        addFTPData("penseimoveis.imovelpro.com.br", "penseimoveis", "rRbdrgfrg43535dbDr4", 9236L, "/www.affariimoveis.com.br");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 9242L, "/11298");
        addFTPData("177.71.188.45", "Pense", "4h21ui4y198hrnjds", 9247L, "/1710");
        addFTPData("imobiliariasupreema.com.br", "pense@imobiliariasupreema.com.br", "pense123", 24951L, "/");
        addFTPData("carga.rbs.com.br", "UNION", "fdffa368", 9294L, "/acrc");
        addFTPData("BPI.hagah.sami.samisistemas.com.br", "hagah", "Xert772", 9314L, "/inf/hagah");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 9315L, "/954XML");
        addFTPData("carga.rbs.com.br", "jungimoveis", "grupo@RBS123", 9318L, "/pense/santarita");
        addFTPData("ftp.bancoi.com.br", "bancoi68", "penseirs", 9327L, "/milcor/hagah");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 9334L, "/10179");
        addFTPData("carga.rbs.com.br", "RLSOFT", "grupo@123", 9341L, "/vanin");
        addFTPData("177.71.188.45", "Pense", "4h21ui4y198hrnjds", 9348L, "/3536");
        addFTPData("repo.lenova.com.br", "penseimoveis", "79HZxPXT", 9359L, "/62213/hagah");
        addFTPData("177.71.188.45", "Pense", "4h21ui4y198hrnjds", 9371L, "/2460");
        addFTPData("177.71.188.45", "Pense", "4h21ui4y198hrnjds", 9377L, "/1595");
        addFTPData("repo.lenova.com.br", "penseimoveis", "79HZxPXT", 9378L, "/30913/hagah");
        addFTPData("177.71.188.45", "Pense", "4h21ui4y198hrnjds", 9386L, "/1451");
        addFTPData("penseimoveis.imovelpro.com.br", "penseimoveis", "rRbdrgfrg43535dbDr4", 9486L, "/www.lasimoveisbc.com.br");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 9586L, "/10778");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 9786L, "/947XML");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 9836L, "/980XML");
        addFTPData("ftp.bancoi.com.br", "bancoi68", "penseirs", 9936L, "/condominiosdapraia/hagah");
        addFTPData("repo.lenova.com.br", "penseimoveis", "79HZxPXT", 10087L, "/34531/hagah");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 10138L, "/11234");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 10186L, "/1172XML");
        addFTPData("carga.rbs.com.br", "kuickfast", "3f020a17", 10236L, "/imcimoveis");
        addFTPData("177.71.188.45", "Pense", "4h21ui4y198hrnjds", 10237L, "/3553");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 10239L, "/10693");
        addFTPData("177.71.188.45", "Pense", "4h21ui4y198hrnjds", 10240L, "/2921");
        addFTPData("177.71.188.45", "Pense", "4h21ui4y198hrnjds", 10340L, "/1870");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 10341L, "/6535");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 10436L, "/1191XML");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 11038L, "/1089XML");
        addFTPData("ftp.sobressai.com.br", "sobressai02", "penseimoveis", 11136L, "/216");
        addFTPData("acaciaimovel.com.br", "pense@acaciaimovel.com.br", "pense123", 11087L, "/");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 11189L, "/10761");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 11287L, "/971XML");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 11340L, "/3061XML");
        addFTPData("177.71.188.45", "Pense", "4h21ui4y198hrnjds", 11539L, "/3795");
        addFTPData("ftp.sobressai.com.br", "sobressai02", "penseimoveis", 25184L, "/111");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 11638L, "/10265");
        addFTPData("177.71.188.45", "Pense", "4h21ui4y198hrnjds", 11739L, "/3844");
        addFTPData("177.71.188.45", "Pense", "4h21ui4y198hrnjds", 11693L, "/3819");
        addFTPData("ftp.sobressai.com.br", "sobressai02", "penseimoveis", 11789L, "/78");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 11791L, "/11856");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 11792L, "/11031");
        addFTPData("penseimoveis.savasimoveis.com.br", "penseimoveis", "B.#-H9@G", 11794L, "/");
        addFTPData("177.71.188.45", "Pense", "4h21ui4y198hrnjds", 11795L, "/3750");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 11992L, "/9809");
        addFTPData("ftp.bancoi.com.br", "bancoi68", "penseirs", 12090L, "/excellence-gramado/hagah");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 12138L, "/7473");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 24930L, "/12930");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 12639L, "/12801");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 12641L, "/1186XML");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 12892L, "/1039XML");
        addFTPData("carga.rbs.com.br", "MSInformatica", "097b3e3f", 12938L, "/sineiaimoveis/pense");
        addFTPData("ftp.sobressai.com.br", "sobressai02", "penseimoveis", 13088L, "/252");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 13139L, "/1044XML");
        addFTPData("carga.rbs.com.br", "UNION", "fdffa368", 13239L, "/trc");
        addFTPData("177.71.188.45", "Pense", "4h21ui4y198hrnjds", 13490L, "/3459");
        addFTPData("177.71.188.45", "Pense", "4h21ui4y198hrnjds", 14041L, "/6918");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 13542L, "/1051XML");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 13588L, "/1060XML");
        addFTPData("castelcd.com.br", "ccd_penseimoveis", "U*ZVwVFL", 13589L, "/vdimobiliaria");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 13739L, "/1070XML");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 13888L, "/1081XML");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 25227L, "/2672");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 13939L, "/1065XML");
        addFTPData("carga.rbs.com.br", "plug7", "eafd4ab0", 18488L, "/hagah/piasseta_1");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 14189L, "/1078XML");
        addFTPData("carga.rbs.com.br", "Tec-Imoveis", "0bdae98c", 14288L, "/lavitaimoveis");
        addFTPData("penseimoveis.imovelpro.com.br", "penseimoveis", "rRbdrgfrg43535dbDr4", 14538L, "/www.ezairimoveis.com.br");
        addFTPData("repo.lenova.com.br", "penseimoveis", "79HZxPXT", 15038L, "/10045/hagah");
        addFTPData("ftp.loderimoveis.com.br", "loderimoveis", "edu35231600", 15088L, "/www");
        addFTPData("carga.rbs.com.br", "viasw", "grupo@rbs123", 15191L, "/pense/alianca");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 15288L, "/1092XML");
        addFTPData("ftp.sobressai.com.br", "sobressai02", "penseimoveis", 15390L, "/335");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 15438L, "/1097XML");
        addFTPData("carga.rbs.com.br", "3_ILAB", "678568d2", 15739L, "/banck");
        addFTPData("penseimoveis.imovelpro.com.br", "penseimoveis", "rRbdrgfrg43535dbDr4", 15742L, "/www.sternimoveis.com.br");
        addFTPData("carga.rbs.com.br", "area_digital", "grupo@rbs123", 16289L, "/viverfloripa_pense");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 16438L, "/11436");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 16440L, "/1113XML");
        addFTPData("177.71.188.45", "Pense", "4h21ui4y198hrnjds", 16442L, "/5434");
        addFTPData("penseimoveis.imovelpro.com.br", "penseimoveis", "rRbdrgfrg43535dbDr4", 16588L, "/www.leticiabarcelosimoveis.com.br");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 16638L, "/1115XML");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 16739L, "/9685");
        addFTPData("realityhouse.com.br", "penseimoveis@realityhouse.com.br", "j3ip7x68P3", 16788L, "/");
        addFTPData("ftp.bancoi.com.br", "bancoi68", "penseirs", 16888L, "/unilar-gramado/hagah");
        addFTPData("carga.rbs.com.br", "CODE49", "C0D349", 25281L, "/camargo2355/hagah");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 16988L, "/1121XML");
        addFTPData("carga.rbs.com.br", "MSInformatica", "097b3e3f", 17488L, "/zecasthiago/pense");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 17789L, "/1149XML");
        addFTPData("penseimoveis.imovelpro.com.br", "penseimoveis", "rRbdrgfrg43535dbDr4", 17988L, "/www.tvimovelpauloteixeira.com.br");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 18190L, "/1163XML");
        addFTPData("carga.rbs.com.br", "jungimoveis", "grupo@RBS123", 18539L, "/pense/ximmoveis");
        addFTPData("repo.lenova.com.br", "penseimoveis", "79HZxPXT", 18638L, "/273141/hagah");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 18739L, "/12141");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 19788L, "/1169XML");
        addFTPData("ftp.novaeraimoveis.imb.br", "novaera_pense@novaeraimoveis.imb.br", "fQDiqCc6XfsB", 19838L, "/hagah");
        addFTPData("repo.lenova.com.br", "penseimoveis", "79HZxPXT", 19938L, "/300071/hagah");
        addFTPData("windows1.ftpmultiusuario.ws", "pense_blumenhouse@nativadesign", "pense663", 19991L, "/663");
        addFTPData("carga.rbs.com.br", "UNION", "fdffa368", 20038L, "/rafaelcassioimoveis");
        addFTPData("repo.lenova.com.br", "penseimoveis", "79HZxPXT", 20338L, "/167653/hagah");
        addFTPData("ftp.bancoi.com.br", "bancoi68", "penseirs", 20538L, "/pontodoimovel/hagah");
        addFTPData("repo.lenova.com.br", "penseimoveis", "79HZxPXT", 20588L, "/338322/hagah");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 20740L, "/1182XML");
        addFTPData("penseimoveis.imovelpro.com.br", "penseimoveis", "rRbdrgfrg43535dbDr4", 20838L, "/www.imobiliariapenha.com.br");
        addFTPData("177.71.188.45", "Pense", "4h21ui4y198hrnjds", 20888L, "/4751");
        addFTPData("ftp.inetsoft.com.br", "inetsoft01", "PenseRBS", 20938L, "/europa/hagah");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 21140L, "/1193XML");
        addFTPData("maerciaimoveis.com.br", "pense@maerciaimoveis.com.br", "pense123", 21189L, "/");
        addFTPData("penseimoveis.imovelpro.com.br", "penseimoveis", "rRbdrgfrg43535dbDr4", 21491L, "/www.inesimoveis.cim.br");
        addFTPData("ftp.imobiliariaup.com.br", "pense@imobiliariaup.com.br", "00y6bw83hosz", 21539L, "/hagah");
        addFTPData("177.71.188.45", "Pense", "4h21ui4y198hrnjds", 21540L, "/4364");
        addFTPData("repo.lenova.com.br", "penseimoveis", "79HZxPXT", 21542L, "/282051/hagah");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 21891L, "/1208XML");
        addFTPData("carga.rbs.com.br", "CODE49", "C0D349", 21988L, "/riccio/hagah");
        addFTPData("penseimoveis.imovelpro.com.br", "penseimoveis", "rRbdrgfrg43535dbDr4", 22041L, "/www.farolimoveis.com.br");
        addFTPData("177.71.188.45", "Pense", "4h21ui4y198hrnjds", 22089L, "/5231");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 22143L, "/1210XML");
        addFTPData("ftp.ferreira.imb.br", "ferreira", "1q2w3e4r5t", 22144L, "/httpdocs/xmlPenseImoveis");
        addFTPData("repo.lenova.com.br", "penseimoveis", "79HZxPXT", 22238L, "/303311/hagah");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 22438L, "/11338");
        addFTPData("carga.rbs.com.br", "viasw", "grupo@rbs123", 22539L, "/pense/alugar");
        addFTPData("penseimoveis.imovelpro.com.br", "penseimoveis", "rRbdrgfrg43535dbDr4", 22588L, "/www.almirimoveisembombinhassc.com.br");
        addFTPData("177.71.188.45", "Pense", "4h21ui4y198hrnjds", 22590L, "/4202");
        addFTPData("ftp.bancoi.com.br", "bancoi68", "penseirs", 22688L, "/rodrigues/hagah");
        addFTPData("177.71.188.45", "Pense", "4h21ui4y198hrnjds", 23138L, "/1587");
        addFTPData("carga.rbs.com.br", "Tec-Imoveis", "0bdae98c", 24934L, "/catharino");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 23338L, "/1230XML");
        addFTPData("carga.rbs.com.br", "viasw", "grupo@rbs123", 23538L, "/pense/kindel");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 23588L, "/11829");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 23341L, "/1217XML");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 23589L, "/3007XML");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 23888L, "/1232XML");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 23989L, "/11050");
        addFTPData("costanorteimoveis.com.br", "pense@costanorteimoveis.com.br", "pense123", 23990L, "/");
        addFTPData("ftp.regenteimoveis.com.br", "regenteimoveis1", "jr61g5av", 24138L, "/public_html/adm/Pense");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 24288L, "/1238XML");
        addFTPData("ftp.bancoi.com.br", "bancoi68", "penseirs", 24438L, "/euzebioimoveis/hagah");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 24907L, "/1772/praiadebelas");
        addFTPData("177.71.188.45", "Pense", "4h21ui4y198hrnjds", 24908L, "/4928");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 24918L, "/3010XML");
        addFTPData("carga.rbs.com.br", "MSInformatica", "097b3e3f", 24927L, "/coelho/pense");
        addFTPData("carga.rbs.com.br", "atodasistemas", "04424908", 24928L, "/casanativa");
        addFTPData("177.71.188.45", "Pense", "4h21ui4y198hrnjds", 24932L, "/5125");
        addFTPData("penseimoveis.imovelpro.com.br", "penseimoveis", "rRbdrgfrg43535dbDr4", 24940L, "/www.grannimoveis.com.br");
        addFTPData("penseimoveis.imovelpro.com.br", "penseimoveis", "rRbdrgfrg43535dbDr4", 24942L, "/www.cristianoimoveis.com.br");
        addFTPData("ftp.esrimoveis.com.br", "esrimoveis", "ED272C6ADDAA", 24944L, "/Web/hagah");
        addFTPData("carga.rbs.com.br", "Tec-Imoveis", "0bdae98c", 24945L, "/teerimoveis");
        addFTPData("repo.lenova.com.br", "penseimoveis", "79HZxPXT", 24948L, "/316981/hagah");
        addFTPData("ftp.integracao.penseimoveis.com.br", "suacasaimoveis", "su#401sa", 24949L, "/suacasaimoveis");
        addFTPData("ftp.hotimovel.com.br", "penseimoveis@hotimovel.com.br", "piHI#0215", 24950L, "/");
        addFTPData("177.71.188.45", "Pense", "4h21ui4y198hrnjds", 24953L, "/4425");
        addFTPData("carga.rbs.com.br", "CODE49", "C0D349", 24957L, "/5starimoveis/hagah");
        addFTPData("ftp.sobressai.com.br", "sobressai02", "penseimoveis", 24960L, "/238");
        addFTPData("carga.rbs.com.br", "viasw", "grupo@rbs123", 24963L, "/pense/wallau");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 24964L, "/8927");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 24967L, "/3059XML");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 24969L, "/3016XML");
        addFTPData("penseimoveis.imovelpro.com.br", "penseimoveis", "rRbdrgfrg43535dbDr4", 24970L, "/www.augustuscorretordeimoveis.com.br");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 24980L, "/12412");
        addFTPData("177.71.188.45", "Pense", "4h21ui4y198hrnjds", 24981L, "/5035");
        addFTPData("177.71.188.45", "Pense", "4h21ui4y198hrnjds", 24983L, "/5143");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 24986L, "/3015XML");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 24997L, "/3017XML");
        addFTPData("ftp.sobressai.com.br", "sobressai02", "penseimoveis", 25001L, "/471");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 25006L, "/3019XML");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 25009L, "/53");
        addFTPData("177.71.188.45", "Pense", "4h21ui4y198hrnjds", 25012L, "/4531");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 25019L, "/3025XML");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 25022L, "/3026XML");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 25026L, "/3035XML");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 25028L, "/3036XML");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 25032L, "/3028XML");
        addFTPData("repo.lenova.com.br", "penseimoveis", "79HZxPXT", 25040L, "/271151/hagah");
        addFTPData("ftp.sobressai.com.br", "sobressai02", "penseimoveis", 25047L, "/442");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 25055L, "/3032XML");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 25057L, "/3033XML");
        addFTPData("ftp.britoimoveisrs.com.br", "britoimo", "d2x5v3p0", 25069L, "/public_html/hagah");
        addFTPData("castelcd.com.br", "ccd_penseimoveis", "U*ZVwVFL", 25070L, "/beconciimoveis");
        addFTPData("ftp.sobressai.com.br", "sobressai02", "penseimoveis", 25075L, "/437");
        addFTPData("177.71.188.45", "Pense", "4h21ui4y198hrnjds", 25074L, "/5538");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 25080L, "/3052XML");
        addFTPData("repo.lenova.com.br", "penseimoveis", "79HZxPXT", 25085L, "/309421/hagah");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 25087L, "/3040XML");
        addFTPData("repo.lenova.com.br", "penseimoveis", "79HZxPXT", 25094L, "/255853/hagah");
        addFTPData("repo.lenova.com.br", "penseimoveis", "79HZxPXT", 25101L, "/49123/hagah");
        addFTPData("repo.lenova.com.br", "penseimoveis", "79HZxPXT", 25110L, "/342732/hagah");
        addFTPData("ftp.integracao.penseimoveis.com.br", "lopez_sc", "L0p3z1205", 25116L, "/lopes_sc");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 25122L, "/3044XML");
        addFTPData("repo.lenova.com.br", "penseimoveis", "79HZxPXT", 25123L, "/292901/hagah");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 25132L, "/6739");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 25133L, "/3047XML");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 25135L, "/3050XML");
        addFTPData("ftp.sobressai.com.br", "sobressai02", "penseimoveis", 25137L, "/139");
        addFTPData("177.71.188.45", "Pense", "4h21ui4y198hrnjds", 25138L, "/6020");
        addFTPData("repo.lenova.com.br", "penseimoveis", "79HZxPXT", 25140L, "/8023/hagah");
        addFTPData("repo.lenova.com.br", "penseimoveis", "79HZxPXT", 25142L, "/71883/hagah");
        addFTPData("ftp.sobressai.com.br", "sobressai02", "penseimoveis", 25146L, "/438");
        addFTPData("177.71.188.45", "Pense", "4h21ui4y198hrnjds", 25150L, "/5469");
        addFTPData("repo.lenova.com.br", "penseimoveis", "79HZxPXT", 25153L, "/11071/hagah");
        addFTPData("repo.lenova.com.br", "penseimoveis", "79HZxPXT", 25156L, "/274551/hagah");
        addFTPData("177.71.188.45", "Pense", "4h21ui4y198hrnjds", 25159L, "/4467");
        addFTPData("ftp.sobressai.com.br", "sobressai02", "penseimoveis", 25160L, "/353");
        addFTPData("windows1.ftpmultiusuario.ws", "blumenau_pense@nativadesign", "pense400", 25161L, "/400");
        addFTPData("penseimoveis.imovelpro.com.br", "penseimoveis", "rRbdrgfrg43535dbDr4", 25162L, "/www.intermediacao.imb.br");
        addFTPData("repo.lenova.com.br", "penseimoveis", "79HZxPXT", 25163L, "/314991/hagah");
        addFTPData("penseimoveis.imovelpro.com.br", "penseimoveis", "rRbdrgfrg43535dbDr4", 25164L, "/www.amaralimoveisbc.com.br");
        addFTPData("cli22452.dominioimovel.com.br", "crilarimoveis_hagah", "Tb7rM1w1N0D2vgU9", 25165L, "/hagah");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 25168L, "/11853");
        addFTPData("ftp.bancoi.com.br", "bancoi68", "penseirs", 25169L, "/apinvestimentos/hagah");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 25176L, "/3053XML");
        addFTPData("repo.lenova.com.br", "penseimoveis", "79HZxPXT", 25180L, "/336052/hagah");
        addFTPData("penseimoveis.imovelpro.com.br", "penseimoveis", "rRbdrgfrg43535dbDr4", 25185L, "/www.imoveisguilherme.com.br");
        addFTPData("177.71.188.45", "Pense", "4h21ui4y198hrnjds", 25186L, "/2802");
        addFTPData("carga.rbs.com.br", "agimovel", "85450355", 25187L, "/agimovel2041");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 25195L, "/3056XML");
        addFTPData("penseimoveis.imovelpro.com.br", "penseimoveis", "rRbdrgfrg43535dbDr4", 25198L, "/www.confiarebc.com.br");
        addFTPData("penseimoveis.imovelpro.com.br", "penseimoveis", "rRbdrgfrg43535dbDr4", 25199L, "/www.imobillenegocios.com");
        addFTPData("ftp.sobressai.com.br", "sobressai02", "penseimoveis", 25200L, "/405");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 25201L, "/12599");
        addFTPData("177.71.188.45", "Pense", "4h21ui4y198hrnjds", 25202L, "/2342");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 25204L, "/12841");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 25209L, "/9651");
        addFTPData("177.71.188.45", "Pense", "4h21ui4y198hrnjds", 25210L, "/2379");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 25211L, "/3060XML");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 25216L, "/12860");
        addFTPData("carga.rbs.com.br", "vegasistemas", "ab2798d1", 25217L, "/fernando");
        addFTPData("penseimoveis.imovelpro.com.br", "penseimoveis", "rRbdrgfrg43535dbDr4", 25218L, "/www.lucianiimoveis.com.br");
        addFTPData("177.71.188.45", "Pense", "4h21ui4y198hrnjds", 25222L, "/2334");
        addFTPData("penseimoveis.imovelpro.com.br", "penseimoveis", "rRbdrgfrg43535dbDr4", 25229L, "/www.getulioimoveis.com.br");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 25232L, "/193XML");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 25236L, "/3067XML");
        addFTPData("imobiliariabehling.com.br", "pense@imobiliariabehling.com.br", "behlingpi", 25237L, "/hagah");
        addFTPData("177.71.188.45", "Pense", "4h21ui4y198hrnjds", 25238L, "/4292");
        addFTPData("penseimoveis.imovelpro.com.br", "penseimoveis", "rRbdrgfrg43535dbDr4", 25243L, "/www.partalaimoveis.com.br");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 25244L, "/3062XML");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 25247L, "/12924");
        addFTPData("ftp.sobressai.com.br", "sobressai02", "penseimoveis", 25248L, "/480");
        addFTPData("ftp.sobressai.com.br", "sobressai02", "penseimoveis", 25249L, "/288");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 25250L, "/3064XML");
        addFTPData("penseimoveis.imovelpro.com.br", "penseimoveis", "rRbdrgfrg43535dbDr4", 25258L, "/www.imoveismundial.com.br");
        addFTPData("windows1.ftpmultiusuario.ws", "pense_edifique@nativadesign", "pense333", 25260L, "/333");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 25264L, "/3070XML");
        addFTPData("carga.rbs.com.br", "agimovel", "85450355", 25267L, "/pedrazzi");
        addFTPData("ftp.bancoi.com.br", "bancoi68", "penseirs", 9353L, "/ferrari-paraguassu/hagah");
        addFTPData("ftp.bancoi.com.br", "bancoi68", "penseirs", 9388L, "/ferrari-ubirajara/hagah");
        addFTPData("carga.rbs.com.br", "BR_Brokers", "73fe0139", 24545L, "/noblesse/moinhos");
        addFTPData("realizaimoveis.com.br", "pense@realizaimoveis.com.br", "realizapi", 1918L, "/hagah");
        addFTPData("177.71.188.45", "Pense", "4h21ui4y198hrnjds", 2746L, "/3951");
        addFTPData("177.71.188.45", "Pense", "4h21ui4y198hrnjds", 2747L, "/3941");
        addFTPData("177.71.188.45", "Pense", "4h21ui4y198hrnjds", 2751L, "/3948");
        addFTPData("177.71.188.45", "Pense", "4h21ui4y198hrnjds", 7051L, "/3946");
        addFTPData("177.71.188.45", "Pense", "4h21ui4y198hrnjds", 7052L, "/3944");
        addFTPData("177.71.188.45", "Pense", "4h21ui4y198hrnjds", 7656L, "/3937");
        addFTPData("177.71.188.45", "Pense", "4h21ui4y198hrnjds", 7657L, "/3835");
        addFTPData("177.71.188.45", "Pense", "4h21ui4y198hrnjds", 7660L, "/3838");
        addFTPData("177.71.188.45", "Pense", "4h21ui4y198hrnjds", 17039L, "/3840");
        addFTPData("177.71.188.45", "Pense", "4h21ui4y198hrnjds", 17040L, "/3947");
        addFTPData("177.71.188.45", "Pense", "4h21ui4y198hrnjds", 24972L, "/4327");
        addFTPData("177.71.188.45", "Pense", "4h21ui4y198hrnjds", 24973L, "/4321");
        addFTPData("177.71.188.45", "Pense", "4h21ui4y198hrnjds", 24975L, "/3943");
        addFTPData("177.71.188.45", "Pense", "4h21ui4y198hrnjds", 24976L, "/3942");
        addFTPData("177.71.188.45", "Pense", "4h21ui4y198hrnjds", 24977L, "/3940");
        addFTPData("177.71.188.45", "Pense", "4h21ui4y198hrnjds", 24978L, "/3939");
        addFTPData("177.71.188.45", "Pense", "4h21ui4y198hrnjds", 24979L, "/3935");
        addFTPData("177.71.188.45", "Pense", "4h21ui4y198hrnjds", 25194L, "/5447");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 2538L, "/1772/avenidadoforte");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 6482L, "/1772/ipiranga");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 6650L, "/1772/humaita");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 7040L, "/1772/balneariocamboriu");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 7494L, "/1772/guaiba");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 7495L, "/1772/lajeado");
        addFTPData("carga.rbs.com.br", "CODE49", "C0D349", 8129L, "/auxaluguel/pense/gramado");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 9390L, "/1772/canela");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 12888L, "/1772/central_parque");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 14892L, "/1772/zonanorte");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 15192L, "/1772/parquegermania");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 15791L, "/1772/novaipanema");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 21738L, "/1772/joaoabbott");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 24937L, "/1772/garibaldi");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 25272L, "/3072XML");
        addFTPData("carga.rbs.com.br", "CODE49", "C0D349", 8125L, "/auxaluguel/pense/novohamburgo");
        addFTPData("carga.rbs.com.br", "CODE49", "C0D349", 8121L, "/auxaluguel/pense/centro");
        addFTPData("carga.rbs.com.br", "CODE49", "C0D349", 8122L, "/auxaluguel/pense/floresta");
        addFTPData("carga.rbs.com.br", "CODE49", "C0D349", 8123L, "/auxaluguel/pense/meninodeus");
        addFTPData("carga.rbs.com.br", "CODE49", "C0D349", 8128L, "/auxaluguel/pense/petropolis");
        addFTPData("carga.rbs.com.br", "CODE49", "C0D349", 8130L, "/auxaluguel/pense/preference");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 24906L, "/1772/plinio");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 25066L, "/1772/venancioaires");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 25128L, "/1772/carosgomes1206");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 25129L, "/1772/carlosgomes1205");
        addFTPData("carga.rbs.com.br", "CODE49", "C0D349", 8127L, "/auxaluguel/pense/zonanorte");
        addFTPData("penseimoveis.imovelpro.com.br", "penseimoveis", "rRbdrgfrg43535dbDr4", 25285L, "/www.litoralbcimoveis.com");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 25288L, "/1772/country");
        addFTPData("ftp.sobressai.com.br", "sobressai02", "penseimoveis", 25294L, "/173");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 25296L, "/11982");
        addFTPData("ftp.integracao.penseimoveis.com.br", "Vista", "a1da63f7", 25295L, "/2715");
        addFTPData("repo.lenova.com.br", "penseimoveis", "79HZxPXT", 25298L, "/341432/hagah");
        addFTPData("penseimoveis.imovelpro.com.br", "penseimoveis", "rRbdrgfrg43535dbDr4", 25299L, "/www.imovesigandolfi.com.br");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 25300L, "/3082XML");
        addFTPData("carga.rbs.com.br", "Tec-Imoveis", "0bdae98c", 25304L, "/virtualimoveis");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 25305L, "/3078XML");
        addFTPData("carga.rbs.com.br", "plug7", "eafd4ab0", 25309L, "/hagah/nobilitare_1");
        addFTPData("penseimoveis.imovelpro.com.br", "penseimoveis", "rRbdrgfrg43535dbDr4", 25311L, "/www.imoveismd.com.br");
        addFTPData("177.71.188.45", "Pense", "4h21ui4y198hrnjds", 25313L, "/9403");
        addFTPData("ftp.integracao.penseimoveis.com.br", "siemti", "ftp@258", 25315L, "/3081XML");
        addFTPData("penseimoveis.imovelpro.com.br", "penseimoveis", "rRbdrgfrg43535dbDr4", 25316L, "/www.imoveiscv.com.br");
    }
    
    
}
