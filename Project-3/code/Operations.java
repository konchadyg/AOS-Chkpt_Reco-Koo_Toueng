//========================================================
//|     CS6378 - PROJECT 3                               |
//|     Member 1: Piyush Makani [PXM140430]              |
//|     Member 2: Konchady Gaurav Shenoy [KXS168430]     |
//|     Instructor: Dr. Neeraj Mittal                    |
//|     Nov-Dec 2016                                     |
//|     The University of Texas at Dallas                |
//========================================================

public class Operations {

	public int initiaterID[];
	public char operationType[];
	public int currentPointer = 0;
			
			public Operations(int totalNoOfNodes)
			{
				initiaterID = new int[totalNoOfNodes];
				operationType = new char[totalNoOfNodes];
				currentPointer = 0;
			}
}
