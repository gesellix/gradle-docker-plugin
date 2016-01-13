package de.gesellix.gradle.docker

class PortFinder {

    static int findFreePort() {
        ServerSocket socket = null
        try {
            socket = new ServerSocket(0)
            socket.setReuseAddress(true)
            int port = socket.getLocalPort()
            try {
                socket.close()
            } catch (IOException ignore) {
                // Ignore IOException on close()
            }
            return port
        } catch (IOException ignore) {
        } finally {
            if (socket != null) {
                try {
                    socket.close()
                } catch (IOException ignore) {
                }
            }
        }
        throw new IllegalStateException("Could not find a free TCP/IP port")
    }
}
