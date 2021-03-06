/**
 * @author Natalia Sánchez Moraga
 * @version 2020-a
 * 
 * Octubre 2011: versión inicial, jmrueda
 * agosto 2020: revisado, jllopez, slana
 * 
 */
package servidor;

import java.io.File;
import java.util.LinkedList;
import java.util.ListIterator;

import comunicaciones.Parametros;

public class HiloLiberador extends Thread
{
	private static final String errorConstructor = "Alguno de los parámetros es null";
	private final Parametros param;
	private final Cache cache;
	private LinkedList<String> listaBorrar;
	
	/**
	 * Crea un hilo liberador de peticiones que realiza una espera inactiva 
	 * hasta que es despertado. Entoces ejecuta el algoritmo de reloj y 
	 * por último elimina de disco los ficheros devueltos en una lista por el 
	 * algoritmo de reloj.
	 * @param cache Lista de elementos del cache web.
	 * @param param Parámetros de comfiguración del servidor.
	 * @throws IllegalArgumentException si alguno de los argumentos es nulo.
	 */	
	public HiloLiberador ( Cache cache, Parametros param ) throws IllegalArgumentException
	{
		if ( cache == null || param == null )
			throw new IllegalArgumentException ( errorConstructor );

		this.cache = cache;
		this.param = param;
		listaBorrar = new LinkedList<String>();
	}
	
	private void ejecutarAlgoritmoReloj ()
	{
		debug ( 1, " comienza su ejecución." );
		try
		{
			do {
				synchronized (cache) {
					cache.wait();
				}
				listaBorrar = cache.algoritmoReloj();
				borrarFichero ( listaBorrar );
				
			} while ( true );
		}
		catch ( InterruptedException e )
		{
			debug ( 0, " interrumpido: " + e.getMessage() );
		}
		debug ( 1, " termina su ejecución." );
	}
	
	/**
	 * Borra de disco los ficheros dentro de la lista pasada como parámetro.
	 * @param listaBorrar Se le pasa una lista con los nombres de los ficheros a borrar.
	 */
	private void borrarFichero (LinkedList<String> listaBorrar) {
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
	private void debug ( int nivel, String mensaje )
	{
		if ( param.debug >= nivel )
			System.err.println ( Thread.currentThread().getName() + mensaje );
	}
	
	/**
	 * Sobreescribe el método run() de la interfaz Runnable.
	 * Llama a ejecutarAlgoritmoReloj().
	 */
	@Override
	public void run() {
		ejecutarAlgoritmoReloj ();
	}
}
