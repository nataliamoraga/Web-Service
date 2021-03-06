// 
// Decompiled by Procyon v0.5.36
// 

package comunicaciones;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.io.DataInputStream;
import java.net.HttpURLConnection;
import java.io.OutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.util.Date;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.StringTokenizer;
import java.io.Reader;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ByteArrayInputStream;
import java.net.URL;

public class Peticion
{
    private Cliente cliente;
    private URL url;
    private Parametros parametros;
    private static final int TAM_BLOQUE = 8192;
    
    protected Peticion(final byte[] buf, final Cliente cliente, final Parametros parametros) throws IOException, MalformedURLException {
        this.parametros = parametros;
        final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(buf)));
        final int n = 80;
        final String s = "http";
        String file = null;
        String nextToken = null;
        if (this.parametros.debug > 2) {
            System.err.println("ServidorRed: Leo petici\u00f3n\n=================================");
        }
        String line;
        while ((line = bufferedReader.readLine()) != null && line.compareTo("") != 0) {
            if (this.parametros.debug > 2) {
                System.err.println(line);
            }
            if (line.startsWith("GET ")) {
                file = new StringTokenizer(line.substring(4), " \r").nextToken();
            }
            else if (line.startsWith("POST ")) {
                file = new StringTokenizer(line.substring(5), " \r").nextToken();
            }
            else {
                if (!line.startsWith("Host: ")) {
                    continue;
                }
                nextToken = new StringTokenizer(line.substring(6), " \r").nextToken();
            }
        }
        if (line == null) {
            throw new MalformedURLException("Petici\u00f3n demasiado larga.");
        }
        if (this.parametros.debug > 2) {
            System.err.println("=================================\nServidorRed: construyendo Petici\u00f3n(" + s + "," + nextToken + "," + n + "," + file + ")");
        }
        if (file == null) {
            throw new MalformedURLException("No se ha detectado la URL");
        }
        if (!file.startsWith("/")) {
            this.url = new URL(file);
        }
        else {
            this.url = new URL(s, nextToken, n, file);
        }
        this.cliente = cliente;
    }
    
    public Cliente getCliente() {
        return this.cliente;
    }
    
    public URL getURL() {
        return this.url;
    }
    
    public RecursoWeb descargar() {
        final String string = this.parametros.directorioCache + "/" + "cacheweb-recurso-tmp-" + new Date().getTime() + "-" + this.hashCode() + ".tmp";
        RecursoWeb generarErrorAlVuelo = null;
        if (this.parametros.debug > 1) {
            System.err.format("ServidorRed: descargando '%s' en %s.\n", this.url, string);
        }
        DataInputStream dataInputStream = null;
        DataOutputStream dataOutputStream = null;
        try {
            try {
                dataOutputStream = new DataOutputStream(new FileOutputStream(string));
                final byte[] array = new byte[8192];
                final HttpURLConnection httpURLConnection = (HttpURLConnection)this.url.openConnection(this.parametros.proxy);
                try {
                    dataInputStream = new DataInputStream(httpURLConnection.getInputStream());
                }
                catch (IOException ex) {
                    if (httpURLConnection.getResponseCode() == -1) {
                        throw ex;
                    }
                    dataInputStream = new DataInputStream(httpURLConnection.getErrorStream());
                }
                int read;
                while ((read = dataInputStream.read(array, 0, 8192)) != -1) {
                    dataOutputStream.write(array, 0, read);
                }
                generarErrorAlVuelo = new RecursoWeb(this.url, httpURLConnection.getHeaderFields(), string, null);
                generarErrorAlVuelo.setCodigoRespuesta(httpURLConnection.getResponseCode());
            }
            finally {
                if (dataInputStream != null) {
                    dataInputStream.close();
                }
                if (dataOutputStream != null) {
                    dataOutputStream.close();
                }
            }
        }
        catch (Exception ex2) {
            generarErrorAlVuelo = this.generarErrorAlVuelo(ex2);
        }
        if (this.parametros.debug >= 5) {
            System.err.println("Cabeceras recibidas:\n********************************");
            System.err.println(generarErrorAlVuelo.getTextoCabeceras());
            System.err.println("********************************");
        }
        return generarErrorAlVuelo;
    }
    
    private RecursoWeb generarErrorAlVuelo(final Exception ex) {
        final String string = "" + "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">" + "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">" + "<title>ERROR: no se ha podido descargar el URL solicitado</title>" + "</head><body>" + "<h2>No se ha podido descargar el URL solicitado</h2>" + "<p><strong>URL:</strong><a href=\"" + this.url + "\">" + this.url + "</a></p>" + "<p><strong>Motivo del error: </B>" + ex.getLocalizedMessage() + "</p>" + "<p>P\u00e1gina de error generada autom\u00e1ticamente por el paquete de comunicaciones del cach\u00e9-web.</p>" + "<!-- Esto es basurilla para que el Internet Explorer no muestre su propio mensaje de error, sino el que se le env\u00eda. Hay que generar una p\u00e1gina con m\u00e1s de 512 bytes de tama\u00f1o. -->" + "<!-- Esto es basurilla para que el Internet Explorer no muestre su propio mensaje de error, sino el que se le env\u00eda. Hay que generar una p\u00e1gina con m\u00e1s de 512 bytes de tama\u00f1o. -->" + "<!-- Esto es basurilla para que el Internet Explorer no muestre su propio mensaje de error, sino el que se le env\u00eda. Hay que generar una p\u00e1gina con m\u00e1s de 512 bytes de tama\u00f1o. -->" + "<!-- Esto es basurilla para que el Internet Explorer no muestre su propio mensaje de error, sino el que se le env\u00eda. Hay que generar una p\u00e1gina con m\u00e1s de 512 bytes de tama\u00f1o. -->" + "<!-- Esto es basurilla para que el Internet Explorer no muestre su propio mensaje de error, sino el que se le env\u00eda. Hay que generar una p\u00e1gina con m\u00e1s de 512 bytes de tama\u00f1o. -->" + "<!-- Esto es basurilla para que el Internet Explorer no muestre su propio mensaje de error, sino el que se le env\u00eda. Hay que generar una p\u00e1gina con m\u00e1s de 512 bytes de tama\u00f1o. -->" + "</body></html>";
        final HashMap<Object, ArrayList<String>> hashMap = new HashMap<Object, ArrayList<String>>();
        final ArrayList<String> list = new ArrayList<String>();
        list.add("HTTP/1.0 503 Servicio no disponible");
        hashMap.put(null, list);
        final ArrayList<String> list2 = new ArrayList<String>();
        list2.add("CacheWeb SSOO 2011");
        hashMap.put("Server", list2);
        final ArrayList<String> list3 = new ArrayList<String>();
        list3.add("1.0");
        hashMap.put("Mime-Version", list3);
        final ArrayList<String> list4 = new ArrayList<String>();
        list4.add(new Date().toString());
        hashMap.put("Date", list4);
        hashMap.put("Expires", list4);
        final ArrayList<String> list5 = new ArrayList<String>();
        list5.add("text/html");
        hashMap.put("Content-Type", list5);
        final ArrayList<String> list6 = new ArrayList<String>();
        list6.add("" + string.length());
        hashMap.put("Content-Length", list6);
        final RecursoWeb recursoWeb = new RecursoWeb(this.url, (Map<String, List<String>>)hashMap, null, string);
        recursoWeb.setCodigoRespuesta(503);
        return recursoWeb;
    }
}
