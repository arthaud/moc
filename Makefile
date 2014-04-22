#--------------------------------------------------------
# the grammar
XMOC=MOC
XASM=ASM
#--------------------------------------------------------
# directories containing egg
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
	$(JDIR)/javac -classpath $(GJAR) moc/type/*.java
	$(JDIR)/javac -classpath $(GJAR) moc/st/*.java
	$(JDIR)/javac -classpath $(GJAR) moc/cg/*.java
	$(JDIR)/javac -classpath $(GJAR) moc/compiler/*.java

class:
	$(JDIR)/javac -classpath $(GJAR) moc/egg/*.java

clean:
	find . -name '*.class' -delete
	rm -rf moc/egg
