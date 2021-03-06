// 
// Decompiled by Procyon v0.5.36
// 

package comunicaciones;

import java.net.SocketAddress;
import java.net.InetSocketAddress;
import gnu.getopt.Getopt;
import java.net.Proxy;

public class Parametros
{
    public final int debug;
    public final String directorioCache;
    public final int puerto;
    public final int lim_sup;
    public final int lim_inf;
    public final int num_hilos;
    public final Proxy proxy;
    
    public Parametros(final String[] array) throws ParamException {
        String optarg = "";
        int int1 = 0;
        int int2 = 0;
        String optarg2 = "Cacheweb-tmp";
        int int3 = 0;
        int int4 = 100000;
        int int5 = 50000;
        int int6 = 1;
        if (array.length == 0) {
            ayuda();
        }
        final Getopt getopt = new Getopt("Par\u00e1metros servidor cach\u00e9-web", array, "g:d:p:u:l:t:x:r:");
        int getopt2;
        while ((getopt2 = getopt.getopt()) != -1) {
            try {
                switch (getopt2) {
                    case 103: {
                        int2 = Integer.parseInt(getopt.getOptarg());
                        continue;
                    }
                    case 100: {
                        optarg2 = getopt.getOptarg();
                        continue;
                    }
                    case 112: {
                        int3 = Integer.parseInt(getopt.getOptarg());
                        continue;
                    }
                    case 117: {
                        int4 = Integer.parseInt(getopt.getOptarg());
                        continue;
                    }
                    case 108: {
                        int5 = Integer.parseInt(getopt.getOptarg());
                        continue;
                    }
                    case 116: {
                        int6 = Integer.parseInt(getopt.getOptarg());
                        continue;
                    }
                    case 120: {
                        optarg = getopt.getOptarg();
                        continue;
                    }
                    case 114: {
                        int1 = Integer.parseInt(getopt.getOptarg());
                        continue;
                    }
                    default: {
                        ayuda();
                        continue;
                    }
                }
                continue;
            }
            catch (Exception ex) {
                switch (getopt2) {
                    case 103:
                    case 108:
                    case 112:
                    case 114:
                    case 116:
                    case 117: {
                        throw new ParamException("La opci\u00f3n -" + (char)getopt2 + " requiere un argumento num\u00e9rico");
                    }
                    case 100:
                    case 120: {
                        throw new ParamException("La opci\u00f3n -" + (char)getopt2 + " requiere una cadena de caracteres");
                    }
                    default: {
                        throw new ParamException(ex.getMessage());
                    }
                }
            }
            break;
        }
        if (int3 == 0) {
            throw new ParamException("Debe especificarse el puerto de escucha deseado");
        }
        if (int2 < 0 || int3 < 0 || int4 < 0 || int5 < 0 || int6 < 0 || int1 < 0) {
            throw new ParamException("No se pueden especificar valores negativos");
        }
        if (int6 < 1) {
            throw new ParamException("El n\u00famero m\u00ednimo de hilos de atenci\u00f3n de peticiones debe ser al menos 1");
        }
        if (int5 > int4) {
            throw new ParamException("El objetivo de uso de memoria no puede ser mayor que el m\u00e1ximo");
        }
        if ((optarg.equals("") && int1 != 0) || (!optarg.equals("") && int1 == 0)) {
            throw new ParamException("Si se especifica servidor proxy, hay que especificar el puerto, y viceversa");
        }
        Proxy no_PROXY;
        if (optarg.equals("")) {
            no_PROXY = Proxy.NO_PROXY;
        }
        else {
            try {
                no_PROXY = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(optarg, int1));
            }
            catch (Exception ex2) {
                throw new ParamException("Par\u00e1metros del servidor Proxy no v\u00e1lidos");
            }
        }
        this.debug = int2;
        this.directorioCache = optarg2;
        this.puerto = int3;
        this.lim_sup = int4;
        this.lim_inf = int5;
        this.num_hilos = int6;
        this.proxy = no_PROXY;
    }
    
    private static void ayuda() throws ParamException {
        throw new ParamException("Opciones de uso:\n\t-p PUERTO: puerto de escucha del servidor cach\u00e9 web\n\t[-g NUMERO]: nivel de depuraci\u00f3n [0]\n\t[-d DIRECTORIO]: directorio para los ficheros temporales [Cacheweb-tmp]\n\t[-u NUMERO]: l\u00edmite superior de uso del cach\u00e9 web (en Bytes) [100.000]\n\t[-l NUMERO]: l\u00edmite inferior de uso del cach\u00e9 web (en Bytes) [50.000]\n\t[-t NUMERO]: n\u00famero de hilos de atenci\u00f3n de peticiones [1]\n\t[-x NOMBRE]: servidor proxy (en caso de ser necesario)\n\t[-r PUERTO]: puerto del servidor proxy (en caso de ser necesario)\n");
    }
}
