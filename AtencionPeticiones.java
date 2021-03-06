/**
 * @author Natalia Sánchez Moraga
 * @version 2020-a
 * 
 * Octubre 2011: versión inicial, jmrueda
 * agosto 2020: revisado, jllopez, slana
 * 
 */
package servidor;

import java.io.FileNotFoundException;
import java.net.URL;
import java.util.concurrent.BlockingQueue;

import comunicaciones.RecursoWeb;
import comunicaciones.Parametros;
import comunicaciones.Peticion;

/**
 * Hilo de atención de peticiones. Desencola peticiones y las sirve.
 *
 */
public class AtencionPeticiones extends Thread
{
	private static final String errorConstructor = "Algún parámetro es null";
	private final BlockingQueue<Peticion> cola;
	private final Parametros param;
	private final Cache cache;
	private volatile boolean terminar;

	/**
	 * Crea un hilo de atención de peticiones que desencola peticiones y las
	 * va sirviendo.
	 * @param cola Cola de donde sacar las peticiones recibidas.
	 * @param cache Lista de elementos del cache web.
	 * @param param Parámetros de comfiguración del servidor.
	 * @throws IllegalArgumentException si alguno de los argumentos es nulo.
	 */	
	public AtencionPeticiones (
		BlockingQueue<Peticion> cola,
		Cache cache,
		Parametros param )
	throws IllegalArgumentException
	{
		if (cola == null || cache == null || param == null )
			throw new IllegalArgumentException ( errorConstructor );
		this.cola = cola;
		this.cache = cache;
		this.param = param;
		terminar = false;
	}

	private void atenderPeticiones ()
	{
		debug ( 1, " comienza su ejecución." );
		try
		{
			do
				atenderPetición();
			while ( !terminar );
		}
		catch ( InterruptedException e )
		{
			debug ( 0, " interrumpido: " + e.getMessage() );
		}
		debug ( 1, " termina su ejecución." );
	}

	/**
	 * Atiende una petición. Extrae una petición de la cola y la atiende.
	 * @throws InterruptedException si el hilo es interrumpido.
	 */
	private void atenderPetición ()
	throws InterruptedException
	{
		final Peticion pet = cola.take();
		final URL url =  pet.getURL();
		debug ( 2, " Desencola petición para URL: " + url );
		final NodoCache nodo = cache.buscarNodo ( url );
		if ( nodo.debeDescargarse() ) 
			descargar ( pet, nodo );	
		final RecursoWeb elementoWeb = nodo.recursoWeb();
		try
		{
			pet.getCliente().enviarRespuesta ( elementoWeb );
		}
		catch ( FileNotFoundException e )
		{
			debug ( 0, " Error: " + e.getMessage() );
		}
		nodo.liberarRecurso();
		debug ( 2, " Respuesta enviada: " + elementoWeb.getFichero() );
	}

	/**
	 * Descarga un recurso web desde el servidor. Esta operación puede
	 * ser bastante costosa en tiempo.
	 */
	private void descargar ( Peticion pet, NodoCache nodo )
	{
		final URL url = pet.getURL();
		debug ( 2, " URL no encontrado: " + url + " será descargado" );
		final RecursoWeb r = pet.descargar();
		nodo.anotarRecursoWeb ( r );
		cache.incrementarUsoMemoria ( r.getTamano() );
		final String fichero = r.getFichero();
		debug ( 2, " URL: " + url + " descargado en " + fichero );
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
	 * Llama a atenderPeticiones().
	 */
	@Override
	public void run() {
		atenderPeticiones();
	}
	
	public void terminar () {
		terminar = true;
	}
}
