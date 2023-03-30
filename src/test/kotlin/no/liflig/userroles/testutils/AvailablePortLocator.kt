package no.liflig.userroles.testutils

import java.net.InetAddress
import java.util.Random
import javax.net.ServerSocketFactory

/** Locates an available port on the local system. */
object AvailablePortLocator {
  fun findAvailableTcpPort(): Int {
    val minPort = 1024
    val maxPort = 65535
    val portRange = maxPort - minPort
    var candidatePort: Int
    var searchCounter = 0
    do {
      check(searchCounter <= portRange) {
        String.format(
            "Could not find an available port in the range [%d, %d] after %d attempts",
            minPort,
            maxPort,
            searchCounter,
        )
      }
      candidatePort = findRandomPort(minPort, maxPort)
      searchCounter++
    } while (!isPortAvailable(candidatePort))
    return candidatePort
  }

  private fun isPortAvailable(port: Int): Boolean {
    return try {
      val serverSocket =
          ServerSocketFactory.getDefault()
              .createServerSocket(
                  port,
                  1,
                  InetAddress.getByName("localhost"),
              )
      serverSocket.close()
      true
    } catch (ex: Exception) {
      false
    }
  }

  private fun findRandomPort(minPort: Int, maxPort: Int): Int {
    val portRange = maxPort - minPort
    return minPort + Random(System.currentTimeMillis()).nextInt(portRange + 1)
  }
}
