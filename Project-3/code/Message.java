//========================================================
//|     CS6378 - PROJECT 3                               |
//|     Member 1: Piyush Makani [PXM140430]              |
//|     Member 2: Konchady Gaurav Shenoy [KXS168430]     |
//|     Instructor: Dr. Neeraj Mittal                    |
//|     Nov-Dec 2016                                     |
//|     The University of Texas at Dallas                |
//========================================================

import java.io.Serializable;
public class Message implements Comparable<Object>, Serializable {
	private static final long serialVersionUID = 5950169519310163575L;
	
	boolean complete = false;
	boolean application = false;
	boolean checkpointRequest = false;
	boolean recoveryRequest = false; 
	
	int[] vectorClock;
	int label;
	int originator;
	int llrValue;
	int llsValue;
	
	
	@Override
	public int compareTo(Object arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

//	public int compareTo(Object anotherMessage) throws ClassCastException 
//	{
//	    if (!(anotherMessage instanceof Message)) throw new ClassCastException("A Person object expected.");
//	    int anotherMessageTime = ((Message) anotherMessage).vectorClock;  
//	    return this.vectorClock - anotherMessageTime;    
//	}
	
}
