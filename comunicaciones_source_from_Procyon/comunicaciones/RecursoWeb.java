// 
// Decompiled by Procyon v0.5.36
// 

package comunicaciones;

import java.util.Iterator;
import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.net.URL;

public class RecursoWeb
{
    private URL url;
    private Map<String, List<String>> cabeceras;
    private String nombreFichero;
    private String textoCabeceras;
    private String textoCuerpo;
    private int codigoRespuesta;
    private Date instanteCreacion;
    private long tamanoRecurso;
    
    protected RecursoWeb(final URL url, final Map<String, List<String>> cabeceras, final String s, final String textoCuerpo) {
        this.codigoRespuesta = -1;
        if ((s != null && textoCuerpo != null) || (s == null && textoCuerpo == null)) {
            throw new IllegalArgumentException("Ha de especificarse o bien el fichero o bien el cuerpo.");
        }
        this.url = url;
        this.cabeceras = cabeceras;
        this.nombreFichero = s;
        this.textoCuerpo = textoCuerpo;
        this.generarTextoCabeceras();
        this.instanteCreacion = new Date();
        if (s != null) {
            this.tamanoRecurso = new File(s).length();
        }
        else {
            this.tamanoRecurso = textoCuerpo.length();
        }
    }
    
    public URL getUrl() {
        return this.url;
    }
    
    protected String getTextoCabeceras() {
        return this.textoCabeceras + "Age: " + (new Date().getTime() - this.instanteCreacion.getTime()) / 1000L + "\r\n";
    }
    
    public String getFichero() {
        return this.nombreFichero;
    }
    
    public long getTamano() {
        return this.tamanoRecurso;
    }
    
    protected String getTextoCuerpo() {
        return this.textoCuerpo;
    }
    
    protected int getCodigoRespuesta() {
        return this.codigoRespuesta;
    }
    
    protected void setCodigoRespuesta(final int codigoRespuesta) {
        this.codigoRespuesta = codigoRespuesta;
    }
    
    protected boolean tieneFichero() {
        return this.nombreFichero != null;
    }
    
    private void generarTextoCabeceras() {
        boolean b = false;
        this.textoCabeceras = "";
        for (final String str : this.cabeceras.keySet()) {
            final String s = this.cabeceras.get(str).iterator().next();
            if (str == null) {
                this.textoCabeceras = this.textoCabeceras + s + "\r\n";
            }
            else if (str.compareToIgnoreCase("Connection") != 0 && str.compareToIgnoreCase("Keep-Alive") != 0 && s.compareToIgnoreCase("chunked") != 0) {
                this.textoCabeceras = this.textoCabeceras + str + ": " + s + "\r\n";
            }
            if (str != null && str.compareToIgnoreCase("Content-Length") != 0) {
                b = true;
            }
        }
        if (!b) {
            this.textoCabeceras += "Proxy-Connection: close\r\n";
        }
    }
}
