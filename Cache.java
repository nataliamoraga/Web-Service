/**
 * @author Natalia Sánchez Moraga
 * @version 2020-a
 * 
 * agosto 2020: versión inicial, jl
 * octubre 2020: modificado, jl, slana
 * 
 */
package servidor;

import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import comunicaciones.Parametros;
import comunicaciones.RecursoWeb;

/**
 * Cache de elementos web. Almacena recursos web identificados por su URL.
 * Permite localizar y obtener un RecursoWeb, dado su URL, y darlo de alta
 * si no está presente aún. También ofrece un método para recorrer los
 * recursos almacenados y hacer limpieza de los menos recientemente utilizados.
 */
public class Cache
{
	private static final String errorConst = "El parámetro es null";
	private final HashMap<URL,NodoCache> mapa;
	private long usoMemoria;
	private final Parametros param;
	private static Lock cerrojo, mutex;
	private LinkedList<URL> lista;
	private ListIterator<URL> it;
	
	/**
	 * Crea un cache web.
	 * 
	 * @param param Parámetros de configuración del servidor.
	 * @throws IllegalArgumentException si el parámetro es null.
	 */
	public Cache ( Parametros param )
	throws IllegalArgumentException
	{
		if ( param == null )
			throw new IllegalArgumentException ( errorConst );
		this.param = param;
		mapa = new HashMap<URL,NodoCache>();
		usoMemoria = 0;
		cerrojo = new ReentrantLock();
		mutex = new ReentrantLock();
		lista = new LinkedList<URL>();
		it = lista.listIterator();
	}

	/**
	 * Busca un elemento web en el cache web. Dado el URL del elemento, si
	 * el elemento no está aún en el cache, lo añade, y devuelve el
	 * NodoCache solicitado, marcándolo como usado por un hilo de atención
	 * de peticiones más.
	 * @param url URL del elemento buscado.
	 * @return el NodoCache correspondiente al url indicado.
	 */
	public NodoCache buscarNodo ( URL url )
	{
		mutex.lock();
		try {
			final NodoCache existente = mapa.get ( url );
			if ( existente == null )
				return añadirNodo ( url );
			existente.usarRecurso();
			return existente;
		} finally {
			mutex.unlock();
		}
	}

	/**
	 * Añade un elemento web al cache web.
	 * Añade también su url al iterador de una lista.
	 * @param url URL del elemento a añadir.
	 * @return el nuevo NodoCache que se ha añadido.
	 */
	private NodoCache añadirNodo ( URL url )
	{
		cerrojo.lock();
		try {
		final NodoCache nuevo = new NodoCache();
		mapa.put ( url, nuevo );
		it.add( url ); //guarda el url en la posición más alejada del iterador
		return nuevo;
		} finally {
			cerrojo.unlock();
		}
	}

	/**
	 * Anotar el RecursoWeb correspondiente a un nodo del cache.
	 * Actualiza el uso de espacio en disco por parte de los recursos
	 * existentes en el cache web.
	 * Comprueba si se ha superado el límite pasado por parametro y 
	 * si es así se lo notifica al hilo liberador.
	 * @param n El nodo en el que se anotará el recurso web.
	 * @param r RecursoWeb que se anota en el nodo n.
	 */
	public synchronized void incrementarUsoMemoria ( long mem )
	{
		usoMemoria = usoMemoria + mem;
		if ( usoMemoria > param.lim_sup ) {
			notifyAll();
		}  
	}
	
	/**
	 * Actualiza el uso de espacio en disco por parte de los recursos
	 * existentes en el cache web.
	 * @param mem Tamaño que debe liberarse. 
	 */
	public synchronized void decrementarUsoMemoria (long mem)
	{
		usoMemoria = usoMemoria - mem;
	}

	/**
	 * Implementa el algoritmo de reloj.
	 * @return listaBorrar Devuelve una lista con los nombres de los ficheros a borrar.
	 */
	public LinkedList<String> algoritmoReloj () 
	{
		URL url;
		NodoCache nodo;
		RecursoWeb elementoWeb;
		LinkedList<String> listaBorrar = new LinkedList<String>();
		
		cerrojo.lock();
		try {
		while ( usoMemoria>param.lim_inf && mapa.size()>param.num_hilos) {
			
			if ( !it.hasNext() ) {
				it = lista.listIterator(0);
			}
			url = it.next();
			nodo = mapa.get ( url );
			if ( nodo.noEnUso() ) {
				if ( nodo.usadoRecientemente() ) {
					nodo.marcarNoUsadoRecientemente();
				} else {
					elementoWeb = nodo.recursoWeb();
					listaBorrar.add( elementoWeb.getFichero() );
					decrementarUsoMemoria ( elementoWeb.getTamano() );
					debug ( 0, " Recurso con url: " +url+ " elegido como victima" );
					mapa.remove(url);
					it.remove();
				}
			}
		}
		return listaBorrar;
		} finally {
			cerrojo.unlock();
		}
	}
	
	/**
	 * Duelve una lista con los nombres de todos los ficheros en la caché.
	 * Además borra todos los elementos del mapa y del iterador que recorre la lista.
	 * @return listaBorrar Devuelve una lista con los nombres de los ficheros a borrar.
	 */
	public LinkedList<String> limpiarCache () {
		URL url;
		LinkedList<String> listaBorrar = new LinkedList<String>();
		
		cerrojo.lock();
		try {
		it = lista.listIterator(0);
		while ( it.hasNext() ) {
			url = it.next();
			listaBorrar.add( mapa.get(url).recursoWeb().getFichero() );
			decrementarUsoMemoria ( mapa.get(url).recursoWeb().getTamano() );
			mapa.remove( url );
			it.remove();
		}
		return listaBorrar;
		} finally {
			cerrojo.unlock();
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
		if ( Main.param.debug >= nivel )
			System.err.println ( Thread.currentThread().getName() + mensaje );
	}
}
