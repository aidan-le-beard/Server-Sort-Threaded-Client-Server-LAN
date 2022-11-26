public class SortThread extends Thread {

    // variables that will be needed from main 
    private int i;
    private String threadType;
    private int theOtheri;

    // constructor
    SortThread(int i, String threadType, int theOtheri) {
        this.i = i;
        this.threadType = threadType;
        this.theOtheri = theOtheri;
    }

    public void run() {

        // insertion sort initially passed array(s)
        if (this.threadType.equals("Insertion")) {
            for (int j = 1; j < SortingServer.arrays[i].length; j++) {
                int key = SortingServer.arrays[i][j];
                int k = j - 1;
                while (k >= 0 && SortingServer.arrays[i][k] > key) {
                    SortingServer.arrays[i][k + 1] = SortingServer.arrays[i][k]; 
                    k--;
                }
                SortingServer.arrays[i][k + 1] = key;
            }

            // set mutex as true: this is ready to merge now
            SortingServer.mutexArray[i] = true;

        // merge sorted arrays
        } else {
            // array to hold 2 merged arrays is the size of them added together
            int[] array2 = new int [SortingServer.arrays[i].length + SortingServer.arrays[theOtheri].length];
            int j = 0, k = 0, l = 0; // variables for merging

            // merge until one array is finished
            while (j < SortingServer.arrays[i].length && k < SortingServer.arrays[theOtheri].length) {
                if (SortingServer.arrays[i][j] <= SortingServer.arrays[theOtheri][k]) {
                    array2[l] = SortingServer.arrays[i][j];
                    j++;
                } else {
                    array2[l] = SortingServer.arrays[theOtheri][k];
                    k++;
                }
                l++;
            }

            // merge the leftovers from one of the arrays
            while (j < SortingServer.arrays[i].length) { 
                array2[l] = SortingServer.arrays[i][j];
                j++;
                l++;
            }
            
            while (k < SortingServer.arrays[theOtheri].length) {
                array2[l] = SortingServer.arrays[theOtheri][k];
                k++;
                l++;
            }
   
            SortingServer.arrays[theOtheri] = array2; // store merged array in next position for next merge
            SortingServer.mutexArray[theOtheri] = true; // release mutex so this can be merged again
        } // end merge

    } // end run()
} // end class