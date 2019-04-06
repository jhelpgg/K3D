class khelp.asm.Add

method calculate
	parameter	int	first
	parameter	int	second
	return		int
{					; []
	VAR String toto ;
	VAR byte[] data ;
	VAR Add add
	Add()
	ASTORE add
	add.calculate(first, second)
	ILOAD first     ; []				->	[first]
	ILOAD second    ; [first]		    ->	[first, second]
    IADD            ; [first, second]   ->	[first+second]
	IRETURN         ; [first+second]	->	[] EXIT
}
