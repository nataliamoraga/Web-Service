// 
// Decompiled by Procyon v0.5.36
// 

package comunicaciones;

import java.util.Iterator;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class Cliente
{
    private static final int TAM_BLOQUE = 8192;
    private static final int MAX_MENSAJE = 65536;
    private SocketChannel socket;
    private ByteBuffer bufferEntrada;
    private Parametros parametros;
    
    protected Cliente(final SocketChannel socket, final Parametros parametros) {
        this.socket = socket;
        this.bufferEntrada = ByteBuffer.allocate(65536);
        this.parametros = parametros;
    }
    
    public void enviarRespuesta(final RecursoWeb recursoWeb) throws FileNotFoundException {
        FileInputStream fileInputStream = null;
        try {
            final byte[] bytes = (recursoWeb.getTextoCabeceras() + "\r\n").getBytes();
            this.enviarBloque(bytes, bytes.length);
            final String fichero = recursoWeb.getFichero();
            if (fichero != null) {
                if (this.parametros.debug > 1) {
                    System.err.format("Cliente: EnviarRespuesta '%s'.\n", fichero);
                }
                fileInputStream = new FileInputStream(fichero);
                final byte[] b = new byte[8192];
                int read;
                while ((read = fileInputStream.read(b)) != -1) {
                    this.enviarBloque(b, read);
                }
            }
            else {
                final byte[] bytes2 = recursoWeb.getTextoCuerpo().getBytes();
                this.enviarBloque(bytes2, bytes2.length);
            }
            if (recursoWeb.getTextoCabeceras().indexOf("Content-Length: ") == -1) {
                this.socket.socket().shutdownOutput();
            }
        }
        catch (IOException ex) {}
        finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                }
                catch (IOException ex2) {}
            }
        }
    }
    
    protected void enviarCodigo(final RecursoWeb recursoWeb, final int i, final String s) {
        if (this.parametros.debug > 1) {
            System.err.format("Cliente: Enviar Codigo '%d - %s'.\n", i, s);
        }
        try {
            final byte[] bytes = (recursoWeb.getTextoCabeceras() + "\r\n").getBytes();
            this.enviarBloque(bytes, bytes.length);
            this.socket.socket().shutdownOutput();
        }
        catch (IOException ex) {}
    }
    
    protected ByteBuffer getBufferEntrada() {
        return this.bufferEntrada;
    }
    
    private void enviarBloque(final byte[] array, final int length) throws IOException {
        final ByteBuffer wrap = ByteBuffer.wrap(array, 0, length);
        final Selector open = Selector.open();
        this.socket.register(open, 4);
        while (wrap.remaining() > 0) {
            open.select();
            for (final SelectionKey selectionKey : open.selectedKeys()) {
                if ((selectionKey.readyOps() & 0x4) == 0x4) {
                    ((SocketChannel)selectionKey.channel()).write(wrap);
                }
            }
        }
        open.close();
    }
}
