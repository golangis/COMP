grammar Javamm;

@header {
    package pt.up.fe.comp2023;
}

INT : '0' | [1-9][0-9]* ;
ID : [a-zA-Z_$][a-zA-Z_$0-9]* ;

WS : [ \t\n\r\f]+ -> skip ;
SINGLE_LINE_COMMENT : '//' (~'\n')* -> skip;
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
    : ('public')? type methodname=ID '(' methodDeclarationParameters ')' '{' varDeclaration* statement* 'return' expression ';' '}' #MethodDecl
    | ('public')? 'void' methodname=ID '(' methodDeclarationParameters ')' '{' varDeclaration* statement* ('return' ';')? '}' #VoidMethodDecl
    | ('public')? 'static' 'void' methodname='main' '(' parametertype=ID '[' ']' parametername=ID ')' '{' varDeclaration* statement* '}' #MainMethodDecl
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

type locals[boolean isArray = false]
    : typename='int' (('[' ']') { $isArray = true; })?
    | typename='boolean'
    | typename=ID
    ;

statement
    : '{' statement* '}' #CodeBlock
    | 'if' '(' expression ')' statement 'else' statement #Condition
    | 'while' '(' expression ')' statement #Cycle
    | expression ';' #Expr
    | varname=ID '=' expression ';' #Assignment
    | arrayname=ID '[' expression ']' '=' expression ';' #ArrayAssignment
    ;

expression
    : '(' expression ')' #ParenthesesExpr
    | 'new' 'int' '[' expression ']' #ArrayCreation
    | 'new' classname=ID '(' ')' #ObjectCreation
    | expression '[' expression ']' #ArraySubscript
    | expression '.' field='length' #LengthFieldAccess
    | expression '.' methodcall=ID '(' methodCallParameters ')' #MethodCall
    | '!' expression #NegationExpr
    | expression op=('*' | '/') expression #ArithmeticExpr
    | expression op=('+' | '-') expression #ArithmeticExpr
    | expression op=('<' | '>') expression #ComparisonExpr
    | expression op='&&' expression #LogicalExpr
    | expression op='||' expression #LogicalExpr
    | value=INT #Integer
    | value=('true' | 'false') #Boolean
    | 'this' #This
    | value=ID #Identifier
    ;
