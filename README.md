# Card identifier
Simple app to identify cards on poker desk

# Task
- Identify cards in center of the screen, example of screens could be found in [`/img_marked](/imgs_marked).
- Error rate in card recognition shouldn't be more than 3%. 
- No usage of frameworks.

# Solution
Example data set split on two not equivalent parts by [`SetDivider`](/src/com/brainshells/exam/test/SetDivider.java) for train and check.\
The [`Train`](/src/com/brainshells/exam/test/Train.java) is responsible to process train data set and generate value vectors.\
To generate value vectors at first central cards are cut from the screen, then set of cards split by one. 
Each individual image of card is split on 9 (3x3) sectors, for each sector portion of black pixels are calculated this result a vector value.
All this logic could be found in [`CardUtils`](/src/com/brainshells/exam/test/CardUtils.java).\
The [`Main`](/src/com/brainshells/exam/test/Main.java) is make the same for check data set provided with first argument, 
simple KNN algorithm is used to identify card by comparing vectors.

# Compile & Run
To compile Java 17+ is required.\
The [`run.bat`](/run.bat) script could be used in Windows to run application, e.g.
```shell
./run.bat imgs_marked
```