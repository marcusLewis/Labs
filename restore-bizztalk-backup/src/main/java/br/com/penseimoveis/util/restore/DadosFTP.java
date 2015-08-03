package br.com.penseimoveis.util.restore;

import java.io.IOException;
import java.net.SocketException;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

public class DadosFTP {

    private String ftp;
    private String usuario;
    private String senha;
    private String pathXml;
    private Long contrato;
    
    public DadosFTP(String ftp, String usuario, String senha, Long contrato, String pathXML) {
        this.ftp = ftp;
        this.usuario = usuario;
        this.senha = senha;
        this.pathXml = pathXML;
        this.contrato = contrato;
    }
    
    public FTPClient connect() throws SocketException, IOException {
        FTPClient ret = new FTPClient(); 
    
        ret.connect(this.getFtp());
        
        int replyCode = ret.getReplyCode();
        if (!FTPReply.isPositiveCompletion(replyCode)) {
            throw new IOException("Não foi possivel conectar no servidor. Server reply code: " + replyCode);
        }
        
        
        boolean login = ret.login(this.getUsuario(), this.getSenha());
        
        if (!login) {
            throw new IOException("Não foi possivel conectar ao ftp");
        }
        
        return ret;
    }
    
    public String getFtp() {
        return ftp;
    }

    public void setFtp(String ftp) {
        this.ftp = ftp;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getPathXml() {
        return pathXml;
    }

    public void setPathXml(String pathXml) {
        this.pathXml = pathXml;
    }

    public Long getContrato() {
        return contrato;
    }

    public void setContrato(Long contrato) {
        this.contrato = contrato;
    }

}
