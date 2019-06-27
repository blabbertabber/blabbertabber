## Comparison of Diarizers

_This is a comparison of several diarizer back-ends over the course of a few
years. The comparisons are neither consistent nor comprehensive._

### Table of Contents

* [Comparison of Diarizers](#comparison-of-diarizers)
   * [Test Methodology](#test-methodology)
   * [Google Cloud Speech-to-Text](#google-cloud-speech-to-text)
   * [Aalto-speech (Aalto University, Finland)](#aalto-speech-aalto-university-finland)
   * [IBDiarization (Idiap Research Institute)](https://github.com/idiap/IBDiarization/tree/master/src/pydiarization)
   * [IBM Bluemix Watson Speech To Text (STT)](#ibm-bluemix-watson-speech-to-text-stt)
      * [Testing](#testing)
   * [ICSI](#icsi)
   * [LIUM](#lium)
   * [DiarTK](#diartk)

### Test Methodology

We test each diarizer the software using a known corpus. We use the [AMI
Corpus](http://groups.inf.ed.ac.uk/ami/download/) <sup><a
href="#ami">[AMI]</a></sup> , which is used by the [NIST Rich Transcription
Evaluation](http://nist.gov/itl/iad/mig/rt.cfm).

We download the AMI Corpus
[annotations](http://groups.inf.ed.ac.uk/ami/AMICorpusAnnotations/ami_public_manual_1.6.2.zip)
to score the diarizer performance, using
[`pyannote.metrics`](https://github.com/pyannote/pyannote-metrics)
<sup><a href="#scoring">[scoring]</a></sup>
.

We make sure to convert the output of our diarizers into the RTTM
<sup><a href="#rttm">[RTTM]</a></sup>
format
to score our results

#### Setup

```
pip3 install pyannote.metrics
pip3 install pyannote.db.odessa.ami
```

_Other corpus databases can be found here: <https://pypi.org/search/?q=pyannote.db>._

### Aalto-speech (Aalto University, Finland)

~~Summary: the Aalto diarizer performed well, with a calculated **20.4% overall
speaker diarization error**. On a 2-CPU system (Intel(R) Core(TM) i7-6770HQ CPU @
2.60GHz), it diarizes a 600-second file in 88 seconds, **6.8×** faster than
realtime.~~

Summary: the Aalto diarizer had an overall speaker diarization error of 44.33%. It was
correct slightly more than half the time.

```bash
docker pull blabbertabber/aalto-speech-diarizer
cd ~/go/src/github.com/blabbertabber/blabbertabber
for MEETING in ES2011{a,b,c,d}; do
  time docker run \
    --volume=$PWD/benchmarks:/benchmarks \
    --workdir /speaker-diarization \
    blabbertabber/aalto-speech-diarizer \
    /speaker-diarization/spk-diarization2.py \
      /benchmarks/sources/amicorpus/${MEETING}/audio/${MEETING}.Mix-Headset.wav \
      -o /benchmarks/Aalto/${MEETING}.out
  #
  perl -ne \
      "@l = split; (\$start = \$l[2]) =~ s/start-time=//;
      (\$end = \$l[3]) =~ s/end-time=//;
      \$dur = \$end - \$start;
      (\$spkr = \$l[4]) =~ s/speaker=//;
      print \"SPEAKER $MEETING 1 \$start \$dur <NA> <NA> \$spkr <NA>\n\"" \
    < benchmarks/Aalto/${MEETING}.out \
    > benchmarks/Aalto/${MEETING}.rttm
  ~/bin/md-eval-v21.pl \
    -m \
    -afc \
    -c 0.25 \
    -r benchmarks/sources/${MEETING}.rttm \
    -s benchmarks/Aalto/${MEETING}.rttm \
    > benchmarks/Aalto/${MEETING}-eval.txt
done
```

### Google Cloud Speech-to-Text

Google’s pricing is $2.88/hr for the video model ($1.44 for the “default” model). https://cloud.google.com/speech-to-text/pricing.

https://cloud.google.com/speech-to-text/

Here’s the JSON they suggest, placed in `benchmarks/Google/ES2008a-request.json`:

```json
{
  "audio": {
    "uri": "gs://blabbertabber/ES2011d.wav"
  },
  "config": {
    "diarizationSpeakerCount": 4,
    "enableSpeakerDiarization": true,
    "encoding": "LINEAR16",
    "languageCode": "en-US",
    "model": "video"
  }
}
```

I have changed the model from “default” to “video” because they suggest:

> Best for audio that originated from video or includes multiple speakers. Ideally the audio is recorded at a 16khz or greater sampling rate. This is a premium model that costs more than the standard rate.

Note: Google seems to have an affinity for identifying only 2 speakers; we
tested the 4 speaker meeting ES2011d, and, when we didn't specifically
configure 4 speakers, Google found only two.

```bash
gcloud auth application-default login
curl -s -H "Content-Type: application/json" \
    -H "Authorization: Bearer "$(gcloud auth application-default print-access-token) \
    https://speech.googleapis.com/v1p1beta1/speech:longrunningrecognize \
    -d @benchmarks/Google/ES2008a-request.json \
    > benchmarks/Google/ES2008a-out.json
curl -s -H "Content-Type: application/json" \
    -H "Authorization: Bearer "$(gcloud auth application-default print-access-token) \
    https://speech.googleapis.com/v1p1beta1/speech:longrunningrecognize \
    -d @benchmarks/Google/ES2011d-request.json \
    > benchmarks/Google/ES2011d-out.json
curl -s -H "Content-Type: application/json" \
    -H "Authorization: Bearer "$(gcloud auth application-default print-access-token) \
    https://speech.googleapis.com/v1p1beta1/speech:longrunningrecognize \
    -d @benchmarks/Google/ES2016a-request.json \
    > benchmarks/Google/ES2016a-out.json
```
uh-oh:
```json
{
  "error": {
    "code": 400,
    "message": "Sync input too long. For audio longer than 1 min use LongRunningRecognize with a 'uri' parameter.",
    "status": "INVALID_ARGUMENT"
  }
}
```
and now:
```json
{
  "name": "6125554835674908336"
}
```
```bash
curl -H "Authorization: Bearer "$(gcloud auth application-default print-access-token) \
     -H "Content-Type: application/json; charset=utf-8" \
     "https://speech.googleapis.com/v1/operations/6125554835674908336" \
    > benchmarks/Google/ES2008a-out.json
curl -H "Authorization: Bearer "$(gcloud auth application-default print-access-token) \
     -H "Content-Type: application/json; charset=utf-8" \
     "https://speech.googleapis.com/v1/operations/5054793013139618594" \
    > benchmarks/Google/ES2011d-out.json
curl -H "Authorization: Bearer "$(gcloud auth application-default print-access-token) \
     -H "Content-Type: application/json; charset=utf-8" \
     "https://speech.googleapis.com/v1/operations/7786109410645577605" \
    > benchmarks/Google/ES2016a-out.json
```
The output consists of a [what else?] JSON file with layout similar to the following:
```json
{
  "name": "6125554835674908336",
  "metadata": {
    "@type": "type.googleapis.com/google.cloud.speech.v1p1beta1.LongRunningRecognizeMetadata",
    "progressPercent": 100,
    "startTime": "2019-02-10T21:05:54.228754Z",
    "lastUpdateTime": "2019-02-10T21:11:03.862025Z"
  },
  "done": true,
  "response": {
    "@type": "type.googleapis.com/google.cloud.speech.v1p1beta1.LongRunningRecognizeResponse",
    "results": [
      {
        "alternatives": [
          {
            "transcript": "Okay, good morning. Everybody. I'm glad you could all come. I'm really excited to start this team. I'm just going to have a little PowerPoint presentation for us for kickoff meeting. My name is Roseland Grand. I'll be the project manager. Our agenda today is we're going to do a little opening and then I'm going to talk a little bit about the project then we'll move into acquaintance that just getting to know each other a little bit including a tool training exercise on the will move into the project plan do a little discussion and close since we only have 25 minutes.",
            "confidence": 0.89557,
            "words": [
              {
                "startTime": "31.200s",
                "endTime": "32.800s",
                "word": "Okay,"
              }
            ]
          }
        ],
       "languageCode": "en-us"
      },
      {
        "alternatives": [
          {
            "transcript": " Let me see if I could do that right now.",
            "confidence": 0.80341655,
            "words": [
              {
                "startTime": "31.200s",
                "endTime": "32.800s",
                "word": "Okay,",
                "speakerTag": 3
              }
            ]
          }
        ],
       "languageCode": "en-us"
      }
    ]
  }
}
```

Let's create an MDTM & RTTM file.
Google has a bug — zero-length utterances: https://issuetracker.google.com/issues/124723573.
This generates `pyannote-metrics.py` error `ValueError: Segments must not be empty.`.
We have an extra step to strip out the 145 zero-length utterances. `awk '$4 != "0" { print }'`
:
```bash
for MEETING in ES2008a ES2011d ES2016a; do
  jq \
    -j \
    -r \
    '.response.results[-1].alternatives[].words[] |
        .startTime|=(rtrimstr("s")|tonumber) |
        .endTime|=(rtrimstr("s")|tonumber) |
        "SPEAKER meeting 1 ", .startTime, " ", (.endTime-.startTime), " <NA> <NA> ", ("spkr_"+(.speakerTag|tostring)), " <NA>\n"' \
    < benchmarks/Google/${MEETING}-out.json \
    > benchmarks/Google/${MEETING}.rttm
  jq \
    -j \
    -r \
    --arg MEETING $MEETING \
    '.response.results[-1].alternatives[].words[] |
      .startTime|=(rtrimstr("s")|tonumber) |
      .endTime|=(rtrimstr("s")|tonumber) |
      $MEETING, " 1 ", .startTime, " ", (.endTime-.startTime), " speaker NA unknown ", ("spkr_"+(.speakerTag|tostring)), "\n"' \
    < benchmarks/Google/${MEETING}-out.json |
    awk '$4 != "0" { print }' \
    > benchmarks/Google/${MEETING}.mdtm
done
```
Now let's score it:
```bash
for MEETING in ES2008a ES2011d ES2016a; do
    md-eval-v21.pl -m -afc -c 0.25 -r benchmarks/sources/${MEETING}.rttm -s benchmarks/Google/${MEETING}.rttm > benchmarks/Google/${MEETING}-eval.txt
done
pyannote-metrics.py diarization --subset=development AMI.SpeakerDiarization.MixHeadset benchmarks/Google/ES2011d.mdtm
```

And the results for ES2011d (`pyannote-metrics.py` first, `md-eval-v21.pl` second):

```
Diarization (collar = 0 ms)      diarization error rate    purity    coverage    total    correct      %    false alarm     %    missed detection      %    confusion      %
-----------------------------  ------------------------  --------  ----------  -------  ---------  -----  -------------  ----  ------------------  -----  -----------  -----
ES2011d.Mix-Headset                               61.43     54.02       63.77  1981.14     839.36  42.37          75.22  3.80              639.16  32.26       502.62  25.37

OVERALL SPEAKER DIARIZATION ERROR = 92.32 percent of scored speaker time  `(c=1 f=meeting)
```

### IBM Watson Speech To Text (STT)

IBM transcription came in at a disappointing **43.95% correct**, but it should
be noted that IBM diarization is limited to two speakers (for which it performs
quite well, typically 90+% correct), but causes poor performance for meetings
with three or more participants, meetings such as our benchmark ES2008a, which
has four participants.

IBM’s pricing is $0.75/hr.
https://www.ibm.com/cloud/watson-speech-to-text/pricing. Half-to-quarter of the
Google pricing.

The output consists of two files:

1. `hypotheses.txt`, which is the transcription of the conversation, no labels,
which looks something like this: `1: design %HESITATION %HESITATION swift
transaction sure now she so you go through when you put all all the things you
need to do and then %HESITATION putting %HESITATION OO these walkers line site
is also be black then you put all the things you do is you put an estimated
times to do each one these things so at the end you have the list of things they
want you to do and the times…`

1. `0.json.txt`, which contains the speaker information, though it’s buried in the JSON:

```json
{
    "result_index": 3,
    "results": [
        {
            "alternatives": [
                {
                    "timestamps": [
                        [
                            "now",
                            12.87,
                            13.32
                        ]
                    ],
                    "transcript": "now "
                }
            ],
            "final": false
        }
    ],
    "speaker_labels": [
        {
            "confidence": 0.445,
            "final": false,
            "from": 9.24,
            "speaker": 2,
            "to": 9.55
        },
        {
            "confidence": 0.456,
            "final": false,
            "from": 12.87,
            "speaker": 0,
            "to": 13.32
        }
    ]
}
```

#### Testing: Four-speaker Meeting

We use `curl` to trigger the diarization, and then convert the resulting JSON
into RTTM format before scoring.

```bash
export APIKEY=3VN5ZagcWDdOYmJBz5eTCNUIAGEQCyXXXXXXXXXXXXX

for MEETING in ES2016a; do
    curl \
      -X POST \
      -u "apikey:$APIKEY" \
      --header "Content-Type: audio/wav" \
      --data-binary @benchmarks/sources/${MEETING}.wav \
      "https://stream.watsonplatform.net/speech-to-text/api/v1/recognize?speaker_labels=true&max_alternatives=1" \
      > benchmarks/IBM/${MEETING}/out.json
done
```

Let's do a quick check to see if IBM has identified only two speakers or the
correct four speakers (spoiler: it only identifies 2 speakers):

```
for MEETING in ES2008a ES2016a; do
    jq -r .speaker_labels[].speaker < benchmarks/IBM/${MEETING}/out.json | sort | uniq -c
done
 326 0
1821 2
1071 0
   2 1
1477 2
```

```bash
cd benchmarks/
for MEETING in ES2008a ES2016a; do
    jq -r -j '.speaker_labels[] | "SPEAKER meeting 1 ", .from, " ", (.to-.from), " <NA> <NA> ", ("spkr_"+(.speaker|tostring)), " <NA>\n"' \
        < IBM/${MEETING}/out.json \
        > IBM/${MEETING}.rttm
    md-eval-v21.pl -m -afc -c 0.25 -r sources/${MEETING}.rttm -s IBM/${MEETING}.rttm > IBM/${MEETING}-eval.txt
done
```

And the results for ES2011d (`pyannote-metrics.py` first, `md-eval-v21.pl` second):

```
OVERALL SPEAKER DIARIZATION ERROR = 56.05 percent of scored speaker time
```


### ICSI

The ICSI performed quite well:

- Summary: the ICSI diarizer performed phenomenally well, with a calculated
  *6.7% overall speaker diarization error*
- We should temper our enthusiasm: part of the excellent performance may be due to the lopsidedness of the conversation (i.e. Brendan spoke 84% of the time, Charlotte 13%, and I 3%). It would be reasonable to assume a typical DER of ~20% (or better).
- We used the perl script `md-eval-v21.pl` to evaluate the accuracy.
- The ICSI's diarizer's output is here.
- The annotated meeting is here.
- The output of `md-eval-v21.pl` is here.
- Our annotation had a shortcoming — we didn't account for silence / non-speech (it would have made annotating very difficult). We took a leap of faith and assumed that ICSI was 100% correct. It leaves an upper bound of 32.27%; however, having listened to the meeting several times, I can assure you there are long periods of silence.
- Brendan was the most expansive speaker (378s, 85%), and closely identified with "clus_0"
- Charlotte spoke for 52s (13%) and was most strongly identified with "clus_1" (however it misidentified 4 seconds of my speech as "clus_1" (@36s)) Brendan was also mis-identified as "clus_1" (@179s), and I could hear mouse-clicking in the background, so I don't know if the environmental noise confused the diarizer.
- I spoke for 13s, and was most closely associate with "clus_12" (an 8s-burst @531s). No one else was identified with clus_12.
- Examining specific clusters sometimes shows higher error rates. For example, Charlotte was associated with clus_1 (45s), but Brendan was mis-attributed for 11s and Brian for 4s. So, for clus_1, the error rate was ~25%.
- On the other hand, some clusters show very low error rates. For example, clus_0 showed a 1.1% error rate.

Invocation:

```
/Users/cunnie/workspace/ib_diarization_toolkit/md-eval-v21.pl -m -afc -c 0.25 -r /Users/cunnie/Google Drive/BlabberTabber/ICSI-diarizer-sample-meeting-cunnie.rttm.txt -s /Users/cunnie/Google Drive/BlabberTabber/ICSI-diarizer-sample-meeting.rttm.txt

command line (run on 2016 Jun 4 at 10:16:56):  /Users/cunnie/workspace/ib_diarization_toolkit/md-eval-v21.pl -m -afc -c 0.25 -r /Users/cunnie/Google Drive/BlabberTabber/ICSI-diarizer-sample-meeting-cunnie.rttm.txt -s /Users/cunnie/Google Drive/BlabberTabber/ICSI-diarizer-sample-meeting.rttm.txt

Time-based metadata alignment

Metadata evaluation parameters:
    time-optimized metadata mapping
        max gap between matching metadata events = 1 sec
        max extent to match for SU's = 0.5 sec

Speaker Diarization evaluation parameters:
    The max time to extend no-score zones for NON-LEX exclusions is 0.5 sec
    The no-score collar at SPEAKER boundaries is 0.25 sec

Exclusion zones for evaluation and scoring are:
                             -----MetaData-----        -----SpkrData-----
     exclusion set name:     DEFAULT    DEFAULT        DEFAULT    DEFAULT
     token type/subtype      no-eval   no-score        no-eval   no-score
             (UEM)              X                         X
         LEXEME/un-lex                    X
        NON-LEX/breath                                              X
        NON-LEX/cough                                               X
        NON-LEX/laugh                                               X
        NON-LEX/lipsmack                                            X
        NON-LEX/other                                               X
        NON-LEX/sneeze                                              X
        NOSCORE/<na>            X         X               X         X
 NO_RT_METADATA/<na>            X
             SU/unannotated               X
'brendan' => 'clus_0'
   346.26 secs matched to 'clus_0'
    11.33 secs matched to 'clus_1'
     2.50 secs matched to 'clus_14'
    17.96 secs matched to 'clus_2'
'brian' => 'clus_12'
     0.21 secs matched to 'clus_0'
     4.47 secs matched to 'clus_1'
     8.28 secs matched to 'clus_12'
'charlotte' => 'clus_1'
     3.90 secs matched to 'clus_0'
    45.70 secs matched to 'clus_1'
     2.66 secs matched to 'clus_14'
     0.21 secs matched to 'clus_2'

*** Performance analysis for Speaker Diarization for c=1 f=meeting ***

    EVAL TIME =    600.00 secs
  EVAL SPEECH =    600.00 secs (100.0 percent of evaluated time)
  SCORED TIME =    585.50 secs ( 97.6 percent of evaluated time)
SCORED SPEECH =    585.50 secs (100.0 percent of scored time)
   EVAL WORDS =      0
 SCORED WORDS =      0         (100.0 percent of evaluated words)
---------------------------------------------
MISSED SPEECH =    149.61 secs ( 25.6 percent of scored time)
FALARM SPEECH =      0.00 secs (  0.0 percent of scored time)
 MISSED WORDS =      0         (100.0 percent of scored words)
---------------------------------------------
SCORED SPEAKER TIME =    585.50 secs (100.0 percent of scored speech)
MISSED SPEAKER TIME =    149.61 secs ( 25.6 percent of scored speaker time)
FALARM SPEAKER TIME =      0.00 secs (  0.0 percent of scored speaker time)
 SPEAKER ERROR TIME =     39.33 secs (  6.7 percent of scored speaker time)
SPEAKER ERROR WORDS =      0         (100.0 percent of scored speaker words)
---------------------------------------------
 OVERALL SPEAKER DIARIZATION ERROR = 32.27 percent of scored speaker time  `(c=1 f=meeting)
---------------------------------------------
 Speaker type confusion matrix -- speaker weighted
  REF\SYS (count)      unknown               MISS
unknown                   3 / 100.0%          0 /   0.0%
  FALSE ALARM             2 /  66.7%
---------------------------------------------
 Speaker type confusion matrix -- time weighted
  REF\SYS (seconds)    unknown               MISS
unknown              435.89 /  74.4%     149.61 /  25.6%
  FALSE ALARM          0.00 /   0.0%
---------------------------------------------

*** Performance analysis for Speaker Diarization for ALL ***

    EVAL TIME =    600.00 secs
  EVAL SPEECH =    600.00 secs (100.0 percent of evaluated time)
  SCORED TIME =    585.50 secs ( 97.6 percent of evaluated time)
SCORED SPEECH =    585.50 secs (100.0 percent of scored time)
   EVAL WORDS =      0
 SCORED WORDS =      0         (100.0 percent of evaluated words)
---------------------------------------------
MISSED SPEECH =    149.61 secs ( 25.6 percent of scored time)
FALARM SPEECH =      0.00 secs (  0.0 percent of scored time)
 MISSED WORDS =      0         (100.0 percent of scored words)
---------------------------------------------
SCORED SPEAKER TIME =    585.50 secs (100.0 percent of scored speech)
MISSED SPEAKER TIME =    149.61 secs ( 25.6 percent of scored speaker time)
FALARM SPEAKER TIME =      0.00 secs (  0.0 percent of scored speaker time)
 SPEAKER ERROR TIME =     39.33 secs (  6.7 percent of scored speaker time)
SPEAKER ERROR WORDS =      0         (100.0 percent of scored speaker words)
---------------------------------------------
 OVERALL SPEAKER DIARIZATION ERROR = 32.27 percent of scored speaker time  `(ALL)
---------------------------------------------
 Speaker type confusion matrix -- speaker weighted
  REF\SYS (count)      unknown               MISS
unknown                   3 / 100.0%          0 /   0.0%
  FALSE ALARM             2 /  66.7%
---------------------------------------------
 Speaker type confusion matrix -- time weighted
  REF\SYS (seconds)    unknown               MISS
unknown              435.89 /  74.4%     149.61 /  25.6%
  FALSE ALARM          0.00 /   0.0%
---------------------------------------------
```

### LIUM

Current Diarization Invocation: (patterned after http://www-lium.univ-lemans.fr/diarization/doku.php/quick_start)

```
/usr/bin/java -Xmx2024m \
  -jar ~/Downloads/lium_spkdiarization-8.4.1.jar \
  --fInputMask=$HOME/Google\ Drive/BlabberTabber/BlabberTabber_meeting.wav \
  --sOutputMask=/tmp/blabtab.seg \
  --doCEClustering  blabtab
```

Let’s try on Linux (melody)

```
curl -OL http://www-lium.univ-lemans.fr/diarization/lib/exe/fetch.php/lium_spkdiarization-8.4.1.jar.gz
gunzip lium_spkdiarization-8.4.1.jar.gz

ln -s ~/ib_diarization_toolkit/data/mfcc/AMI_20050204-1206.{fea,mfc}
/usr/bin/java -Xmx2024m \
  -jar ~/lium_spkdiarization-8.4.1.jar \
  --fInputMask=$HOME/ib_diarization_toolkit/data/mfcc/AMI_20050204-1206.mfc \
  --fInputDesc=htk \
  --sOutputMask=/tmp/lium.rttm \
  --doCEClustering  blabtab
/usr/bin/java -Xmx2024m \
  -jar ~/lium_spkdiarization-8.4.1.jar \
  --fInputMask=$HOME/audiofiles/amicorpus/IS1001a/audio/IS1001a.Mix-Headset.wav \
  --sOutputMask=/tmp/lium.rttm \
  --doCEClustering  blabtab
perl ~/ib_diarization_toolkit/md-eval-v21.pl \
  -m -afc -c 0.25 -r ~/ib_diarization_toolkit/data/rttm/AMI_20050204-1206.rttm \
  -s /tmp/lium.rttm
```

Target Corpus: ~~ES2008a (an arbitrary choice, a half-hour mock business
meeting)~~ IS100x

Evaluation Script: NIST provides an evaluation script which tells you how accurate your diarizer is: http://www.itl.nist.gov/iad/mig//tests/rt/2006-spring/code/md-eval-v21.pl , but unfortunately expects an RTTM format

Various Output Formats: LIUM generates a .seg file:
- Almost the same as RTTM v1.3 with some differences:
  - hundredths of seconds instead of seconds
  - no “confidence” field

Constraining the Number of Speakers: LIUM Documentation says that you can restrict the number of speakers to 2 by passing `–-cMinimumOfCluster=2` to the last command in this file: `~/Downloads/LIUM_SpkDiarization.script.v3.9/diarization.sh`

```
cp ~/Google\ Drive/BlabberTabber/BlabberTabber_meeting.wav /tmp/
./diarization.sh /tmp/BlabberTabber_meeting.wav
```

- diarization.sh: 12 speakers
- diarization.sh with –-cMinimumOfCluster=2: 12 speakers
No difference in output! The option does NOTHING!

```
diff output/BlabberTabber_meeting.c.3.seg /tmp/
```

ES2008a:

```
. diarization.sh ../amicorpus/ES2008a/audio/ES2008a.Mix-Headset.wav
awk '{print $8}' output/BlabberTabber_meeting.c.3.seg | grep ^S | sort | uniq | wc -l
```

15 speakers instead of 4! LIUM is identifying too many speakers, not just us,
but also for canonical wave files.

Converting .wav to .htk:

Converting .htk to .wav: You will hear a “pop” at the beginning because this command converts the header information into sound:

```
sox -t .raw -r 16000 -c 1 -w -s -x in.wav out.wav
```

Converting raw file to .wav file

```
sudo apt-get install -y sox
sox -t .raw -r 16000 -c 1 -w -s -x /home/ubuntu/ib_diarization_toolkit/result.dir/AMI_20050204-1206.post.0 /tmp/out.wav
```

Counting the individual speakers

```
awk '{print $8}' /tmp/blabtab.seg | grep ^S | sort | uniq | wc -l
```

### DiarTK

We dismiss this one because it's not a complete solution — it assumes that a
feature file is already present, and skips the speech/non-speech detection phase
(one of the more difficult aspects of diarization).

## Footnotes

<a id="ami"><sup>[AMI]</sup></a>
Originally we created our own test file, `ICSI-diarizer-sample-meeting.wav`,
which we manually annotated (`ICSI-diarizer-sample-meeting-cunnie.rttm.txt`),
but we've since moved away from that for two reasons:_

1. Our annotation was incomplete & did not take into account periods of
non-speech (silence). Speech/non-speech detection is a difficult aspect of
diarization, and we weren't measuring it. In fact, this forced us to use the
sub-par metric `SPEAKER ERROR TIME` instead of the more desirable `OVERALL
SPEAKER DIARIZATION ERROR`

1. The participation of the three speakers was lopsided; two speakers did most
of the speaking, and the third (me) was mostly quiet. Having a meeting with
mostly two speakers obscured the shortcomings of diarizers which limited the
number of identified speakers to 2. The IBM Watson Speech to Text diarizer, for
example, posted an exceptionally low `SPEAKER ERROR TIME` of 7.3%, but when the
AMI ES2008a four-speaker meeting was used, the performance cratered, turning in
an `OVERALL SPEAKER DIARIZATION ERROR` of 56.05%

Another factor that played into our decision to use the AMI Meeting Corpus is
that it's free, whereas the corpuses ("corpi"?) that Quan Wang, Google
Researcher, mentions in his [YouTube
video](https://www.youtube.com/watch?v=pGkqwRPzx9U&t=1178s) require membership
which costs [$24k](https://www.ldc.upenn.edu/members/join-ldc), which was well
outside our price range. The non-profit membership, a steal at $2.4k, is still
outside our budget.

- NIST SRE 2000 Disk-8, _a.k.a._ CALLHOME
- NIST SRE 2000 Disk-6, _a.k.a._ Switchboard
- ICSI meeting corpus

<a id="scoring"><sup>[scoring]</sup></a> Our original scoring tool was
[`md-eval-v21.pl`](https://github.com/cunnie/bin/blob/26eecbc292fc9066be0554447fe69afcafd2295c/md-eval-v21.pl),
using the metric `SPEAKER ERROR TIME`. Later, when we switched to the AMI
Corpus, we were able to use the more accurate `OVERALL SPEAKER DIARIZATION ERROR`
metric. We discarded `md-eval-v21.pl` at the suggestion of [Quan
Wang](https://arxiv.org/pdf/1810.04719.pdf), who, in his
[video](https://www.youtube.com/watch?v=pjxGPZQeeO4), highly recommends using
[`pyannote.metrics`](https://github.com/pyannote/pyannote-metrics) for scoring,
.

Our decision to use `pyannote.metrics` is one that we're not sure is correct;
the tool seems geared more towards an audio researcher than to someone who
merely wants to score results.

<a id="rttm"><sup>[RTTM]</sup></a> [RTTM
v1.3](https://catalog.ldc.upenn.edu/docs/LDC2004T12/RTTM-format-v13.pdf) is a
format for diarization output used by most diarization-scoring tools, e.g.
`pyannote-metrics.py` and `md-eval-v21.pl`.

Here is a typical line:

```
SPEAKER ES2008a 1       0       36.4    <NA> <NA>       Speaker_0 <NA>
```

The fields of interest are file/meeting ("ES2008a"), start time ("0"), duration
("36.4"), and speaker ("Speaker_0").

RTTM has the following field definitions:

- **type** is always `SPEAKER`
- **file** is the waveform file base name (i.e., without path names or extensions).
  for scoring, `pyannote` expects this to be the AMI meeting, e.g. `ES2008a`.
- **chnl** is the waveform channel (e.g., “1” or “2”). Always "1"
- **tbeg** is the beginning time of the object, in seconds, measured from the
  start time of the file.4If there is no beginning time, use tbeg = “<NA>”.
- **tdur** is the duration of the object, in seconds. If there is no duration,
  use tdur = “<NA>”.
- **stype** is the subtype of the object.  If there is no subtype, use stype = “<NA>”.
- **ortho** is the orthographic rendering (spelling) of the object for STT object
  types.  If there is no orthographic representation, use ortho = “<NA>”.
- **name** is the name of the speaker.  name must uniquely specify the speaker
within the scope of the file.  If name is not applicable or if no claim is being
made as to the identity of the speaker, use name = “<NA>”.
- **conf**  is  the  confidence  (probability)  that  the  object  information
is  correct.    If conf  is  not available, use conf = “<NA>”.

We took an ill-fated excursion using an alternative format, MDTM. It was a
mistake.

One of the advantages of using the `pyannote` solution is that the package
`pyannote.db.odessa.ami` has converted the AMI corpus data into the ~~MDTM~~
RTTM format.

The AMI corpus's native format is NITE (XML-based). It seems that some of the
NITE files can be used to create an RTTM file:
`ami_public_auto_1.5/ASR/ASR_AS_CTM_v1.0_feb07/ES2008a.{A,B,C,D}.words.xml`
files which seem to have start and stop times for every word uttered by the four
speakers (where {A,B,C,D} [are the
speakers](https://github.com/idiap/IBDiarization/issues/10)).

We previously converted the ES2008a NITE transcript to RTTM using a custom tool,
[nite_xml_to_rttm.py](https://github.com/cunnie/bin/blob/95edd6db4d446659b50978cebdf34f90b19d87a4/nite_xml_to_rttm.py).

```bash
nite_xml_to_rttm.py ~/Downloads/ami_public_manual_1.6.2/words/ES2008a.*.words.xml |
    sort -n -k 4 |
    squash_rttm.py \
    > sources/ES2008a.rttm
nite_xml_to_rttm.py ~/Downloads/ami_public_manual_1.6.2/words/ES2008a.*.words.xml |
    sort -n -k 4 |
    squash_rttm.py \
    > sources/ES2011d.rttm
nite_xml_to_rttm.py ~/Downloads/ami_public_manual_1.6.2/words/ES2016a.*.words.xml |
    sort -n -k 4 |
    squash_rttm.py \
    > sources/ES2016a.rttm
```

<a id="es2011d"><sup>[ES2011d]</sup></a> The choice of meeting `ES2011d` was
arbitrary; we discovered that the `pyannote` database was incomplete, did not
include either of our original meetings, `ES2008a` and `ES2016a`. Also, to
function properly, `pyannote` expected us to diarize _every_ meeting included in
its database, and that was not what we planned.

This is the list of AMI meetings that have been added to `pyannote`'s database:

```
ES2003a
ES2003b
ES2003c
ES2003d
ES2011a
ES2011b
ES2011c
ES2011d
IB4001
IS1008a
IS1008b
IS1008c
IS1008d
TS3004a
TS3004b
TS3004c
TS3004d
TS3006a
TS3006b
TS3006c
TS3006d
```

This is the error it gave, and the error we fixed by removing all but the
`ES2011d` entries from the `pyannote` database:

```
ValueError: Could not find hypothesis for file "ES2003a.Mix-Headset".
```

## Further Reading

_[Accurate Online Speaker Diarization with Supervised
Learning](https://ai.googleblog.com/2018/11/accurate-online-speaker-diarization.html)_,
Chong Wang, Research Scientist, Google AI
