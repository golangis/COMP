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
    : 'import' imports+= ID ('.' imports+=ID)* ';' #ImportDecl
    ;

classDeclaration
    : 'class' name = ID ('extends' ID)? '{' varDeclaration* methodDeclaration* '}' #ClassDecl
    ;

methodDeclaration
    : ('public')? type name = ID '(' parameters ')' '{' (varDeclaration | statement )* 'return' expression ';' '}' #MethodDecl
    | ('public')? 'void' name = ID '(' parameters ')' '{' (varDeclaration | statement )* ('return' ';')? '}' #MethodDecl
    | ('public')? 'static' 'void' 'main' '(' ID '[' ']' ID ')' '{' (varDeclaration | statement )* '}' #MainMethodDecl
    ;

parameters
    : (type ID (',' type ID)*)? #ParametersDecl
    ;

varDeclaration
    : type ID ';' #VarDecl
    ;

type
    : 'int' '[' ']' #TypeArray
    | 'boolean' #TypeBoolean
    | 'int' #TypeInt
    | ID #TypeID
    ;

statement
    : '{' statement* '}' #CodeBlock
    | 'if' '(' expression ')' statement 'else' statement #Condition
    | 'while' '(' expression ')' statement #Cycle
    | expression ';' #Expr
    | ID '=' expression ';' #Assignment
    | ID '[' expression ']' '=' expression ';' #ArrayAssignment
    ;

expression
    : '(' expression ')' #ParenthesesExpr
    | '!' expression #NegationExpr
    | expression op=('*' | '/') expression #BinExpr
    | expression op=('+' | '-') expression #BinExpr
    | expression op='<' expression #BinExpr
    | expression op='&&' expression #BinExpr
    | expression '[' expression ']' #ArraySubscript
    | expression '.' 'length' #MemberAccess
    | expression '.' ID '(' (expression (',' expression)*)? ')' #MethodCall
    | 'new' 'int' '[' expression ']' #ArrayCreation
    | 'new' ID '(' ')' #VarCreation
    | INT #Integer
    | 'true' #Boolean
    | 'false' #Boolean
    | 'this' #This
    | ID #Identifier
    ;
