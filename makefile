JFLAGS = -cp .:jai_core.jar:jai_codec.jar

default: Mimage.class Image.class Clusterizer.class

Mimage.class: Mimage.java
	javac $(JFLAGS) Mimage.java

Image.class: Image.java
	javac $(JFLAGS) Image.java

Clusterizer.class: Clusterizer.java
	javac $(JFLAGS) Clusterizer.java

run: Mimage.class
	java $(JFLAGS) Mimage ./Dataset/ 352 288 3

clean:
	$(RM) *.class