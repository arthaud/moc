#!/usr/bin/env python3
import sys
import os

class Preprocessor:
    def __init__(self, src, dest, include_dirs, debug=False):
        self.src = src
        self.dest = dest
        self.include_dirs = include_dirs
        self.debug = debug

    def run(self):
        with open(self.dest, 'w') as self.destination:
            self.included = []
            self.include(self.src)

    def include(self, path):
        try:
            with open(path, 'r') as f:
                self.included.append(os.path.abspath(path))
                line_number = 0

                for line in f.readlines():
                    if line.strip().startswith('#'):
                        line = line.strip()
                        if line.startswith('#include'):
                            filename = ' '.join(line.split(' ')[1:])

                            if filename[0] in ('"', "'") and filename[-1] in ('"', "'"):
                                filename = filename[1:-1]

                            include_paths = list(filter(os.path.isfile, map(lambda s: os.path.join(s, filename), self.include_dirs)))

                            if not include_paths:
                                print('error: Cannot find included file "%s"' % filename, file=sys.stderr)
                                exit(1)

                            include_path = include_paths[0]
                            if os.path.abspath(include_path) in self.included:
                                print('warning: File %s already included, ignoring' % filename)
                            else:
                                self.destination.write('// FILE INCLUDED: %s\n' % filename)
                                self.include(include_path)
                                self.destination.write('// END FILE INCLUDED: %s\n' % filename)
                        else:
                            print('error: Unknown preprocessor directive "%s"' % line, file=sys.stderr)
                            exit(1)
                    elif self.debug:
                        self.destination.write(line.rstrip('\r\n') + '// %s:%s\n' % (path, line_number))
                    else:
                        self.destination.write(line)

                    line_number += 1
        except FileNotFoundError as e:
            print(e, file=sys.stderr)
            exit(1)

if __name__ == '__main__':
    import argparse

    parser = argparse.ArgumentParser(description='Prepocessor for the Micro Objective-C language')
    parser.add_argument('-d', '--debug', action='store_const', const=True, default=False, help='Add comments on each line with original file and line')
    parser.add_argument('-I', default='.', help='The list of directories to be searched for include files, separated by colon')
    parser.add_argument('src', metavar='SRC', type=str, help='The source file')
    parser.add_argument('dest', metavar='DEST', type=str, help='The destination file')
    args = parser.parse_args()

    include_dirs = list(filter(None, args.I.split(':')))
    Preprocessor(args.src, args.dest, include_dirs, args.debug).run()
