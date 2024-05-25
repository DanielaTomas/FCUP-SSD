# FCUP-SSD

In the root of the Java project:

* Create JAR file:
    ```
    ./gradlew clean jar 
    ```

* Provision nodes with JAR file, for example with:

    ```
    scp <path-to-jar-file> <username>@<node-ip-or-name>:<path-to-file-on-destiny>
    ```

* First, initialize a bootstrap node, go the folder containing the jar file and run:

    ```
    java -cp <name-of-jar-file> Main.Peer <port>
    ```

* Afterwards run this on any other number of nodes:

    ```
    java -cp <name-of-jar-file> Main.Peer <port> <bootstrap-node-ip>
    ```

After having atleast 2 nodes, including the bootstrap running use the options given to you in the menu to interact with the program.