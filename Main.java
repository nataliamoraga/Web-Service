/**
 * @author Natalia Sánchez Moraga
 * @version 2020-a
 * 
 * Octubre 2011: versión inicial, jmrueda
 * octubre 2020: revisado, jllopez, slana
 * 
 */
package servidor;


import java.util.concurrent.BlockingQueue;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.io.File;
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
		final Cache caché = new Cache (param);
		final ServidorRed servidorRed = new ServidorRed ( cola, param );
		Thread.currentThread().setName ( "Servidor cache-web: " );
		(new Thread(servidorRed)).start();
		Thread.sleep(1000);
		debug ( 0, "Puerto de escucha: " + servidorRed.getPuertoEscucha() );	
		
		//  cachewebMonohilo ( param, cola );
		cachewebMultihilo ( param, cola, caché );	
		
		System.in.read();
		System.exit(0);
	}

	/**
	 * Crea un hilo liberador y lo pone en marcha. 
	 * También crea un número de hilos de atención de peticiones pasados como parámetro.
	 * Guarda los hilos de atención de peticiones en un array de hilos y los pone en marcha.
	 * La función implementa la terminación ordenada de los hilos de atención de peticiones.
	 * @param param Parámetros recibos por línea de mandatos.
	 * @param cola Cola de peticiones.
	 * @param caché Caché de almacenamiento de recursos web.
	 * @param cerrojo Cerrojo común a todos los hilos.
	 */
	private static void cachewebMultihilo ( Parametros param, BlockingQueue<Peticion> cola, Cache caché )
	throws InterruptedException, FileNotFoundException
	{
		Thread hiloLiberador = new Thread(new HiloLiberador(caché, param));
		hiloLiberador.setName ( "Hilo liberador:");
		hiloLiberador.start();
		
		Thread[] hilos = new AtencionPeticiones[param.num_hilos];
		for (int i=0; i<param.num_hilos; i++) {
			hilos[i] = new AtencionPeticiones(cola, caché, param);
			hilos[i].setName ( "Hilo " + String.valueOf(i) + ":");
			hilos[i].start();
		}
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run () {	
				LinkedList<String> lista = new LinkedList<String>();
				
				for (int i=0; i<param.num_hilos; i++) {
					((AtencionPeticiones) hilos[i]).terminar();
					debug ( 0, ": Se pide al Hilo " + i + " que termine" );
				}
				for (int i=0; i<param.num_hilos; i++) {
					hilos[i].interrupt();
					try {
						hilos[i].join();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				lista = caché.limpiarCache();
				borrarDisco (lista);
			}
		});
	}
	
	/* Uncomment for  single thread */
	/**************************************************************************************
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
	**************************************************************************************/
	
	/**
	 * Borra de disco los ficheros dentro de la lista pasada como parámetro.
	 * @param listaBorrar Se le pasa una lista con los nombres de los ficheros a borrar.
	 */
	public static void borrarDisco (LinkedList<String> listaBorrar) {
		File file;
		ListIterator<String> it = listaBorrar.listIterator();
		
		while ( it.hasNext() ) {
			file = new File( it.next() );
			file.delete();
			debug ( 0, " Fichero" + file + " borrado" );
		}	
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
