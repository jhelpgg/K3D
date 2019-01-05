class khelp.asm.Add

implements Operation

method calculate
	parameter	int	first
	parameter	int	second
	return		int
{					; []
	ILOAD first     ; []				->	[first]
	ILOAD second    ; [first]		    ->	[first, second]
    IADD            ; [first, second]   ->	[first+second]
	IRETURN         ; [first+second]	->	[] EXIT
}
