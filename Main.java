/**
 * @author Sistemas Operativos - DTE
 * @version 2020-a
 * 
 * Octubre 2011: versión inicial, jmrueda
 * octubre 2020: revisado, jllopez, slana
 * 
 */
package servidor;


import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ArrayBlockingQueue;
import java.io.FileNotFoundException;

import comunicaciones.RecursoWeb;
import comunicaciones.Parametros;
import comunicaciones.ServidorRed;
import comunicaciones.Peticion;

/**
 * Programa principal del servidor cache web.
 *
 */
public class Main
{
	static Parametros param; // parámetros de configuración del servidor

	/**
	 * @param args
	 */
	public static void main ( String[] args ) throws Exception
	{
		param = new Parametros ( args );
		
		final BlockingQueue<Peticion> cola = new ArrayBlockingQueue<Peticion>(10);
		final ServidorRed servidorRed = new ServidorRed ( cola, param );
		Thread.currentThread().setName ( "Servidor cache-web " );
		(new Thread(servidorRed)).start();
		Thread.sleep(1000);
		debug ( 0, "Puerto de escucha: " + servidorRed.getPuertoEscucha() );	
		
		cachewebMonohilo ( param, cola );
		// cachewebMultihilo ( param, cola );
	}

	private static void cachewebMonohilo ( Parametros param, BlockingQueue<Peticion> cola )
	throws InterruptedException, FileNotFoundException
	{
		do
		{
			final Peticion peticion = cola.take();
			debug ( 2, " Desencolo petición para " + peticion.getURL() );
			final RecursoWeb elementoWeb = peticion.descargar();
			debug ( 2, "RecursoWeb descargado" );
			peticion.getCliente().enviarRespuesta ( elementoWeb );
			debug ( 2, "Respuesta enviada" );
		} while (true);
	}

	/**
	 * Muestra el mensaje de depuración. Si el nivel de depuración es
	 * mayor o igual que el indicado, se muestra el mensaje precedido por
	 * el nombre del hilo para facilitar el seguimiento.
	 * @param nivel El nivel de depuración mínimo para que se muestre el
	 * mensaje.
	 * @param mensaje El mensaje de depuración a visualizar.
	 */
	private static void debug ( int nivel, String mensaje )
	{
		if ( param.debug >= nivel )
			System.err.println ( Thread.currentThread().getName() + mensaje );
	}
}
