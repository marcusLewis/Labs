package br.com.penseimoveis.util.restore;

public class Constants {

    public static final String EXCEL_FILE_NAME = "Bizztalk_backup.xls";
    
    public static final String TXT_FILE_NAME = "contratos-restore.txt";
    
    
    public static final int IDX_COLUNA_CONTRATO = 0;
    public static final int IDX_COLUNA_DATA = 1;
    public static final int IDX_COLUNA_PASTA = 2;
    public static final int IDX_COLUNA_ARQUIVO = 3;
    
    public static final DadosFTP FTP_BACKUP_BIZZTALK = new DadosFTP("ftp.integracao.penseimoveis.com.br", "manoel_correa", "$ivY%ncktn7PfMyGvG%s", null, null);
    
    
}
