#!c:\perl\bin\perl.exe

sub getFormData{
	my($buffer)="";

	if($ENV{'REQUEST_METHOD'} eq 'GET'){
		$buffer = $ENV{'QUERY_STRING'};
	}else{
		read(STDIN, $buffer, $ENV{'CONTENT_LENGTH'})
	}
	
	@i = split(/&/,$buffer);
	foreach $i (@i) {
		($clave, $valor) = split(/=/, $i);
		$valor=~tr/+/ /;
		$valor=~s/%(..)/pack("c",hex($1))/ge;
		$clave=~tr/+/ /;
		$clave=~s/%(..)/pack("c",hex($1))/ge;
		if(!defined($field{$clave})){
			$field{$clave}= $valor;
		}else{
			$field{$clave} .= ",$valor";
		}
	}
}

#Sub GetFormInput {
#	(*fval) = @_ if @_;
#	local($buf);

#	if($ENV{'REQUEST_METHOD'} eq 'POST'){
#		read(STDIN,$buf,$ENV{'CONTENT_LENGTH'})
#	}else{
#		$buf=$ENV{'QUERY_STRING'};
#	}
#	if($buf eq ""){
#		return 0;
#	}else{
#		@fval=split(/&/,$buf);
#		foreach $i (0 .. $#fval){
#			($name,$val)=split(/=/, fval[$i],2);
#			$val=~tr/+/ /;
#			$val=~s/%(..)/pack("c",hex($1))/ge;
#			$name=~tr/+/ /;
#			$name=~s/%(..)/pack("c", hex($1))/ge;
#
#			if(!defined($field{$name})){
#				$field{$name}=$val;
#			}else{
#				$field{$name} .= ",$val";
#			}
#		}
#	}
#	return 1;
#}

sub showENV{
	foreach (keys %ENV) {
		print("<B>$_:</B> $ENV{$_}<hr>");
	}
}



1;