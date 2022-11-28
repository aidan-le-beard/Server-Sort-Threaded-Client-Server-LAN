# Threaded Sort Using Client Server LAN Connection

## You do NOT have permission to use this code for any schoolwork purposes under any circumstances. 

## You do NOT have permission to use this code for any commercial purposes without speaking to me to work out a deal.

This program initially creates a LAN connection between a client and a server. The client creates random arrays of size N, 10N, 100N, and 1000N. The client sends the random arrays to the server, who sorts each array with 1 thread, 2 threads, 4 threads, 8 threads, and 16 threads. The array is decomposed into arrays of equal size of the number of threads that the sort is using. Sorting is done using insertion sort in a thread for the decomposed arrays, and then each array is merged together using the merge step of merge sort, also in a thread. The number of runs one wishes to happen is configurable, and the program prints out a table of the average sort time based on the number of runs/trials, detailing how long the sort took with the varying sizes and varying amount of threads, as well as the speedup value T(n) / T(n,p), and the efficiency value ((T(n) / T(n,p)) / p).

### To execute on Windows:

Knowledge of Java IDE usage and Java code execution is assumed.

1) Place Client.java on one machine.
2) Place SortThread.java and SortingServer.java on a second machine that is on the same Wi-Fi network.
3) Edit Client.java line 10: put the IP of the machine with the other 2 files on it. This can be found on the other machine in command prompt by typing “ipconfig” and retrieving the IPv4 Address from the section titled “Wireless LAN adapter Wi-Fi.”
4) **Optional:** edit Client.java lines 11 and 12 to be how many runs (trials) you wish to average, and what the starting array size you wish to be sorted is, respectively. Default is 1 run averaged, and N starting at 10. The program sorts an array of size N, N\*10, N\*100, and N\*1000. 
5) Run Client.java on one machine, and SortingServer.java on the second machine.

### Sample Output:

![image](https://user-images.githubusercontent.com/33675444/204068779-6694fce1-fa75-49a6-91eb-7906aa906bb4.png)

![image](https://user-images.githubusercontent.com/33675444/204068788-5d35c74a-dc96-4dce-8056-08e7d28ce2ab.png)

![image](https://user-images.githubusercontent.com/33675444/204068795-1779f249-9dde-4801-a79c-f20e89d72c08.png)
