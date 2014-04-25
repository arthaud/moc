import sys

def main():
    try:
        source_file = sys.argv[1]
        destination_file = sys.argv[2]
    except IndexError:
        print("Usage: %s source_file destination_file" % sys.argv[0], file=sys.stderr)
        return

    destination = open(destination_file, 'w')

    for line in open(source_file, 'r'):
        if line.strip().startswith('#'):
            line = line.strip()
            if line.startswith('#include'):
                filename = ' '.join(line.split(' ')[1:])
                destination.write('// FILE INCLUDED: %s\n' % filename)
                destination.write(open(filename).read())
                destination.write('// END FILE INCLUDED: %s\n' % filename)
            else:
                print("Unknown preprocessor directive : " % line, file=sys.stderr)
        else:
            destination.write(line)

    destination.close()

if __name__ == '__main__':
    main()
