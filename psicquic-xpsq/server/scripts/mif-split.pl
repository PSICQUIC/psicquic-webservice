#!/usr/bin/perl

#-------------------------------------------------------------------------------
#
# mif-splitter: splits a mif file into parts; parts correspond to individual 
#               mif entries further split into segments that contain no more 
#               than SIZE interactions  
#                
#   NOTE: works ONLY with expanded files that contain no internal references
#
#-------------------------------------------------------------------------------


my $file="";
my $size = 500;

for( my $i=0; $i < @ARGV; $i++ ) {
    if( $ARGV[$i]=~/FILE=(.+)/ ) {
        $file = $1;
    }

    if( $ARGV[$i]=~/SIZE=(.+)/ ) {
        $size = $1;
    }
}

if($file eq "" ) { exit; }

open( F, "<$file" );

my $eset = "";
my $esetOK = 0;

my $entryNo = 0;

while( my $ln = <F> ){
    
    if( $esetOK == 0 ){
        $eset .= $ln;
    }
  
    if( $ln =~ /\<entrySet / ){
        &parseEntry();
    }
}

sub parseEntry{

    my $entryOK = 0;
    
    while( my $ln = <F> ){
        if( $ln =~ /\<entry/ ){
            $entryOK = 1;
            $entryNo++;
            print "PE ".$entryNo." ".$ln;
        }
     
        if( $entryOK == 1){
            &parseBlocks( $entryNo );
            $entryOK = 0;
        }
   
        if( $ln =~ /\<\/entry/ ){
            $entryOK = 0;
            print $entryNo." done\n";
        }
        
        if( $ln =~ /\<\/entrySet/ ){
            $entryOK = 0;
            goto EX;
        }
    }
  EX:
    print $entryNo."  done\n";
}
    
sub parseBlocks{
    
    my ( $entryNo ) = @_;

    my $head = "<entry>\n";
    my $headOk = 0;

    #print "XXX $entryNo\n";    
    
    while( my $ln = <F> ){
        
        if( $headOk == 0){
            $head .= $ln;
        }
        
        if( $ln =~ /\<interactionList\>/ ){
            $headOk = 1;
            goto BH;
        }
    }
    
  BH:
    my $block = 1;
    my $blockNo = 1;
    my $blockIn = 0;
    
    while( my $ln = <F> ){
        
        if( $ln =~ /<interaction / ){
            if( $blockIn == 0 ){
                open(OF, ">$file"."-".$entryNo."-".$blockNo);
                print ">$file"."-".$entryNo."-".$blockNo."\n";
                $blockNo++;
                $blockIn = 1;
                print OF $eset."\n".$head;
            }
        }
        
        if( $blockIn > 0){
            print OF $ln;
        }
        
        if( $ln=~/<\/interaction>/ ){   
            if( $block == $size ){ 
                print OF "</interactionList>\n</entry>\n</entrySet>\n";
                close(OF);
                $block = 1;
                $blockIn = 0;
            } else {
                $block++;
            }
        }
          
        if( $ln=~/<\/interactionList/ ){
            $blockIn = 0;
            print OF "</entry>\n</entrySet>\n";
            close(OF);
            goto C;
        }
    }    
  C:
}


