import java.io.*; // catch exceptions
import java.lang.Math; // to find the ceiling to evenly divide the array
import java.net.*; // setup a server socket to listen to client
import java.util.Arrays;

public class SortingServer {

    public static int[][] arrays; // holds the split array: accessible by threads
    public static volatile Boolean[] mutexArray; /* when merging threads we need to lock them in place
                                                    to avoid race conditions with mutex. accessible by thread. volatile to 
                                                    be able to run an empty while that keeps checking: while(!mutexArray[index]){} */
    public static void main(String[] args) throws IOException, ClassNotFoundException {

        // Accept client connection
        Socket clientSocket = null;
        ServerSocket serverSocket = null;
        PrintWriter outText = null;
        BufferedReader inText = null;
        ObjectInputStream inputStream = null;
        ObjectOutputStream outputStream = null;
        try {
            System.out.println("Server is listening.");
            serverSocket = new ServerSocket(5555);
            clientSocket = serverSocket.accept();
        } catch (IOException e) {
            System.err.println("Could not listen on port: 5555.");
            System.exit(1);
        }
        outText = new PrintWriter(clientSocket.getOutputStream(), true); // print text to client
        inText = new BufferedReader(new InputStreamReader(clientSocket.getInputStream())); // receive text from client
        outputStream = new ObjectOutputStream(clientSocket.getOutputStream()); // get array from client
        outputStream.flush(); // doesn't work without flush
        inputStream = new ObjectInputStream(clientSocket.getInputStream()); // print array to client
        long sortingTime;
        int sorted = 0;

        outText.println("Connected.");
            
        int howManyRuns = inputStream.readInt(); // read how many tests the user wishes to perform
        for (int loops = 0; loops < howManyRuns * 20; loops++) { // for 5 different p values and 4 N values = 20 loops per test

            int N = inputStream.readInt(), p = inputStream.readInt(); // integers for array size; amount of threads
            int[] array = new int[N]; // create an array of the size N 
            array = (int[]) inputStream.readObject(); // receive the random array to be sorted from the client
            final int holdN = N; // N will be manipulated, but need to keep the size
            arrays = new int[p][]; // array of arrays for when the larger array is split
            System.out.println("Sorting array with N = " + N + " and p = " + p);

            // create as many mutexes as threads to lock each thread individually
            mutexArray = new Boolean[p]; 
            for (int i = 0; i < p; i++) {
                mutexArray[i] = false;
            }

            /* split the array step 1:
            we can have N = 10 and p = 16: special case with more threads than array size */
            if (p > holdN) {
                while (p > 0) {
                    if (N > 0) {
                        arrays[p - 1] = new int[] {array[N - 1]};
                        N--;
                        p--;
                    } else {
                        arrays[p - 1] = new int[0];
                        p--;
                    }
                }

            // split the array: every other case
            } else {
                // split the array with quotient ceiling until it is evenly divisible by p
                while (N % p != 0) {
                    int[] array2 = new int[(int) Math.ceil((double) N / p)];
                    for (int i = N - 1; i > N - 1 - Math.ceil((double) N / p); i--) {
                        array2[(i - N + 1) * -1] = array[i];
                    }
                    arrays[p - 1] = array2;
                    N -= Math.ceil((double) N / p);
                    p--;
                }   

                // finish splitting evenly when array is evenly divisible
                while (N > 0) {
                    int[] array3 = new int[N / p];
                    for (int i = N - 1; i > N - 1 - N / p; i--) {
                        array3[(i - N + 1) * -1] = array[i];
                    }
                    arrays[p - 1] = array3;
                    N -= N / p;
                    p--;
                } // end while
              } // end else

            // array is decomposed: begin sorting, and start timer
            // Timing in this scenario after the decomposition, but including the merging part.
            long startTime = System.nanoTime(); // measure the time

            // sort each array individually using threading
            for (int i = 0; i < arrays.length; i++) {
                new SortThread(i, "Insertion", 0).start();
            }

            /* merge arrays (merge sort final step) using threading
            loop through the threads until finding two threads that are not locked, then merge them */
            int counter = 0;
            int index = -1;
            int counter2 = 0;
            for (int i = arrays.length - 1; i >= 0; i--) {
                if (mutexArray[i] && counter == 0) {
                    counter++;
                    index = i;
                } else if (mutexArray[i] && i != index && index != -1) {
                    mutexArray[i] = false;
                    mutexArray[index] = false;
                    new SortThread(index, "Merge", i).start();
                    counter2++;
                    counter--;
                    index = -1;
                }
                if (counter2 == arrays.length - 1) {
                    index = i;
                    break;
                } else if (i == 0) {
                    i = arrays.length;
                }
            } // end for

            // wait for merging/sorting to finish
            while (!mutexArray[index]) {}

            // print how long sorting took
            sortingTime = (System.nanoTime() - startTime);
            int[] sortedArray = arrays[index]; // store the now merged/sorted array
            
            System.out.println("Sorting took: " + sortingTime + " ns.");

            // sort the initial random array for comparison
            mutexArray[index] = false;
            arrays[index] = Arrays.copyOf(array, array.length);
            new SortThread(index, "Insertion", 0).start();
            while (!mutexArray[index]) {};

            // compare sorted initial array to thread-sorted array to verify sort is correct
            for (int i = 0; i < sortedArray.length; i++) {
                if (arrays[index][i] != sortedArray[i]) {
                    sorted = 0;
                    break;
                } else if (i == sortedArray.length - 1) {
                    sorted = 1;
                }
            } // end verification for loop

            outputStream.writeLong(sortingTime); // send how long the sort took to client
            outputStream.flush();
            outputStream.writeInt(sorted); // send if the array was successfully sorted (1) or not (0) to client
            outputStream.flush();
            // send back sorted array to client (if N == 10)
            if (holdN == 10 && loops < 20) {
                outputStream.writeObject(sortedArray);
                outputStream.flush();
            } 
        } // end connection loop

        // close connections
        outputStream.close();
        inputStream.close();
        inText.close();
        outText.close();
        clientSocket.close();
        serverSocket.close();
    } // end main
} // end class