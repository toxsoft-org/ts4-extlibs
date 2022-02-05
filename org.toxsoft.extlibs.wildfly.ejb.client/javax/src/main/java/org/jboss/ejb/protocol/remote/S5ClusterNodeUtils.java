package org.jboss.ejb.protocol.remote;

import java.net.Socket;
import java.net.SocketAddress;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.jboss.ejb.client.EJBClientContext;
import org.jboss.ejb.protocol.remote.NodeInformation.ClusterNodeInformation;
import org.jboss.logging.Logger;
import org.jboss.marshalling.Pair;
import org.wildfly.common.net.CidrAddressTable;
import org.wildfly.common.net.CidrAddressTable.Mapping;

/**
 * Информация о узлах кластера сервера wildfly (библиотека вспомогательных методов для доступа к
 * org.jboss.ejb.protocol.remote)
 * <p>
 * <ul>
 * <li>Ключевые точки отладки EJBClientContext
 * <li>1. EJBClientChannel. processMessage(..., CLUSTER_TOPOLOGY_COMPLETE), 254;</li>
 * <li>1. NodeInformation. addAddress(...), 229;</li>
 * <li>1. DiscoveryEJBClientInterceptor. doFirstMatchDiscovery(...), 375;</li>
 * <li>2. DiscoveryEJBClientInterceptor. doAnyDiscovery(...), 439;</li>
 * <li>3. RemotingEJBDiscoveryProvider. addNode(...), 116;</li>
 * <li>4. RemotingEJBDiscoveryProvider. discover(...), 162;</li>
 * <li>5. RemotingEJBDiscoveryProvider. discover(...), 197;</li>
 * <li>6. XnioWorker. getBindAddressTable(), 996;</li>
 * <li>7. CidrAddressTable. doPut(...), 100.</li>
 * </ul>
 *
 * @author mvk
 */
public final class S5ClusterNodeUtils {

  /**
   * Таймаут(мсек) попытки получить блокировку на управление удаленными вызовами
   */
  public static final long TRY_LOCK_TIMEOUT_DEFAULT = 10000;

  /**
   * Слушатель изменений топологии кластера
   */
  public interface IS5ClusterTopologyListener {

    /**
     * Создание кластера
     *
     * @param aClusterName String имя кластера
     * @param aNodes {@link List}&lt;{@link Pair}&lt;String,{@link InetSocketAddress}&gt;&gt; список узлов кластера
     *          после создания. Ключ: имя узла;<br>
     *          Значение: адрес узла {@link InetSocketAddress}.
     */
    @SuppressWarnings( "unused" )
    default void onTopologyComplete( String aClusterName, List<Pair<String, InetSocketAddress>> aNodes ) {
      // nop
    }

    /**
     * Удаление кластера
     *
     * @param aClusterName String имя кластера
     */
    @SuppressWarnings( "unused" )
    default void onTopologyRemoval( String aClusterName ) {
      // nop
    }

    /**
     * Добавление узлов в кластер
     *
     * @param aClusterName String имя кластера
     * @param aNodes {@link List}&lt;{@link Pair}&lt;String,{@link InetSocketAddress}&gt;&gt; список узлов кластера
     *          после добавления. Ключ: имя узла;<br>
     *          Значение: адрес узла {@link InetSocketAddress}.
     * @param aAddedNodes {@link List} список имен узлов добавленых в кластер
     */
    @SuppressWarnings( "unused" )
    default void onTopologyAddition( String aClusterName, List<Pair<String, InetSocketAddress>> aNodes,
        List<String> aAddedNodes ) {
      // nop
    }

    /**
     * Удаление узлов из кластера
     *
     * @param aClusterName String имя кластера
     * @param aNodes {@link List}&lt;{@link Pair}&lt;String,{@link InetSocketAddress}&gt;&gt; список узлов кластера
     *          после удаления. Ключ: имя узла;<br>
     *          Значение: адрес узла {@link InetSocketAddress}.
     * @param aRemovedNodes {@link List} список имен узлов удаленных из кластера
     */
    @SuppressWarnings( "unused" )
    default void onTopologyNodeRemoval( String aClusterName, List<Pair<String, InetSocketAddress>> aNodes,
        List<String> aRemovedNodes ) {
      // nop
    }
  }

  /**
   * Карта слушателей изменений кластера.
   * <p>
   * Ключ: контекст клиента;<br>
   * Значение: список слушателей.
   */
  private static final Map<EJBClientContext, List<IS5ClusterTopologyListener>> clusterTopologyListeners =
      new HashMap<>();

  /**
   * Блокировка доступа к {@link #clusterTopologyListeners}.
   */
  private static final ReentrantReadWriteLock clusterTopologyListenerLock = new ReentrantReadWriteLock();

  /**
   * Журнал
   */
  private static final Logger logger = Logger.getLogger( S5ClusterNodeUtils.class.getName() );

  /**
   * Конструктор
   */
  private S5ClusterNodeUtils() {
  }

  // ------------------------------------------------------------------------------------
  // Открытое API
  //
  /**
   * Возвращает список адресов доступных узлов кластера открытого соединения в указанном контексте клиента
   *
   * @param aContext {@link EJBClientContext} контекст клиента
   * @return {@link List}&lt;{@link Pair}&lt;String,{@link InetSocketAddress}&gt;&gt; список узлов кластера.<br>
   *         Ключ первой карты: имя кластера;<br>
   *         Ключ второй карты: имя узла;<br>
   *         Значение: адрес узла {@link InetSocketAddress}
   */
  public static Map<String, List<Pair<String, InetSocketAddress>>> getNodeAddrs( EJBClientContext aContext ) {
    Map<String, List<Pair<String, InetSocketAddress>>> retValue = new HashMap<>();
    if( aContext == null ) {
      logger.errorf( "getAvailableNodeAddrs: aContext = null" ); //$NON-NLS-1$
      return retValue;
    }
    final RemoteEJBReceiver ejbReceiver = aContext.getAttachment( RemoteTransportProvider.ATTACHMENT_KEY );
    if( ejbReceiver != null ) {
      List<NodeInformation> nodes = ejbReceiver.getDiscoveredNodeRegistry().getAllNodeInformation();
      for( NodeInformation node : nodes ) {
        ConcurrentMap<String, ClusterNodeInformation> addrs = node.getClustersByName();
        for( String clusterName : addrs.keySet() ) {
          ClusterNodeInformation addr = addrs.get( clusterName );
          List<Pair<String, InetSocketAddress>> clusterNodes = retValue.get( clusterName );
          if( clusterNodes == null ) {
            clusterNodes = new LinkedList<>();
            retValue.put( clusterName, clusterNodes );
          }
          for( CidrAddressTable<InetSocketAddress> cidr : addr.getAddressTablesByProtocol().values() ) {
            for( Iterator<Mapping<InetSocketAddress>> it = cidr.iterator(); it.hasNext(); ) {
              clusterNodes.add( new Pair<>( node.getNodeName(), it.next().getValue() ) );
            }
          }
        }
      }
    }
    return retValue;
  }

  /**
   * Добавляет слушателя изменений топологии кластера
   * <p>
   * Если слушатель уже зарегистирован, то ничего не делает
   *
   * @param aContext EJBClientContext контекст слушателя
   * @param aListener {@link IS5ClusterTopologyListener} слушатель
   */
  public static void addClusterTopologyListener( EJBClientContext aContext, IS5ClusterTopologyListener aListener ) {
    if( aContext == null ) {
      logger.errorf( "addClusterTopologyListener: aContext = null" ); //$NON-NLS-1$
      return;
    }
    if( aListener == null ) {
      logger.errorf( "addClusterTopologyListener: aListener = null" ); //$NON-NLS-1$
      return;
    }
    clusterTopologyListenerLock.writeLock().lock();
    try {
      List<IS5ClusterTopologyListener> listeners = clusterTopologyListeners.get( aContext );
      if( listeners == null ) {
        listeners = new LinkedList<>();
        clusterTopologyListeners.put( aContext, listeners );
      }
      if( listeners.contains( aListener ) == false ) {
        listeners.add( aListener );
      }
    }
    finally {
      clusterTopologyListenerLock.writeLock().unlock();
    }
  }

  /**
   * Удаляет слушателя изменений топологии кластера
   *
   * @param aContext EJBClientContext контекст слушателя
   * @param aListener {@link IS5ClusterTopologyListener} слушатель
   */
  public static void removeClusterTopologyListener( EJBClientContext aContext, IS5ClusterTopologyListener aListener ) {
    if( aContext == null ) {
      logger.errorf( "addClusterTopologyListener: aContext = null" ); //$NON-NLS-1$
      return;
    }
    if( aListener == null ) {
      logger.errorf( "addClusterTopologyListener: aListener = null" ); //$NON-NLS-1$
      return;
    }
    clusterTopologyListenerLock.writeLock().lock();
    try {
      List<IS5ClusterTopologyListener> listeners = clusterTopologyListeners.get( aContext );
      if( listeners != null ) {
        listeners.remove( aListener );
      }
    }
    finally {
      clusterTopologyListenerLock.writeLock().unlock();
    }
  }

  // ------------------------------------------------------------------------------------
  // API пакета
  //
  /**
   * Обработка сообщений об изменений в кластере
   *
   * @param aProtocolMessage int тип сообщения (протокол)
   * @param aClusterName String имя кластера
   * @param aNodes String имена узлов операции
   */
  static void onTopologyUpdated( int aProtocolMessage, String aClusterName, List<String> aNodes ) {
    if( aClusterName == null ) {
      logger.errorf( "onTopologyUpdated: aClusterName = null" ); //$NON-NLS-1$
      return;
    }
    if( aNodes == null ) {
      logger.errorf( "onTopologyUpdated: aNodes = null" ); //$NON-NLS-1$
      return;
    }
    clusterTopologyListenerLock.readLock().lock();
    try {
      for( EJBClientContext context : clusterTopologyListeners.keySet() ) {
        List<Pair<String, InetSocketAddress>> nodeAddrs = getNodeAddrs( context ).get( aClusterName );
        if( nodeAddrs == null ) {
          logger.errorf( "onTopologyUpdated: aListener = null" ); //$NON-NLS-1$
          continue;
        }
        List<IS5ClusterTopologyListener> listeners = clusterTopologyListeners.get( context );
        for( IS5ClusterTopologyListener listener : listeners ) {
          switch( aProtocolMessage ) {
            case Protocol.CLUSTER_TOPOLOGY_COMPLETE:
              listener.onTopologyComplete( aClusterName, nodeAddrs );
              break;
            case Protocol.CLUSTER_TOPOLOGY_REMOVAL:
              listener.onTopologyRemoval( aClusterName );
              break;
            case Protocol.CLUSTER_TOPOLOGY_ADDITION:
              listener.onTopologyAddition( aClusterName, nodeAddrs, aNodes );
              break;
            case Protocol.CLUSTER_TOPOLOGY_NODE_REMOVAL:
              listener.onTopologyNodeRemoval( aClusterName, nodeAddrs, aNodes );
              break;
            default:
              logger.errorf( "onTopologyUpdated: unknow message = " + aProtocolMessage ); //$NON-NLS-1$
              continue;
          }
        }
      }
    }
    finally {
      clusterTopologyListenerLock.readLock().unlock();
    }
  }

  /**
   * Проводит проверку доступности узла кластера сервера {@link URL}.
   * <p>
   * Для проверки используется отправка пакетов 'HEAD request' и обработка кода ответа в диапазоне 200-399.
   *
   * @param aHostname String имя хоста на котором работает узел
   * @param aPort int номер порта на котором работает узел
   * @param aTimeout int таймаут в мсек ожидания узла
   * @return <b>true</b> указанный узел доступен для обращения; <b>false</b> указанный узел недоступен для обращения.
   */
   static boolean availableNode( String aHostname, int aPort, int aTimeout ) {
    if( aHostname == null ) {
      logger.errorf( "availableNode(...): aHostname = null" ); //$NON-NLS-1$
      return false;
    }
    try( Socket socket = new Socket() ) {
      socket.setReuseAddress( true );
      SocketAddress sa = new InetSocketAddress( aHostname, aPort );
      socket.connect( sa, aTimeout );
      return true;
    }
    catch( Throwable e ) {
      logger.warnf( "Узел %s:%d недоступен", aHostname, Integer.valueOf( aPort ) ); //$NON-NLS-1$
    }
    return false;
  }
}
