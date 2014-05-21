./preprocessor.py $1 /tmp/p.moc && ./mocc /tmp/p.moc && nasm -f elf /tmp/p.x86 && ld -m elf_i386 -o ${1%.*} /tmp/p.o
