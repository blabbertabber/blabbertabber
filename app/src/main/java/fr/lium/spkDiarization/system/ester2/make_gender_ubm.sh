#!/bin/bash

version=$1
prog=$2
d=$3
echo "-->"$version
echo "-->"$prog
#parametre
nbcep=15
nbcepE=16
wdir=.

mfccdir=$wdir/sph_gmm

silThr=0.1

nbComp=128
name=gender-v$version-$d


gmmdir=$wdir/$name
mkdir $gmmdir
segdir=$wdir/seg_gmm
wldlst=$gmmdir/wld.lst
rm -f $wldlst

#for lst in FS-small; do
for lst in MS MT FS FT; do
    segIn=$segdir/$lst.seg
    segSil=$gmmdir/$lst.sil.seg
    segConcat=$gmmdir/$lst.concat.seg
    segCR=$gmmdir/$lst.wld.cr.seg
    seggmm=$gmmdir/$lst.wld.seg
    gmmInit=$gmmdir/$lst.init.gmm
    gmm=$gmmdir/$lst.gmm
    fMask=$mfccdir"/%s.sph"
    fMask2=$gmmdir"/%s.mfcc"

    echo $gmm >> $wldlst
	echo "make $gmm"
	
	#make silence segmentation
	java -Xmx2024m -classpath $prog fr.lium.spkDiarization.programs.MSpeechDetector --help --sInputMask=$segIn --sOutputMask=$segSil --fInputMask=$fMask --fInputDesc=audio16kHz2sphinx,1:1:0:0:0:0,$nbcepE,0:0:0 --fInputSpeechThr=$silThr --fInputMemoryOccupationRate=0.4 $lst 
	# concat and norm prm
	cat $segSil | awk -v name=$lst '{print $1" "$2" "$3" "$4" "$5" "$6" "$7" "$8""name;}' | sort > $segConcat

	java -Xmx2024m -classpath $prog fr.lium.spkDiarization.tools.SConcatFeatureSet --help --fOutputDesc=sphinx,1:1:0:0:0:0,$nbcepE,0:0:0 --sInputMask=$segConcat --sOutputMask=$segCR --fInputMask=$fMask --fOutputMask=$fMask2 --fInputDesc=audio16kHz2sphinx,1:1:0:0:0:0,$nbcepE,0:0:0 --fOutputMemoryOccupationRate=0.4 $lst

	#make model
	mkdir $gmmdir 2> /dev/null
	cat $segCR | awk -v name=$lst '{print $1" "$2" "$3" "$4" "$5" "$6" "$7" "name;}' | sort > $seggmm

	java -Xmx2024m -classpath $prog fr.lium.spkDiarization.programs.MTrainInit --help --sInputMask=$seggmm --fInputMask=$fMask2 --fInputDesc=sphinx,1:3:2:0:0:0,$nbcepE,1:1:300:4 --kind=DIAG --nbComp=$nbComp --tInputMask="" --emCtrl=1,5,0.05 --emInitMethod=split_all --varCtrl=0.01,10.0 --tOutputMask=$gmmInit $lst
	java -Xmx2024m -classpath $prog fr.lium.spkDiarization.programs.MTrainEM --help --sInputMask=$seggmm --fInputMask=$fMask2 --emCtrl=1,20,0.01 --varCtrl=0.01,10.0 --fInputDesc=sphinx,1:3:2:0:0:0,$nbcepE,1:1:300:4 --kind=DIAG --nbComp=$nbComp --tInputMask=$gmmInit --tOutputMask=$gmm $lst
done

java -Xmx2024m -classpath $prog fr.lium.spkDiarization.tools.SMergeModel --help --fInputMask=$wldlst --tOutputMask=$gmmdir/ubm.gmm
#java -Xmx1024m -classpath "$LOCALCLASSPATH" fr.lium.spkDiarization.tools.SConcatModel $swap --help --trace --fInputMask=$wldlst --fOutputMask=$gmmdir/gender.gmms

