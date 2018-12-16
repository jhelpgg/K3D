class khelp.asm.Div

field int test public final

method <init>
    parameter   int test
{                                   ; []
    ALOAD this                      ; []            ->  [this]
    DUP                             ; [this]        ->  [this, this]
    INVOKESPECIAL Object.<init>()   ; [this, this]  ->  [this]
    ILOAD test                      ; [this]        ->  [this, test]
    PUTFIELD test                   ; [this, test]  ->  []
    RETURN                          ; []            ->  [] EXIT
}


method calculate open
	parameter	int	first
	parameter	int	second
	return		int
{												; []
	ILOAD first									; []						->	[first]
	ILOAD second								; [first]		    		->	[first, second]
	TRY ArithmeticException arithmeticException
		TRY Exception exception
			IDIV 								; [first, second]			->	[first/second]
			IRETURN								; [first/second]			->	[] EXIT
		CATCH exception issue
	CATCH arithmeticException arithmetic

LABEL issue										; [Exception]
	PUSH -1										; [Exception]				->	[Exception, -1]
	IRETURN										; [Exception, -1]			->	[] EXIT

LABEL arithmetic								; [ArithmeticException]
	PUSH -2										; [ArithmeticException]     ->  [ArithmeticException, -2]
	IRETURN										; [ArithmeticException, -2] ->  [] EXIT
}