// 
// Decompiled by Procyon v0.5.36
// 

package comunicaciones;

import java.net.ServerSocket;
import java.net.SocketAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.Set;
import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Hashtable;
import java.util.concurrent.BlockingQueue;
import java.nio.channels.ServerSocketChannel;
import java.io.File;

public class ServidorRed implements Runnable
{
    public static final String TMP_PREFIJO = "cacheweb-recurso-tmp-";
    private File directorioCache;
    private ServerSocketChannel socketAceptacion;
    private BlockingQueue<Peticion> colaPeticiones;
    private int puertoEscuchaDeseado;
    private Parametros parametros;
    
    public ServidorRed(final BlockingQueue<Peticion> colaPeticiones, final Parametros parametros) throws IllegalArgumentException {
        if (colaPeticiones == null) {
            throw new IllegalArgumentException("Debe especificarse la cola de peticiones");
        }
        this.colaPeticiones = colaPeticiones;
        this.puertoEscuchaDeseado = parametros.puerto;
        this.parametros = parametros;
    }
    
    @Override
    public void run() {
        final Hashtable<SocketChannel, Cliente> hashtable = new Hashtable<SocketChannel, Cliente>();
        try {
            this.crearDirectorioCache();
            synchronized (this) {
                this.socketAceptacion = this.crearSocketAceptacion();
                this.notifyAll();
            }
            final Selector open = Selector.open();
            this.socketAceptacion.configureBlocking(false);
            this.socketAceptacion.register(open, 16);
        Label_0064_Outer:
            while (true) {
                while (true) {
                    try {
                        while (true) {
                            if (open.select() == 0) {
                                continue Label_0064_Outer;
                            }
                            final Set<SelectionKey> selectedKeys = open.selectedKeys();
                            for (final SelectionKey selectionKey : selectedKeys) {
                                if ((selectionKey.readyOps() & 0x10) == 0x10) {
                                    final SocketChannel accept = this.socketAceptacion.accept();
                                    accept.configureBlocking(false);
                                    accept.register(open, 1);
                                    hashtable.put(accept, new Cliente(accept, this.parametros));
                                }
                                else {
                                    if ((selectionKey.readyOps() & 0x1) != 0x1) {
                                        continue Label_0064_Outer;
                                    }
                                    final SocketChannel key = (SocketChannel)selectionKey.channel();
                                    try {
                                        final Cliente cliente = hashtable.get(key);
                                        final ByteBuffer bufferEntrada = cliente.getBufferEntrada();
                                        if (key.read(bufferEntrada) == -1) {
                                            if (this.parametros.debug > 1) {
                                                System.err.println("Servidor Red: Cliente desconectado");
                                            }
                                            selectionKey.channel().close();
                                            selectionKey.cancel();
                                            hashtable.remove(key);
                                        }
                                        else {
                                            if (bufferEntrada.position() < 4) {
                                                continue Label_0064_Outer;
                                            }
                                            bufferEntrada.position(bufferEntrada.position() - 4);
                                            final byte[] dst = new byte[4];
                                            bufferEntrada.get(dst);
                                            if (dst[0] != 13 || dst[1] != 10 || dst[2] != 13 || dst[3] != 10) {
                                                continue Label_0064_Outer;
                                            }
                                            final byte[] dst2 = new byte[bufferEntrada.position()];
                                            bufferEntrada.position(0);
                                            bufferEntrada.get(dst2);
                                            bufferEntrada.rewind();
                                            final Peticion peticion = new Peticion(dst2, cliente, this.parametros);
                                            if (peticion == null) {
                                                continue Label_0064_Outer;
                                            }
                                            try {
                                                if (this.parametros.debug > 1) {
                                                    System.err.format("Servidor Red: encolando petici\u00f3n para '%s'.\n", peticion.getURL());
                                                }
                                                this.colaPeticiones.put(peticion);
                                                if (this.parametros.debug <= 1) {
                                                    continue Label_0064_Outer;
                                                }
                                                System.err.format("Servidor Red:petici\u00f3n '%s' encolada.\n", peticion.getURL());
                                            }
                                            catch (InterruptedException ex) {}
                                        }
                                    }
                                    catch (IOException ex2) {
                                        selectionKey.channel().close();
                                        selectionKey.cancel();
                                        hashtable.remove(key);
                                    }
                                }
                            }
                            selectedKeys.clear();
                        }
                    }
                    catch (IOException x) {
                        System.err.println("XXXXX\nExcepci\u00f3n detectada en el hilo servidor de red\nXXXXX");
                        System.err.println(x);
                        x.printStackTrace(System.err);
                        continue Label_0064_Outer;
                    }
                    continue;
                }
            }
        }
        catch (IOException x2) {
            System.err.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXX\nExcepci\u00f3n detectada en el hilo servidor de red\nXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
            System.err.println(x2);
            x2.printStackTrace(System.err);
        }
    }
    
    private void crearDirectorioCache() {
        this.directorioCache = new File(this.parametros.directorioCache);
        if (this.directorioCache.exists()) {
            if (!this.directorioCache.isDirectory()) {
                throw new RuntimeException("El directorio temporal para recursos descargados en realidad no es un directorio.");
            }
            for (final File file : this.directorioCache.listFiles()) {
                if (file.canWrite() && file.getName().startsWith("cacheweb-recurso-tmp-")) {
                    file.delete();
                }
            }
        }
        else if (!this.directorioCache.mkdir()) {
            throw new RuntimeException("No se ha podido crear el directorio temporal para recursos descargados.");
        }
    }
    
    private ServerSocketChannel crearSocketAceptacion() throws IOException {
        final ServerSocketChannel open = ServerSocketChannel.open();
        final ServerSocket socket = open.socket();
        int n = 0;
        for (int puertoEscuchaDeseado = this.puertoEscuchaDeseado; puertoEscuchaDeseado < 65000 && n == 0; ++puertoEscuchaDeseado) {
            try {
                socket.bind(new InetSocketAddress(puertoEscuchaDeseado), 5);
                n = 1;
            }
            catch (IOException ex) {}
        }
        if (n == 0) {
            throw new RuntimeException("No se ha podido crear el socket de escucha.");
        }
        return open;
    }
    
    public int getPuertoEscucha() {
        synchronized (this) {
            while (true) {
                if (this.socketAceptacion != null && this.socketAceptacion.socket() != null) {
                    if (this.socketAceptacion.socket().isBound()) {
                        break;
                    }
                }
                try {
                    this.wait();
                }
                catch (InterruptedException ex) {}
            }
            return this.socketAceptacion.socket().getLocalPort();
        }
    }
}
