JFLAGS = -cp .:jai_core.jar:jai_codec.jar

default: Mimage.class Image.class 

Mimage.class: Mimage.java
	javac $(JFLAGS) Mimage.java

Image.class: Image.java
	javac $(JFLAGS) Image.java

run: Mimage.class
	java $(JFLAGS) Mimage ./Dataset/ 352 288

clean:
	$(RM) *.class