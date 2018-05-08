#!/bin/bash


# Change this to your netid
netid=kxs168430


# Root directory of your project
PROJDIR=$HOME/proj3

CONFIG=$PROJDIR/code/config.txt

#
# Directory your java classes are in
#
BINDIR=$PROJDIR/code

#
# Your main project class
#
PROG=Node
#PROG=Test

#cat $CONFIG | sed -e "s/#.*//" | sed -e "/^\s*$/d" | grep "\s*[0-9]\+\s*\w\+.*" |
cat $CONFIG | sed -e "s/#.*//" | sed -e "/^\s*$/d" | grep "dc" |
(
    while read line 
    do

	n=$( echo $line | awk '{ print $1 }' )
	host=$( echo $line | awk '{ print $2 }' )
	
	echo "ID" $n "Host: " $host;
	
        ssh -o StrictHostKeyChecking=no $netid@$host 'killall -9 java' &
    done
)

#rm -f $HOME/output[0-9].txt;

echo "Removing below files:"
ls -ltr  $HOME/{checkpointData*.txt,GlobalStates.txt}

rm -f $HOME/{checkpointData*.txt,GlobalStates.txt}

#mv $HOME/{output.txt,output.old};
#echo "" > $HOME/output.txt;


