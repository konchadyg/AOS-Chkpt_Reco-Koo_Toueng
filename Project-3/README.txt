//========================================================
//|     CS6378 - PROJECT 3                               |
//|     Member 1: Piyush Makani [PXM140430]              |
//|     Member 2: Konchady Gaurav Shenoy [KXS168430]     |
//|     Instructor: Dr. Neeraj Mittal                    |
//|     Nov-Dec 2016                                     |
//|     The University of Texas at Dallas                |
//========================================================

Instructions:

1. Find Project3_<netid1_netid2>.zip
2. In the server, place all the files in $HOME/proj3. Create if not existing. 
   You may change the config file if needed.
3. Update the netid variable, present in the launcher and cleanup script.
4. Run the following commands.

	$ cd $HOME/proj3/code
	$ cd javac -verbose Node.java 
	$ cd ..
	$ chmod -v 755 launcher.sh
	$./launcher.sh

Files with checkpoint data are generated in $HOME as checkpointData<Identifier>.txt
	
5. To check for Consistent Global State.
	Open $HOME/GlobalStates.txt, also generated during the program run.

Consider attached sample output GlobalStates.txt

The data can be tabulated as follows:

------------------------------------------------------------
Node | GS1 | GS2 | GS3 | GS4 | GS5 | GS6 | GS7 | GS8 | GS9 |
------------------------------------------------------------
  0  |  0  |  0  |  1  |  2  |  2  |  2  |  3  |  3  |  3  |
  1  |  0  |  1  |  1  |  2  |  3  |  4  |  4  |  4  |  4  |
  2  |  0  |  0  |  1  |  1  |  1  |  1  |  1  |  1  |  2  |
  3  |  0  |  0  |  1  |  2  |  3  |  4  |  4  |  5  |  5  |
  4  |  0  |  0  |  1  |  2  |  3  |  3  |  3  |  3  |  3  |
------------------------------------------------------------

  
6. To run the cleanup script {Note: The script will delete the output files in $HOME. Please take backups if needed).
	> open another teminal window and login into a different dcXX host (which is not mentioned in config.txt).
	> 	$ bash
		$ cd $HOME/proj3
		$ chmod -v 755 cleanup.sh
		$ ./cleanup.sh
		
===========================================================================================================================
		