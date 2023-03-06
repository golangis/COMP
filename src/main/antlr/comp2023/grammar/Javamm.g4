grammar Javamm;

@header {
    package pt.up.fe.comp2023;
}

INT : '0' | [1-9][0-9]* ;
ID : [a-zA-Z_$][a-zA-Z_$0-9]* ;

WS : [ \t\n\r\f]+ -> skip ;
SINGLE_LINE_COMMENT : '//' (~'\n')* '\n' -> skip;
MULTI_LINE_COMMENT: '/*' .*? '*/' -> skip;

program
    : importDeclaration* classDeclaration EOF
    ;

importDeclaration
    : 'import' ID ('.' ID)* ';'
    ;

classDeclaration
    : 'class' ID ('extends' ID)? '{' varDeclaration* methodDeclaration* '}'
    ;

methodDeclaration
    : ('public')? type ID '(' (type ID (',' type ID)*)? ')' '{' varDeclaration* statement* 'return' expression ';' '}'
    | ('public')? 'static' 'void' 'main' '(' 'String' '[' ']' ID ')' '{' varDeclaration* statement* '}'
    ;

varDeclaration
    : type ID ';'
    ;

type
    : 'int' '[' ']'
    | 'boolean'
    | 'int'
    | ID
    ;

statement
    : '{' statement* '}'
    | 'if' '(' expression ')' statement 'else' statement
    | 'while' '(' expression ')' statement
    | expression ';'
    | ID '=' expression ';'
    | ID '[' expression ']' '=' expression ';'
    ;

expression
    :  '(' expression ')'
    |  '!' expression
    | expression op=('*' | '/') expression
    | expression op=('+' | '-') expression
    | expression op='<' expression
    | expression op='&&' expression
    | expression '[' expression ']'
    | expression '.' 'length'
    | expression '.' ID '(' (expression (',' expression)*)? ')'
    | 'new' 'int' '[' expression ']'
    | 'new' ID '(' ')'
    | INT
    | 'true'
    | 'false'
    | 'this'
    | ID
    ;
