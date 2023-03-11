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
    : 'import' imports+=ID ('.' imports+=ID)* ';' #ImportDecl
    ;

classDeclaration
    : 'class' classname=ID ('extends' superclass=ID)? '{' varDeclaration* methodDeclaration* '}' #ClassDecl
    ;

methodDeclaration
    : ('public')? type methodname=ID '(' methodDeclarationParameters ')' '{' (varDeclaration | statement)* 'return' expression ';' '}' #MethodDecl
    | ('public')? 'void' methodname=ID '(' methodDeclarationParameters ')' '{' (varDeclaration | statement)* ('return' ';')? '}' #VoidMethodDecl
    | ('public')? 'static' 'void' methodname='main' '(' parametertype=ID '[' ']' parametername=ID ')' '{' (varDeclaration | statement)* '}' #MainMethodDecl
    ;

methodDeclarationParameters
    : (type parametername+=ID (',' type parametername+=ID)*)? #MethodDeclParameters
    ;

methodCallParameters
    : (expression (',' expression)*)? #MethodParameters
    ;

varDeclaration
    : type varname=ID ';' #VarDecl
    ;

type
    : 'int' '[' ']' #TypeArray
    | 'boolean' #TypeBoolean
    | 'int' #TypeInt
    | typename=ID #TypeID
    ;

statement
    : '{' statement* '}' #CodeBlock
    | 'if' '(' condition=expression ')' iftrue=statement 'else' iffalse=statement #Condition
    | 'while' '(' condition=expression ')' whiletrue=statement #Cycle
    | expression ';' #Expr
    | varname=ID '=' value=expression ';' #Assignment
    | arrayname=ID '[' index=expression ']' '=' value=expression ';' #ArrayAssignment
    ;

expression
    : '(' expression ')' #ParenthesesExpr
    | '!' expression #NegationExpr
    | expression op=('*' | '/') expression #BinExpr
    | expression op=('+' | '-') expression #BinExpr
    | expression op=('<' | '>') expression #BinExpr
    | expression op='&&' expression #BinExpr
    | expression op='||' expression #BinExpr
    | expression '[' expression ']' #ArraySubscript
    | expression '.' 'length' #MemberAccess
    | expression '.' methodcall=ID '(' methodCallParameters ')' #MethodCall
    | 'new' 'int' '[' expression ']' #ArrayCreation
    | 'new' classname=ID '(' ')' #ObjectCreation
    | value=INT #Integer
    | value=('true' | 'false') #Boolean
    | 'this' #This
    | value=ID #Identifier
    ;
