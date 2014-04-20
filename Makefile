#--------------------------------------------------------
# la grammaire
XMOC=MOC
XASM=ASM
#--------------------------------------------------------
# répertoires contenant egg
EJAR=eggc-6.0.0.jar
GJAR=$(EJAR):.
#--------------------------------------------------------
# java, javac, jar
JDIR=/usr/bin
#--------------------------------------------------------
all: src att class

src:
	(cd moc ; $(JDIR)/java -jar ../$(EJAR) $(XMOC).egg)
	(cd moc ; $(JDIR)/java -jar ../$(EJAR) $(XASM).egg)

att:
	$(JDIR)/javac -classpath $(GJAR) moc/compiler/*.java
	$(JDIR)/javac -classpath $(GJAR) moc/tds/*.java
	$(JDIR)/javac -classpath $(GJAR) moc/type/*.java
	$(JDIR)/javac -classpath $(GJAR) moc/gc/*.java

class:
	$(JDIR)/javac -classpath $(GJAR) moc/egg/*.java

clean:
	rm -f moc/compiler/*.class
	rm -f moc/tds/*.class
	rm -f moc/type/*.class
	rm -f moc/gc/*.class
	rm -rf moc/egg
