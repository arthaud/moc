TEXC = latexmk -lualatex

.PHONY: FORCE

all: report.pdf language.pdf FORCE

%.pdf: %.tex FORCE
	$(TEXC) -latexoption=-shell-escape $<

clean:
	rm -f *.aux
	rm -f *.log
	rm -f *.lol
	rm -f *.out
	rm -f *.toc
