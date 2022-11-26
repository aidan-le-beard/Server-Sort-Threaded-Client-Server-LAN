import java.util.Random;
import java.net.*;
import java.io.*;

public class Client {
    public static Random random = new Random(); // create Random for random numbers for populating array
    public static int N = 10, p = 1; // integers for array size; amount of threads
      public static void main (String[] args) throws IOException, ClassNotFoundException {

        final String serverIP = "XXX.XXX.X.XX"; //### PUT SERVER IP HERE ###
        final int howManyRuns = 1; //### PUT HOW MANY RUNS YOU WISH TO AVERAGE HERE (Default 1)
        final int startingN = 10; //### PUT WHAT N SHOULD START AS HERE (Default N=10). Program does N, N*10, N*100, N*1000

        // set up connection and communication
        Socket clientSocket = null;
        PrintWriter outText = null;
        BufferedReader inText = null;
        ObjectInputStream inputStream = null;
        ObjectOutputStream outputStream = null;
        try {
            clientSocket = new Socket(serverIP, 5555); 
        } catch (UnknownHostException e) {
            System.err.println("Unable to connect to: " + serverIP);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Unable to get I/O to: " + serverIP);
            System.exit(1);
        }

        outText = new PrintWriter(clientSocket.getOutputStream(), true); // print text to server
        inText = new BufferedReader(new InputStreamReader(clientSocket.getInputStream())); // get text from server
        inputStream = new ObjectInputStream(clientSocket.getInputStream()); // print array to server
        outputStream = new ObjectOutputStream(clientSocket.getOutputStream()); // get array from server
        outputStream.flush(); // need this to start

        // array that will need sorting and arrays for data storage and calculation
        int[] array = null;
        double[][] sortingTime = new double[20][4];
        double[] singleThreadTime = new double[5];
        System.out.println(inText.readLine()); // confirm connection

        outputStream.writeInt(howManyRuns);
        outputStream.flush();
        for (int outer = 0; outer < howManyRuns; outer++) { // outer loop for how many runs are being performed
            N = startingN;
            p = 1;
        // begin loop of testing all thread/array combinations 
        for (int i = 0; i < 20; i++) {

            // populate a new random array that will be sorted with 1, 2, 4, 8 and 16 cores
            if (p == 1) {
                array = randomArray(outer);
            }   

            // write array size, threads, and array to server
            outputStream.writeInt(N);
            outputStream.flush();
            outputStream.writeInt(p);
            outputStream.flush();
            outputStream.writeObject(array);
            outputStream.flush();

            // store how long sorting took, and calculate sped up and efficiency values
            double howLongToSort = (double) inputStream.readLong();
            if (i % 5 == 0) {
                singleThreadTime[i % 4] = howLongToSort;
            }
            sortingTime[i][0] += howLongToSort; // store sorting time
            // store speedup value T(n) / T(n,p)
            sortingTime[i][1] += singleThreadTime[(int) Math.floor(i / 5)] / howLongToSort; 
            // store efficiency value ((T(n) / T(n,p)) / p)
            sortingTime[i][2] += (singleThreadTime[(int) Math.floor(i / 5)] / howLongToSort) / p; 
            sortingTime[i][3] += (double) inputStream.readInt(); // store if array was sorted correctly

            // print sorted array if N == 10
            if (N == 10 && outer == 0) {
                int[] sortedArray = new int[10];
                System.out.println("Sorted array for N = 10 for " + p + " threads: ");
                sortedArray = (int[]) inputStream.readObject();
                for (int j = 0; j < sortedArray.length; j++) {
                    System.out.print(sortedArray[j] + " ");
                }
                System.out.println();
            }

            // iterate through all array and thread combinations, then break
            if (p < 16) {
                p *= 2;
            } else if (N < startingN * 1000) {
                N *= 10;
                p = 1;
            } 
        } // end inner for
    }// end outer for

        // close connections
        outText.close();
        inText.close();
        inputStream.close();
        outputStream.close();
        clientSocket.close();

        // print tables by iterating through 2D data storage array
        System.out.println();
        System.out.printf("%-23s%35s","Run or execution times","Array Size\n");
        System.out.println("-------------------------------------------------------------------");
        System.out.printf("%-17s%-23s%-23s%-23s%-20s", "No. of Cores",startingN,
                            startingN * 10,startingN * 100,startingN * 1000);       
        System.out.println();
        for (int j = 0; j < sortingTime[j].length; j++) {
            if (j == 3) {
                for (int i = 0; i < sortingTime.length; i++) {
                    if (sortingTime[i][3] / howManyRuns != 1) {
                        System.out.println("Not all arrays were correctly sorted.");
                        break;
                    } else if (i == sortingTime.length - 1) {
                        System.out.println("All arrays were correctly sorted.");
                    }
                }
                break;
            }
            for (int i = 0; i < sortingTime.length; i += 5) {
                if (i < 5) {
                    int q = 0;
                    if (i == 0) {q = 1;}else if(i==1){q = 2;}else if(i==2){q = 4;}else if(i==3){q = 8;}else if(i==4){q = 16;}
                    System.out.printf("%-14s", "p = " + q);
                }
                System.out.printf("%-23s", "|  " + (sortingTime[i][j] / howManyRuns));
                if (i == 19) {
                    break;
                } else if (i >= 15) {
                    i -= 19;
                    System.out.println();
                }
            }
            System.out.println();
            System.out.println();
            if (j == 0) {
                System.out.printf("%-23s%35s","Speed Up","Array Size\n-------------------------------------------------------------------\n");
                System.out.printf("%-17s%-23s%-23s%-23s%-20s", "No. of Cores",startingN,
                                            startingN * 10,startingN * 100,startingN * 1000);       
                System.out.println(); 
            } else if (j == 1) {
                System.out.printf("%-23s%35s","Efficiency","Array Size\n-------------------------------------------------------------------\n");
                System.out.printf("%-17s%-23s%-23s%-23s%-20s", "No. of Cores",startingN,
                                            startingN * 10,startingN * 100,startingN * 1000);       
                System.out.println(); 
            }
        }

    } // end main

    // method to generate random number array. Use same array for 1, 2, 4, 8, and 16 threads 
    private static int[] randomArray(int outer) {
        // populate array to be sorted with random numbers. Print if N = 10
        int[] array = new int[N]; // create array with input size N
        if (N == 10 && outer == 0) { 
            System.out.println("Unsorted Array:");
        }
        for (int i = 0; i < N; i++) {
            array[i] = (random.nextInt(10000) + 1);
            if (N == 10 && outer == 0) {
                System.out.print(array[i] + " ");
            }
        }
        if (N == 10 && outer == 0) { 
            System.out.println();
        }
        return array;
    }

} // end class