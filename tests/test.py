#!/usr/bin/env python3
import subprocess
import re

# Manage colors
ALL_OFF = '\033[1;0m'
BOLD = '\033[1;1m'
BLUE = BOLD + '\033[1;34m'
GREEN = BOLD + '\033[1;32m'
RED = BOLD + '\033[1;31m'
YELLOW = BOLD + '\033[1;33m'

def bold(s):
    return BOLD + s + ALL_OFF

def blue(s):
    return BLUE + s + ALL_OFF

def green(s):
    return GREEN + s + ALL_OFF

def red(s):
    return RED + s + ALL_OFF

def yellow(s):
    return YELLOW + s + ALL_OFF

def compile_command(path):
    return ['../mocc', '-m', 'tam', path]

def check(title, path, error=False, stderr_match=None, stdout_match=None):
    global tests, errors
    p = subprocess.Popen(compile_command(path), stdout=subprocess.PIPE, stderr=subprocess.PIPE)

    tests += 1
    message = '  %s ... ' % title

    p.wait()
    if (error == (p.returncode > 0)
        and (stdout_match is None or re.search(stdout_match, p.stdout.read().decode('utf8'), re.DOTALL))
        and (stderr_match is None or re.search(stderr_match, p.stderr.read().decode('utf8'), re.DOTALL))):
        message += 'ok'
    else:
        errors += 1
        message += red('error')

    print(message)

def run():
    global tests, errors
    tests = 0
    errors = 0

    print(bold('Cleaning tests...'))
    subprocess.check_call(['find', '.', '-name', '*.tam', '-delete'])

    print(bold('Running tests...'))
    check('empty code', 'success/empty.moc')
    check('error with a variable already defined',
        'error/locals-1.moc', True, 'The variable a already exists')
    check('error when using an undefined variable',
        'error/locals-2.moc', True, 'Variable x undefined')
    check('affectation',
        'error/affectation.moc', True, 'Left operand cannot be affected')

    check('simple test with functions', 'success/functions.moc')
    check('error with functions with the same name',
        'error/functions-1.moc', True, 'The variable main already exists')
    check('error with an already defined parameter',
        'error/functions-2.moc', True, 'The parameter a already exists')
    check('error with undefined functions',
        'error/functions-3.moc', True, 'Function print undefined')
    check('too few parameters',
        'error/functions-4.moc', True, 'Too few arguments to call print')
    check('too many parameters',
        'error/functions-5.moc', True, 'Too many arguments to call print')
    check('incompatible parameter',
        'error/functions-6.moc', True, 'Types void and int are incompatible')
    check('incompatible return type',
        'error/return.moc', True, 'Types int and pointer on char are incompatible')

    check('useless casting 1',
        'warning/cast-useless-1.moc', False, 'Useless cast')
    check('useless casting 2',
        'warning/cast-useless-2.moc', False, 'Useless cast')

    check('if statement 1', 'success/if.moc')
    check('if statement 2',
        'error/if.moc', True, 'Type char cannot be casted to boolean')

    check('while statement 1', 'success/while.moc')
    check('while statement 2',
        'error/while.moc', True, 'Type char cannot be casted to boolean')

    check('inline asm', 'success/asm.moc')
    check('pointers', 'success/pointer.moc')
    check('strings', 'success/string.moc')

    check('type consistency 1', 'success/types-1.moc')
    check('type consistency 2', 'success/types-2.moc')
    check('type consistency 3',
        'error/types-1.moc', True, 'Types int and char are incompatible')
    check('type consistency 4',
        'error/types-2.moc', True, 'Types int and bool are incompatible')
    check('type consistency 5',
        'error/types-3.moc', True, 'Cannot use operator "\+" on types int and bool')
    check('type consistency 6',
        'error/types-4.moc', True, 'Cannot cast type int to type void')
    check('type consistency 7',
        'error/types-5.moc', True, 'print is not a function')
    check('type consistency 8',
        'error/types-6.moc', True, 'print is not a variable')
    check('type consistency 9',
        'error/types-7.moc', True, 'Types int and char are incompatible')

    check('class definition', 'success/class.moc')
    check('class inheritance', 'success/class-inheritance.moc')

    check('error with classes with the same name',
        'error/class-1.moc', True, 'The variable Point already exists')
    check('error with undefined superclass',
        'error/class-2.moc', True, 'Class Foo undefined')
    check('error with already defined attributes 1',
        'error/class-3.moc', True, 'The attribute x already exists')
    check('error with already defined attributes 2',
        'error/class-4.moc', True, 'The attribute x already exists')
    check('error with already defined methods 1',
        'error/class-5.moc', True, 'The method int \(null x\) already exists')
    check('error with already defined methods 2',
        'error/class-6.moc', True, 'The method void \(int x, int y\) already exists')
    check('error with already defined parameters 1',
        'error/class-7.moc', True, 'The parameter x already exists')
    check('error with already defined parameters 2',
        'error/class-8.moc', True, 'The variable a already exists')
    check('error when using self outside a method',
        'error/class-9.moc', True, 'Using self outside a method')
    check('error when using self inside a static method',
        'error/class-10.moc', True, 'Method id \(null test\) of class Point is static')
    check('error when using super outside a method',
        'error/class-11.moc', True, 'Using super outside a method')
    check('error when using super inside a static method',
        'error/class-12.moc', True, 'Method id \(null test\) of class Point is static')
    check('error when using super inside a class without superclass',
        'error/class-13.moc', True, 'Cannot use super in a class without a superclass')
    check('error with already defined arguments',
        'error/class-14.moc', True, 'The parameter x already exists')
    check('error when trying to call a method of a no instance type',
        'error/class-15.moc', True, 'Type int is not an instance type')
    check('error with undefined methods',
        'error/class-16.moc', True, 'Class Point have no method null foo')
    check('error when trying to call a static method with an instance',
        'error/class-17.moc', True, 'Method null init of class Point is static')
    check('error when trying to call a static method of an undefined class',
        'error/class-18.moc', True, 'Class Foo undefined')
    check('error with undefined static methods',
        'error/class-19.moc', True, 'Class Point have no method null foo')
    check('error when trying to call a no static method of a class',
        'error/class-20.moc', True, 'Method null x of class Point is not static')

    check('type consistency with classes 1',
        'error/class-21.moc', True, 'Class Point have no method bool x, int y')
    check('type consistency with classes 2',
        'error/class-22.moc', True, 'Types bool and int are incompatible')
    check('type consistency with classes 3',
        'error/class-23.moc', True, 'Types int and bool are incompatible')
    check('type consistency with classes 4',
        'error/class-24.moc', True, 'Types int and bool are incompatible')
    check('type consistency with classes 5',
        'error/class-25.moc', True, 'Types instance of class Foo (.*) and instance of class Point (.*) are incompatible')

    check('class casting', 'success/class-cast.moc')

    print(bold('Results :'))
    if errors > 0:
        print(red('  %s tests failed.' % errors))
    else:
        print(green('  %s tests passed successfully.' % tests))

run()
