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
    : 'import' ids += ID ('.' ids+=ID)* ';'
    ;

classDeclaration
    : 'class' ID ('extends' ID)? '{' varDeclaration* methodDeclaration* '}'
    ;

methodDeclaration
    : ('public')? type ID '(' parameters ')' '{' (varDeclaration | statement )* 'return' expression ';' '}' #FunctionDeclaration
    | ('public')? 'void' ID '(' parameters ')' '{' (varDeclaration | statement )* ('return' ';')? '}' #FunctionDeclaration
    | ('public')? 'static' 'void' 'main' '(' ID '[' ']' ID ')' '{' (varDeclaration | statement )* '}' #MainFuncDeclaration
    ;

parameters
    : (type ID (',' type ID)*)?
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
    : '{' statement* '}' #CodeBlock
    | 'if' '(' expression ')' statement 'else' statement #Condition
    | 'while' '(' expression ')' statement #Cycle
    | expression ';' #Expr
    | ID '=' expression ';' #Attribution
    | ID '[' expression ']' '=' expression ';' #Attribution
    ;

expression
    : '(' expression ')' #Parentheses
    | '!' expression #Negation
    | expression op=('*' | '/') expression #BinExp
    | expression op=('+' | '-') expression #BinExp
    | expression op='<' expression #BinExp
    | expression op='&&' expression #BinExp
    | expression '[' expression ']' #ArraySubscript
    | expression '.' 'length' #MemberAccess
    | expression '.' ID '(' (expression (',' expression)*)? ')' #FunctionCall
    | 'new' 'int' '[' expression ']' #ArrayCreation
    | 'new' ID '(' ')' #VarCreation
    | INT #Integer
    | 'true' #Boolean
    | 'false' #Boolean
    | 'this' #This
    | ID #Identifier
    ;
