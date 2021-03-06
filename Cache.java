/**
 * @author Sistemas Operativos - DTE
 * @version 2020-a
 * 
 * agosto 2020: versión inicial, jl
 * octubre 2020: modificado, jl, slana
 * 
 */
package servidor;

import java.net.URL;
import java.util.HashMap;

import comunicaciones.Parametros;

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
		final NodoCache existente = mapa.get ( url );
		if ( existente == null )
			return añadirNodo ( url );
		existente.usarRecurso();
		return existente;
	}

	/**
	 * Añaade un elemento web al cache web.
	 * @param url URL del elemento a añadir.
	 * @return el nuevo NodoCache que se ha añadido.
	 */
	private NodoCache añadirNodo ( URL url )
	{
		final NodoCache nuevo = new NodoCache();
		mapa.put ( url, nuevo );
		return nuevo;
	}

	/**
	 * Anotar el RecursoWeb correspondiente a un nodo del cache.
	 * Actualiza el uso de espacio en disco por parte de los recursos
	 * existentes en el cache web.
	 * @param n El nodo en el que se anotará el recurso web.
	 * @param r RecursoWeb que se anota en el nodo n.
	 */
	public void incrementarUsoMemoria ( long mem )
	{
		usoMemoria = usoMemoria + mem;
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
