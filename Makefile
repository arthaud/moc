#--------------------------------------------------------
# the grammar
XMOC=MOC
#--------------------------------------------------------
# directories containing egg
EJAR=eggc-6.0.0.jar
GJAR=$(EJAR):.
#--------------------------------------------------------
# java, javac, jar
JDIR=/usr/bin
#--------------------------------------------------------
all: src att

src:
	(cd moc ; $(JDIR)/java -jar ../$(EJAR) $(XMOC).egg)

att:
	$(JDIR)/javac -classpath $(GJAR) moc/type/*.java moc/st/*.java moc/cg/*.java moc/compiler/*.java moc/egg/*.java

clean:
	find . -name '*.class' -delete
	rm -rf moc/egg

clean_compiled:
	find . -name '*.tam' -delete
	find . -name '*.x86' -delete
